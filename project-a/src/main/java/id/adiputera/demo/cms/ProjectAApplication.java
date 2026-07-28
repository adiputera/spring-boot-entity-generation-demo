package id.adiputera.demo.cms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import id.adiputera.demo.cms.core.service.ProductService;
import id.adiputera.demo.cms.product.models.Product;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import id.adiputera.demo.cms.core.repository.ProductRepository;

/**
 * The main application class for Project A.
 * Initializes the Spring Boot application and configures JPA auditing and repositories.
 *
 * @author Yusuf F. Adiputera
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableJpaRepositories
public class ProjectAApplication {
    /**
     * The main entry point for the Project A Spring Boot application.
     *
     * @param args The command line arguments.
     */
    public static void main(String[] args) {
        SpringApplication.run(ProjectAApplication.class, args);
    }
    
    /**
     * Creates a command line runner bean to execute a simple test flow upon startup.
     *
     * @param productService The product service used to retrieve product details.
     * @param productRepository The product repository used for database operations.
     * @return The CommandLineRunner instance.
     */
    @Bean
    public CommandLineRunner run(ProductService productService, ProductRepository productRepository) {
        return args -> {
            Product product = new Product();
            product.setCode("PROD-A-001");
            product.setCustomSeoTitle("Awesome Product A"); 
            
            // Save to H2 Database
            Product savedProduct = productRepository.save(product);
            
            System.out.println("===========================================");
            System.out.println("Product Saved with ID: " + savedProduct.getId());
            System.out.println("Product Creation Time: " + savedProduct.getCreationTime());
            System.out.println("Product Code (via Core Service): " + productService.getProductCode(savedProduct));
            System.out.println("Product SEO (via Project A Ext): " + savedProduct.getCustomSeoTitle());
            System.out.println("===========================================");
            
            // Prove retrieval works
            Product fetchedProduct = productRepository.findByCode("PROD-A-001");
            System.out.println("Fetched from DB, SEO Title is: " + fetchedProduct.getCustomSeoTitle());
            System.out.println("===========================================");
        };
    }
}
