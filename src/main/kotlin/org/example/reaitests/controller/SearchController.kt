package org.example.reaitests.controller

import jakarta.servlet.http.HttpServletResponse
import org.example.reaitests.service.ProductService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.util.UriComponentsBuilder

@Controller
class SearchController(private val productService: ProductService) {

    @GetMapping("/search")
    fun search(@RequestParam(defaultValue = "") q: String, model: Model): String {
        model.addAttribute("q", q)
        model.addAttribute("products", productService.search(q))
        return "search"
    }

    @GetMapping("/search/results")
    fun results(@RequestParam(defaultValue = "") q: String, model: Model, response: HttpServletResponse): String {
        response.setHeader(
            "HX-Replace-Url",
            UriComponentsBuilder.fromPath("/search").apply { if (q.isNotBlank()) queryParam("q", q) }.encode().toUriString()
        )
        model.addAttribute("products", productService.search(q))
        return "products :: productsTable"
    }
}
