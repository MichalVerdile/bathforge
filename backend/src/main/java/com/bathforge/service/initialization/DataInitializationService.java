package com.bathforge.service.initialization;

import com.bathforge.model.products.Category;
import com.bathforge.model.products.Color;
import com.bathforge.repository.products.CategoryRepository;
import com.bathforge.repository.products.ColorRepository;
import com.bathforge.repository.products.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class DataInitializationService implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializationService.class);

    private final CategoryRepository categoryRepository;
    private final ColorRepository colorRepository;
    private final ProductRepository productRepository;
    private final AssetImportService assetImportService;

    @Value("${bathforge.auto-import.enabled:true}")
    private boolean autoImportEnabled;

    @Value("${bathforge.auto-import.skip-if-products-exist:true}")
    private boolean skipIfProductsExist;

    public DataInitializationService(CategoryRepository categoryRepository,
            ColorRepository colorRepository,
            ProductRepository productRepository,
            AssetImportService assetImportService) {
        this.categoryRepository = categoryRepository;
        this.colorRepository = colorRepository;
        this.productRepository = productRepository;
        this.assetImportService = assetImportService;
    }

    private static final List<String> CATEGORY_NAMES = List.of(
            "accessoires", "furniture", "basins", "wcs", "bathtubs",
            "shower", "fittings", "fittings_bathtubs", "towel_radiators", "coverings");

    private static final List<ColorData> PALETTE_WHITE_CONCRETE = List.of(
            new ColorData("Glacier White", "#ffffff"),
            new ColorData("Elegant Gray", "#c3b6a5"),
            new ColorData("Ash Aggregate", "#766f69"),
            new ColorData("Carbon Concrete", "#5e5751"),
            new ColorData("Deep Nocturne", "#010101"));

    private static final List<ColorData> PALETTE_GLOSS = List.of(
            new ColorData("Gloss White", "#e3e3e3"),
            new ColorData("Gloss Black", "#020202"),
            new ColorData("Fumo", "#454545"),
            new ColorData("Talpa", "#6c625f"));

    private static final List<ColorData> PALETTE_FITTINGS = List.of(
            new ColorData("Chrom", "#a7aab1"),
            new ColorData("Inox", "#7a7772"),
            new ColorData("Dark Grey", "#514c49"),
            new ColorData("Copper", "#856d61"),
            new ColorData("Gold", "#bfa371"),
            new ColorData("Pale Gold", "#b7a082"));

    private static final List<ColorData> PALETTE_TOWEL_RADIATORS = List.of(
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

    private static final List<ColorData> PALETTE_BATHTUBS = List.of(
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

    private static final List<ColorData> PALETTE_SHOWER = List.of(
            new ColorData("White", "#efeff1"),
            new ColorData("Silver", "#b7b6bc"),
            new ColorData("Chrom", "#a7aab1"),
            new ColorData("Black Opac", "#25262a"));

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Starting BathForge data initialization...");

        initializeCategories();
        initializeColors();
        importAssetsIfNeeded();

        log.info("BathForge data initialization completed!");
    }

    private void initializeCategories() {
        for (String name : CATEGORY_NAMES) {
            if (!categoryRepository.existsByNameIgnoreCase(name)) {
                categoryRepository.save(new Category(name, ""));
                log.info("Created category: {}", name);
            }
        }
    }

    private void initializeColors() {
        Map<String, List<ColorData>> colorByCategory = Map.of(
                "accessoires", PALETTE_WHITE_CONCRETE,
                "furniture", PALETTE_WHITE_CONCRETE,
                "basins", PALETTE_GLOSS,
                "wcs", PALETTE_GLOSS,
                "bathtubs", PALETTE_BATHTUBS,
                "shower", PALETTE_SHOWER,
                "fittings", PALETTE_FITTINGS,
                "fittings_bathtubs", PALETTE_FITTINGS,
                "towel_radiators", PALETTE_TOWEL_RADIATORS);

        Map<String, Category> categories = categoryRepository.findAll().stream()
                .collect(Collectors.toMap(c -> c.getName().toLowerCase(Locale.ROOT), c -> c));

        colorByCategory.forEach((categoryKey, palette) -> {
            Category category = categories.get(categoryKey);
            if (category == null) {
                log.warn("Category '{}' missing in DB while initializing colors", categoryKey);
                return;
            }
            saveColorsForCategory(category, palette);
        });
    }

    private void saveColorsForCategory(Category category, List<ColorData> colorsData) {
        for (ColorData data : colorsData) {
            if (!colorRepository.existsByNameIgnoreCaseAndCategory(data.name, category)) {
                colorRepository.save(new Color(data.name, data.hexCode, category));
                log.info("Created color '{}' for category '{}'", data.name, category.getName());
            }
        }
    }

    private void importAssetsIfNeeded() {
        try {
            if (!autoImportEnabled) {
                log.info("Automatic asset import is disabled");
                return;
            }

            long existingProducts = productRepository.count();
            if (existingProducts > 0 && skipIfProductsExist) {
                log.info("Found {} existing products, skipping asset import (skipIfProductsExist=true)",
                        existingProducts);
                return;
            } else if (existingProducts > 0) {
                log.info("Found {} existing products, proceeding with auto-import (skipIfProductsExist=false)",
                        existingProducts);
            }

            log.info("Starting automatic asset import…");

            Optional<Path> assetsDir = findAssetsDirectory();
            if (assetsDir.isEmpty()) {
                log.warn("Assets directory not found. Skipping automatic import.");
                log.info("You can manually import assets later using: POST /api/admin/import-assets");
                return;
            }

            Path path = assetsDir.get();
            log.info("Found assets directory: {}", path);

            Map<String, Object> result = assetImportService.importAllAssets(path.toString());

            if ("success".equals(result.get("status"))) {
                log.info("Successfully imported assets!");
                @SuppressWarnings("unchecked")
                List<String> imported = (List<String>) result.get("imported");
                if (imported != null) {
                    imported.forEach(s -> log.info("{}", s));
                }
                log.info("Total products in database: {}", result.get("totalProducts"));
            } else {
                log.error("Asset import failed: {}", result.get("message"));
                @SuppressWarnings("unchecked")
                List<String> errors = (List<String>) result.get("errors");
                if (errors != null) {
                    errors.forEach(err -> log.error("{}", err));
                }
            }

        } catch (Exception e) {
            log.error("Error during automatic asset import: {}", e.getMessage(), e);
            log.info("You can manually import assets later using: POST /api/admin/import-assets");
        }
    }

    private Optional<Path> findAssetsDirectory() {
        // First try to find assets in backend resources (for production/Railway)
        try {
            Path resourcesAssets = Paths.get("src/main/resources/static/assets").toAbsolutePath().normalize();
            if (Files.isDirectory(resourcesAssets) && hasAnySubdirectory(resourcesAssets)) {
                log.info("Found assets in backend resources: {}", resourcesAssets);
                return Optional.of(resourcesAssets);
            }
        } catch (Exception e) {
            log.debug("Backend resources assets not found: {}", e.getMessage());
        }

        // Fallback to frontend paths for local development
        List<String> candidates = List.of(
                "../frontend/public/assets",
                "frontend/public/assets",
                "../../../frontend/public/assets",
                "../../frontend/public/assets",
                "./frontend/public/assets");

        return candidates.stream()
                .map(p -> Paths.get(p).toAbsolutePath().normalize())
                .filter(Files::isDirectory)
                .filter(this::hasAnySubdirectory)
                .findFirst();
    }

    private boolean hasAnySubdirectory(Path dir) {
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir, Files::isDirectory)) {
            return ds.iterator().hasNext();
        } catch (Exception e) {
            log.debug("Failed checking subdirectories for {}: {}", dir, e.getMessage());
            return false;
        }
    }

    private static final class ColorData {
        final String name;
        final String hexCode;

        ColorData(String name, String hexCode) {
            this.name = name;
            this.hexCode = hexCode;
        }
    }
}
