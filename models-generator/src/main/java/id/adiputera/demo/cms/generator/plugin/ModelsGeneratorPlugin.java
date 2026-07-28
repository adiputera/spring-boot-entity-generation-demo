package id.adiputera.demo.cms.generator.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.jvm.tasks.Jar;

import java.io.File;

/**
 * A Gradle plugin that sets up the models generation process.
 * It creates the required source sets, dependencies, and tasks.
 *
 * @author Yusuf F. Adiputera
 */
public class ModelsGeneratorPlugin implements Plugin<Project> {
    
    /**
     * Applies the plugin to the given project.
     *
     * @param project The Gradle project to apply the plugin to.
     */
    @Override
    public void apply(Project project) {
        // Ensure the Java plugin is applied
        project.getPluginManager().apply(JavaPlugin.class);
        
        JavaPluginExtension javaExtension = project.getExtensions().getByType(JavaPluginExtension.class);
        SourceSetContainer sourceSets = javaExtension.getSourceSets();
        
        // Create 'models' source set
        SourceSet modelsSourceSet = sourceSets.create("models");
        modelsSourceSet.getJava().setSrcDirs(java.util.Collections.singletonList(
                new File(project.getBuildDir(), "generated-sources/models")));
                
        SourceSet mainSourceSet = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME);
        
        // Define configurations
        Configuration modelsImpl = project.getConfigurations().getByName(modelsSourceSet.getImplementationConfigurationName());
        Configuration modelsAnnotationProc = project.getConfigurations().getByName(modelsSourceSet.getAnnotationProcessorConfigurationName());
        
        // Add dependencies
        project.getDependencies().add(modelsImpl.getName(), "org.springframework.boot:spring-boot-starter-data-jpa");
        project.getDependencies().add(modelsImpl.getName(), "org.mapstruct:mapstruct:1.6.0.Beta1");
        project.getDependencies().add(modelsAnnotationProc.getName(), "org.mapstruct:mapstruct-processor:1.6.0.Beta1");
        project.getDependencies().add(modelsImpl.getName(), "org.springframework.boot:spring-boot-starter-validation");
        project.getDependencies().add(modelsImpl.getName(), "com.fasterxml.jackson.core:jackson-annotations");
        project.getDependencies().add(modelsImpl.getName(), "io.swagger.core.v3:swagger-annotations:2.2.21");
        
        // Set up the compile classpath for models (modelsCompileClasspath + main compile classpath)
        modelsSourceSet.setCompileClasspath(
                project.getConfigurations().getByName(modelsSourceSet.getCompileClasspathConfigurationName())
                        .plus(mainSourceSet.getCompileClasspath())
        );
        
        // Create the generateModels task
        TaskProvider<GenerateModelsTask> generateModels = project.getTasks().register("generateModels", GenerateModelsTask.class, task -> {
            task.getOutputDir().set(project.file(project.getBuildDir() + "/generated-sources/models"));
            
            // Collect YAMLs from main classpath (Core module)
            task.getYamlFiles().from(project.provider(() -> 
                project.getConfigurations().getByName(mainSourceSet.getCompileClasspathConfigurationName()).getFiles().stream()
                    .filter(f -> f.exists() && (f.getName().endsWith(".jar") || f.isDirectory()))
                    .map(f -> f.isDirectory() ? project.fileTree(f).matching(pat -> pat.include("**/*-items.yaml", "**/*-dtos.yaml")) : project.zipTree(f).matching(pat -> pat.include("**/*-items.yaml", "**/*-dtos.yaml")))
                    .collect(java.util.stream.Collectors.toList())
            ));
            
            // Collect YAMLs from local resources
            task.getYamlFiles().from(project.fileTree("src/main/resources").matching(pat -> pat.include("**/*-items.yaml", "**/*-dtos.yaml")));
            
            // Also depend on the main classpath configuration so it triggers correctly
            task.dependsOn(project.getConfigurations().getByName(mainSourceSet.getCompileClasspathConfigurationName()));
        });
        
        // Wire generation into compilation
        project.getTasks().named(modelsSourceSet.getCompileJavaTaskName(), JavaCompile.class).configure(task -> {
            task.dependsOn(generateModels);
        });
        
        // Create modelsJar task
        TaskProvider<Jar> modelsJar = project.getTasks().register("modelsJar", Jar.class, task -> {
            task.getArchiveClassifier().set("models");
            task.from(modelsSourceSet.getOutput());
            task.dependsOn(project.getTasks().named(modelsSourceSet.getClassesTaskName()));
        });
        
        // Make main compile depend on modelsJar and include it in classpath
        project.getTasks().named(mainSourceSet.getCompileJavaTaskName(), JavaCompile.class).configure(task -> {
            task.dependsOn(modelsJar);
            task.setClasspath(task.getClasspath().plus(project.files(modelsJar.flatMap(Jar::getArchiveFile))));
        });
        
        // We also need to add modelsImplementation dependencies to main compilation so it compiles against them
        project.getConfigurations().getByName(mainSourceSet.getCompileOnlyConfigurationName()).extendsFrom(modelsImpl);
    }
}
