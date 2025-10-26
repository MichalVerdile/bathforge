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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.*;

@Service
public class AssetImportService {

    private static final Logger logger = LoggerFactory.getLogger(AssetImportService.class);

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ColorRepository colorRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductColorRepository productColorRepository;

    private static final Map<String, String> CATEGORY_MAPPING = Map.of(
            // Use database category names as canonical values
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
            // Align keys with actual category names used in DB
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

    @Transactional
    public Map<String, Object> importAllAssets(String assetsPath) {
        logger.info("Starting asset import from: {}", assetsPath);

        Map<String, Object> result = new HashMap<>();
        List<String> importedProducts = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        File assetsDir = new File(assetsPath);
        if (!assetsDir.exists() || !assetsDir.isDirectory()) {
            throw new RuntimeException("Assets directory not found: " + assetsPath);
        }

        try {
            File[] categoryDirs = assetsDir.listFiles(File::isDirectory);
            if (categoryDirs != null) {
                for (File categoryDir : categoryDirs) {
                    String categoryName = categoryDir.getName().toLowerCase();
                    if (CATEGORY_MAPPING.containsKey(categoryName)) {
                        try {
                            int imported = processCategoryDirectory(assetsDir, categoryDir, categoryName);
                            importedProducts.add(categoryName + ": " + imported + " products");
                            logger.info("Imported {} products from category: {}", imported, categoryName);
                        } catch (Exception e) {
                            String error = "Error processing category " + categoryName + ": " + e.getMessage();
                            errors.add(error);
                            logger.error(error, e);
                        }
                    } else {
                        logger.warn("Unknown category directory: {}", categoryName);
                    }
                }
            }

            result.put("status", "success");
            result.put("imported", importedProducts);
            result.put("errors", errors);
            result.put("totalProducts", productRepository.count());

        } catch (Exception e) {
            logger.error("Failed to import assets", e);
            result.put("status", "error");
            result.put("message", e.getMessage());
            result.put("errors", errors);
        }

        return result;
    }

    private int processCategoryDirectory(File assetsRoot, File categoryDir, String categoryName) {
        String normalizedCategory = CATEGORY_MAPPING.get(categoryName);
        Category category = categoryRepository.findByNameIgnoreCase(normalizedCategory)
                .orElseThrow(() -> new RuntimeException("Category not found: " + normalizedCategory));

        List<Color> categoryColors = colorRepository.findByCategoryId(category.getId());

        int importedCount = 0;
        File[] files = categoryDir.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    // Recurse into subdirectories (e.g., bathtubs/garniture)
                    importedCount += processCategoryDirectory(assetsRoot, file, categoryName);
                    continue;
                }

                // Accept GLB/GLTF everywhere; additionally accept image files for 'coverings'
                if (isModelFile(file) || ("coverings".equalsIgnoreCase(categoryName) && isImageFile(file))) {
                    try {
                        Product product = createProductFromModelFile(assetsRoot, categoryDir, file, category);
                        if (product != null) {
                            if (!productRepository.existsByNameAndCategoryId(product.getName(), category.getId())) {
                                Product savedProduct = productRepository.save(product);

                                for (Color color : categoryColors) {
                                    if (!productColorRepository.existsByProductIdAndColorId(savedProduct.getId(),
                                            color.getId())) {
                                        ProductColor productColor = new ProductColor();
                                        productColor.setProduct(savedProduct);
                                        productColor.setColor(color);
                                        productColorRepository.save(productColor);
                                    }
                                }

                                importedCount++;
                                logger.debug("Created product: {} in category: {}", product.getName(),
                                        normalizedCategory);
                            } else {
                                logger.debug("Product already exists: {} in category: {}", product.getName(),
                                        normalizedCategory);
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Error processing file: {} - {}", file.getName(), e.getMessage());
                    }
                }
            }
        }

        return importedCount;
    }

