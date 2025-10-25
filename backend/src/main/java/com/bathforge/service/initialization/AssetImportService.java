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
                            int imported = processCategoryDirectory(categoryDir, categoryName);
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

    private int processCategoryDirectory(File categoryDir, String categoryName) {
        String normalizedCategory = CATEGORY_MAPPING.get(categoryName);
        Category category = categoryRepository.findByNameIgnoreCase(normalizedCategory)
                .orElseThrow(() -> new RuntimeException("Category not found: " + normalizedCategory));

        List<Color> categoryColors = colorRepository.findByCategoryId(category.getId());

        int importedCount = 0;
        int productCounter = 1;
        File[] files = categoryDir.listFiles();

        if (files != null) {
            for (File file : files) {
                if (isValidModelFile(file)) {
                    try {
                        Product product = createProductFromFile(file, category, productCounter);
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
                                productCounter++; // Increment counter for next product
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

    private boolean isValidModelFile(File file) {
        if (file.isDirectory())
            return false;

        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".glb") ||
                fileName.endsWith(".gltf") ||
                fileName.endsWith(".jpg") ||
                fileName.endsWith(".jpeg") ||
                fileName.endsWith(".png") ||
                fileName.endsWith(".webp");
    }

    private Product createProductFromFile(File file, Category category, int productNumber) {
        String fileName = file.getName();

        String productName = formatProductName(category.getName(), productNumber);

        Product product = new Product();
        product.setName(productName);
        product.setDescription("");
        product.setCategory(category);

        String relativePath = "/assets/" + category.getName().toLowerCase() + "/" + fileName;
        product.setModelPath(relativePath);

        // Set random price range
        Product.PriceRange[] priceRanges = Product.PriceRange.values();
        Product.PriceRange randomPriceRange = priceRanges[new Random().nextInt(priceRanges.length)];
        product.setPriceRange(randomPriceRange);

        Product.MountingType mountingType = MOUNTING_MAPPING.getOrDefault(category.getName().toLowerCase(),
                Product.MountingType.WALL);
        product.setMountingType(mountingType);

        return product;
    }

    private String formatProductName(String categoryName, int productNumber) {
        // Capitalize the first letter of category name
        String formattedCategory = categoryName.substring(0, 1).toUpperCase() +
                categoryName.substring(1).toLowerCase();

        return formattedCategory + " " + productNumber;
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