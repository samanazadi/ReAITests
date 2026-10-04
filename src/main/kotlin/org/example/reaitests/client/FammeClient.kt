package org.example.reaitests.client

import com.fasterxml.jackson.annotation.JsonProperty
import org.example.reaitests.model.Product
import org.example.reaitests.model.ProductVariant
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import java.math.BigDecimal

@Component
class FammeClient(
    restClientBuilder: RestClient.Builder,
    @Value("\${famme.products-url}") private val productsUrl: String,
) {

    private val restClient = restClientBuilder.build()

    fun fetchProducts(): List<Product> =
        restClient.get()
            .uri(productsUrl)
            .retrieve()
            .body<FammeProductsResponse>()
            ?.products
            .orEmpty()
            .map { it.toProduct() }

    private fun FammeProduct.toProduct() = Product(
        externalId = id,
        title = title,
        vendor = vendor?.ifBlank { null }?.lowercase()?.replaceFirstChar { it.titlecase() },
        productType = productType?.ifBlank { null },
        variants = variants.map { variant ->
            ProductVariant(
                title = variant.title,
                price = variant.price,
                featuredImageSrc = variant.featuredImage?.src,
                available = variant.available,
            )
        },
    )

    private data class FammeProductsResponse(val products: List<FammeProduct>)

    private data class FammeProduct(
        val id: Long,
        val title: String,
        val vendor: String?,
        @JsonProperty("product_type") val productType: String?,
        val variants: List<FammeVariant>,
    )

    private data class FammeVariant(
        val title: String,
        val price: BigDecimal,
        val available: Boolean,
        @JsonProperty("featured_image") val featuredImage: FammeImage?,
    )

    private data class FammeImage(val src: String)
}
