package id.adiputera.demo.cms.core.service;

import id.adiputera.demo.cms.product.models.Product;
import org.springframework.stereotype.Service;

/**
 * Service for Product core business logic.
 *
 * @author Yusuf F. Adiputera
 */
@Service
public class ProductService {
    
    /**
     * Standard Product code retrieval.
     *
     * @param product The product instance.
     * @return The product code.
     */
    public String getProductCode(Product product) {
        // Look ma, no generics!
        return product.getCode();
    }
}
