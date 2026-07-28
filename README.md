# SAP Commerce-Style Entity Generation Demo

This repository is a **Proof of Concept (POC)** demonstrating a dynamic, classloader-based entity generation architecture for Spring Boot. It provides a solution for highly extensible multi-tenant or multi-project platforms where different instances require vastly different domain model attributes without relying on complex, leaky Java generics.

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

Inspired by **SAP Commerce (Hybris)**, this architecture completely abandons Java Generics for domain extensibility. Instead, we generate the entire domain model dynamically at **compile time**.

1. The Core Library (`core-commerce`) writes business logic against a standard `Product` class. **No Generics.**
2. When compiling a specific instance (e.g., `project-a`), the build script deeply merges the `core-items.yaml` with the `projectA-items.yaml`.
3. A custom Java Generator spits out the merged `Product.java` containing **both** the core fields and the custom `project-a` fields.
4. The generated files are compiled into a completely isolated `models.jar` and placed on the `compileOnly` classpath.

This completely prevents `NoSuchMethodError` runtime collisions while providing perfect IDE intellisense and strongly-typed core business logic!

---

## 🏗️ Project Structure

The repository is structured as a multi-module Gradle project:

* **`models-generator/`**: A standalone Java CLI tool containing the SnakeYAML parser and FreeMarker template (`Entity.java.ftl`). It deeply merges multiple YAML definitions and generates standard JPA Java classes. (Zero Lombok dependency).
* **`core-commerce/`**: The core reusable library. Contains standard `core-items.yaml` and generic-free services like `ProductService.java`.
* **`project-a/`**: An implementation project. Injects `projectA-items.yaml` to add a `customSeoTitle` to the Product.
* **`project-b/`**: A totally separate implementation project. Injects `projectB-items.yaml` to add an `internalWarehouseId` to the Product.

---

## 🚀 How It Works Under the Hood

### 1. The YAML Definition
Models are defined declaratively in YAML, allowing inheritance and deep-merging.

**`core-items.yaml`**
```yaml
models:
  Product:
    attributes:
      code:
        type: String
```

**`projectA-items.yaml`**
```yaml
models:
  Product:
    attributes:
      customSeoTitle:
        type: String
```

### 2. The Gradle Build Lifecycle
When you build `project-a`:
1. Gradle runs the `generateModels` task.
2. `Generator.java` reads both `core-items.yaml` and `projectA-items.yaml`, merging them into a single AST.
3. FreeMarker renders `Product.java` with *both* `code` and `customSeoTitle` (and their respective Getters/Setters).
4. The `.java` files are compiled and zipped into `build/generated-models/models.jar`.
5. `project-a` adds `models.jar` as a `compileOnly` dependency, satisfying the Core library's requirements *and* its own custom attribute requirements.

---

## 🛠️ Building & Running

### Prerequisites
- JDK 17+ (Tested up to JDK 25)

### Build the Project
Run a clean build from the root directory:
```bash
./gradlew clean build
```
You will see that **all** projects compile successfully, proving that `core-commerce` can execute generic-free logic against the dynamically enhanced `Product` models.

### Examine the Generated Code
To see the magic in action, inspect the generated `.java` files for the different projects:
- **Project A's Product:** `project-a/build/generated-sources/models/.../Product.java`
- **Project B's Product:** `project-b/build/generated-sources/models/.../Product.java`

You will notice that they are completely different classes, but they both seamlessly fulfill the requirements of the Core `ProductService`!

---

## 👨‍💻 Maintainer
**Author:** Yusuf F. Adiputera
