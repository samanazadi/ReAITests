package org.example.reaitests.model

data class Product(
    val id: Long? = null,
    val externalId: Long? = null,
    val title: String,
    val vendor: String? = null,
    val productType: String? = null,
    val variants: List<ProductVariant> = emptyList(),
) {
    val availableCount: Int get() = variants.count { it.available }
    val thumbnailSrc: String? get() = variants.firstNotNullOfOrNull { it.featuredImageSrc }
}
