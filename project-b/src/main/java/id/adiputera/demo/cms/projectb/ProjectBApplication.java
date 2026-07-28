package id.adiputera.demo.cms.projectb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import id.adiputera.demo.cms.core.service.ProductService;
import id.adiputera.demo.cms.product.models.Product;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.CommandLineRunner;

/**
 * The main application class for Project B.
 * Initializes the Spring Boot application.
 *
 * @author Yusuf F. Adiputera
 */
@SpringBootApplication(scanBasePackages = "id.adiputera.demo.cms")
public class ProjectBApplication {
    /**
     * The main entry point for the Project B Spring Boot application.
     *
     * @param args The command line arguments.
     */
    public static void main(String[] args) {
        SpringApplication.run(ProjectBApplication.class, args);
    }
    
    /**
     * Creates a command line runner bean to execute a simple test flow upon startup.
     *
     * @param productService The product service used to retrieve product details.
     * @return The CommandLineRunner instance.
     */
    @Bean
    public CommandLineRunner run(ProductService productService) {
        return args -> {
            Product product = new Product();
            product.setCode("PROD-B-999");
            product.setInternalWarehouseId(4422L); // This exists ONLY in Project B!
            
            System.out.println("===========================================");
            System.out.println("Product Code (via Core Service): " + productService.getProductCode(product));
            System.out.println("Product Warehouse ID (via Project B Ext): " + product.getInternalWarehouseId());
            System.out.println("===========================================");
        };
    }
}
