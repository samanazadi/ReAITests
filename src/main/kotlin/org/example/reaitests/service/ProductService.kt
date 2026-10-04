package org.example.reaitests.service

import org.example.reaitests.client.FammeClient
import org.example.reaitests.model.Product
import org.example.reaitests.model.ProductFilter
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

    fun search(filter: ProductFilter): List<Product> =
        productRepository.searchActive(filter.copy(title = filter.title.trim()))

    fun findProductTypes(): List<String> = productRepository.findActiveProductTypes()

    fun findVariants(productId: Long): List<ProductVariant> = productRepository.findVariantsByProductId(productId)

    @Transactional
    fun create(product: Product): Long {
        val productId = checkNotNull(productRepository.insert(product)) {
            "Product with external id ${product.externalId} already exists"
        }
        productRepository.insertVariants(productId, product.variants)
        return productId
    }

    fun findById(id: Long): Product = productRepository.findActiveById(id) ?: throw ProductNotFoundException(id)

    @Transactional
    fun update(id: Long, product: Product) {
        if (!productRepository.update(id, product)) {
            throw ProductNotFoundException(id)
        }
        productRepository.deleteVariants(id)
        productRepository.insertVariants(id, product.variants)
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
