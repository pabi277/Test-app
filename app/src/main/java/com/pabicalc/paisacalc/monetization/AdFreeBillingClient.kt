package com.pabicalc.paisacalc.monetization

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams

/**
 * The only class in PaisaCalc that talks to Google Play billing. One
 * non-consumable product ([AdsConfig.AD_FREE_PRODUCT_ID]); nothing connects to
 * Play until the user opens the ad-free entry or asks to restore.
 *
 * Purchase tokens and order IDs never leave this class and are never logged.
 */
class AdFreeBillingClient(
    context: Context,
    private val productId: String,
    private val callbacks: Callbacks,
) {

    interface Callbacks {

        /** Connected, and the product query finished (the price may be unknown). */
        fun onBillingReady(priceText: String?)

        /** Play billing cannot be used: not installed, service error, or no product. */
        fun onBillingUnavailable(reasonCode: Int, message: String)

        /** Result of the purchase sheet the user just used. */
        fun onPurchaseFlowResult(responseCode: Int, purchases: List<Purchase>)

        /** Result of an explicit restore request. */
        fun onRestoreResult(purchases: List<Purchase>)
    }

    private val client: BillingClient = BillingClient.newBuilder(context)
        .setListener { billingResult, purchases ->
            callbacks.onPurchaseFlowResult(billingResult.responseCode, purchases.orEmpty())
        }
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder().enableOneTimeProducts().build(),
        )
        .build()

    private var productDetails: ProductDetails? = null

    /** Connects if needed, then reports readiness through [Callbacks]. */
    fun connect() {
        if (client.isReady) {
            queryProductDetails()
            return
        }
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryProductDetails()
                } else {
                    callbacks.onBillingUnavailable(
                        billingResult.responseCode,
                        billingResult.debugMessage,
                    )
                }
            }

            override fun onBillingServiceDisconnected() {
                // Reported on the next call; the connection is re-established on demand.
            }
        })
    }

    /** Opens the Play purchase sheet for the ad-free product. */
    fun buy(activity: Activity) {
        val details = productDetails
        if (details == null) {
            callbacks.onBillingUnavailable(
                BillingClient.BillingResponseCode.ITEM_UNAVAILABLE,
                "The ad-free product is not available on this account",
            )
            return
        }
        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(details)
                        .build(),
                ),
            )
            .build()
        client.launchBillingFlow(activity, params)
    }

    /** Asks Play for everything this account owns, so a purchase can be restored. */
    fun restore() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        client.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                callbacks.onRestoreResult(purchases)
            } else {
                callbacks.onBillingUnavailable(
                    billingResult.responseCode,
                    billingResult.debugMessage,
                )
            }
        }
    }

    /** A purchase must be acknowledged; an unacknowledged one is refunded by Play. */
    fun acknowledge(purchase: Purchase) {
        if (purchase.isAcknowledged) return
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        client.acknowledgePurchase(params) { _ ->
            // The entitlement is already granted locally; an ack retry is harmless.
        }
    }

    fun endConnection() {
        client.endConnection()
    }

    private fun queryProductDetails() {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productId)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build(),
                ),
            )
            .build()
        client.queryProductDetailsAsync(params) { billingResult, queryResult ->
            if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                callbacks.onBillingUnavailable(
                    billingResult.responseCode,
                    billingResult.debugMessage,
                )
                return@queryProductDetailsAsync
            }
            val details = queryResult.productDetailsList
                .orEmpty()
                .firstOrNull { it.productId == productId }
            productDetails = details
            callbacks.onBillingReady(details?.oneTimePurchasePriceText())
        }
    }
}

/**
 * The localized, tax-inclusive price Play returns for a one-time product, or
 * null when the account cannot see one. The price is never hard-coded in the
 * app: Play Console owns it.
 */
private fun ProductDetails.oneTimePurchasePriceText(): String? =
    oneTimePurchaseOfferDetailsList.orEmpty().firstOrNull()?.formattedPrice
