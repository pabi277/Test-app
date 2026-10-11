#!/usr/bin/env python3
"""PaisaCalc EMI engine — independent reference implementation + golden data.

Mirrors the SHIPPED Kotlin policy (app/src/main/java/.../engine/EmiEngine.kt)
exactly and cross-checks it against a 60-digit exact-decimal control path.

  Base policy
  1. monthlyRate  = rateMicroPercent / 1_200_000_000        (rate in 1e-6 %)
  2. emiExact     = P * r * (1+r)^n / ((1+r)^n - 1)          (r == 0 -> P / n)
     monthlyEmi   = half-up round of emiExact to whole rupees
  3. monthly interest = half-up round of (openingBalance * rateMicroPercent
     / 1_200_000_000) in EXACT integer arithmetic, never floating point
  4. months 1..n-1: principal = min(monthlyEmi - interest, balance)
     month n     : principal = balance (the final installment clears the loan)
  5. the schedule stops the month the balance reaches zero
  6. totals are the sums of the printed columns

  Prepayments (phase 2)
  - A prepayment applies at installment numbers `from`, `from + step`,
    `from + 2*step`, ... until the loan closes, where step is 1 for monthly,
    3 for quarterly, 6 for half-yearly, 12 for yearly; ONE_TIME applies only
    at `from`. The amount is capped at the balance (never overpaid).
  - REDUCE_TENURE keeps the EMI and closes the loan earlier.
  - REDUCE_EMI recomputes the EMI over the remaining months; when both kinds
    fire in the same month, the reduce-EMI wish wins.
  - The interest saved is measured against the same loan with no prepayments.

Usage: python3 tools/emi_reference.py [--kotlin] [--out FILE]
Writes emi_golden.json (gitignored) next to the current directory.
"""
import argparse
import json
import math
from decimal import Decimal, ROUND_HALF_UP, getcontext

getcontext().prec = 60

DENOM = 1_200_000_000  # rateMicroPercent -> monthly rate on a rupee balance
STEPS = {"ONE_TIME": 0, "MONTHLY": 1, "QUARTERLY": 3, "HALF_YEARLY": 6, "YEARLY": 12}


def half_up_div(numerator: int, denominator: int) -> int:
    """Half-up integer division for non-negative operands (mirrors roundedDiv)."""
    q, r = divmod(numerator, denominator)
    return q + 1 if 2 * r >= denominator else q


def java_round_double(x: float) -> int:
    """Java Math.round(double): exact round-half-up toward +infinity."""
    return int(Decimal(x).quantize(Decimal(1), rounding=ROUND_HALF_UP))


def micro_percent(rate_percent) -> int:
    """Exact text -> rateMicroPercent, no floating point."""
    d = Decimal(str(rate_percent))
    return int((d * 1_000_000).quantize(Decimal(1), rounding=ROUND_HALF_UP))


def emi_exact(principal: int, rate_micro: int, n: int) -> float:
    if rate_micro == 0:
        return principal / n
    r = rate_micro / DENOM
    f = math.pow(1.0 + r, n)
    return principal * r * f / (f - 1.0)


def applies_at(frequency: str, start: int, installment: int) -> bool:
    if frequency == "ONE_TIME":
        return installment == start
    step = STEPS[frequency]
    return installment >= start and (installment - start) % step == 0


def schedule(principal, rate_micro, n, prepayments=(), exact_decimal=False):
    """The shipped policy. exact_decimal=True switches to the control path."""
    if exact_decimal:
        def emi_for(balance, months):
            if rate_micro == 0:
                return Decimal(balance) / Decimal(months)
            r = Decimal(rate_micro) / Decimal(DENOM)
            f = (1 + r) ** months
            return Decimal(balance) * r * f / (f - 1)

        def round_rupees(value):
            return int(value.quantize(Decimal(1), rounding=ROUND_HALF_UP))

        def interest_of(balance):
            return round_rupees(Decimal(balance) * Decimal(rate_micro) / Decimal(DENOM))
    else:
        def emi_for(balance, months):
            return emi_exact(balance, rate_micro, months)

        def round_rupees(value):
            return java_round_double(value)

        def interest_of(balance):
            return half_up_div(balance * rate_micro, DENOM)

    emi = round_rupees(emi_for(principal, n))
    initial_emi = emi
    balance, total_interest, rows = principal, 0, []
    for number in range(1, n + 1):
        if balance == 0:
            break
        interest = interest_of(balance)
        scheduled_principal = emi - interest
        if number == n:
            principal_component = balance
        elif scheduled_principal <= 0:
            principal_component = 0
        else:
            principal_component = min(scheduled_principal, balance)
        remaining = balance - principal_component

        prepaid, reduce_emi = 0, False
        for frequency, start, amount, strategy in prepayments:
            if remaining == 0:
                break
            if applies_at(frequency, start, number):
                applied = min(amount, remaining)
                prepaid += applied
                remaining -= applied
                if strategy == "REDUCE_EMI":
                    reduce_emi = True

        total_interest += interest
        balance = remaining
        rows.append((number, principal_component + interest, principal_component,
                     interest, balance, prepaid))
        if balance == 0:
            break
        if reduce_emi and number < n:
            emi = round_rupees(emi_for(balance, n - number))

    return dict(
        emi=initial_emi,
        final_emi=emi,
        rows=rows,
        total_interest=total_interest,
        total_payable=principal + total_interest,
        closing=balance,
        total_prepaid=sum(r[5] for r in rows),
        applications=sum(1 for r in rows if r[5] > 0),
    )


