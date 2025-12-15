package com.bathforge.dto.quote;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public class QuoteRequestDTO {

    // User information
    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    // Password is optional - not required for logged-in users
    private String password;

    private String phone;
    private String company;

    // Scene information
    @NotBlank(message = "Scene ID is required")
    private String sceneId;

    private String roomDimensions;
    private List<WallLengthDTO> wallLengths;
    private RoomDataDTO roomData;
    private List<ProductSelectionDTO> products;
    private List<CoveringSelectionDTO> coverings;
    private String sceneSnapshot; // Base64 encoded image
    private String additionalNotes;

    // Constructors
    public QuoteRequestDTO() {
    }

    // Getters and Setters
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getSceneId() {
        return sceneId;
    }

    public void setSceneId(String sceneId) {
        this.sceneId = sceneId;
    }

    public String getRoomDimensions() {
        return roomDimensions;
    }

    public void setRoomDimensions(String roomDimensions) {
        this.roomDimensions = roomDimensions;
    }

    public List<WallLengthDTO> getWallLengths() {
        return wallLengths;
    }

    public void setWallLengths(List<WallLengthDTO> wallLengths) {
        this.wallLengths = wallLengths;
    }

    public RoomDataDTO getRoomData() {
        return roomData;
    }

    public void setRoomData(RoomDataDTO roomData) {
        this.roomData = roomData;
    }

    public List<ProductSelectionDTO> getProducts() {
        return products;
    }

    public void setProducts(List<ProductSelectionDTO> products) {
        this.products = products;
    }

    public List<CoveringSelectionDTO> getCoverings() {
        return coverings;
    }

    public void setCoverings(List<CoveringSelectionDTO> coverings) {
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

    // Inner classes
    public static class ProductSelectionDTO {
        private String name;
        private String category;
        private String color;
        private String position;
        private Long productId;
        private Long colorId;
        private Double positionX;
        private Double positionY;
        private Double positionZ;
        private Double rotationX;
        private Double rotationY;
        private Double rotationZ;
        private Double scaleX;
        private Double scaleY;
        private Double scaleZ;

        public ProductSelectionDTO() {
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

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public Long getColorId() {
            return colorId;
        }

        public void setColorId(Long colorId) {
            this.colorId = colorId;
        }

        public Double getPositionX() {
            return positionX;
        }

        public void setPositionX(Double positionX) {
            this.positionX = positionX;
        }

        public Double getPositionY() {
            return positionY;
        }

        public void setPositionY(Double positionY) {
            this.positionY = positionY;
        }

        public Double getPositionZ() {
            return positionZ;
        }

        public void setPositionZ(Double positionZ) {
            this.positionZ = positionZ;
        }

        public Double getRotationX() {
            return rotationX;
        }

        public void setRotationX(Double rotationX) {
            this.rotationX = rotationX;
        }

        public Double getRotationY() {
            return rotationY;
        }

        public void setRotationY(Double rotationY) {
            this.rotationY = rotationY;
        }

        public Double getRotationZ() {
            return rotationZ;
        }

        public void setRotationZ(Double rotationZ) {
            this.rotationZ = rotationZ;
        }

        public Double getScaleX() {
            return scaleX;
        }

        public void setScaleX(Double scaleX) {
            this.scaleX = scaleX;
        }

        public Double getScaleY() {
            return scaleY;
        }

        public void setScaleY(Double scaleY) {
            this.scaleY = scaleY;
        }

        public Double getScaleZ() {
            return scaleZ;
        }

        public void setScaleZ(Double scaleZ) {
            this.scaleZ = scaleZ;
        }
    }

    public static class CoveringSelectionDTO {
        private String type;
        private String name;
        private String color;
        private Long productId;
        private String surfaceIdentifier;
        private Double repeatX;
        private Double repeatY;

        public CoveringSelectionDTO() {
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

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public String getSurfaceIdentifier() {
            return surfaceIdentifier;
        }

        public void setSurfaceIdentifier(String surfaceIdentifier) {
            this.surfaceIdentifier = surfaceIdentifier;
        }

        public Double getRepeatX() {
            return repeatX;
        }

        public void setRepeatX(Double repeatX) {
            this.repeatX = repeatX;
        }

        public Double getRepeatY() {
            return repeatY;
        }

        public void setRepeatY(Double repeatY) {
            this.repeatY = repeatY;
        }
    }

    public static class WallLengthDTO {
        private int wall;
        private double length;

        public WallLengthDTO() {
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

    public static class RoomDataDTO {
        private String verticesData;
        private Double roomHeight;
        private String roomProperties;

        public RoomDataDTO() {
        }

        public String getVerticesData() {
            return verticesData;
        }

        public void setVerticesData(String verticesData) {
            this.verticesData = verticesData;
        }

        public Double getRoomHeight() {
            return roomHeight;
        }

        public void setRoomHeight(Double roomHeight) {
            this.roomHeight = roomHeight;
        }

        public String getRoomProperties() {
            return roomProperties;
        }

        public void setRoomProperties(String roomProperties) {
            this.roomProperties = roomProperties;
        }
    }
}
