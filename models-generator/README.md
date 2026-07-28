# Models Generator Plugin

This project contains the source code for the `id.adiputera.models-generator` Gradle plugin. This plugin implements a compile-time AST (Abstract Syntax Tree) merging engine that parses YAML model definitions across multiple modules and generates robust Java JPA Entities (POJOs), DTOs, and MapStruct mappers using FreeMarker templates.

## 🚀 How It Works

1. **Scan YAML Definitions**: The plugin scans the dependency classpath and local `src/main/resources` for any files matching `*-items.yaml` and `*-dtos.yaml`.
2. **Deep Merging**: It parses these YAML definitions into memory and deep-merges them. This allows downstream projects (like `project-a` or `project-b`) to "extend" a core entity (like `Product`) by simply declaring new attributes in their own YAML file without modifying the core project.
3. **Template Engine**: It uses FreeMarker templates (`pojo.ftl`, `dto.ftl`, `mapper.ftl`) to generate valid Java files.
4. **Compile-Time Weaving**: The plugin automatically creates a `models` SourceSet, compiles the generated Java files into a `models.jar`, and transparently injects it into the main project's compilation and runtime classpaths.

## 📦 Usage

If you are using this inside a Monorepo via Composite Builds (`includeBuild 'models-generator'` in `settings.gradle`), you can apply it directly to any project module:

```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '4.1.0'
    id 'io.spring.dependency-management'
    id 'id.adiputera.models-generator'
}
```

The plugin automatically adds the necessary MapStruct, JPA, and Jackson annotations to the compilation classpath.

## 🛠️ Plugin Architecture

- `ModelsGeneratorPlugin.java`: The main Gradle Plugin entry point. It registers the source sets, resolves MapStruct/Spring Data dependencies, and configures task execution order.
- `GenerateModelsTask.java`: A custom Gradle `DefaultTask` that safely handles caching, output directories, and file resolution.
- `Generator.java`: The core engine that parses YAML using SnakeYAML and evaluates the FreeMarker templates.

## 📝 Conventions
- The base package for all generated code is statically configured to `id.adiputera`.
- Models are placed in `id.adiputera.demo.cms.<domain>.models`.
- DTOs and Mappers are placed in `id.adiputera.demo.cms.<domain>.dtos`.