# --- phase 1 cases (no prepayments): re-verifies the shipped golden table ----
BASE_CASES = [
    ("home_10L_20y", 1_000_000, "8.5", 240, "HomeFirst published ~8,678"),
    ("home_25L_15y", 2_500_000, "9.25", 180, ""),
    ("car_5L_5y", 500_000, "12", 60, ""),
    ("crore_1Cr_30y", 10_000_000, "7.5", 360, ""),
    ("zero_rate", 250_000, "0", 24, "zero-interest edge"),
    ("odd_999999_37m", 999_999, "13.75", 37, "odd principal + tenure"),
    ("lakh_1L_1y", 100_000, "10", 12, "marketfeed published ~8,792"),
    ("pow_10L_10y", 1_000_000, "8.75", 120, "Ujjivan published 12,532 (truncated)"),
    ("pow_5L_10y", 500_000, "8.35", 120, "Deal4Loans published 6,159"),
    ("pow_15L_20y", 1_500_000, "8.6", 240, "Deal4Loans published 13,112"),
]

# --- phase 2 cases (prepayments), incl. the Techinfornity quarterly scenario --
PREPAY_CASES = [
    ("prepay_one_time_1L", 1_000_000, "8.5", 240,
     [("ONE_TIME", 25, 100_000, "REDUCE_TENURE")], "one-time prepayment in year 3"),
    ("prepay_monthly_5k", 1_000_000, "8.5", 240,
     [("MONTHLY", 13, 5_000, "REDUCE_TENURE")], "monthly from installment 13"),
    ("prepay_quarterly_25k", 1_000_000, "8.5", 240,
     [("QUARTERLY", 13, 25_000, "REDUCE_TENURE")], "Techinfornity scenario: amount + start month"),
    ("prepay_half_yearly_50k", 1_000_000, "8.5", 240,
     [("HALF_YEARLY", 13, 50_000, "REDUCE_TENURE")], "half-yearly from 13"),
    ("prepay_yearly_1L", 1_000_000, "8.5", 240,
     [("YEARLY", 13, 100_000, "REDUCE_TENURE")], "yearly from 13"),
    ("prepay_quarterly_25k_reduce_emi", 1_000_000, "8.5", 240,
     [("QUARTERLY", 13, 25_000, "REDUCE_EMI")], "quarterly, EMI reduction, tenure kept"),
    ("prepay_one_time_closes_loan", 1_000_000, "8.5", 240,
     [("ONE_TIME", 5, 950_000, "REDUCE_TENURE")], "one-time that nearly clears the loan"),
    ("prepay_capped_at_balance", 100_000, "10", 12,
     [("ONE_TIME", 6, 500_000, "REDUCE_TENURE")], "amount above the balance is capped"),
    ("prepay_zero_rate", 250_000, "0", 24,
     [("QUARTERLY", 5, 20_000, "REDUCE_TENURE")], "zero-rate loan with prepayments"),
    ("prepay_car_quarterly", 500_000, "12", 60,
     [("QUARTERLY", 7, 15_000, "REDUCE_TENURE")], "car loan, quarterly from 7"),
    ("prepay_mixed_strategies", 1_000_000, "8.5", 240,
     [("QUARTERLY", 13, 25_000, "REDUCE_EMI"), ("YEARLY", 37, 50_000, "REDUCE_TENURE")],
     "reduce-EMI and shorten-tenure together"),
]


