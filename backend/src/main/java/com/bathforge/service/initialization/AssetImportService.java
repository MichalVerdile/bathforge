package com.bathforge.service.initialization;

import com.bathforge.model.products.Category;
import com.bathforge.model.products.Color;
import com.bathforge.model.products.Product;
import com.bathforge.model.products.ProductColor;
import com.bathforge.repository.products.CategoryRepository;
import com.bathforge.repository.products.ColorRepository;
import com.bathforge.repository.products.ProductColorRepository;
import com.bathforge.repository.products.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Service responsible for importing bathroom product assets from the
 * filesystem.
 * Processes 3D models, images, and metadata to populate the product database
 * with categories,
 * colors, thumbnails, and mounting information.
 */
@Service
public class AssetImportService {

    private static final Logger log = LoggerFactory.getLogger(AssetImportService.class);

    private final CategoryRepository categoryRepository;
    private final ColorRepository colorRepository;
    private final ProductRepository productRepository;
    private final ProductColorRepository productColorRepository;

    public AssetImportService(CategoryRepository categoryRepository,
            ColorRepository colorRepository,
            ProductRepository productRepository,
            ProductColorRepository productColorRepository) {
        this.categoryRepository = categoryRepository;
        this.colorRepository = colorRepository;
        this.productRepository = productRepository;
        this.productColorRepository = productColorRepository;
    }

    private static final Map<String, String> CATEGORY_MAPPING = Map.of(
            "accessoires", "accessoires",
            "basins", "basins",
            "bathtubs", "bathtubs",
            "coverings", "coverings",
            "fittings", "fittings",
            "fittings_bathtubs", "fittings_bathtubs",
            "furniture", "furniture",
            "shower", "shower",
            "towel_radiators", "towel_radiators",
            "wcs", "wcs");

    private static final Map<String, Product.MountingType> MOUNTING_MAPPING = Map.of(
            "accessoires", Product.MountingType.WALL,
            "basins", Product.MountingType.WALL,
            "bathtubs", Product.MountingType.FREESTANDING,
            "coverings", Product.MountingType.FLOOR,
            "fittings", Product.MountingType.WALL,
            "fittings_bathtubs", Product.MountingType.WALL,
            "furniture", Product.MountingType.FLOOR,
            "shower", Product.MountingType.WALL,
            "towel_radiators", Product.MountingType.WALL,
            "wcs", Product.MountingType.FLOOR);

    private static final Set<String> MODEL_EXTS = Set.of(".glb", ".gltf");
    private static final Set<String> IMAGE_EXTS = Set.of(".jpg", ".jpeg", ".png", ".webp");

    /**
     * Imports all product assets from the specified directory path.
     * Scans category directories, processes 3D models and images, and creates
     * product entries in the database.
     *
     * @param assetsPath the absolute path to the assets root directory
     * @return a map containing import status, list of imported products, errors,
     *         and total product count
     * @throws IllegalArgumentException if the assets directory does not exist
     */
    @Transactional
    public Map<String, Object> importAllAssets(String assetsPath) {
        final Path assetsRoot = Paths.get(assetsPath).toAbsolutePath().normalize();
        if (!Files.isDirectory(assetsRoot)) {
            throw new IllegalArgumentException("Assets directory not found: " + assetsRoot);
        }

        log.info("Starting asset import from: {}", assetsRoot);

        final List<String> importedProducts = new ArrayList<>();
        final List<String> errors = new ArrayList<>();

        try (DirectoryStream<Path> categories = Files.newDirectoryStream(assetsRoot, Files::isDirectory)) {
            for (Path categoryDir : categories) {
                final String rawCategory = categoryDir.getFileName().toString().toLowerCase(Locale.ROOT);
                if (!CATEGORY_MAPPING.containsKey(rawCategory)) {
                    log.warn("Unknown category directory: {}", rawCategory);
                    continue;
                }

                try {
                    int imported = processCategoryDirectory(assetsRoot, categoryDir, rawCategory);
                    importedProducts.add(rawCategory + ": " + imported + " products");
                    log.info("Imported {} products from category: {}", imported, rawCategory);
                } catch (Exception e) {
                    String msg = "Error processing category " + rawCategory + ": " + e.getMessage();
                    errors.add(msg);
                    log.error(msg, e);
                }
            }
        } catch (IOException e) {
            log.error("Failed to enumerate category directories", e);
            return Map.of(
                    "status", "error",
                    "message", e.getMessage(),
                    "errors", errors);
        }

        return Map.of(
                "status", "success",
                "imported", importedProducts,
                "errors", errors,
                "totalProducts", productRepository.count());
    }

