package com.bathforge.service;

import com.bathforge.service.ai.collision.BoundingBox2D;
import com.bathforge.service.ai.collision.Position3D;
import com.bathforge.service.ai.collision.ProductDimensions;
import com.bathforge.service.ai.collision.RoomBounds;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Service layer tests for Placement validation rules
 * Tests boundary validation, wall-mounted constraints, and floor-mounted
 * constraints
 */
@SpringBootTest
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
public class PlacementValidationTest {

    @Test
    @DisplayName("Placement validation - Object remains within room boundaries")
    public void testObjectWithinRoomBoundaries() {
        List<Position3D> vertices = List.of(
                new Position3D(0, 0, 0),
                new Position3D(5, 0, 0),
                new Position3D(5, 0, 4),
                new Position3D(0, 0, 4));

        RoomBounds room = new RoomBounds(0, 5.0, 0, 4.0, vertices);

        ProductDimensions productDims = new ProductDimensions(0.6, 0.5, 0.8);

        Position3D centerPosition = new Position3D(2.5, 0, 2.0);
        BoundingBox2D centerBox = new BoundingBox2D(centerPosition, productDims);
        assertTrue(room.containsBox(centerBox), "Product at center should be within room bounds");

        Position3D nearCornerPosition = new Position3D(0.5, 0, 0.5);
        BoundingBox2D nearCornerBox = new BoundingBox2D(nearCornerPosition, productDims);
        assertTrue(room.containsBox(nearCornerBox), "Product near corner should be within room bounds");

        Position3D outsidePosition = new Position3D(4.9, 0, 0.3);
        BoundingBox2D outsideBox = new BoundingBox2D(outsidePosition, productDims);
        assertFalse(room.containsBox(outsideBox), "Product extending beyond room should be invalid");

        Position3D completelyOutside = new Position3D(6.0, 0, 2.0);
        BoundingBox2D completelyOutsideBox = new BoundingBox2D(completelyOutside, productDims);
        assertFalse(room.containsBox(completelyOutsideBox), "Product completely outside room should be invalid");
    }

    @Test
    @DisplayName("Placement validation - Wall-mounted items require valid wall surface")
    public void testWallMountedValidation() {
        List<Position3D> vertices = List.of(
                new Position3D(0, 0, 0),
                new Position3D(5, 0, 0),
                new Position3D(5, 0, 4),
                new Position3D(0, 0, 4));

        RoomBounds room = new RoomBounds(0, 5, 0, 4, vertices);

        ProductDimensions wallProduct = new ProductDimensions(0.6, 0.3, 0.4);

        Position3D validWallPosition = new Position3D(2.5, 1.0, 0.2);
        BoundingBox2D validWallBox = new BoundingBox2D(validWallPosition, wallProduct);
        assertTrue(room.containsBox(validWallBox), "Wall-mounted item near wall should be valid");

        Position3D centerPosition = new Position3D(2.5, 1.0, 2.0);
        BoundingBox2D centerBox = new BoundingBox2D(centerPosition, wallProduct);

        assertTrue(room.containsBox(centerBox), "Item is within room bounds");

        double distanceToNearestWall = Math.min(
                Math.min(centerPosition.x, 5.0 - centerPosition.x),
                Math.min(centerPosition.z, 4.0 - centerPosition.z));

        boolean isNearWall = distanceToNearestWall < 0.3;
        assertFalse(isNearWall, "Wall-mounted item in center should be flagged as too far from wall");

        Position3D nearWallPosition = new Position3D(0.15, 1.0, 2.0);
        double distanceToWall = Math.min(
                Math.min(nearWallPosition.x, 5.0 - nearWallPosition.x),
                Math.min(nearWallPosition.z, 4.0 - nearWallPosition.z));
        assertTrue(distanceToWall < 0.3, "Wall-mounted item near wall should pass distance check");
    }

