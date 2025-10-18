package com.bathforge.service.initialization;

import com.bathforge.model.products.Category;
import com.bathforge.model.products.Color;
import com.bathforge.repository.products.CategoryRepository;
import com.bathforge.repository.products.ColorRepository;
import com.bathforge.repository.products.ProductRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class DataInitializationService implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializationService.class);

    private final CategoryRepository categoryRepository;
    private final ColorRepository colorRepository;
    private final ProductRepository productRepository;
    private final AssetImportService assetImportService;

    @Value("${bathforge.auto-import.enabled:true}")
    private boolean autoImportEnabled;

    @Value("${bathforge.auto-import.skip-if-products-exist:true}")
    private boolean skipIfProductsExist;

    @Autowired
    public DataInitializationService(CategoryRepository categoryRepository,
            ColorRepository colorRepository,
            ProductRepository productRepository,
            AssetImportService assetImportService) {
        this.categoryRepository = categoryRepository;
        this.colorRepository = colorRepository;
        this.productRepository = productRepository;
        this.assetImportService = assetImportService;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        logger.info("🚀 Starting BathForge data initialization...");

        // Step 1: Initialize categories
        initializeCategories();

        // Step 2: Initialize colors
        initializeColors();

        // Step 3: Import assets if no products exist
        importAssetsIfNeeded();

        logger.info("✅ BathForge data initialization completed!");
    }

    private void initializeCategories() {
        List<String> categoryNames = Arrays.asList(
                "accessoires", "furniture", "basins", "wcs", "bathtubs",
                "shower", "fittings", "fittings_bathtubs", "towel_radiators", "coverings");

        for (String categoryName : categoryNames) {
            if (!categoryRepository.existsByNameIgnoreCase(categoryName)) {
                Category category = new Category(categoryName, "");
                categoryRepository.save(category);
                System.out.println("Created category: " + categoryName);
            }
        }
    }

    private void initializeColors() {
        // Initialize colors for each category
        initializeAccessoiresColors();
        initializeFurnitureColors();
        initializeBasinsColors();
        initializeWCsColors();
        initializeBathtubsColors();
        initializeShowerColors();
        initializeFittingsColors();
        initializeFittingsBathtubsColors();
        initializeTowelRadiatorsColors();
    }

    private void initializeAccessoiresColors() {
        Category category = categoryRepository.findByNameIgnoreCase("accessoires").orElse(null);
        if (category == null)
            return;

        List<ColorData> colors = Arrays.asList(
                new ColorData("Glacier White", "#ffffff"),
                new ColorData("Elegant Gray", "#c3b6a5"),
                new ColorData("Ash Aggregate", "#766f69"),
                new ColorData("Carbon Concrete", "#5e5751"),
                new ColorData("Deep Nocturne", "#010101"));

        saveColorsForCategory(category, colors);
    }

    private void initializeFurnitureColors() {
        Category category = categoryRepository.findByNameIgnoreCase("furniture").orElse(null);
        if (category == null)
            return;

        // Same as Accessoires
        List<ColorData> colors = Arrays.asList(
                new ColorData("Glacier White", "#ffffff"),
                new ColorData("Elegant Gray", "#c3b6a5"),
                new ColorData("Ash Aggregate", "#766f69"),
                new ColorData("Carbon Concrete", "#5e5751"),
                new ColorData("Deep Nocturne", "#010101"));

        saveColorsForCategory(category, colors);
    }

    private void initializeBasinsColors() {
        Category category = categoryRepository.findByNameIgnoreCase("basins").orElse(null);
        if (category == null)
            return;

        List<ColorData> colors = Arrays.asList(
                new ColorData("Gloss White", "#e3e3e3"),
                new ColorData("Gloss Black", "#020202"),
                new ColorData("Fumo", "#454545"),
                new ColorData("Talpa", "#6c625f"));

        saveColorsForCategory(category, colors);
    }

    private void initializeWCsColors() {
        Category category = categoryRepository.findByNameIgnoreCase("wcs").orElse(null);
        if (category == null)
            return;

        // Same as Basins
        List<ColorData> colors = Arrays.asList(
                new ColorData("Gloss White", "#e3e3e3"),
                new ColorData("Gloss Black", "#020202"),
                new ColorData("Fumo", "#454545"),
                new ColorData("Talpa", "#6c625f"));

        saveColorsForCategory(category, colors);
    }

    private void initializeBathtubsColors() {
        Category category = categoryRepository.findByNameIgnoreCase("bathtubs").orElse(null);
        if (category == null)
            return;

        List<ColorData> colors = Arrays.asList(
                new ColorData("Black", "#000000"),
                new ColorData("Rubin", "#9e183d"),
                new ColorData("Mustard", "#e7b32b"),
                new ColorData("Lime", "#e7b32b"),
                new ColorData("Artic", "#005365"),
                new ColorData("Fucile", "#414640"),
                new ColorData("Earth", "#958a74"),
                new ColorData("Pearl", "#a9a8a3"),
                new ColorData("Gray", "#818586"),
                new ColorData("Light Grey", "#cbc7bc"),
                new ColorData("White", "#efeff1"));

        saveColorsForCategory(category, colors);
    }

    private void initializeShowerColors() {
        Category category = categoryRepository.findByNameIgnoreCase("shower").orElse(null);
        if (category == null)
            return;

        List<ColorData> colors = Arrays.asList(
                new ColorData("White", "#efeff1"),
                new ColorData("Silver", "#b7b6bc"),
                new ColorData("Chrom", "#a7aab1"),
                new ColorData("Black Opac", "#25262a"));

        saveColorsForCategory(category, colors);
    }

    private void initializeFittingsColors() {
        Category category = categoryRepository.findByNameIgnoreCase("fittings").orElse(null);
        if (category == null)
            return;

        List<ColorData> colors = Arrays.asList(
                new ColorData("Chrom", "#a7aab1"),
                new ColorData("Inox", "#7a7772"),
                new ColorData("Dark Grey", "#514c49"),
                new ColorData("Copper", "#856d61"),
                new ColorData("Gold", "#bfa371"),
                new ColorData("Pale Gold", "#b7a082"));

        saveColorsForCategory(category, colors);
    }

    private void initializeFittingsBathtubsColors() {
        Category category = categoryRepository.findByNameIgnoreCase("fittings_bathtubs").orElse(null);
        if (category == null)
            return;

        // Same as Fittings
        List<ColorData> colors = Arrays.asList(
                new ColorData("Chrom", "#a7aab1"),
                new ColorData("Inox", "#7a7772"),
                new ColorData("Dark Grey", "#514c49"),
                new ColorData("Copper", "#856d61"),
                new ColorData("Gold", "#bfa371"),
                new ColorData("Pale Gold", "#b7a082"));

        saveColorsForCategory(category, colors);
    }

    private void initializeTowelRadiatorsColors() {
        Category category = categoryRepository.findByNameIgnoreCase("towel_radiators").orElse(null);
        if (category == null)
            return;

        List<ColorData> colors = Arrays.asList(
                new ColorData("Cold White", "#fcfdfd"),
                new ColorData("Milk", "#f9f4ea"),
                new ColorData("Aluminum", "#efeeec"),
                new ColorData("Cream", "#f9ecd8"),
                new ColorData("Exotic Orange", "#8a4a24"),
                new ColorData("Metheor Blue", "#4c5868"),
                new ColorData("Cold Silver", "#d5d6d0"),
                new ColorData("Corten", "#5e3a22"),
                new ColorData("Grafit Grey", "#6c6866"),
                new ColorData("Cold Iron", "#48493f"),
                new ColorData("Cold Black", "#1b1d1d"));

        saveColorsForCategory(category, colors);
    }

    private void saveColorsForCategory(Category category, List<ColorData> colorsData) {
        for (ColorData colorData : colorsData) {
            if (!colorRepository.existsByNameIgnoreCaseAndCategory(colorData.name, category)) {
                Color color = new Color(colorData.name, colorData.hexCode, category);
                colorRepository.save(color);
                System.out.println("Created color: " + colorData.name + " for category: " + category.getName());
            }
        }
    }

    private void importAssetsIfNeeded() {
        try {
            // Check if auto-import is enabled
            if (!autoImportEnabled) {
                logger.info("🚫 Automatic asset import is disabled");
                return;
            }

            // Check if products already exist and we should skip
            long existingProducts = productRepository.count();
            if (existingProducts > 0 && skipIfProductsExist) {
                logger.info("📦 Found {} existing products, skipping asset import (skipIfProductsExist=true)",
                        existingProducts);
                return;
            } else if (existingProducts > 0) {
                logger.info("� Found {} existing products, but auto-import will proceed (skipIfProductsExist=false)",
                        existingProducts);
            }

            logger.info("🔍 Starting automatic asset import...");

            // Try to find the assets directory
            String assetsPath = findAssetsDirectory();
            if (assetsPath == null) {
                logger.warn("⚠️  Assets directory not found. Skipping automatic import.");
                logger.info("💡 You can manually import assets later using: POST /api/admin/import-assets");
                return;
            }

            logger.info("📁 Found assets directory: {}", assetsPath);

            // Import assets
            Map<String, Object> result = assetImportService.importAllAssets(assetsPath);

            if ("success".equals(result.get("status"))) {
                logger.info("🎉 Successfully imported assets!");
                if (result.get("imported") != null) {
                    @SuppressWarnings("unchecked")
                    List<String> imported = (List<String>) result.get("imported");
                    imported.forEach(item -> logger.info("  ✅ {}", item));
                }
                logger.info("📊 Total products in database: {}", result.get("totalProducts"));
            } else {
                logger.error("❌ Asset import failed: {}", result.get("message"));
                if (result.get("errors") != null) {
                    @SuppressWarnings("unchecked")
                    List<String> errors = (List<String>) result.get("errors");
                    errors.forEach(error -> logger.error("  🔴 {}", error));
                }
            }

        } catch (Exception e) {
            logger.error("💥 Error during automatic asset import: {}", e.getMessage(), e);
            logger.info("💡 You can manually import assets later using: POST /api/admin/import-assets");
        }
    }

    private String findAssetsDirectory() {
        // Try multiple possible paths for the assets directory
        String[] possiblePaths = {
                "../frontend/public/assets", // Most common case - backend and frontend siblings
                "frontend/public/assets", // If running from project root
                "../../../frontend/public/assets", // If running from build/libs
                "../../frontend/public/assets", // Alternative structure
                "./frontend/public/assets" // Current directory
        };

        for (String path : possiblePaths) {
            File assetsDir = new File(path);
            if (assetsDir.exists() && assetsDir.isDirectory()) {
                // Check if it contains category directories
                File[] categoryDirs = assetsDir.listFiles(File::isDirectory);
                if (categoryDirs != null && categoryDirs.length > 0) {
                    return assetsDir.getAbsolutePath();
                }
            }
        }

        return null;
    }

    private static class ColorData {
        String name;
        String hexCode;

        ColorData(String name, String hexCode) {
            this.name = name;
            this.hexCode = hexCode;
        }
    }
}