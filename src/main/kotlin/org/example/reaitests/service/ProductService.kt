package org.example.reaitests.service

import org.example.reaitests.client.FammeClient
import org.example.reaitests.model.Product
import org.example.reaitests.model.ProductVariant
import org.example.reaitests.repository.ProductRepository
import org.springframework.dao.DataAccessException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val transactionTemplate: TransactionTemplate,
    private val fammeClient: FammeClient,
) {

    fun findAll(): List<Product> = productRepository.findAllActive()

    fun search(query: String): List<Product> =
        if (query.isBlank()) findAll() else productRepository.searchActiveByTitle(query.trim())

    fun findVariants(productId: Long): List<ProductVariant> = productRepository.findVariantsByProductId(productId)

    @Transactional
    fun create(product: Product): Long {
        val productId = checkNotNull(productRepository.insert(product)) {
            "Product with external id ${product.externalId} already exists"
        }
        productRepository.insertVariants(productId, product.variants)
        return productId
    }

    fun delete(id: Long) {
        if (!productRepository.softDelete(id)) {
            throw ProductNotFoundException(id)
        }
    }

    fun importFromFamme(): Int = importProducts(fammeClient.fetchProducts().take(50))

    private fun importProducts(products: List<Product>): Int =
        products.count { product ->
            try {
                transactionTemplate.execute {
                    productRepository.insert(product)?.also { productId ->
                        productRepository.insertVariants(productId, product.variants)
                    }
                } != null
            } catch (e: DataAccessException) {
                println("Skipped product ${product.externalId}: ${e.message}")
                false
            }
        }
}
