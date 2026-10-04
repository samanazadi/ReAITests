package org.example.reaitests.repository

import org.example.reaitests.model.Product
import org.example.reaitests.model.ProductFilter
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

    fun update(id: Long, product: Product): Boolean =
        jdbcClient.sql(
            """
            update products
            set title = :title, vendor = :vendor, product_type = :productType
            where id = :id and deleted_at is null
            """.trimIndent()
        )
            .param("id", id)
            .param("title", product.title)
            .param("vendor", product.vendor)
            .param("productType", product.productType)
            .update() == 1

    fun deleteVariants(productId: Long) {
        jdbcClient.sql("delete from product_variants where product_id = :productId")
            .param("productId", productId)
            .update()
    }

    fun findAllActive(): List<Product> = findActive()

    fun searchActive(filter: ProductFilter): List<Product> {
        val conditions = mutableListOf<String>()
        val params = mutableMapOf<String, Any>()
        if (filter.title.isNotBlank()) {
            conditions += "and p.title ilike :titlePattern"
            params["titlePattern"] = "%${escapeLike(filter.title)}%"
        }
        when (filter.type) {
            "" -> {}
            ProductFilter.NO_TYPE -> conditions += "and p.product_type is null"
            else -> {
                conditions += "and p.product_type = :type"
                params["type"] = filter.type
            }
        }
        if (filter.inStock) {
            conditions += "and exists (select 1 from product_variants av where av.product_id = p.id and av.available)"
        }
        return findActive(conditions.joinToString(" "), params)
    }

    fun findActiveProductTypes(): List<String> =
        jdbcClient.sql(
            """
            select distinct product_type
            from products
            where deleted_at is null and product_type is not null
            order by product_type
            """.trimIndent()
        )
            .query { rs, _ -> rs.getString("product_type") }
            .list()

    fun findActiveById(id: Long): Product? = findActive("and p.id = :id", mapOf("id" to id)).firstOrNull()

    private fun escapeLike(value: String): String =
        value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")

    private fun findActive(condition: String = "", params: Map<String, Any> = emptyMap()): List<Product> {
        return jdbcClient.sql(
            """
            select p.id, p.external_id, p.title, p.vendor, p.product_type,
                   v.id as variant_id, v.title as variant_title, v.price, v.featured_image_src, v.available
            from products p
            left join product_variants v on v.product_id = p.id
            where p.deleted_at is null $condition
            order by p.id, v.id
            """.trimIndent()
        )
            .params(params)
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

    fun softDelete(id: Long): Boolean =
        jdbcClient.sql("update products set deleted_at = now() where id = :id and deleted_at is null")
            .param("id", id)
            .update() == 1

    fun findVariantsByProductId(productId: Long): List<ProductVariant> =
        jdbcClient.sql(
            """
            select v.id, v.title, v.price, v.featured_image_src, v.available
            from product_variants v
            join products p on p.id = v.product_id
            where v.product_id = :productId and p.deleted_at is null
            order by v.id
            """.trimIndent()
        )
            .param("productId", productId)
            .query { rs, _ ->
                ProductVariant(
                    id = rs.getLong("id"),
                    title = rs.getString("title"),
                    price = rs.getBigDecimal("price"),
                    featuredImageSrc = rs.getString("featured_image_src"),
                    available = rs.getBoolean("available"),
                )
            }
            .list()
}
