package com.bathforge.service.initialization;

import com.bathforge.model.products.Product;
import com.bathforge.repository.products.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ProductDescriptionInitializer {

    private static final Logger log = LoggerFactory.getLogger(ProductDescriptionInitializer.class);

    private final ProductRepository productRepository;

    public ProductDescriptionInitializer(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeDescriptions() {
        log.info("Starting product description initialization for AI showcase items...");

        long updatedCount = 0;

        // BASINS
        updatedCount += updateProductDescription("Basins 1",
            "Floating cabinet, dark walnut veneer, white vessel sink. Japandi, Organic Modern. Warm, textured wood.");

        updatedCount += updateProductDescription("Basins 5",
            "Cylindrical pedestal basin, matte stone resin. Zen, Luxury Minimalist. Totem-like, pure form.");

        updatedCount += updateProductDescription("Basins 7",
            "Asymmetrical geometric wall-hung vanity. Ultra-Modern, Futurist. Matte grey. Sharp, avant-garde.");

        // BATHTUBS
        updatedCount += updateProductDescription("Bathtubs 4",
            "Soft-rectangular freestanding tub, matte white. Scandi, Zen. Smooth, calming, rounded corners.");

        updatedCount += updateProductDescription("Bathtubs 3",
            "Oval natural marble tub, heavy veining. Opulent, Classic Luxury. Bold, dramatic stone statement.");

        // TOWEL RADIATORS
        updatedCount += updateProductDescription("Towel_radiators 13",
            "Modular square ribbed tile radiator. Industrial Chic, Post-Modern. Geometric, tactile checkerboard.");

        updatedCount += updateProductDescription("Towel_radiators 7",
            "Vertical waffle-grid radiator, wood accents. Japandi, Soft Minimalist. Cozy, textured.");

        // WCS
        updatedCount += updateProductDescription("Wcs 8",
            "Toilet suite, tall rectangular exposed cistern. Architectural, Contemporary. Geometric, formal, structural.");

        updatedCount += updateProductDescription("Wcs 5",
            "Back-to-wall toilet, tapered silhouette. Minimalist, Modern. Clean, curvy, unobtrusive.");

        // FURNITURE
        updatedCount += updateProductDescription("Furniture 4",
            "Tall open shelving unit, matte taupe. Minimalist Utility. Linear, vertical storage.");

        updatedCount += updateProductDescription("Furniture 8",
            "Low blocky chest of drawers, matte taupe. Modern Modular. Solid, grounded, horizontal.");

        // COVERINGS
        updatedCount += updateProductDescription("Coverings 2",
            "Gray and white mixed tiles. Contemporary, Classic. Neutral, timeless. Wall-only.");

        updatedCount += updateProductDescription("Coverings 4",
            "White matte bricks. Scandi, Modern Farmhouse. Bright, airy, crisp. Wall-only.");

        log.info("Product description initialization completed. Updated {} products.", updatedCount);
    }

    private long updateProductDescription(String productName, String description) {
        try {
            return productRepository.findByNameIgnoreCase(productName)
                .map(product -> {
                    product.setDescription(description);
                    productRepository.save(product);
                    log.debug("Updated description for product: {}", productName);
                    return 1L;
                })
                .orElseGet(() -> {
                    log.warn("Product not found for description update: {}", productName);
                    return 0L;
                });
        } catch (Exception e) {
            log.error("Error updating description for product {}: {}", productName, e.getMessage());
            return 0L;
        }
    }
}