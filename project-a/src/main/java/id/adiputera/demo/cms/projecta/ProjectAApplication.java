package id.adiputera.demo.cms.projecta;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import id.adiputera.demo.cms.core.service.ProductService;
import id.adiputera.demo.cms.product.models.Product;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication(scanBasePackages = "id.adiputera.demo.cms")
@EnableJpaAuditing
public class ProjectAApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProjectAApplication.class, args);
    }
    
    @Bean
    public CommandLineRunner run(ProductService productService) {
        return args -> {
            Product product = new Product();
            product.setCode("PROD-A-001");
            product.setCustomSeoTitle("Awesome Product A"); // This exists ONLY in Project A!
            
            System.out.println("===========================================");
            System.out.println("Product Code (via Core Service): " + productService.getProductCode(product));
            System.out.println("Product SEO (via Project A Ext): " + product.getCustomSeoTitle());
            System.out.println("===========================================");
        };
    }
}
