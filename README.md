# Dynamic Entity & DTO Generation Architecture

This repository is a **Proof of Concept (POC)** demonstrating a dynamic, classloader-based entity and DTO generation architecture for Spring Boot. It provides a solution for highly extensible multi-tenant or multi-project platforms where different instances require vastly different domain model attributes without relying on complex, leaky Java generics.

## 🎯 The Problem: The Generics Trap

In traditional Spring Boot architectures, when you want to extend a base model (like `Product`) with customer-specific fields, you usually resort to generics:

```java
// Core Library
public class ProductService<T extends Product> {
    public void process(T product) { ... }
}
```

As the application grows, generics bleed into Repositories, Controllers, and Mappers. You end up with massive generic sprawl (`class CustomProductService extends ProductService<CustomProduct>`), making the codebase brittle, hard to read, and difficult to maintain.

## 💡 The Solution: Compile-Time Model Weaving

This architecture completely abandons Java Generics for domain extensibility. Instead, we generate the entire domain model and Data Transfer Objects (DTOs) dynamically at **compile time**.

1. The Core Library (`core-commerce`) writes business logic against a standard `Product` class. **No Generics.**
2. When compiling a specific instance (e.g., `project-a`), the build script deeply merges the `core-items.yaml` with the `projectA-items.yaml`, and `core-dtos.yaml` with `projectA-dtos.yaml`.
3. A custom Java Generator spits out the merged `Product.java`, `ProductDTO.java`, and `ProductMapper.java` containing **both** the core fields and the custom `project-a` fields.
4. The generated files are compiled into a completely isolated `models.jar` and placed on the `compileOnly` classpath.

This completely prevents `NoSuchMethodError` runtime collisions while providing perfect IDE intellisense and strongly-typed core business logic!

---

## 🏗️ Project Structure

The repository is structured as a multi-module Gradle project:

* **`models-generator/`**: A standalone Java CLI tool containing the SnakeYAML parser and FreeMarker templates (`Entity.java.ftl`, `Dto.java.ftl`, `Mapper.java.ftl`, `Enum.java.ftl`). It deeply merges multiple YAML definitions and generates standard JPA Java classes and MapStruct interfaces. (Zero Lombok dependency for ultimate JDK forward compatibility).
* **`core-commerce/`**: The core reusable library. Contains standard `core-items.yaml`, `core-dtos.yaml`, and generic-free services like `ProductService.java`.
* **`project-a/`**: An implementation project. Injects `projectA-items.yaml` and `projectA-dtos.yaml` to add custom logic (e.g., adding `customSeoTitle` to the Product and mapping it in the DTO).
* **`project-b/`**: A totally separate implementation project. Injects `projectB-items.yaml` to add custom logic without bleeding into `project-a` or `core-commerce`.

---

## 🚀 How It Works Under the Hood

### 1. The YAML Definition
Models, Enums, and DTOs are defined declaratively in YAML, allowing deep-merging, inheritance, and advanced relational configuration.

**`core-items.yaml`** (Entities)
```yaml
models:
  Product:
    attributes:
      code:
        type: String
```

**`core-dtos.yaml`** (Data Transfer Objects & Mappers)
```yaml
dtos:
  ProductDTO:
    model: "id.adiputera.demo.cms.product.models.Product"
    attributes:
      formattedPrice:
        type: String
```

**`projectA-items.yaml`** (Extension)
```yaml
models:
  Product:
    attributes:
      customSeoTitle:
        type: String
```

### 2. MapStruct Integration
The generator has native integration with [MapStruct](https://mapstruct.org/). When you define a `model:` property inside a DTO configuration, the system automatically generates a MapStruct mapper interface (`ProductMapper.java`). 
You can deeply configure the MapStruct integration using YAML:
* `mappings:` (Allows overriding source/target logic, custom Java expressions, and ignoring fields).
* `uses:` (Delegates mapping logic to external custom Java classes).
* `decorator:` (Fully overrides MapStruct logic with an external Decorator class).
* `generateMapper: false` (Opts out of mapper generation entirely).

### 3. The Gradle Build Lifecycle
When you build `project-a`:
1. Gradle runs the `generateModels` task via the custom `models-plugin.gradle`.
2. `Generator.java` reads the YAMLs from the Core `compileClasspath` and merges them with the local `project-a` YAMLs into a single AST.
3. FreeMarker renders `Product.java` with *both* `code` and `customSeoTitle` (with pure Java Getters and Setters). It also renders the Enums, DTOs, and MapStruct Interfaces.
4. The `.java` files are compiled and zipped into `build/generated-models/models.jar`. MapStruct's annotation processor runs during this step, automatically implementing the generated Mapper interfaces.
5. `project-a` adds `models.jar` as a `compileOnly` dependency, satisfying the Core library's requirements *and* its own custom attribute requirements.

---

## 🛠️ Building & Running

### Prerequisites
- JDK 17+ (Tested against JDK 25)

### Build the Project
Run a clean build from the root directory:
```bash
./gradlew clean build
```
You will see that **all** projects compile successfully, proving that `core-commerce` can execute generic-free logic against the dynamically enhanced `Product` models.

### Examine the Generated Code
To see the magic in action, inspect the generated `.java` files for the different projects:
- **Project A:** `project-a/build/generated-sources/models/.../Product.java`
- **Project B:** `project-b/build/generated-sources/models/.../Product.java`

You will notice that they are completely different classes, but they both seamlessly fulfill the requirements of the Core `ProductService`! Furthermore, check out the `ProductMapperImpl.class` that MapStruct generated based on your merged YAML overrides.

---

## 👨‍💻 Maintainer
**Author:** Yusuf F. Adiputera
