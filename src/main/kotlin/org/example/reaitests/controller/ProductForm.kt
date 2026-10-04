package org.example.reaitests.controller

import jakarta.validation.Valid
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.example.reaitests.model.Product
import org.example.reaitests.model.ProductVariant
import org.hibernate.validator.constraints.URL
import java.math.BigDecimal

class ProductForm {
    @field:NotBlank(message = "Title is required")
    var title: String = ""

    var vendor: String = ""

    var productType: String = ""

    @field:Valid
    @field:Size(min = 1, message = "Add at least one variant")
    var variants: MutableList<VariantForm> = mutableListOf()

    fun toProduct() = Product(
        title = title.trim(),
        vendor = vendor.trim().ifBlank { null },
        productType = productType.trim().ifBlank { null },
        variants = variants.map { it.toVariant() },
    )

    companion object {
        fun empty() = ProductForm().apply { variants = mutableListOf(VariantForm()) }

        fun from(product: Product) = ProductForm().apply {
            title = product.title
            vendor = product.vendor.orEmpty()
            productType = product.productType.orEmpty()
            variants = product.variants.map { variant ->
                VariantForm().apply {
                    title = variant.title
                    price = variant.price
                    imageSrc = variant.featuredImageSrc.orEmpty()
                    available = variant.available
                }
            }.toMutableList()
        }
    }
}

class VariantForm {
    @field:NotBlank(message = "Variant title is required")
    var title: String = ""

    @field:NotNull(message = "Price is required")
    @field:DecimalMin(value = "0.00", message = "Price must be 0 or more")
    var price: BigDecimal? = null

    @field:URL(message = "Image must be a valid URL")
    var imageSrc: String = ""

    var available: Boolean = true

    fun toVariant() = ProductVariant(
        title = title.trim(),
        price = requireNotNull(price),
        featuredImageSrc = imageSrc.trim().ifBlank { null },
        available = available,
    )
}
