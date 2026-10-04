package org.example.reaitests.controller

import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.example.reaitests.service.ProductService
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.ResponseBody

@Controller
class ProductController(private val productService: ProductService) {

    @GetMapping("/")
    fun index(): String = "index"

    @GetMapping("/products")
    fun products(model: Model): String {
        model.addAttribute("products", productService.findAll())
        model.addAttribute("productForm", ProductForm.empty())
        return "products :: productsSection"
    }

    @PostMapping("/products")
    fun create(
        @Valid @ModelAttribute productForm: ProductForm,
        bindingResult: BindingResult,
        model: Model,
        response: HttpServletResponse,
    ): String {
        if (bindingResult.hasErrors()) {
            response.status = HttpStatus.UNPROCESSABLE_CONTENT.value()
        } else {
            productService.create(productForm.toProduct())
            model.addAttribute("productForm", ProductForm.empty())
        }
        model.addAttribute("products", productService.findAll())
        return "products :: productsSection"
    }

    @DeleteMapping("/products/{id}")
    @ResponseBody
    fun delete(@PathVariable id: Long): String {
        productService.delete(id)
        return ""
    }

    @GetMapping("/products/{id}/variants")
    fun variants(@PathVariable id: Long, model: Model): String {
        model.addAttribute("variants", productService.findVariants(id))
        return "products :: variantsTable"
    }
}
