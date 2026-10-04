package org.example.reaitests.controller

import org.example.reaitests.service.ProductService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@Controller
class ProductController(private val productService: ProductService) {

    @GetMapping("/")
    fun index(): String = "index"

    @GetMapping("/products")
    fun products(model: Model): String {
        model.addAttribute("products", productService.findAll())
        return "products :: table"
    }

    @GetMapping("/products/{id}/variants")
    fun variants(@PathVariable id: Long, model: Model): String {
        model.addAttribute("variants", productService.findVariants(id))
        return "products :: variants"
    }
}
