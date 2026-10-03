package org.example.reaitests.model

import java.math.BigDecimal

data class ProductVariant(
    val id: Long? = null,
    val title: String,
    val price: BigDecimal,
    val featuredImageSrc: String? = null,
    val available: Boolean = true,
)
