package com.servalabs.perms.common.upgrade.core

import com.servalabs.perms.common.BuildConfigWrap
import com.servalabs.perms.common.upgrade.core.data.Sku

object MyPermSku {

    object Iap {
        val PRO_UPGRADE: Sku.Iap = object : Sku.Iap {
            override val id: String = "${BuildConfigWrap.APPLICATION_ID}.iap.upgrade.pro"
            override fun toString(): String = "MyPermSku.Iap.PRO_UPGRADE($id)"
        }
    }

    object Sub {
        val BASE_OFFER = Sku.Subscription.Offer(basePlanId = "upgrade-pro-baseplan")
        val TRIAL_OFFER = Sku.Subscription.Offer(basePlanId = "upgrade-pro-baseplan", offerId = "upgrade-pro-baseplan-trial")

        val PRO_UPGRADE: Sku.Subscription = object : Sku.Subscription {
            override val id: String = "upgrade.pro"
            override fun toString(): String = "MyPermSku.Sub.PRO_UPGRADE($id)"
        }
    }

    val PRO_SKUS: Set<Sku> = setOf(Iap.PRO_UPGRADE, Sub.PRO_UPGRADE)

    fun resolve(productId: String): Sku? = PRO_SKUS.firstOrNull { it.id == productId }
}