def run_cases(cases, with_prepayments):
    out, all_ok = {}, True
    for tag, principal, rate, n, *rest in cases:
        micro = micro_percent(rate)
        prepayments = rest[0] if with_prepayments else []
        note = rest[-1]
        got = schedule(principal, micro, n, prepayments)
        ref = schedule(principal, micro, n, prepayments, exact_decimal=True)
        rows_match = all(a == b for a, b in zip(got["rows"], ref["rows"]))
        ok = (rows_match and got["emi"] == ref["emi"] and got["final_emi"] == ref["final_emi"]
              and got["total_interest"] == ref["total_interest"] and got["closing"] == 0
              and sum(r[2] for r in got["rows"]) + got["total_prepaid"] == principal)
        all_ok &= ok
        baseline = schedule(principal, micro, n)
        entry = dict(
            principal=principal, rateMicroPercent=micro, months=n, note=note,
            prepayments=[dict(frequency=f, fromInstallment=s, amount=a, strategy=st)
                         for f, s, a, st in prepayments],
            monthlyEmi=got["emi"], finalEmi=got["final_emi"],
            totalInterest=got["total_interest"], totalPayable=got["total_payable"],
            totalPrepaid=got["total_prepaid"], applications=got["applications"],
            installments=len(got["rows"]),
            interestSaved=baseline["total_interest"] - got["total_interest"],
            monthsSaved=len(baseline["rows"]) - len(got["rows"]),
            baselineTotalInterest=baseline["total_interest"],
            baselineInstallments=len(baseline["rows"]),
            baselineEndOffset=len(baseline["rows"]),
            endOffset=len(got["rows"]),
            ref_agrees=ok,
            rows={k: got["rows"][k - 1] for k in sorted(
                {1, len(got["rows"]), n,
                 next((i for i, r in enumerate(got["rows"], 1) if r[5] > 0), 1)} & set(range(1, len(got["rows"]) + 1)))},
            first_prepayment_row=next((i for i, r in enumerate(got["rows"], 1) if r[5] > 0), None),
        )
        out[tag] = entry
    return out, all_ok


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--kotlin", action="store_true", help="emit Kotlin test data block")
    ap.add_argument("--out", default="emi_golden.json", help="where to write the golden JSON")
    args = ap.parse_args()

    base, ok_base = run_cases(BASE_CASES, with_prepayments=False)
    print("== base cases (no prepayments) ==")
    for tag, g in base.items():
        print(f"{tag:16s} EMI={g['monthlyEmi']:>9,} totalInt={g['totalInterest']:>12,} "
              f"rows={g['installments']:>3} ok={g['ref_agrees']}")

    prep, ok_prep = run_cases(PREPAY_CASES, with_prepayments=True)
    print("\n== prepayment cases ==")
    for tag, g in prep.items():
        print(f"{tag:34s} EMI={g['monthlyEmi']:>8,}->{g['finalEmi']:>8,} "
              f"int={g['totalInterest']:>11,} prepaid={g['totalPrepaid']:>10,} "
              f"saved={g['interestSaved']:>10,} months={g['monthsSaved']:>3} "
              f"rows={g['installments']:>3}/{g['baselineInstallments']:>3} ok={g['ref_agrees']}")

    print(f"\ninteger policy == exact-decimal control on every row: base={ok_base} prepayments={ok_prep}")
    with open(args.out, "w") as f:
        json.dump({"base": base, "prepayments": prep}, f, indent=1)
    print(f"wrote {args.out}")

    if args.kotlin:
        print("\n// ---- Kotlin prepayment golden data ----")
        for tag, g in prep.items():
            rows = g["rows"]
            prep_ctor = ", ".join(
                "Prepayment(PrepaymentFrequency.{}, {}, {:_}L, PrepaymentStrategy.{})".format(
                    q["frequency"], q["fromInstallment"], q["amount"], q["strategy"]
                )
                for q in g["prepayments"]
            )
            first_row = rows[1]
            last_row = rows[g["installments"]]
            print(
                f"    PrepayGolden(\n"
                f'        name = "{tag}",\n'
                f"        principal = {g['principal']:_}L, rateMicroPercent = {g['rateMicroPercent']:_}L, months = {g['months']},\n"
                f"        prepayments = listOf({prep_ctor}),\n"
                f"        expectedMonthlyEmi = {g['monthlyEmi']:_}L, expectedFinalEmi = {g['finalEmi']:_}L,\n"
                f"        expectedTotalInterest = {g['totalInterest']:_}L, expectedTotalPayable = {g['totalPayable']:_}L,\n"
                f"        expectedTotalPrepaid = {g['totalPrepaid']:_}L, expectedApplications = {g['applications']},\n"
                f"        expectedInstallments = {g['installments']}, expectedInterestSaved = {g['interestSaved']:_}L,\n"
                f"        expectedMonthsSaved = {g['monthsSaved']}, expectedBaselineInstallments = {g['baselineInstallments']},\n"
                f"        expectedFirstPaid = {first_row[1]:_}L, expectedFirstPrincipal = {first_row[2]:_}L,\n"
                f"        expectedFirstInterest = {first_row[3]:_}L, expectedFirstClosing = {first_row[4]:_}L,\n"
                f"        expectedFinalPaid = {last_row[1]:_}L, expectedFinalPrincipal = {last_row[2]:_}L,\n"
                f"        expectedFinalInterest = {last_row[3]:_}L, expectedFinalPrepaid = {last_row[5]:_}L,\n"
                f"    ),"
            )


if __name__ == "__main__":
    main()