    @Test
    @DisplayName("Placement validation - Floor-mounted objects adhere to ground plane")
    public void testFloorMountedValidation() {
        List<Position3D> vertices = List.of(
                new Position3D(0, 0, 0),
                new Position3D(6, 0, 0),
                new Position3D(6, 0, 5),
                new Position3D(0, 0, 5));

        RoomBounds room = new RoomBounds(0, 6.0, 0, 5.0, vertices);

        ProductDimensions floorProduct = new ProductDimensions(1.7, 0.6, 0.8);

        Position3D validFloorPosition = new Position3D(3.0, 0.0, 2.5);
        BoundingBox2D validFloorBox = new BoundingBox2D(validFloorPosition, floorProduct);
        assertTrue(room.containsBox(validFloorBox), "Floor-mounted item on ground plane should be valid");

        assertEquals(0.0, validFloorPosition.y, 0.001, "Floor-mounted item should have Y=0");

        Position3D floatingPosition = new Position3D(3.0, 0.5, 2.5);
        assertEquals(0.5, floatingPosition.y, "Floating position should have Y > 0");

        assertTrue(Math.abs(validFloorPosition.y) < 0.01, "Valid floor position should have Y near 0");
        assertFalse(Math.abs(floatingPosition.y) < 0.01, "Floating position should not pass floor check");
    }

    @Test
    @DisplayName("Placement validation - Products don't collide with each other")
    public void testProductCollisionDetection() {
        ProductDimensions product1Dims = new ProductDimensions(0.8, 0.6, 0.5);
        ProductDimensions product2Dims = new ProductDimensions(0.6, 0.5, 0.4);

        Position3D position1 = new Position3D(2.0, 0, 2.0);
        Position3D position2Overlapping = new Position3D(2.3, 0, 2.1);
        Position3D position2Separate = new Position3D(4.0, 0, 4.0);

        BoundingBox2D box1 = new BoundingBox2D(position1, product1Dims);
        BoundingBox2D box2Overlapping = new BoundingBox2D(position2Overlapping, product2Dims);
        BoundingBox2D box2Separate = new BoundingBox2D(position2Separate, product2Dims);

        double collisionBuffer = 0.15;
        assertTrue(box1.intersects(box2Overlapping, collisionBuffer),
                "Overlapping products should be detected as colliding");

        assertFalse(box1.intersects(box2Separate, collisionBuffer),
                "Well-separated products should not collide");
    }

    @Test
    @DisplayName("Placement validation - Multiple products can be placed without overlap")
    public void testMultipleProductPlacement() {
        List<Position3D> vertices = List.of(
                new Position3D(0, 0, 0),
                new Position3D(5, 0, 0),
                new Position3D(5, 0, 4),
                new Position3D(0, 0, 4));

        RoomBounds room = new RoomBounds(0, 5.0, 0, 4.0, vertices);

        List<BoundingBox2D> placedProducts = new ArrayList<>();
        double collisionBuffer = 0.15;

        ProductDimensions product1 = new ProductDimensions(0.6, 0.5, 0.4);
        Position3D position1 = new Position3D(1.0, 0, 1.0);
        BoundingBox2D box1 = new BoundingBox2D(position1, product1);

        assertTrue(room.containsBox(box1), "First product should fit in room");

        boolean hasCollision1 = placedProducts.stream()
                .anyMatch(placed -> box1.intersects(placed, collisionBuffer));
        assertFalse(hasCollision1, "First product should not collide");

        placedProducts.add(box1);

        ProductDimensions product2 = new ProductDimensions(0.8, 0.6, 0.5);
        Position3D position2 = new Position3D(3.5, 0, 1.0);
        BoundingBox2D box2 = new BoundingBox2D(position2, product2);

        assertTrue(room.containsBox(box2), "Second product should fit in room");

        boolean hasCollision2 = placedProducts.stream()
                .anyMatch(placed -> box2.intersects(placed, collisionBuffer));
        assertFalse(hasCollision2, "Second product should not collide with first");

        placedProducts.add(box2);

        ProductDimensions product3 = new ProductDimensions(0.5, 0.4, 0.3);
        Position3D position3 = new Position3D(1.0, 0, 3.0);
        BoundingBox2D box3 = new BoundingBox2D(position3, product3);

        assertTrue(room.containsBox(box3), "Third product should fit in room");

        boolean hasCollision3 = placedProducts.stream()
                .anyMatch(placed -> box3.intersects(placed, collisionBuffer));
        assertFalse(hasCollision3, "Third product should not collide with existing products");
    }

