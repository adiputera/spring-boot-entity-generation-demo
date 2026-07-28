package id.adiputera.demo.cms.generator.plugin;

import id.adiputera.demo.cms.generator.Generator;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A custom Gradle task that triggers the models generation process.
 *
 * @author Yusuf F. Adiputera
 */
public abstract class GenerateModelsTask extends DefaultTask {

    /**
     * Gets the YAML files used as input for generation.
     *
     * @return The collection of YAML input files.
     */
    @InputFiles
    public abstract ConfigurableFileCollection getYamlFiles();

    /**
     * Gets the output directory where generated models will be placed.
     *
     * @return The output directory property.
     */
    @OutputDirectory
    public abstract DirectoryProperty getOutputDir();

    /**
     * Executes the task to generate models from the provided YAML files.
     */
    @TaskAction
    public void generate() {
        try {
            String outDir = getOutputDir().get().getAsFile().getAbsolutePath();
            List<String> paths = getYamlFiles().getFiles().stream()
                    .filter(File::exists)
                    .map(File::getAbsolutePath)
                    .collect(Collectors.toList());
            
            getLogger().info("Generating models into " + outDir + " using " + paths.size() + " yaml files.");
            
            Generator.generate(outDir, paths);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate models", e);
        }
    }
}
