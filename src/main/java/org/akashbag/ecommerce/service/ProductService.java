package org.akashbag.ecommerce.service;

import lombok.RequiredArgsConstructor;
import org.akashbag.ecommerce.dto.request.ProductRequest;
import org.akashbag.ecommerce.dto.response.ProductResponse;
import org.akashbag.ecommerce.enums.ProductCategory;
import org.akashbag.ecommerce.model.Product;
import org.akashbag.ecommerce.model.User;
import org.akashbag.ecommerce.repository.ProductRepository;
import org.akashbag.ecommerce.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final UploadService uploadService;
    private final UserRepository userRepository;

    public void createProduct(ProductRequest productRequest, List<MultipartFile> productImage) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        User user = userRepository.getUserByEmail(authentication.getName());
        List<String> images = new ArrayList<>();

        if (productImage != null && !productImage.isEmpty()) {
            List<CompletableFuture<String>> futures = productImage.stream()
                    .map(file -> CompletableFuture.supplyAsync(()-> uploadService.uploadImage(file)))
                    .toList();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            images = futures.stream()
                    .map(CompletableFuture::join)
                    .toList();
        }

        Product product = Product.builder()
                .name(productRequest.getName())
                .userId(user.getId())
                .description(productRequest.getDescription())
                .price(productRequest.getPrice())
                .category(productRequest.getCategory())
                .discountedPrice(productRequest.getDiscountedPrice())
                .images(images)
                .quantity(productRequest.getQuantity())
                .totalReviews(0)
                .averageRating(0)
                .createdAt(LocalDateTime.now())
                .isLive(productRequest.isLive())
                .build();

        productRepository.save(product);
    }

    public void updateProduct(ProductRequest productRequest, String id, List<MultipartFile> productImages) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        List<String> finalImageList = new ArrayList<>();
        if (productRequest.getImages() != null) {
            finalImageList.addAll(productRequest.getImages());
        }
        else if (product.getImages() != null) {
            finalImageList.addAll(product.getImages());
        }

        if (productImages != null && !productImages.isEmpty()) {
            List<CompletableFuture<String>> futures = productImages.stream()
                    .map(file -> CompletableFuture.supplyAsync(() -> uploadService.uploadImage(file)))
                    .toList();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            List<String> newUploadedImages = futures.stream()
                    .map(CompletableFuture::join)
                    .toList();

            finalImageList.addAll(newUploadedImages);
        }
        product.setImages(finalImageList);

        if (productRequest.getName() != null) product.setName(productRequest.getName());
        if (productRequest.getDescription() != null) product.setDescription(productRequest.getDescription());
        if (productRequest.getCategory() != null) product.setCategory(productRequest.getCategory());
        if (productRequest.getPrice() > 0) product.setPrice(productRequest.getPrice());
        if (productRequest.getDiscountedPrice() > 0) product.setDiscountedPrice(productRequest.getDiscountedPrice());
        if (productRequest.getQuantity() > 0) product.setQuantity(productRequest.getQuantity());
        product.setLive(productRequest.isLive());

        productRepository.save(product);
    }

    public Product getProductById(String id) {
        return productRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Product not found"));
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public void deleteProductById(String id) {
        productRepository.deleteById(id);
    }

    public void changeStatus(String id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Product not found"));
        product.setLive(!product.isLive());
        productRepository.save(product);
    }

    public Page<ProductResponse> searchProduct(String name, String category, String sortDir, int page, int size) {
        // 1. Handle Sorting
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by("price").descending()
                : Sort.by("price").ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        // 2. Handle Category Logic
        ProductCategory productCategory = null;
        if (category != null && !category.isEmpty() && !category.equals("ALL")) {
            try {
                productCategory = ProductCategory.valueOf(category);
            } catch (IllegalArgumentException e) {
                productCategory = null;
            }
        }

        // 3. Fetch Data (The Logic Fix)
        Page<Product> pageProduct;

        if (productCategory == null) {
            // If Category is ALL, search only by Name
            pageProduct = productRepository.findByNameContainingIgnoreCase(name, pageable);
        } else {
            // If Category is selected, search by Name AND Category
            pageProduct = productRepository.findByNameContainingIgnoreCaseAndCategory(name, productCategory, pageable);
        }

        // 4. Map to Response (Same as before)
        return pageProduct.map(product -> {
            List<String> firstImage = (product.getImages() != null && !product.getImages().isEmpty())
                    ? List.of(product.getImages().getFirst())
                    : new ArrayList<>();

            return ProductResponse.builder()
                    .id(product.getId())
                    .name(product.getName())
                    .description(product.getDescription())
                    .category(product.getCategory())
                    .price(product.getPrice())
                    .images(firstImage)
                    .discountedPrice(product.getDiscountedPrice())
                    .quantity(product.getQuantity())
                    .averageRating(product.getAverageRating())
                    .totalRating(product.getTotalReviews())
                    .build();
        });
    }
}