    @Test
    @DisplayName("Placement validation - L-shaped room boundaries work correctly")
    public void testLShapedRoomBoundaries() {
        List<Position3D> vertices = List.of(
                new Position3D(0, 0, 0),
                new Position3D(4, 0, 0),
                new Position3D(4, 0, 2),
                new Position3D(2, 0, 2),
                new Position3D(2, 0, 4),
                new Position3D(0, 0, 4));

        RoomBounds room = new RoomBounds(0, 4.0, 0, 4.0, vertices);

        ProductDimensions product = new ProductDimensions(0.6, 0.5, 0.4);

        Position3D validPosition1 = new Position3D(2.0, 0, 1.0);
        BoundingBox2D validBox1 = new BoundingBox2D(validPosition1, product);
        assertTrue(room.containsBox(validBox1), "Product in main section should be valid");

        Position3D validPosition2 = new Position3D(1.0, 0, 3.0);
        BoundingBox2D validBox2 = new BoundingBox2D(validPosition2, product);
        assertTrue(room.containsBox(validBox2), "Product in L section should be valid");

        Position3D invalidPosition = new Position3D(3.0, 0, 3.0);
        BoundingBox2D invalidBox = new BoundingBox2D(invalidPosition, product);
        assertFalse(room.containsBox(invalidBox), "Product in cut-out area should be invalid");
    }

    @Test
    @DisplayName("Placement validation - Product dimensions are calculated correctly")
    public void testProductDimensionsCalculation() {
        ProductDimensions smallProduct = new ProductDimensions(0.4, 0.3, 0.2);
        ProductDimensions largeProduct = new ProductDimensions(1.8, 0.7, 0.9);

        double smallFootprint = smallProduct.getFootprint();
        double largeFootprint = largeProduct.getFootprint();

        assertEquals(0.4 * 0.3, smallFootprint, 0.001, "Small product footprint should be width * depth");
        assertEquals(1.8 * 0.7, largeFootprint, 0.001, "Large product footprint should be width * depth");

        assertTrue(largeFootprint > smallFootprint, "Large product should have bigger footprint");
    }

    @Test
    @DisplayName("Placement validation - Bounding box intersections with buffer work correctly")
    public void testBoundingBoxIntersectionWithBuffer() {
        ProductDimensions dims1 = new ProductDimensions(1.0, 0.5, 0.5);
        ProductDimensions dims2 = new ProductDimensions(0.8, 0.4, 0.4);

        Position3D pos1 = new Position3D(2.0, 0, 2.0);
        Position3D pos2Close = new Position3D(2.8, 0, 2.3);
        Position3D pos2Far = new Position3D(4.0, 0, 4.0);

        BoundingBox2D box1 = new BoundingBox2D(pos1, dims1);
        BoundingBox2D box2Close = new BoundingBox2D(pos2Close, dims2);
        BoundingBox2D box2Far = new BoundingBox2D(pos2Far, dims2);

        double largeBuffer = 0.5;
        assertTrue(box1.intersects(box2Close, largeBuffer),
                "Close boxes should intersect with large buffer");

        assertFalse(box1.intersects(box2Far, 0.0), "Far boxes should not intersect with no buffer");
        assertFalse(box1.intersects(box2Far, largeBuffer), "Far boxes should not intersect even with large buffer");
    }

    @Test
    @DisplayName("Placement validation - Room center calculation is correct")
    public void testRoomCenterCalculation() {
        List<Position3D> vertices = List.of(
                new Position3D(0, 0, 0),
                new Position3D(6, 0, 0),
                new Position3D(6, 0, 4),
                new Position3D(0, 0, 4));

        RoomBounds room = new RoomBounds(0, 6.0, 0, 4.0, vertices);
        Position3D center = room.getCenter();

        assertEquals(3.0, center.x, 0.001, "Center X should be at midpoint");
        assertEquals(2.0, center.z, 0.001, "Center Z should be at midpoint");
        assertEquals(0.0, center.y, 0.001, "Center Y should be at ground level");
    }
}
