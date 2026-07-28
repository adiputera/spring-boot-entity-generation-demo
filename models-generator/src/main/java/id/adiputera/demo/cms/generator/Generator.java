package id.adiputera.demo.cms.generator;

import org.yaml.snakeyaml.Yaml;
import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * The core engine of the SAP Commerce-style compile-time model weaving architecture.
 * This class reads multiple YAML definitions, deep-merges them, and generates
 * standard Java POJOs (JPA Entities) via FreeMarker templates.
 * 
 * @author Yusuf F. Adiputera
 */
public class Generator {
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Usage: Generator <output-dir> <yaml-file1> [<yaml-file2> ...]");
            System.exit(1);
        }

        String outputDir = args[0];
        List<String> yamlPaths = Arrays.asList(args).subList(1, args.length);

        System.out.println("Starting generation into: " + outputDir);
        
        Yaml yaml = new Yaml();
        
        // This is our global AST (Abstract Syntax Tree) where all YAMLs will be merged into.
        Map<String, Object> mergedData = new HashMap<>();
        mergedData.put("imports", new ArrayList<String>());
        mergedData.put("enums", new HashMap<String, Object>());
        mergedData.put("models", new HashMap<String, Map<String, Object>>());
        mergedData.put("dtos", new HashMap<String, Map<String, Object>>());
        
        /* 
         * ==========================================
         * PASS 1: PARSING & DEEP MERGING
         * ==========================================
         * We iterate through the core YAML and then the extension YAMLs.
         * By deep-merging, any custom attributes defined in an extension project (like project-a)
         * will be injected directly into the core models.
         */
        for (String path : yamlPaths) {
            System.out.println("Parsing " + path + "...");
            String content = new String(Files.readAllBytes(Paths.get(path)));
            Map<String, Object> data = yaml.load(content);
            
            // 1. Merge global imports
            if (data.containsKey("imports")) {
                ((List<String>) mergedData.get("imports")).addAll((List<String>) data.get("imports"));
            }
            // 2. Merge enums
            if (data.containsKey("enums")) {
                ((Map<String, Object>) mergedData.get("enums")).putAll((Map<String, Object>) data.get("enums"));
            }
            // 3. Deep Merge Models
            if (data.containsKey("models")) {
                Map<String, Map<String, Object>> dataModels = (Map<String, Map<String, Object>>) data.get("models");
                Map<String, Map<String, Object>> mergedModels = (Map<String, Map<String, Object>>) mergedData.get("models");
                
                for (Map.Entry<String, Map<String, Object>> entry : dataModels.entrySet()) {
                    String modelName = entry.getKey();
                    Map<String, Object> modelDef = entry.getValue();
                    
                    if (!mergedModels.containsKey(modelName)) {
                        // If it's a completely new model, just add it.
                        mergedModels.put(modelName, modelDef);
                    } else {
                        // If the model already exists (e.g., Product in core), we merge the new properties!
                        Map<String, Object> existingModel = mergedModels.get(modelName);
                        
                        // Merge Attributes
                        if (modelDef.containsKey("attributes")) {
                            if (!existingModel.containsKey("attributes")) {
                                existingModel.put("attributes", new HashMap<String, Object>());
                            }
                            ((Map<String, Object>) existingModel.get("attributes")).putAll((Map<String, Object>) modelDef.get("attributes"));
                        }
                        
                        // Merge Annotations (like Custom Entity Listeners)
                        if (modelDef.containsKey("annotations")) {
                            if (!existingModel.containsKey("annotations")) {
                                existingModel.put("annotations", new ArrayList<String>());
                            }
                            ((List<String>) existingModel.get("annotations")).addAll((List<String>) modelDef.get("annotations"));
                        }
                        
                        // Merge Imports
                        if (modelDef.containsKey("imports")) {
                            if (!existingModel.containsKey("imports")) {
                                existingModel.put("imports", new ArrayList<String>());
                            }
                            ((List<String>) existingModel.get("imports")).addAll((List<String>) modelDef.get("imports"));
                        }
                        
                        // Override scalar properties if provided
                        if (modelDef.containsKey("package")) existingModel.put("package", modelDef.get("package"));
                        if (modelDef.containsKey("table")) existingModel.put("table", modelDef.get("table"));
                        if (modelDef.containsKey("extends")) existingModel.put("extends", modelDef.get("extends"));
                    }
                }
            }
            
            // 4. Deep Merge DTOs
            if (data.containsKey("dtos")) {
                Map<String, Map<String, Object>> dataDtos = (Map<String, Map<String, Object>>) data.get("dtos");
                Map<String, Map<String, Object>> mergedDtos = (Map<String, Map<String, Object>>) mergedData.get("dtos");
                
                for (Map.Entry<String, Map<String, Object>> entry : dataDtos.entrySet()) {
                    String dtoName = entry.getKey();
                    Map<String, Object> dtoDef = entry.getValue();
                    
                    if (!mergedDtos.containsKey(dtoName)) {
                        mergedDtos.put(dtoName, dtoDef);
                    } else {
                        Map<String, Object> existingDto = mergedDtos.get(dtoName);
                        
                        if (dtoDef.containsKey("attributes")) {
                            if (!existingDto.containsKey("attributes")) {
                                existingDto.put("attributes", new HashMap<String, Object>());
                            }
                            ((Map<String, Object>) existingDto.get("attributes")).putAll((Map<String, Object>) dtoDef.get("attributes"));
                        }
                        
                        if (dtoDef.containsKey("annotations")) {
                            if (!existingDto.containsKey("annotations")) {
                                existingDto.put("annotations", new ArrayList<String>());
                            }
                            ((List<String>) existingDto.get("annotations")).addAll((List<String>) dtoDef.get("annotations"));
                        }
                        
                        if (dtoDef.containsKey("imports")) {
                            if (!existingDto.containsKey("imports")) {
                                existingDto.put("imports", new ArrayList<String>());
                            }
                            ((List<String>) existingDto.get("imports")).addAll((List<String>) dtoDef.get("imports"));
                        }
                        
                        if (dtoDef.containsKey("package")) existingDto.put("package", dtoDef.get("package"));
                        if (dtoDef.containsKey("extends")) existingDto.put("extends", dtoDef.get("extends"));
                        if (dtoDef.containsKey("model")) existingDto.put("model", dtoDef.get("model"));
                        if (dtoDef.containsKey("decorator")) existingDto.put("decorator", dtoDef.get("decorator"));
                        if (dtoDef.containsKey("generateMapper")) existingDto.put("generateMapper", dtoDef.get("generateMapper"));
                        
                        if (dtoDef.containsKey("uses")) {
                            if (!existingDto.containsKey("uses")) {
                                existingDto.put("uses", new ArrayList<String>());
                            }
                            ((List<String>) existingDto.get("uses")).addAll((List<String>) dtoDef.get("uses"));
                        }
                        
                        if (dtoDef.containsKey("mappings")) {
                            if (!existingDto.containsKey("mappings")) {
                                existingDto.put("mappings", new ArrayList<Map<String, Object>>());
                            }
                            List<Map<String, Object>> existingMappings = (List<Map<String, Object>>) existingDto.get("mappings");
                            List<Map<String, Object>> newMappings = (List<Map<String, Object>>) dtoDef.get("mappings");
                            
                            for (Map<String, Object> newMap : newMappings) {
                                String target = (String) newMap.get("target");
                                // Remove any existing mapping with the same target
                                existingMappings.removeIf(m -> target.equals(m.get("target")));
                                existingMappings.add(newMap);
                            }
                        }
                    }
                }
            }
        }
        
        // Clean up duplicate global imports
        List<String> globalImports = (List<String>) mergedData.get("imports");
        Set<String> uniqueGlobalImports = new HashSet<>(globalImports);
        globalImports.clear();
        globalImports.addAll(uniqueGlobalImports);
        
        /* 
         * ==========================================
         * PASS 2: RESOLUTION & VALIDATION
         * ==========================================
         * Now that all extensions are merged, we loop through the final AST
         * to automatically resolve dependencies (like importing related models or superclasses).
         */
        Map<String, Map<String, Object>> mergedModels = (Map<String, Map<String, Object>>) mergedData.get("models");
        for (Map.Entry<String, Map<String, Object>> entry : mergedModels.entrySet()) {
            String modelName = entry.getKey();
            Map<String, Object> modelDef = entry.getValue();
            
            // Deduplicate per-model imports and annotations
            if (modelDef.containsKey("imports")) {
                List<String> imps = (List<String>) modelDef.get("imports");
                Set<String> uniqueImps = new HashSet<>(imps);
                imps.clear();
                imps.addAll(uniqueImps);
            }
            if (modelDef.containsKey("annotations")) {
                List<String> anns = (List<String>) modelDef.get("annotations");
                Set<String> uniqueAnns = new HashSet<>(anns);
                anns.clear();
                anns.addAll(uniqueAnns);
            }
            
            // Auto-extend AbstractItemModel if no superclass is specified
            if (!modelDef.containsKey("extends") && !modelName.equals("AbstractItemModel")) {
                modelDef.put("extends", "AbstractItemModel");
            }
            
            // Auto-resolve imports for related models (e.g., if Category has List<Product>)
            if (modelDef.containsKey("attributes")) {
                Map<String, Map<String, Object>> attrs = (Map<String, Map<String, Object>>) modelDef.get("attributes");
                for (Map.Entry<String, Map<String, Object>> attrEntry : attrs.entrySet()) {
                    Map<String, Object> attrDef = attrEntry.getValue();
                    String type = (String) attrDef.get("type");
                    
                    // Strip List<> or Set<> wrappers to find the base model type
                    String baseType = type.replaceAll("List<(.*)>", "$1");
                    
                    if (mergedModels.containsKey(baseType)) {
                        String targetPkg = (String) mergedModels.get(baseType).get("package");
                        String myPkg = (String) modelDef.get("package");
                        
                        // If the related model is in a different package, automatically add an import
                        if (targetPkg != null && !targetPkg.equals(myPkg)) {
                            if (!modelDef.containsKey("imports")) {
                                modelDef.put("imports", new ArrayList<String>());
                            }
                            ((List<String>) modelDef.get("imports")).add(targetPkg + "." + baseType);
                        }
                    } else {
                        Map<String, Object> mergedEnums = (Map<String, Object>) mergedData.get("enums");
                        if (mergedEnums.containsKey(baseType)) {
                            String targetPkg = "id.adiputera.demo.cms.models.enums";
                            String myPkg = (String) modelDef.get("package");
                            if (!targetPkg.equals(myPkg)) {
                                if (!modelDef.containsKey("imports")) {
                                    modelDef.put("imports", new ArrayList<String>());
                                }
                                ((List<String>) modelDef.get("imports")).add(targetPkg + "." + baseType);
                            }
                        }
                    }
                }
            }
            
            // Auto-resolve imports for superclass
            if (modelDef.containsKey("extends")) {
                String ext = (String) modelDef.get("extends");
                if (mergedModels.containsKey(ext)) {
                    String targetPkg = (String) mergedModels.get(ext).get("package");
                    String myPkg = (String) modelDef.get("package");
                    if (targetPkg != null && !targetPkg.equals(myPkg)) {
                        if (!modelDef.containsKey("imports")) {
                            modelDef.put("imports", new ArrayList<String>());
                        }
                        ((List<String>) modelDef.get("imports")).add(targetPkg + "." + ext);
                    }
                }
            }
        }
        
        Map<String, Map<String, Object>> mergedDtos = (Map<String, Map<String, Object>>) mergedData.get("dtos");
        for (Map.Entry<String, Map<String, Object>> entry : mergedDtos.entrySet()) {
            String dtoName = entry.getKey();
            Map<String, Object> dtoDef = entry.getValue();
            
            if (dtoDef.containsKey("imports")) {
                List<String> imps = (List<String>) dtoDef.get("imports");
                Set<String> uniqueImps = new HashSet<>(imps);
                imps.clear();
                imps.addAll(uniqueImps);
            }
            if (dtoDef.containsKey("annotations")) {
                List<String> anns = (List<String>) dtoDef.get("annotations");
                Set<String> uniqueAnns = new HashSet<>(anns);
                anns.clear();
                anns.addAll(uniqueAnns);
            }
            if (dtoDef.containsKey("uses")) {
                List<String> uses = (List<String>) dtoDef.get("uses");
                Set<String> uniqueUses = new HashSet<>(uses);
                uses.clear();
                uses.addAll(uniqueUses);
            }
            
            if (dtoDef.containsKey("attributes")) {
                Map<String, Map<String, Object>> attrs = (Map<String, Map<String, Object>>) dtoDef.get("attributes");
                for (Map.Entry<String, Map<String, Object>> attrEntry : attrs.entrySet()) {
                    Map<String, Object> attrDef = attrEntry.getValue();
                    String type = (String) attrDef.get("type");
                    
                    String baseType = type.replaceAll("List<(.*)>", "$1");
                    
                    if (mergedDtos.containsKey(baseType)) {
                        String targetPkg = (String) mergedDtos.get(baseType).get("package");
                        String myPkg = (String) dtoDef.get("package");
                        if (targetPkg != null && !targetPkg.equals(myPkg)) {
                            if (!dtoDef.containsKey("imports")) {
                                dtoDef.put("imports", new ArrayList<String>());
                            }
                            ((List<String>) dtoDef.get("imports")).add(targetPkg + "." + baseType);
                        }
                    } else {
                        Map<String, Object> mergedEnums = (Map<String, Object>) mergedData.get("enums");
                        if (mergedEnums.containsKey(baseType)) {
                            String targetPkg = "id.adiputera.demo.cms.models.enums";
                            String myPkg = (String) dtoDef.get("package");
                            if (!targetPkg.equals(myPkg)) {
                                if (!dtoDef.containsKey("imports")) {
                                    dtoDef.put("imports", new ArrayList<String>());
                                }
                                ((List<String>) dtoDef.get("imports")).add(targetPkg + "." + baseType);
                            }
                        }
                    }
                }
            }
        }
        
        /* 
         * ==========================================
         * PASS 3: CODE GENERATION (FreeMarker)
         * ==========================================
         * We inject the unified AST into the FreeMarker template
         * to generate the raw Java source files for compilation.
         */
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_32);
        cfg.setClassForTemplateLoading(Generator.class, "/templates");
        Template template = cfg.getTemplate("Entity.java.ftl");
        
        for (Map.Entry<String, Map<String, Object>> entry : mergedModels.entrySet()) {
            String modelName = entry.getKey();
            Map<String, Object> modelDef = entry.getValue();
            
            // Determine package and create directory structure
            String pkg = modelDef.containsKey("package") ? (String) modelDef.get("package") : "id.adiputera.demo.cms.models";
            String path = outputDir + "/" + pkg.replace('.', '/');
            new File(path).mkdirs();
            
            File outFile = new File(path, modelName + ".java");
            
            // Prepare template context
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("modelName", modelName);
            templateData.put("model", modelDef);
            templateData.put("globalImports", globalImports);
            
            // Write generated Java code to file
            try (Writer writer = new FileWriter(outFile)) {
                template.process(templateData, writer);
            }
            System.out.println("Generated " + outFile.getAbsolutePath());
        }
        
        Template enumTemplate = cfg.getTemplate("Enum.java.ftl");
        Map<String, List<String>> mergedEnums = (Map<String, List<String>>) mergedData.get("enums");
        for (Map.Entry<String, List<String>> entry : mergedEnums.entrySet()) {
            String enumName = entry.getKey();
            List<String> values = entry.getValue();
            
            String pkg = "id.adiputera.demo.cms.models.enums";
            String path = outputDir + "/" + pkg.replace('.', '/');
            new File(path).mkdirs();
            
            File outFile = new File(path, enumName + ".java");
            
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("enumName", enumName);
            templateData.put("packageName", pkg);
            templateData.put("values", values);
            
            try (Writer writer = new FileWriter(outFile)) {
                enumTemplate.process(templateData, writer);
            }
            System.out.println("Generated " + outFile.getAbsolutePath());
        }
        
        Template dtoTemplate = cfg.getTemplate("Dto.java.ftl");
        Template mapperTemplate = cfg.getTemplate("Mapper.java.ftl");
        
        for (Map.Entry<String, Map<String, Object>> entry : mergedDtos.entrySet()) {
            String dtoName = entry.getKey();
            Map<String, Object> dtoDef = entry.getValue();
            
            String pkg = dtoDef.containsKey("package") ? (String) dtoDef.get("package") : "id.adiputera.demo.cms.dtos";
            String path = outputDir + "/" + pkg.replace('.', '/');
            new File(path).mkdirs();
            
            File outFile = new File(path, dtoName + ".java");
            
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("className", dtoName);
            templateData.put("packageName", pkg);
            
            if (dtoDef.containsKey("extends")) templateData.put("extendsClass", dtoDef.get("extends"));
            
            List<String> combinedImports = new ArrayList<>(globalImports);
            if (dtoDef.containsKey("imports")) combinedImports.addAll((List<String>) dtoDef.get("imports"));
            templateData.put("imports", combinedImports);
            
            if (dtoDef.containsKey("annotations")) templateData.put("annotations", dtoDef.get("annotations"));
            if (dtoDef.containsKey("attributes")) templateData.put("attributes", dtoDef.get("attributes"));
            
            try (Writer writer = new FileWriter(outFile)) {
                dtoTemplate.process(templateData, writer);
            }
            System.out.println("Generated " + outFile.getAbsolutePath());
            
            boolean generateMapper = !dtoDef.containsKey("generateMapper") || (Boolean) dtoDef.get("generateMapper");
            if (dtoDef.containsKey("model") && generateMapper) {
                String modelFqdn = (String) dtoDef.get("model");
                String modelPkg = modelFqdn.substring(0, modelFqdn.lastIndexOf('.'));
                String modelName = modelFqdn.substring(modelFqdn.lastIndexOf('.') + 1);
                
                String mapperName = dtoName.replace("DTO", "Mapper");
                if (mapperName.equals(dtoName)) mapperName = dtoName + "Mapper";
                
                File mapperFile = new File(path, mapperName + ".java");
                
                Map<String, Object> mapperData = new HashMap<>();
                mapperData.put("packageName", pkg);
                mapperData.put("mapperName", mapperName);
                mapperData.put("dtoPackage", pkg);
                mapperData.put("dtoClassName", dtoName);
                mapperData.put("modelPackage", modelPkg);
                mapperData.put("modelClassName", modelName);
                
                if (dtoDef.containsKey("decorator")) mapperData.put("decorator", dtoDef.get("decorator"));
                if (dtoDef.containsKey("uses")) mapperData.put("uses", dtoDef.get("uses"));
                if (dtoDef.containsKey("mappings")) mapperData.put("mappings", dtoDef.get("mappings"));
                
                try (Writer writer = new FileWriter(mapperFile)) {
                    mapperTemplate.process(mapperData, writer);
                }
                System.out.println("Generated " + mapperFile.getAbsolutePath());
            }
        }
    }
}

