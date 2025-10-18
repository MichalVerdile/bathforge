package com.bathforge.service.initialization;

import com.bathforge.dto.products.ProductDTO;
import com.bathforge.model.products.Category;
import com.bathforge.model.products.Product.MountingType;
import com.bathforge.model.products.Product.PriceRange;
import com.bathforge.repository.products.CategoryRepository;
import com.bathforge.service.products.ProductService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.Random;

@Service
@Transactional
public class AssetScannerService {

    private final CategoryRepository categoryRepository;
    private final ProductService productService;
    private final Random random = new Random();

    @Autowired
    public AssetScannerService(CategoryRepository categoryRepository, ProductService productService) {
        this.categoryRepository = categoryRepository;
        this.productService = productService;
    }

    /**
     * Scan the assets folder and create products from the files
     */
    public void scanAndCreateProducts(String assetsPath) {
        File assetsDir = new File(assetsPath);
        if (!assetsDir.exists() || !assetsDir.isDirectory()) {
            System.err.println("Assets directory not found: " + assetsPath);
            return;
        }

        File[] categoryDirs = assetsDir.listFiles(File::isDirectory);
        if (categoryDirs == null) {
            System.err.println("No category directories found in: " + assetsPath);
            return;
        }

        for (File categoryDir : categoryDirs) {
            String categoryName = categoryDir.getName();
            scanCategoryDirectory(categoryDir, categoryName);
        }
    }

    private void scanCategoryDirectory(File categoryDir, String categoryName) {
        Category category = categoryRepository.findByNameIgnoreCase(categoryName).orElse(null);
        if (category == null) {
            System.err.println("Category not found in database: " + categoryName);
            return;
        }

        File[] files = categoryDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".glb") ||
                name.toLowerCase().endsWith(".gltf") ||
                name.toLowerCase().endsWith(".jpg") ||
                name.toLowerCase().endsWith(".jpeg") ||
                name.toLowerCase().endsWith(".png"));

        if (files == null) {
            System.err.println("No model/image files found in category: " + categoryName);
            return;
        }

        for (File file : files) {
            createProductFromFile(file, category);
        }
    }

    private void createProductFromFile(File file, Category category) {
        String fileName = file.getName();
        String fileNameWithoutExtension = fileName.substring(0, fileName.lastIndexOf('.'));

        // Generate product name from filename
        String productName = generateProductName(fileNameWithoutExtension);

        // Create relative path from assets folder
        String relativePath = "assets/" + category.getName() + "/" + fileName;

        // Generate random price range and mounting type for demo purposes
        PriceRange priceRange = getRandomPriceRange();
        MountingType mountingType = getRandomMountingType(category.getName());

        try {
            ProductDTO productDTO = new ProductDTO(
                    productName,
                    "", // Description is blank as specified
                    priceRange,
                    relativePath,
                    mountingType,
                    category.getId());

            productService.createProduct(productDTO);
            System.out.println("Created product: " + productName + " in category: " + category.getName());

        } catch (Exception e) {
            System.err.println("Failed to create product from file: " + fileName + " - " + e.getMessage());
        }
    }

    private String generateProductName(String fileName) {
        // Remove common prefixes and clean up the name
        String cleanName = fileName
                .replaceAll("^[A-Z0-9]+_", "") // Remove prefix like "AINAP00001_"
                .replaceAll("_+", " ") // Replace underscores with spaces
                .replaceAll("\\s+", " ") // Remove multiple spaces
                .trim();

        // Capitalize each word
        String[] words = cleanName.split(" ");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (word.length() > 0) {
                result.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    result.append(word.substring(1).toLowerCase());
                }
                result.append(" ");
            }
        }

        return result.toString().trim();
    }

    private PriceRange getRandomPriceRange() {
        PriceRange[] ranges = PriceRange.values();
        return ranges[random.nextInt(ranges.length)];
    }

    private MountingType getRandomMountingType(String categoryName) {
        // Assign mounting types based on category logic
        switch (categoryName.toLowerCase()) {
            case "bathtubs":
                return random.nextBoolean() ? MountingType.FREESTANDING : MountingType.FLOOR;
            case "wcs":
                return random.nextBoolean() ? MountingType.FLOOR : MountingType.WALL;
            case "basins":
                return random.nextBoolean() ? MountingType.WALL : MountingType.FREESTANDING;
            case "accessoires":
            case "towel_radiators":
            case "shower":
                return MountingType.WALL;
            case "furniture":
                return random.nextBoolean() ? MountingType.FLOOR : MountingType.FREESTANDING;
            case "fittings":
            case "fittings_bathtubs":
                return random.nextBoolean() ? MountingType.WALL : MountingType.FREESTANDING;
            case "coverings":
                return random.nextBoolean() ? MountingType.WALL : MountingType.FLOOR;
            default:
                MountingType[] types = MountingType.values();
                return types[random.nextInt(types.length)];
        }
    }

    /**
     * Manual method to trigger scanning from a controller
     */
    public String scanAssetsFromFrontend() {
        try {
            // Assuming the backend is running from the project root
            String frontendAssetsPath = "../frontend/public/assets";
            File assetsDir = new File(frontendAssetsPath);

            if (!assetsDir.exists()) {
                // Try alternative path
                frontendAssetsPath = "frontend/public/assets";
                assetsDir = new File(frontendAssetsPath);
            }

            if (!assetsDir.exists()) {
                return "Assets directory not found. Please check the path.";
            }

            scanAndCreateProducts(assetsDir.getAbsolutePath());
            return "Asset scanning completed successfully!";

        } catch (Exception e) {
            return "Error during asset scanning: " + e.getMessage();
        }
    }
}