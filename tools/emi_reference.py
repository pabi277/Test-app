#!/usr/bin/env python3
"""PaisaCalc EMI engine — independent reference implementation + golden data.

This mirrors the SHIPPED Kotlin policy (app/src/main/java/.../engine/EmiEngine.kt)
exactly, and cross-checks it against a 60-digit exact-decimal path:

  Policy
  1. monthlyRate  = rateMicroPercent / 1_200_000_000        (rate in 1e-6 %)
  2. emiExact     = P * r * (1+r)^n / ((1+r)^n - 1)          (r == 0 -> P / n)
     monthlyEmi   = half-up round of emiExact to whole rupees
  3. monthly interest = half-up round of (openingBalance * rateMicroPercent / 1_200_000_000)
     computed with EXACT integer arithmetic (Long), never floating point
  4. months 1..n-1: principal = min(monthlyEmi - interest, balance)
     month n     : principal = balance (the final installment clears the loan)
  5. totalInterest = sum of monthly interest; totalPayable = P + totalInterest

Usage: python3 tools/emi_reference.py [--kotlin] [--out FILE]
Writes emi_golden.json (gitignored) next to the current directory.
"""
import argparse
import json
import math
from decimal import Decimal, ROUND_HALF_UP, getcontext

getcontext().prec = 60

DENOM = 1_200_000_000  # rateMicroPercent -> monthly rate on a rupee balance


def half_up_div(numerator: int, denominator: int) -> int:
    """Half-up integer division for non-negative operands (mirrors roundedDiv)."""
    q, r = divmod(numerator, denominator)
    return q + 1 if 2 * r >= denominator else q


def java_round_double(x: float) -> int:
    """Java Math.round(double): exact round-half-up toward +infinity."""
    return int(Decimal(x).quantize(Decimal(1), rounding=ROUND_HALF_UP))


def micro_percent(rate_percent: str | float) -> int:
    """Exact text -> rateMicroPercent, no floating point."""
    d = Decimal(str(rate_percent))
    return int((d * 1_000_000).quantize(Decimal(1), rounding=ROUND_HALF_UP))


def emi_exact(principal: int, rate_micro: int, n: int) -> float:
    if rate_micro == 0:
        return principal / n
    r = rate_micro / DENOM
    f = math.pow(1.0 + r, n)
    return principal * r * f / (f - 1.0)


def schedule_int(principal, rate_micro, n):
    """The shipped policy: exact integer interest, double-precision EMI formula."""
    e_exact = emi_exact(principal, rate_micro, n)
    emi = java_round_double(e_exact)
    balance, rows, total_interest = principal, [], 0
    for i in range(1, n + 1):
        interest = half_up_div(balance * rate_micro, DENOM)
        principal_component = balance if i == n else min(emi - interest, balance)
        paid = principal_component + interest
        balance -= principal_component
        total_interest += interest
        rows.append((i, paid, principal_component, interest, balance))
    return dict(emi=emi, emi_exact=e_exact, rows=rows,
                total_interest=total_interest,
                total_payable=principal + total_interest, closing=balance)


def schedule_decimal(principal, rate_micro, n):
    """Exact-decimal control path (independent of binary floating point)."""
    r = Decimal(rate_micro) / Decimal(DENOM)
    if rate_micro == 0:
        e_exact = Decimal(principal) / Decimal(n)
    else:
        f = (1 + r) ** n
        e_exact = Decimal(principal) * r * f / (f - 1)
    emi = int(e_exact.quantize(Decimal(1), rounding=ROUND_HALF_UP))
    balance, rows, total_interest = principal, [], 0
    for i in range(1, n + 1):
        interest = int((Decimal(balance) * r).quantize(Decimal(1), rounding=ROUND_HALF_UP))
        principal_component = balance if i == n else min(emi - interest, balance)
        paid = principal_component + interest
        balance -= principal_component
        total_interest += interest
        rows.append((i, paid, principal_component, interest, balance))
    return dict(emi=emi, rows=rows, total_interest=total_interest,
                total_payable=principal + total_interest, closing=balance)


