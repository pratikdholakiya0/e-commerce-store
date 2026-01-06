package org.akashbag.ecommerce.controller;

import lombok.RequiredArgsConstructor;
import org.akashbag.ecommerce.dto.request.ProductRequest;
import org.akashbag.ecommerce.dto.response.ProductResponse;
import org.akashbag.ecommerce.model.Product;
import org.akashbag.ecommerce.payload.ApiResponse;
import org.akashbag.ecommerce.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @PostMapping(value = "/create", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse> createProduct(@RequestPart ProductRequest product, @RequestPart(required = true) List<MultipartFile> images) {
        productService.createProduct(product, images);

        ApiResponse apiResponse = ApiResponse.builder()
                .message("Product created successfully")
                .status(HttpStatus.OK)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @PutMapping(value = "/update/{id}", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse> updateProduct(
            @PathVariable String id,
            @RequestPart ProductRequest product,
            @RequestPart(required = false) List<MultipartFile> images) {
        productService.updateProduct(product, id, images);

        ApiResponse apiResponse = ApiResponse.builder()
                .message("Product updated successfully")
                .status(HttpStatus.OK)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse> searchProducts(
            @RequestParam(defaultValue = "") String name,
            @RequestParam(required = false) String category, // New Param
            @RequestParam(defaultValue = "asc") String sortDir, // New Param (asc/desc)
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){

        Page<ProductResponse> response = productService.searchProduct(name, category, sortDir, page, size);

        ApiResponse apiResponse = ApiResponse.builder()
                .message("Product searched successfully")
                .data(response)
                .status(HttpStatus.OK)
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable String id) {
        Product product = productService.getProductById(id);
        return ResponseEntity.status(HttpStatus.OK).body(product);
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<Product>> getProduct() {
        List<Product> list = productService.getAllProducts();
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse> deleteProductById(@PathVariable String id) {
        productService.deleteProductById(id);

        ApiResponse apiResponse = ApiResponse.builder()
                .message("Product deleted successfully")
                .status(HttpStatus.OK)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @PutMapping("/changeStatus/{id}")
    public ResponseEntity<ApiResponse> changeStatus(@PathVariable String id) {
        productService.changeStatus(id);

        ApiResponse apiResponse = ApiResponse.builder()
                .message("Product status changed successfully")
                .status(HttpStatus.OK)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }
}
