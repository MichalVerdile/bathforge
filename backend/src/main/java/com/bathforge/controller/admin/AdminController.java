package com.bathforge.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.bathforge.service.initialization.AssetImportService;
import com.bathforge.service.initialization.AssetScannerService;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final AssetScannerService assetScannerService;
    private final AssetImportService assetImportService;

    @Autowired
    public AdminController(AssetScannerService assetScannerService, AssetImportService assetImportService) {
        this.assetScannerService = assetScannerService;
        this.assetImportService = assetImportService;
    }

    /**
     * Import all assets from frontend directory into database
     */
    @PostMapping("/import-assets")
    public ResponseEntity<Map<String, Object>> importAssets() {
        try {
            // Path to frontend assets directory
            String projectRoot = System.getProperty("user.dir");
            String assetsPath = projectRoot + "/../frontend/public/assets";

            // Fallback paths if running from different locations
            File assetsDir = new File(assetsPath);
            if (!assetsDir.exists()) {
                assetsPath = projectRoot + "/frontend/public/assets";
                assetsDir = new File(assetsPath);
            }
            if (!assetsDir.exists()) {
                // Try relative path from project root
                assetsPath = "../frontend/public/assets";
                assetsDir = new File(assetsPath);
            }

            if (!assetsDir.exists()) {
                return ResponseEntity.status(400).body(Map.of(
                        "status", "error",
                        "message", "Assets directory not found. Tried: " + assetsPath
                                + ". Please ensure the frontend is in the correct location."));
            }

            Map<String, Object> result = assetImportService.importAllAssets(assetsDir.getAbsolutePath());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()));
        }
    }

    /**
     * Trigger asset scanning to create products from files (legacy method)
     */
    @PostMapping("/scan-assets")
    public ResponseEntity<Map<String, String>> scanAssets() {
        Map<String, String> response = new HashMap<>();

        try {
            String result = assetScannerService.scanAssetsFromFrontend();
            response.put("message", result);
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Error during asset scanning: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get import statistics
     */
    @GetMapping("/import/statistics")
    public ResponseEntity<Map<String, Object>> getImportStatistics() {
        try {
            Map<String, Object> stats = assetImportService.getImportStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()));
        }
    }

    /**
     * Clear all products from the database
     */
    @DeleteMapping("/products/clear")
    public ResponseEntity<Map<String, Object>> clearAllProducts() {
        try {
            Map<String, Object> result = assetImportService.clearAllProducts();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()));
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "BathForge API is running");
        return ResponseEntity.ok(response);
    }
}