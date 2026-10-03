package org.example.reaitests.repository

import org.example.reaitests.model.Product
import org.example.reaitests.model.ProductVariant
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository

@Repository
class ProductRepository(private val jdbcClient: JdbcClient) {

    fun insert(product: Product): Long? =
        jdbcClient.sql(
            """
            insert into products (external_id, title, vendor, product_type)
            values (:externalId, :title, :vendor, :productType)
            on conflict (external_id) do nothing
            returning id
            """.trimIndent()
        )
            .param("externalId", product.externalId)
            .param("title", product.title)
            .param("vendor", product.vendor)
            .param("productType", product.productType)
            .query(Long::class.java)
            .optional()
            .orElse(null)

    fun insertVariants(productId: Long, variants: List<ProductVariant>) {
        variants.forEach { variant ->
            jdbcClient.sql(
                """
                insert into product_variants (product_id, title, price, featured_image_src, available)
                values (:productId, :title, :price, :featuredImageSrc, :available)
                """.trimIndent()
            )
                .param("productId", productId)
                .param("title", variant.title)
                .param("price", variant.price)
                .param("featuredImageSrc", variant.featuredImageSrc)
                .param("available", variant.available)
                .update()
        }
    }

    fun findAllActive(): List<Product> =
        jdbcClient.sql(
            """
            select p.id, p.external_id, p.title, p.vendor, p.product_type,
                   v.id as variant_id, v.title as variant_title, v.price, v.featured_image_src, v.available
            from products p
            left join product_variants v on v.product_id = p.id
            where p.deleted_at is null
            order by p.id desc, v.id
            """.trimIndent()
        )
            .query { rs, _ ->
                val product = Product(
                    id = rs.getLong("id"),
                    externalId = rs.getObject("external_id") as Long?,
                    title = rs.getString("title"),
                    vendor = rs.getString("vendor"),
                    productType = rs.getString("product_type"),
                )
                val variant = (rs.getObject("variant_id") as Long?)?.let { variantId ->
                    ProductVariant(
                        id = variantId,
                        title = rs.getString("variant_title"),
                        price = rs.getBigDecimal("price"),
                        featuredImageSrc = rs.getString("featured_image_src"),
                        available = rs.getBoolean("available"),
                    )
                }
                product to variant
            }
            .list()
            .groupBy({ it.first }, { it.second })
            .map { (product, variants) -> product.copy(variants = variants.filterNotNull()) }
}