CASES = [
    ("home_10L_20y", 10_00_000, "8.5", 240, "10L @ 8.5% x 240 (HomeFirst: ~8,678)"),
    ("home_25L_15y", 25_00_000, "9.25", 180, "25L @ 9.25% x 180"),
    ("car_5L_5y", 5_00_000, "12", 60, "5L @ 12% x 60"),
    ("crore_1Cr_30y", 1_00_00_000, "7.5", 360, "1Cr @ 7.5% x 360"),
    ("zero_rate", 2_50_000, "0", 24, "2.5L @ 0% x 24 (edge)"),
    ("odd_999999_37m", 9_99_999, "13.75", 37, "9,99,999 @ 13.75% x 37 (odd)"),
    ("lakh_1L_1y", 1_00_000, "10", 12, "1L @ 10% x 12 (marketfeed: ~8,792)"),
    ("pow_10L_10y", 10_00_000, "8.75", 120, "10L @ 8.75% x 120 (Ujjivan: 12,532)"),
    ("pow_5L_10y", 5_00_000, "8.35", 120, "5L @ 8.35% x 120 (Deal4Loans: 6,159)"),
    ("pow_15L_20y", 15_00_000, "8.6", 240, "15L @ 8.6% x 240 (Deal4Loans: 13,112)"),
]

PUBLISHED = {
    "home_10L_20y": 8678, "lakh_1L_1y": 8792, "pow_10L_10y": 12532,
    "pow_5L_10y": 6159, "pow_15L_20y": 13112,
}


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--kotlin", action="store_true", help="emit Kotlin test data block")
    ap.add_argument("--out", default="emi_golden.json", help="where to write the golden JSON")
    args = ap.parse_args()

    golden, all_ok = {}, True
    for tag, principal, rate, n, note in CASES:
        micro = micro_percent(rate)
        got = schedule_int(principal, micro, n)
        ref = schedule_decimal(principal, micro, n)
        rows_match = all(a == b for a, b in zip(got["rows"], ref["rows"]))
        ok = (got["emi"] == ref["emi"] and got["total_interest"] == ref["total_interest"]
              and got["closing"] == ref["closing"] == 0 and rows_match
              and sum(r[2] for r in got["rows"]) == principal)
        all_ok &= ok
        gap = abs((got["emi_exact"] - math.floor(got["emi_exact"])) - 0.5)
        golden[tag] = dict(
            principal=principal, rateMicroPercent=micro, months=n, note=note,
            monthlyEmi=got["emi"], totalInterest=got["total_interest"],
            totalPayable=got["total_payable"], emiExact=f"{got['emi_exact']:.6f}",
            emiRoundingGap=f"{gap:.3e}", ref_agrees=ok,
            rows={k: got["rows"][k - 1] for k in {1, 2, n // 2, n - 1, n}},
        )
        print(f"{tag:16s} EMI={got['emi']:>9,} totalInt={got['total_interest']:>12,} "
              f"totalPay={got['total_payable']:>12,} exact={got['emi_exact']:.6f} "
              f"ok={ok} published={PUBLISHED.get(tag, '-')}")

    print(f"\ninteger policy == exact-decimal reference on every row: {all_ok}")
    print("\npublished cross-checks (ours - published):")
    for tag, pub in PUBLISHED.items():
        print(f"  {tag:16s} {golden[tag]['monthlyEmi']:>9,} vs {pub:>9,} "
              f"-> {golden[tag]['monthlyEmi'] - pub:+d}")

    out = args.out
    with open(out, "w") as f:
        json.dump(golden, f, indent=1)
    print(f"\nwrote {out}")

    if args.kotlin:
        print("\n// ---- Kotlin test data (generated by tools/emi_reference.py) ----")
        for tag, g in golden.items():
            last = g["rows"][g["months"]]
            first = g["rows"][1]
            print(
                f"    Golden(\n"
                f'        name = "{tag}",\n'
                f"        principal = {g['principal']:_}L,\n"
                f"        rateMicroPercent = {g['rateMicroPercent']:_}L,\n"
                f"        months = {g['months']},\n"
                f"        expectedEmi = {g['monthlyEmi']:_}L,\n"
                f"        expectedTotalInterest = {g['totalInterest']:_}L,\n"
                f"        expectedTotalPayable = {g['totalPayable']:_}L,\n"
                f"        expectedFirstMonth = {first[0]}, expectedFirstInterest = {first[3]:_}L,\n"
                f"        expectedFirstPrincipal = {first[2]:_}L, expectedFirstPaid = {first[1]:_}L,\n"
                f"        expectedFirstClosing = {first[4]:_}L,\n"
                f"        expectedFinalPaid = {last[1]:_}L, expectedFinalPrincipal = {last[2]:_}L,\n"
                f"        expectedFinalInterest = {last[3]:_}L, expectedFinalClosing = {last[4]:_}L,\n"
                f"    ),"
            )


if __name__ == "__main__":
    main()
