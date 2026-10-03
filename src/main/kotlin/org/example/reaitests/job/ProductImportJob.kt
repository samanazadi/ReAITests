package org.example.reaitests.job

import org.example.reaitests.service.ProductService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientException

@Component
class ProductImportJob(private val productService: ProductService) {

    @Scheduled(initialDelay = 0)
    fun importProducts() {
        try {
            val imported = productService.importFromFamme()
            println("Imported $imported products from famme.no")
        } catch (e: RestClientException) {
            println("Product import from famme.no failed: ${e.message}")
        }
    }
}
