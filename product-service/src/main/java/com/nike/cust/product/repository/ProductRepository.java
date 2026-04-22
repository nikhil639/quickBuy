package com.nike.cust.product.repository;

import com.nike.cust.product.model.Category;
import com.nike.cust.product.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategory(Category category);
    List<Product> findByAvailableTrue();
    List<Product> findByNameContainingIgnoreCase(String name);
}