    /**
     * Processes a single category directory, importing all product models and their
     * associated metadata.
     *
     * @param assetsRoot  the root assets directory
     * @param categoryDir the category directory to process
     * @param categoryKey the category identifier key
     * @return the number of products imported from this category
     * @throws IOException if there is an error reading the directory
     */
    private int processCategoryDirectory(Path assetsRoot, Path categoryDir, String categoryKey) throws IOException {
        final String canonicalCategory = CATEGORY_MAPPING.get(categoryKey);
        final Category category = categoryRepository.findByNameIgnoreCase(canonicalCategory)
                .orElseThrow(() -> new IllegalStateException("Category not found in DB: " + canonicalCategory));

        final List<Color> categoryColors = colorRepository.findByCategoryId(category.getId());
        int importedCount = 0;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(categoryDir)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    importedCount += processCategoryDirectory(assetsRoot, entry, categoryKey);
                    continue;
                }

                final String nameLower = entry.getFileName().toString().toLowerCase(Locale.ROOT);
                final boolean isModel = hasAnySuffix(nameLower, MODEL_EXTS);
                final boolean isCoveringImage = "coverings".equalsIgnoreCase(categoryKey)
                        && hasAnySuffix(nameLower, IMAGE_EXTS);

                if (!isModel && !isCoveringImage)
                    continue;

