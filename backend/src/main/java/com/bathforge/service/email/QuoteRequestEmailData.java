package com.bathforge.service.email;

import java.util.List;

public class QuoteRequestEmailData {

    private String userFullName;
    private String userEmail;
    private String userPhone;
    private String userCompany;
    private String roomDimensions;
    private List<WallLength> wallLengths;
    private List<ProductDetail> products;
    private List<CoveringDetail> coverings;
    private String sceneSnapshot;
    private String additionalNotes;

    // Constructors
    public QuoteRequestEmailData() {
    }

    // Getters and Setters
    public String getUserFullName() {
        return userFullName;
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getUserCompany() {
        return userCompany;
    }

    public void setUserCompany(String userCompany) {
        this.userCompany = userCompany;
    }

    public String getRoomDimensions() {
        return roomDimensions;
    }

    public void setRoomDimensions(String roomDimensions) {
        this.roomDimensions = roomDimensions;
    }

    public List<WallLength> getWallLengths() {
        return wallLengths;
    }

    public void setWallLengths(List<WallLength> wallLengths) {
        this.wallLengths = wallLengths;
    }

    public List<ProductDetail> getProducts() {
        return products;
    }

    public void setProducts(List<ProductDetail> products) {
        this.products = products;
    }

    public List<CoveringDetail> getCoverings() {
        return coverings;
    }

    public void setCoverings(List<CoveringDetail> coverings) {
        this.coverings = coverings;
    }

    public String getSceneSnapshot() {
        return sceneSnapshot;
    }

    public void setSceneSnapshot(String sceneSnapshot) {
        this.sceneSnapshot = sceneSnapshot;
    }

    public String getAdditionalNotes() {
        return additionalNotes;
    }

    public void setAdditionalNotes(String additionalNotes) {
        this.additionalNotes = additionalNotes;
    }

    // Inner classes for product and covering details
    public static class ProductDetail {
        private String name;
        private String category;
        private String color;
        private String position;

        public ProductDetail() {
        }

        public ProductDetail(String name, String category, String color, String position) {
            this.name = name;
            this.category = category;
            this.color = color;
            this.position = position;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }

        public String getPosition() {
            return position;
        }

        public void setPosition(String position) {
            this.position = position;
        }
    }

    public static class CoveringDetail {
        private String type;
        private String name;
        private String color;

        public CoveringDetail() {
        }

        public CoveringDetail(String type, String name, String color) {
            this.type = type;
            this.name = name;
            this.color = color;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }
    }

    public static class WallLength {
        private int wall;
        private double length;

        public WallLength() {
        }

        public WallLength(int wall, double length) {
            this.wall = wall;
            this.length = length;
        }

        public int getWall() {
            return wall;
        }

        public void setWall(int wall) {
            this.wall = wall;
        }

        public double getLength() {
            return length;
        }

        public void setLength(double length) {
            this.length = length;
        }
    }
}
