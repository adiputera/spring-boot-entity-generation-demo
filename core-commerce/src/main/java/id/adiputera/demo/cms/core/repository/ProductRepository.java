package id.adiputera.demo.cms.core.repository;

import id.adiputera.demo.cms.product.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Product entities.
 *
 * @author Yusuf F. Adiputera
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    /**
     * Finds a product by its code.
     *
     * @param code The product code.
     * @return The product entity, or null if not found.
     */
    Product findByCode(String code);
}
