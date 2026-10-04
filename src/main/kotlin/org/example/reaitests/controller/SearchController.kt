package org.example.reaitests.controller

import jakarta.servlet.http.HttpServletResponse
import org.example.reaitests.model.ProductFilter
import org.example.reaitests.service.ProductService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.util.UriComponentsBuilder

@Controller
class SearchController(private val productService: ProductService) {

    @GetMapping("/search")
    fun search(
        @RequestParam(defaultValue = "") q: String,
        @RequestParam(defaultValue = "") type: String,
        @RequestParam(defaultValue = "false") inStock: Boolean,
        model: Model,
    ): String {
        model.addAttribute("q", q)
        model.addAttribute("type", type)
        model.addAttribute("inStock", inStock)
        model.addAttribute("types", productService.findProductTypes())
        model.addAttribute("noType", ProductFilter.NO_TYPE)
        model.addAttribute("products", productService.search(ProductFilter(q, type, inStock)))
        return "search"
    }

    @GetMapping("/search/results")
    fun results(
        @RequestParam(defaultValue = "") q: String,
        @RequestParam(defaultValue = "") type: String,
        @RequestParam(defaultValue = "false") inStock: Boolean,
        model: Model,
        response: HttpServletResponse,
    ): String {
        response.setHeader(
            "HX-Replace-Url",
            UriComponentsBuilder.fromPath("/search").apply {
                if (q.isNotBlank()) queryParam("q", q)
                if (type.isNotBlank()) queryParam("type", type)
                if (inStock) queryParam("inStock", true)
            }.encode().toUriString()
        )
        model.addAttribute("products", productService.search(ProductFilter(q, type, inStock)))
        return "products :: productsTable"
    }
}
