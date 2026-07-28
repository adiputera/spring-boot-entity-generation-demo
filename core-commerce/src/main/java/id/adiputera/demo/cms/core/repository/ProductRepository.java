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
    Product findByCode(String code);
}
