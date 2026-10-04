package org.example.reaitests.model

data class ProductFilter(
    val title: String = "",
    val type: String = "",
    val inStock: Boolean = false,
) {
    companion object {
        const val NO_TYPE = "_none"
    }
}
