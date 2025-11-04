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
     * Get all products
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get product by ID
     */
    @Transactional(readOnly = true)
    public Optional<ProductDTO> getProductById(Long id) {
        return productRepository.findById(id)
                .map(this::convertToDTO);
    }

    /**
     * Get products by category ID
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsByCategoryId(Long categoryId) {
        return productRepository.findByCategoryId(categoryId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get products by category name
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsByCategoryName(String categoryName) {
        return productRepository.findByCategoryName(categoryName)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get products by price range
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsByPriceRange(PriceRange priceRange) {
        return productRepository.findByPriceRange(priceRange)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get products by mounting type
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsByMountingType(MountingType mountingType) {
        return productRepository.findByMountingType(mountingType)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Search products by name
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> searchProductsByName(String name) {
        return productRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get products with filters
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsWithFilters(Long categoryId, PriceRange priceRange, MountingType mountingType) {
        return productRepository.findWithFilters(categoryId, priceRange, mountingType)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Create new product
     */
    public ProductDTO createProduct(ProductDTO productDTO) {
        Category category = categoryService.getCategoryEntityById(productDTO.getCategoryId());

        Product product = convertToEntity(productDTO, category);
        Product savedProduct = productRepository.save(product);

        return convertToDTO(savedProduct);
    }

    /**
     * Update existing product
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
     * Delete product
     */
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new IllegalArgumentException("Product not found with ID: " + id);
        }
        productRepository.deleteById(id);
    }

    /**
     * Add color to product
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
     * Remove color from product
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
     * Get colors for a product
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

    private ColorDTO convertColorToDTO(Color color) {
        return new ColorDTO(
                color.getId(),
                color.getName(),
                color.getHexCode(),
                color.getCategory().getId(),
                color.getCategory().getName());
    }
}