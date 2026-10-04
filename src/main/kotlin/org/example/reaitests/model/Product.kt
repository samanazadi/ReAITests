package org.example.reaitests.model

import java.math.BigDecimal

data class Product(
    val id: Long? = null,
    val externalId: Long? = null,
    val title: String,
    val vendor: String? = null,
    val productType: String? = null,
    val variants: List<ProductVariant> = emptyList(),
) {
    val minPrice: BigDecimal? get() = variants.minOfOrNull { it.price }
    val maxPrice: BigDecimal? get() = variants.maxOfOrNull { it.price }
    val availableCount: Int get() = variants.count { it.available }
    val thumbnailSrc: String? get() = variants.firstNotNullOfOrNull { it.featuredImageSrc }
}