                try {
                    Product product = createProductFromAsset(assetsRoot, categoryDir, entry, category, categoryKey);
                    if (product == null)
                        continue;

                    if (!productRepository.existsByNameAndCategoryId(product.getName(), category.getId())) {
                        Product saved = productRepository.save(product);
                        attachColors(saved, categoryColors);
                        importedCount++;
                        log.debug("Created product '{}' in category '{}'", product.getName(), canonicalCategory);
                    } else {
                        log.debug("Product already exists: '{}' in category '{}'", product.getName(),
                                canonicalCategory);
                    }
                } catch (Exception e) {
                    log.error("Error processing file {}: {}", entry.getFileName(), e.getMessage(), e);
                }
            }
        }
        return importedCount;
    }

    /**
     * Checks if a filename ends with any of the provided suffixes.
     *
     * @param filenameLower the lowercase filename to check
     * @param suffixes      the set of file extension suffixes to match against
     * @return true if the filename ends with any of the suffixes, false otherwise
     */
    private static boolean hasAnySuffix(String filenameLower, Set<String> suffixes) {
        for (String s : suffixes) {
            if (filenameLower.endsWith(s))
                return true;
        }
        return false;
    }

    /**
     * Associates all available colors with a product if not already linked.
     *
     * @param product the product to attach colors to
     * @param colors  the list of colors available for this product's category
     */
    private void attachColors(Product product, List<Color> colors) {
        for (Color color : colors) {
            if (!productColorRepository.existsByProductIdAndColorId(product.getId(), color.getId())) {
                ProductColor pc = new ProductColor();
                pc.setProduct(product);
                pc.setColor(color);
                productColorRepository.save(pc);
            }
        }
    }

    /**
     * Creates a new Product entity from an asset file.
     * Generates product name, determines mounting type, assigns price range, and
     * resolves thumbnail image.
     *
     * @param assetsRoot  the root assets directory
     * @param categoryDir the category directory containing the asset
     * @param file        the asset file path
     * @param category    the product category
     * @param categoryKey the category identifier key
     * @return a new Product entity configured from the asset
     */
    private Product createProductFromAsset(Path assetsRoot,
            Path categoryDir,
            Path file,
            Category category,
            String categoryKey) {
        final Product product = new Product();

        long nextIndex = productRepository.countByCategoryId(category.getId()) + 1;
        product.setName(formatProductName(category.getName(), (int) nextIndex));
        product.setDescription("");
        product.setCategory(category);

        String modelPath = toRelativeAssetPath(assetsRoot, file);
        product.setModelPath(modelPath);

        Product.PriceRange[] ranges = Product.PriceRange.values();
        product.setPriceRange(ranges[ThreadLocalRandom.current().nextInt(ranges.length)]);

        product.setMountingType(MOUNTING_MAPPING.getOrDefault(
                category.getName().toLowerCase(Locale.ROOT),
                Product.MountingType.WALL));

        String thumbnail = resolveThumbnail(categoryKey, assetsRoot, categoryDir, file);
        product.setThumbnail(thumbnail);

        return product;
    }

    /**
     * Formats a product name from category name and sequential number.
     *
     * @param categoryName  the category name
     * @param productNumber the sequential product number within the category
     * @return formatted product name (e.g., "Basins 1")
     */
    private static String formatProductName(String categoryName, int productNumber) {
        if (categoryName == null || categoryName.isEmpty())
            return "Product " + productNumber;
        String formatted = Character.toUpperCase(categoryName.charAt(0))
                + categoryName.substring(1).toLowerCase(Locale.ROOT);
        return formatted + " " + productNumber;
    }

    /**
     * Converts an absolute asset path to a relative web path.
     *
     * @param assetsRoot the root assets directory
     * @param target     the target file path
     * @return relative path prefixed with "/assets/" and using forward slashes
     */
    private static String toRelativeAssetPath(Path assetsRoot, Path target) {
        Path rel = assetsRoot.relativize(target.toAbsolutePath().normalize());
        return "/assets/" + rel.toString().replace('\\', '/');
    }

    /**
     * Resolves the thumbnail image path for a product asset based on
     * category-specific rules.
     * Different categories use different naming conventions and mapping strategies.
     *
     * @param categoryKey the category identifier
     * @param assetsRoot  the root assets directory
     * @param categoryDir the category directory
     * @param assetFile   the asset file
     * @return the relative path to the thumbnail image, or null if not found
     */
    private String resolveThumbnail(String categoryKey, Path assetsRoot, Path categoryDir, Path assetFile) {
        String base = stripExtension(assetFile.getFileName().toString());
        String baseLower = base.toLowerCase(Locale.ROOT);
        String baseUpper = base.toUpperCase(Locale.ROOT);

        switch (categoryKey) {
            case "furniture":
                return furnitureThumbnail(baseLower, assetsRoot, categoryDir);
            case "fittings":
            case "fittings_bathtubs":
                return imageContainingModelName(assetsRoot, categoryDir, baseLower);
            case "basins":
                return mappedThumbnailByGlb(base, assetsRoot, categoryDir, basinImageMap());
            case "wcs":
                return mappedThumbnailByGlb(base, assetsRoot, categoryDir, wcImageMap());
            case "bathtubs":
                // If under garniture subfolder, skip image for now
                if (categoryDir.getFileName().toString().equalsIgnoreCase("garniture")
                        || assetFile.getParent().getFileName().toString().equalsIgnoreCase("garniture")) {
                    return null;
                }
                return mappedThumbnailByGlb(base, assetsRoot, categoryDir, bathtubImageMap());
            case "shower":
                return showersThumbnail(baseUpper, assetsRoot, categoryDir);
            case "towel_radiators":
                return towelRadiatorsThumbnail(base, assetsRoot, categoryDir);
            case "coverings":
                return toRelativeAssetPath(assetsRoot, assetFile);
            case "accessoires":
                return specificImage(assetsRoot, categoryDir,
                        "Rexa_accessorimensola_specchiera_interlude_gallery_1.jpg");
            default:
                return null;
        }
    }

    /**
     * Removes the file extension from a filename.
     *
     * @param filename the filename to process
     * @return the filename without its extension
     */
    private static String stripExtension(String filename) {
        int idx = filename.lastIndexOf('.');
        return (idx > 0) ? filename.substring(0, idx) : filename;
    }

    private String furnitureThumbnail(String nameLower, Path assetsRoot, Path categoryDir) {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("esperanto", "ESPERANTO_mobili_generale-2.jpg");
        map.put("interlude", "INTERLUDE_mobili_generale-1.jpg");
        map.put("moode", "moode-2024-set-1-A.jpg");
        map.put("r1", "R1_mobili_generale-1.jpg");
        map.put("compact", "Rexa_mobili_compact_living_gallery_21.jpg");
        map.put("ergo", "Rexa_mobili_ergo23_gallery_3-2048x1366.jpg");
        map.put("prime", "Rexa_mobili_prime_gallery_4.jpg");
        map.put("slide", "Rexa_mobili_slide_gallery_2-2048x1366.jpg");
        map.put("unico", "Rexa_mobili_unico_gallery_1-2048x1366.jpg");
        map.put("vision", "rexa_mobili_vision_icon_generale.jpg");

        for (Map.Entry<String, String> e : map.entrySet()) {
            if (nameLower.contains(e.getKey())) {
                return specificImage(assetsRoot, categoryDir, e.getValue());
            }
        }
        return null;
    }

    private String imageContainingModelName(Path assetsRoot, Path dir, String modelBaseLower) {
        try (DirectoryStream<Path> images = Files.newDirectoryStream(dir,
                p -> Files.isRegularFile(p) && isImageFile(p))) {
            for (Path img : images) {
                if (img.getFileName().toString().toLowerCase(Locale.ROOT).contains(modelBaseLower)) {
                    return toRelativeAssetPath(assetsRoot, img);
                }
            }
        } catch (IOException e) {
            log.debug("imageContainingModelName scan error: {}", e.getMessage());
        }
        return null;
    }

    private static boolean isImageFile(Path p) {
        String n = p.getFileName().toString().toLowerCase(Locale.ROOT);
        return hasAnySuffix(n, IMAGE_EXTS);
    }

    private String mappedThumbnailByGlb(String glbBaseName,
            Path assetsRoot,
            Path dir,
            Map<String, String> imgByGlbBase) {
        String img = imgByGlbBase.get(glbBaseName);
        if (img == null) {
            for (Map.Entry<String, String> e : imgByGlbBase.entrySet()) {
                if (e.getKey().equalsIgnoreCase(glbBaseName)) {
                    img = e.getValue();
                    break;
                }
            }
        }
        return (img == null) ? null : specificImage(assetsRoot, dir, img);
    }

    private String specificImage(Path assetsRoot, Path currentDir, String imageFileName) {
        Path candidate = currentDir.resolve(imageFileName);
        if (!Files.exists(candidate)) {
            Path rootDir = currentDir;
            while (rootDir.getParent() != null &&
                    !CATEGORY_MAPPING.containsKey(rootDir.getFileName().toString().toLowerCase(Locale.ROOT))) {
                rootDir = rootDir.getParent();
            }
            candidate = rootDir.resolve(imageFileName);
        }
        return Files.exists(candidate) ? toRelativeAssetPath(assetsRoot, candidate) : null;
    }

    private Map<String, String> basinImageMap() {
        Map<String, String> m = new HashMap<>();
        m.put("Delfopianoret_113", "20241002-7766-113-sx-rubdx.jpg");
        m.put("Delfopianoret_152", "20241011-7803-152_catinooale.jpg");
        m.put("filo_doghe", "20240930-7530-doghe-sx.jpg");
        m.put("filo_mensola", "20240930-7540-mensola-sx.jpg");
        m.put("ITFREEC-ITFREEP", "20240930-7577-freestanding.jpg");
        m.put("ITMT128DX_ITMT128SX", "20250723-7998-mobile-lungo-terra-centrale-.jpg");
        m.put("kanto87", "20241002-7743-kanto_mobile87.jpg");
        m.put("narciso_mini_lavabo", "20230914-7220-lavabo_mini_appoggio_sx.jpg");
        m.put("OTLS_OTCOL", "20241001-7685-otis_colonna.jpg");
        m.put("siwa_cassetto_specchiorettangolare", "20230203-5328-siwa.jpg");
        m.put("siwa_cassetto_specchiotondo", "20230203-5328-siwa.jpg");
        m.put("Tiberino", "20230329-6128-tiberio.jpg");
        return m;
    }

    private Map<String, String> wcImageMap() {
        Map<String, String> m = new HashMap<>();
        m.put("ERVAK-wcaterraEra", "20230317-5942-ERA_TERRA.jpg");
        m.put("ERVSK-wcsospesoEra", "20230515-6935-ERA_WC-SOSP.jpg");
        m.put("SMVAS-wcaterra53", "20230216-5581-wcterra.jpg");
        m.put("SMVAS+SMCM-wcaterraconcassettamonobloccodaterra", "20230223-5667-monoblocco-1.jpg");
        m.put("EJVA-wcterra", "20230227-5686-wc-terra.jpg");
        m.put("EJBS-bidetsospeso", "20230227-5685-bidet-sosp.jpg");
        m.put("EJBI-bidetterra", "20250627-7937-bidet-terra.jpg");
        m.put("CAVSK", "20230515-6931-catini_wc.jpg");
        m.put("CABS", "20230331-6454-catini_bidet.jpg");
        return m;
    }

    private Map<String, String> bathtubImageMap() {
        Map<String, String> m = new HashMap<>();
        m.put("3dpea.com_VBKS1990C2_BOOK_bathtub_190x90_freestanding_SX", "rexa_vasche_book_download_1200px.jpg");
        m.put("3dpea.com_VR1D1980C1_R1_bathtub_190x80_freestanding_DX", "rexa_vasche_R1_download_1200px.jpg");
        m.put("VLU01790C1_LUNI_bathtub_170x90_freestanding", "rexa_vasche_luni_download_1200px.jpg");
        m.put("VMYC187501_MYPLANET_bathtub_180x75x50_freestanding", "rexa_vasche_myplanet_download_1200px.jpg");
        return m;
    }

    private String showersThumbnail(String modelUpper, Path assetsRoot, Path dir) {
        if (modelUpper.contains("DN01FG"))
            return specificImage(assetsRoot, dir, "cq5dam.web.460.460_(8).jpeg");
        if (modelUpper.contains("DN01G"))
            return specificImage(assetsRoot, dir, "cq5dam.web.460.460_(7).jpeg");
        if (modelUpper.contains("FREEF2"))
            return specificImage(assetsRoot, dir, "cq5dam.web.460.460_(6).jpeg");
        if (modelUpper.contains("FREE2G"))
            return specificImage(assetsRoot, dir, "cq5dam.web.460.460_(5).jpeg");
        if (modelUpper.contains("FREE2A"))
            return specificImage(assetsRoot, dir, "cq5dam.web.460.460_(4).jpeg");
        if (modelUpper.contains("KUADF"))
            return specificImage(assetsRoot, dir, "cq5dam.web.460.460_(3).jpeg");
        if (modelUpper.contains("KUADH"))
            return specificImage(assetsRoot, dir, "cq5dam.web.460.460_(2).jpeg");
        if (modelUpper.contains("NHGFL"))
            return specificImage(assetsRoot, dir, "cq5dam.web.460.460_(1).jpeg");
        if (modelUpper.contains("NHG") || modelUpper.contains("NHF"))
            return specificImage(assetsRoot, dir, "cq5dam.web.460.460.jpeg");
        return null;
    }

    private String towelRadiatorsThumbnail(String glbBase, Path assetsRoot, Path dir) {
        String upper = glbBase.toUpperCase(Locale.ROOT);
        if (upper.contains("LANA"))
            return specificImage(assetsRoot, dir, "lana-1.jpg");
        if (upper.contains("ANDROID"))
            return specificImage(assetsRoot, dir, "800x800_14665.jpg");
        if (upper.contains("CARLO") || upper.contains("TINA"))
            return specificImage(assetsRoot, dir, "Antrax-IT_Carlo-03-scaled.jpg");
        if (upper.contains("FLAPS"))
            return specificImage(assetsRoot, dir, "800x800_14733.jpg");
        if (upper.contains("COMPLETO"))
            return specificImage(assetsRoot, dir, "Cover-el.jpg");

        Map<String, String> explicit = new HashMap<>();
        explicit.put("ORESTE&EMMAAC-ANTRAXIT", "800x800_14760.jpg");
        explicit.put("pioli", "800x800_14680.jpg");
        explicit.put("SATURN&MOON-ANTRAXIT", "800x800_14773-2.jpg");
        explicit.put("Tubone-1", "Img_Griffe_Tubone_ritagliata-03-e1717080494952.jpg");

        for (Map.Entry<String, String> e : explicit.entrySet()) {
            if (glbBase.equalsIgnoreCase(e.getKey())) {
                return specificImage(assetsRoot, dir, e.getValue());
            }
        }
        return null;
    }

    /**
     * Retrieves comprehensive statistics about the current import state.
     * Includes counts of products, categories, colors, and product-color
     * associations.
     *
     * @return a map containing import statistics and counts by category
     */
    public Map<String, Object> getImportStatistics() {
        try {
            long totalProducts = productRepository.count();
            long totalCategories = categoryRepository.count();
            long totalColors = colorRepository.count();
            long totalProductColors = productColorRepository.count();

            Map<String, Long> productsByCategory = new HashMap<>();
            for (Category c : categoryRepository.findAll()) {
                productsByCategory.put(c.getName(), productRepository.countByCategoryId(c.getId()));
            }

            Map<String, Object> stats = new LinkedHashMap<>();
            stats.put("totalProducts", totalProducts);
            stats.put("totalCategories", totalCategories);
            stats.put("totalColors", totalColors);
            stats.put("totalProductColors", totalProductColors);
            stats.put("productsByCategory", productsByCategory);
            return stats;
        } catch (Exception e) {
            log.error("Error getting import statistics", e);
            return Map.of("error", e.getMessage());
        }
    }

    /**
     * Deletes all products and their color associations from the database.
     * Useful for resetting the product catalog before a fresh import.
     *
     * @return a map containing the number of deleted products and product-color
     *         associations, or error information
     */
    @Transactional
    public Map<String, Object> clearAllProducts() {
        try {
            long deletedProductColors = productColorRepository.count();
            productColorRepository.deleteAll();

            long deletedProducts = productRepository.count();
            productRepository.deleteAll();

            log.info("Cleared {} products and {} product-color associations", deletedProducts, deletedProductColors);

            return Map.of(
                    "status", "success",
                    "deletedProducts", deletedProducts,
                    "deletedProductColors", deletedProductColors,
                    "message", "All products and their color associations have been cleared");
        } catch (Exception e) {
            log.error("Error clearing products", e);
            return Map.of(
                    "status", "error",
                    "message", e.getMessage());
        }
    }
}
