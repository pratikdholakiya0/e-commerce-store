package org.akashbag.ecommerce.repository;

import org.akashbag.ecommerce.enums.ProductCategory;
import org.akashbag.ecommerce.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends MongoRepository<Product,String> {
    Product getProductById(String id);

    // Spring generates: { "name": { "$regex": name, "$options": "i" } }
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // 2. Search by Name AND Category
    // Spring generates: { "name": { "$regex": name, "$options": "i" }, "category": category }
    Page<Product> findByNameContainingIgnoreCaseAndCategory(String name, ProductCategory category, Pageable pageable);
}