    private boolean isModelFile(File file) {
        if (file.isDirectory())
            return false;
        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".glb") || fileName.endsWith(".gltf");
    }

    private String getBaseName(File file) {
        String name = file.getName();
        int idx = name.lastIndexOf('.');
        return idx > 0 ? name.substring(0, idx) : name;
    }

    private String toRelativeAssetPath(File assetsRoot, File target) {
        String rootPath = assetsRoot.getAbsolutePath();
        String targetPath = target.getAbsolutePath();
        String rel = targetPath.substring(rootPath.length()).replace('\\', '/');
        if (!rel.startsWith("/"))
            rel = "/" + rel;
        return "/assets" + rel; // assets folder is served at /assets
    }

    private Product createProductFromModelFile(File assetsRoot, File categoryDir, File modelFile, Category category) {
        Product product = new Product();
        // Preserve naming style and do not derive from model filename
        long nextIndex = productRepository.countByCategoryId(category.getId()) + 1;
        product.setName(formatProductName(category.getName(), (int) nextIndex));
        product.setDescription("");
        product.setCategory(category);

        // Compute model path relative to /assets, preserving subfolders
        String modelPath = toRelativeAssetPath(assetsRoot, modelFile);
        product.setModelPath(modelPath);

        // Assign price and mounting
        Product.PriceRange[] priceRanges = Product.PriceRange.values();
        product.setPriceRange(priceRanges[new Random().nextInt(priceRanges.length)]);
        product.setMountingType(
                MOUNTING_MAPPING.getOrDefault(category.getName().toLowerCase(), Product.MountingType.WALL));

        // Determine thumbnail by category-specific rules
        String thumbnail = resolveThumbnail(category.getName().toLowerCase(), assetsRoot, categoryDir, modelFile);
        product.setThumbnail(thumbnail);

        return product;
    }

    private String formatProductName(String categoryName, int productNumber) {
        String formattedCategory = categoryName.substring(0, 1).toUpperCase() +
                categoryName.substring(1).toLowerCase();
        return formattedCategory + " " + productNumber;
    }

    private String resolveThumbnail(String categoryName, File assetsRoot, File categoryDir, File modelFile) {
        String base = getBaseName(modelFile);
        String baseLower = base.toLowerCase();

        switch (categoryName) {
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
                if (categoryDir.getName().equalsIgnoreCase("garniture")
                        || modelFile.getParentFile().getName().equalsIgnoreCase("garniture")) {
                    return null;
                }
                return mappedThumbnailByGlb(base, assetsRoot, categoryDir, bathtubImageMap());
            case "shower":
                return showersThumbnail(baseUpper(base), assetsRoot, categoryDir);
            case "towel_radiators":
                return towelRadiatorsThumbnail(base, assetsRoot, categoryDir);
            case "coverings":
                // For coverings, the image should be the same as the modelPath
                return toRelativeAssetPath(assetsRoot, modelFile);
            case "accessoires":
                return specificImage(assetsRoot, categoryDir,
                        "Rexa_accessorimensola_specchiera_interlude_gallery_1.jpg");
            default:
                return null;
        }
    }

    private String baseUpper(String s) {
        return s.toUpperCase();
    }

    private String furnitureThumbnail(String nameLower, File assetsRoot, File categoryDir) {
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

    private String imageContainingModelName(File assetsRoot, File dir, String modelBaseLower) {
        File[] images = dir.listFiles(f -> !f.isDirectory() && isImageFile(f));
        if (images == null)
            return null;
        for (File img : images) {
            if (img.getName().toLowerCase().contains(modelBaseLower)) {
                return toRelativeAssetPath(assetsRoot, img);
            }
        }
        return null;
    }

    private boolean isImageFile(File f) {
        String n = f.getName().toLowerCase();
        return n.endsWith(".jpg") || n.endsWith(".jpeg") || n.endsWith(".png") || n.endsWith(".webp");
    }

    private String mappedThumbnailByGlb(String glbBaseName, File assetsRoot, File dir,
            Map<String, String> imgByGlbBase) {
        String key = glbBaseName; // keep original case for exact keys, but also try case-insensitive
        String img = imgByGlbBase.get(key);
        if (img == null) {
            // try case-insensitive
            for (Map.Entry<String, String> e : imgByGlbBase.entrySet()) {
                if (e.getKey().equalsIgnoreCase(key)) {
                    img = e.getValue();
                    break;
                }
            }
        }
        if (img == null)
            return null;
        return specificImage(assetsRoot, dir, img);
    }

    private String specificImage(File assetsRoot, File dir, String imageFileName) {
        File candidate = new File(dir, imageFileName);
        if (!candidate.exists()) {
            // Try image in the category root if we're in a subfolder
            File rootDir = dir;
            while (rootDir.getParentFile() != null && !CATEGORY_MAPPING.containsKey(rootDir.getName().toLowerCase())) {
                rootDir = rootDir.getParentFile();
            }
            candidate = new File(rootDir, imageFileName);
        }
        return candidate.exists() ? toRelativeAssetPath(assetsRoot, candidate) : null;
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
        m.put("BAB14070-A", "cq5dam.web.460.460.jpeg");
        m.put("CA1317075P", "cq5dam.web.460.460_(1).jpeg");
        m.put("DIVINAWSTANDARDCONTELAIO+RUBINETTERIA", "cq5dam.web.460.460_(2).jpeg");
        m.put("DI5190140D0", "cq5dam.web.460.460_(3).jpeg");
        m.put("SEN2190142", "cq5dam.web.460.460 (4).jpeg");
        m.put("STILE170X70_STANDARDCONTELAIO+RUBINETTERIA", "cq5dam.web.460.460_(5).jpeg");
        m.put("STILECSTANDARDCONTELAIORUBPOGGIATESTA", "cq5dam.web.460.460_(6).jpeg");
        m.put("UNA1135135-A", "cq5dam.web.460.460_(7).jpeg");
        m.put("VEN117070D", "cq5dam.web.460.460_(8).jpeg");
        m.put("VOG115085D", "cq5dam.web.460.460_(9).jpeg");
        return m;
    }

    private String showersThumbnail(String modelUpper, File assetsRoot, File dir) {
        // Order by most specific first
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

    private String towelRadiatorsThumbnail(String glbBase, File assetsRoot, File dir) {
        String upper = glbBase.toUpperCase();
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

    public Map<String, Object> getImportStatistics() {
        Map<String, Object> stats = new HashMap<>();

        try {
            long totalProducts = productRepository.count();
            long totalCategories = categoryRepository.count();
            long totalColors = colorRepository.count();
            long totalProductColors = productColorRepository.count();

            // Get products per category
            Map<String, Long> productsByCategory = new HashMap<>();
            List<Category> categories = categoryRepository.findAll();
            for (Category category : categories) {
                long count = productRepository.countByCategoryId(category.getId());
                productsByCategory.put(category.getName(), count);
            }

            stats.put("totalProducts", totalProducts);
            stats.put("totalCategories", totalCategories);
            stats.put("totalColors", totalColors);
            stats.put("totalProductColors", totalProductColors);
            stats.put("productsByCategory", productsByCategory);

        } catch (Exception e) {
            logger.error("Error getting import statistics", e);
            stats.put("error", e.getMessage());
        }

        return stats;
    }

    @Transactional
    public Map<String, Object> clearAllProducts() {
        Map<String, Object> result = new HashMap<>();

        try {
            // Delete all product-color associations first
            long deletedProductColors = productColorRepository.count();
            productColorRepository.deleteAll();

            // Delete all products
            long deletedProducts = productRepository.count();
            productRepository.deleteAll();

            result.put("status", "success");
            result.put("deletedProducts", deletedProducts);
            result.put("deletedProductColors", deletedProductColors);
            result.put("message", "All products and their color associations have been cleared");

            logger.info("Cleared {} products and {} product-color associations", deletedProducts, deletedProductColors);

        } catch (Exception e) {
            logger.error("Error clearing products", e);
            result.put("status", "error");
            result.put("message", e.getMessage());
        }

        return result;
    }
}