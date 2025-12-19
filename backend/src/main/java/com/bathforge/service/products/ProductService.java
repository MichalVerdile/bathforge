package com.bathforge.service.products;

import com.bathforge.dto.products.ColorDTO;
import com.bathforge.dto.products.ProductDTO;
import com.bathforge.model.products.Category;
import com.bathforge.model.products.Color;
import com.bathforge.model.products.Product;
import com.bathforge.model.products.ProductColor;
import com.bathforge.model.products.Product.MountingType;
import com.bathforge.model.products.Product.PriceRange;
import com.bathforge.repository.products.ProductColorRepository;
import com.bathforge.repository.products.ProductRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing products and their relationships with categories and
 * colors.
 * Provides comprehensive CRUD operations, filtering, search capabilities, and
 * color associations.
 */
@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductColorRepository productColorRepository;
    private final CategoryService categoryService;
    private final ColorService colorService;

    @Autowired
    public ProductService(ProductRepository productRepository,
            ProductColorRepository productColorRepository,
            CategoryService categoryService,
            ColorService colorService) {
        this.productRepository = productRepository;
        this.productColorRepository = productColorRepository;
        this.categoryService = categoryService;
        this.colorService = colorService;
    }

    /**
     * Retrieves all products in the catalog.
     *
     * @return list of all products as DTOs with their available colors
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves products suitable for AI-powered selection.
     * Returns products with descriptions (showcase items) plus all coverings.
     *
     * @return list of products curated for AI design recommendations
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsForAISelection() {
        return productRepository.findProductsForAISelection()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a product by its unique identifier.
     *
     * @param id the product ID
     * @return an Optional containing the product DTO if found, empty otherwise
     */
    @Transactional(readOnly = true)
    public Optional<ProductDTO> getProductById(Long id) {
        return productRepository.findById(id)
                .map(this::convertToDTO);
    }

    /**
     * Retrieves all products in a specific category.
     *
     * @param categoryId the category ID
     * @return list of products in the category
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsByCategoryId(Long categoryId) {
        return productRepository.findByCategoryId(categoryId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all products in a category by category name.
     *
     * @param categoryName the category name
     * @return list of products in the category
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsByCategoryName(String categoryName) {
        return productRepository.findByCategoryName(categoryName)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all products within a specific price range.
     *
     * @param priceRange the price range to filter by
     * @return list of products in the specified price range
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsByPriceRange(PriceRange priceRange) {
        return productRepository.findByPriceRange(priceRange)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all products with a specific mounting type.
     *
     * @param mountingType the mounting type to filter by (e.g., WALL, FLOOR,
     *                     FREESTANDING)
     * @return list of products with the specified mounting type
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsByMountingType(MountingType mountingType) {
        return productRepository.findByMountingType(mountingType)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Searches for products by name using case-insensitive partial matching.
     *
     * @param name the search term to match against product names
     * @return list of products whose names contain the search term
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> searchProductsByName(String name) {
        return productRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Finds a product by exact name match (case-insensitive).
     * Primarily used by AI to look up products by their exact name.
     *
     * @param name the exact product name to search for
     * @return the product DTO if found, null otherwise
     */
    @Transactional(readOnly = true)
    public ProductDTO findByName(String name) {
        return productRepository.findByNameIgnoreCase(name)
                .map(this::convertToDTO)
                .orElse(null);
    }

    /**
     * Retrieves products matching multiple filter criteria.
     *
     * @param categoryId   optional category ID filter (null to ignore)
     * @param priceRange   optional price range filter (null to ignore)
     * @param mountingType optional mounting type filter (null to ignore)
     * @return list of products matching all specified filters
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsWithFilters(Long categoryId, PriceRange priceRange, MountingType mountingType) {
        return productRepository.findWithFilters(categoryId, priceRange, mountingType)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Creates a new product in the catalog.
     *
     * @param productDTO the product data to create
     * @return the created product as a DTO
     * @throws IllegalArgumentException if the specified category does not exist
     */
    public ProductDTO createProduct(ProductDTO productDTO) {
        Category category = categoryService.getCategoryEntityById(productDTO.getCategoryId());

        Product product = convertToEntity(productDTO, category);
        Product savedProduct = productRepository.save(product);

        return convertToDTO(savedProduct);
    }

    /**
     * Updates an existing product.
     *
     * @param id         the ID of the product to update
     * @param productDTO the updated product data
     * @return the updated product as a DTO
     * @throws IllegalArgumentException if the product or category is not found
     */
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + id));

        Category category = categoryService.getCategoryEntityById(productDTO.getCategoryId());

        existingProduct.setName(productDTO.getName());
        existingProduct.setDescription(productDTO.getDescription());
        existingProduct.setPriceRange(productDTO.getPriceRange());
        existingProduct.setModelPath(productDTO.getModelPath());
        existingProduct.setThumbnail(productDTO.getThumbnail());
        existingProduct.setMountingType(productDTO.getMountingType());
        existingProduct.setCategory(category);

        Product updatedProduct = productRepository.save(existingProduct);
        return convertToDTO(updatedProduct);
    }

    /**
     * Deletes a product by its ID.
     *
     * @param id the ID of the product to delete
     * @throws IllegalArgumentException if the product is not found
     */
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new IllegalArgumentException("Product not found with ID: " + id);
        }
        productRepository.deleteById(id);
    }

    /**
     * Associates a color with a product.
     *
     * @param productId the product ID
     * @param colorId   the color ID
     * @throws IllegalArgumentException if the product or color is not found, or if
     *                                  the association already exists
     */
    public void addColorToProduct(Long productId, Long colorId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + productId));

        Color color = colorService.getColorEntityById(colorId);

        if (productColorRepository.existsByProductAndColor(product, color)) {
            throw new IllegalArgumentException("Color is already associated with this product");
        }

        ProductColor productColor = new ProductColor(product, color);
        productColorRepository.save(productColor);
    }

    /**
     * Removes a color association from a product.
     *
     * @param productId the product ID
     * @param colorId   the color ID
     * @throws IllegalArgumentException if the product or color is not found, or if
     *                                  the association does not exist
     */
    public void removeColorFromProduct(Long productId, Long colorId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + productId));

        Color color = colorService.getColorEntityById(colorId);

        ProductColor productColor = productColorRepository.findByProductAndColor(product, color)
                .orElseThrow(() -> new IllegalArgumentException("Color is not associated with this product"));

        productColorRepository.delete(productColor);
    }

    /**
     * Retrieves all colors available for a specific product.
     *
     * @param productId the product ID
     * @return list of colors associated with the product
     * @throws IllegalArgumentException if the product is not found
     */
    @Transactional(readOnly = true)
    public List<ColorDTO> getColorsForProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + productId));

        return productColorRepository.findColorsByProduct(product)
                .stream()
                .map(this::convertColorToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Converts a Product entity to a ProductDTO with available colors.
     *
     * @param product the product entity
     * @return the product DTO
     */
    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPriceRange(product.getPriceRange());
        dto.setModelPath(product.getModelPath());
        dto.setThumbnail(product.getThumbnail());
        dto.setMountingType(product.getMountingType());
        dto.setCategoryId(product.getCategory().getId());
        dto.setCategoryName(product.getCategory().getName());

        List<ColorDTO> colors = productColorRepository.findColorsByProduct(product)
                .stream()
                .map(this::convertColorToDTO)
                .collect(Collectors.toList());
        dto.setAvailableColors(colors);

        return dto;
    }

    /**
     * Converts a ProductDTO to a Product entity.
     *
     * @param productDTO the product DTO
     * @param category   the category to associate the product with
     * @return the product entity
     */
    private Product convertToEntity(ProductDTO productDTO, Category category) {
        Product product = new Product();
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPriceRange(productDTO.getPriceRange());
        product.setModelPath(productDTO.getModelPath());
        product.setThumbnail(productDTO.getThumbnail());
        product.setMountingType(productDTO.getMountingType());
        product.setCategory(category);
        return product;
    }

    /**
     * Converts a Color entity to a ColorDTO.
     *
     * @param color the color entity
     * @return the color DTO
     */
    private ColorDTO convertColorToDTO(Color color) {
        return new ColorDTO(
                color.getId(),
                color.getName(),
                color.getHexCode(),
                color.getCategory().getId(),
                color.getCategory().getName());
    }
}