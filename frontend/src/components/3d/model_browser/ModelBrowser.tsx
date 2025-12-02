import React, { useState, useEffect } from "react";
import {
  MdBathtub,
  MdWash,
  MdShower,
  MdLayers,
  MdLocalFireDepartment,
  MdSpa,
  MdPlumbing,
} from "react-icons/md";
import { FaChair, FaSync, FaFaucet, FaToilet } from "react-icons/fa";
import { useModelData } from "../../../hooks/useModelData";
import { ModelItem, ModelCategory } from "../../../types/api";
import "./ModelBrowser.css";

interface PlacedProduct {
  uniqueId: string;
  productId: number;
  modelItem: ModelItem;
  selectedColorId?: number;
}

interface ModelBrowserProps {
  onModelSelect: (model: ModelItem) => void;
  selectedModel?: ModelItem | null;
  style?: React.CSSProperties;
  onCategoryChange?: (categoryName: string) => void;
  placedProducts?: PlacedProduct[];
  onPlacedProductClick?: (uniqueId: string) => void;
  selectedProductId?: string | null;
  initialViewMode?: "browser" | "list";
}

const getCategoryIcon = (
  categoryName: string,
  size: number = 18
): JSX.Element => {
  const iconMap: { [key: string]: JSX.Element } = {
    bathtubs: <MdBathtub size={size} />,
    basins: <MdWash size={size} />,
    wcs: <FaToilet size={size} />,
    shower: <MdShower size={size} />,
    furniture: <FaChair size={size} />,
    accessories: <MdSpa size={size} />,
    fittings: <FaFaucet size={size} />,
    coverings: <MdLayers size={size} />,
    towel_radiators: <MdLocalFireDepartment size={size} />,
    fittings_bathtubs: <MdPlumbing size={size} />,
  };
  return iconMap[categoryName.toLowerCase()] || <MdSpa size={size} />;
};

const getProductIcon = (category: string, size: number = 18): JSX.Element => {
  const iconMap: { [key: string]: JSX.Element } = {
    bathtub: <MdBathtub size={size} />,
    bathtubs: <MdBathtub size={size} />,
    basin: <MdWash size={size} />,
    basins: <MdWash size={size} />,
    toilet: <FaToilet size={size} />,
    wcs: <FaToilet size={size} />,
    shower: <MdShower size={size} />,
    furniture: <FaChair size={size} />,
    accessoire: <MdSpa size={size} />,
    accessories: <MdSpa size={size} />,
    fitting: <FaFaucet size={size} />,
    fittings: <FaFaucet size={size} />,
    covering: <MdLayers size={size} />,
    coverings: <MdLayers size={size} />,
    towel_radiator: <MdLocalFireDepartment size={size} />,
    towel_radiators: <MdLocalFireDepartment size={size} />,
    fittings_bathtubs: <MdPlumbing size={size} />,
  };
  return iconMap[category.toLowerCase()] || <MdSpa size={size} />;
};

const isImageFile = (url: string): boolean => {
  const imageExtensions = [
    ".jpg",
    ".jpeg",
    ".png",
    ".gif",
    ".bmp",
    ".webp",
    ".tiff",
    ".svg",
  ];
  const lowerUrl = url.toLowerCase();
  return imageExtensions.some((ext) => lowerUrl.endsWith(ext));
};

export default function ModelBrowser({
  onModelSelect,
  selectedModel,
  style,
  onCategoryChange,
  placedProducts = [],
  onPlacedProductClick,
  selectedProductId,
  initialViewMode = "browser",
}: ModelBrowserProps) {
  const { categories, loading, error, refresh } = useModelData();
  const [selectedCategory, setSelectedCategory] = useState<string>("");
  const [viewMode, setViewMode] = useState<"browser" | "list">(initialViewMode);

  useEffect(() => {
    if (categories.length > 0 && !selectedCategory) {
      setSelectedCategory(categories[0].name);
    }
  }, [categories, selectedCategory, loading, error]);

  useEffect(() => {
    if (selectedCategory && onCategoryChange) {
      onCategoryChange(selectedCategory);
    }
  }, [selectedCategory, onCategoryChange]);

  useEffect(() => {
    // Update view mode when initialViewMode changes (e.g., when AI products load)
    if (initialViewMode === "list" && placedProducts.length > 0) {
      setViewMode("list");
    }
  }, [initialViewMode, placedProducts.length]);

  const currentCategory = categories.find(
    (cat) => cat.name === selectedCategory
  );
  const filteredModels = currentCategory?.models || [];

  if (loading) {
    return (
      <div className="loading-state" style={style}>
        <div className="loading-content">
          <div className="state-title">Loading Models</div>
          <div>Please wait while we load your 3D models...</div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="error-state" style={style}>
        <div className="error-content">
          <div className="state-title">Failed to Load Models</div>
          <div className="state-description">{error}</div>
          <button
            className="retry-button"
            onClick={refresh}
            data-tooltip="Retry loading models"
          >
            Try Again
          </button>
        </div>
      </div>
    );
  }

  if (categories.length === 0) {
    return (
      <div className="empty-state" style={style}>
        <div className="empty-content">
          <div className="state-title">No Models Available</div>
          <div className="state-description">
            3D models need to be imported first
          </div>
          <div className="empty-hint" />
          <div className="empty-hint">Use the admin panel to scan assets</div>
        </div>
      </div>
    );
  }

  return (
    <div className="model-browser" style={style}>
      <div className="model-browser-header">
        <div className="view-switch">
          <button
            className={`view-switch-button ${viewMode === "browser" ? "active" : ""}`}
            onClick={() => setViewMode("browser")}
          >
            Browser
          </button>
          <button
            className={`view-switch-button ${viewMode === "list" ? "active" : ""}`}
            onClick={() => setViewMode("list")}
          >
            List ({placedProducts.length})
          </button>
        </div>
      </div>

      {viewMode === "browser" && (
        <>
          <div className="category-tabs">
            {(() => {
              const regularCategories = categories.filter(
                (cat) => cat.name.toLowerCase() !== "coverings"
              );
              const coveringsCategory = categories.find(
                (cat) => cat.name.toLowerCase() === "coverings"
              );
              const sortedCategories = coveringsCategory
                ? [...regularCategories, coveringsCategory]
                : regularCategories;

              return sortedCategories.map((category) => (
                <button
                  key={category.name}
                  className={`category-tab ${
                    category.name.toLowerCase() === "coverings"
                      ? "coverings-tab"
                      : ""
                  } ${selectedCategory === category.name ? "active" : ""}`}
                  onClick={() => setSelectedCategory(category.name)}
                  data-tooltip={category.displayName}
                >
                  <span className="category-icon">
                    {getCategoryIcon(category.name)}
                  </span>
                </button>
              ));
            })()}
          </div>

          <div className="model-list">
        {filteredModels.length === 0 ? (
          <div className="no-models-message">No products in this category.</div>
        ) : (
          filteredModels.map((model) => (
            <div
              key={model.id}
              className={`model-item ${
                selectedModel?.id === model.id ? "selected" : ""
              }`}
              onClick={() => onModelSelect(model)}
            >
              <div className="model-item-preview">
                <img
                  src={model.thumbnail ?? ""}
                  alt={model.name}
                  className="model-item-image"
                  onError={(e) => {
                    const target = e.target as HTMLImageElement;
                    target.style.display = "none";
                    const placeholder =
                      target.nextElementSibling as HTMLElement;
                    if (placeholder) placeholder.style.display = "flex";
                  }}
                />
                <div className="model-item-placeholder">
                  <span className="placeholder-icon">
                    {getProductIcon(model.category)}
                  </span>
                  <span>No image</span>
                </div>

                {isImageFile(model.url) ? (
                  <div className="badge-texture">TEXTURE</div>
                ) : (
                  <div className="badge-3d">3D</div>
                )}

                <button
                  className="add-to-room-button"
                  onClick={(e) => {
                    e.stopPropagation();
                    onModelSelect(model);
                  }}
                  data-tooltip="Add to room"
                >
                  + Add to Room
                </button>
              </div>

              <div className="model-item-info">
                <div className="model-item-name">{model.name}</div>
                <div className="model-item-details">
                  {model.priceRange} • {model.mountingType}
                </div>
                {model.availableColors && model.availableColors.length > 0 && (
                  <div className="model-item-colors">
                    {model.availableColors.length} color
                    {model.availableColors.length > 1 ? "s" : ""}
                  </div>
                )}
              </div>
            </div>
          ))
        )}
          </div>

          <div className="model-browser-footer">
            {filteredModels.length} product(s) available
            {currentCategory && (
              <div className="current-category">
                <span className="current-category-icon">
                  {getCategoryIcon(currentCategory.name, 14)}
                </span>
                <span>{currentCategory.displayName}</span>
              </div>
            )}
          </div>
        </>
      )}

      {viewMode === "list" && (
        <div className="placed-products-list">
          {placedProducts.length === 0 ? (
            <div className="no-products-message">
              <div className="empty-list-title">No Items Placed</div>
              <div className="empty-list-description">
                Add products from the browser to see them here
              </div>
            </div>
          ) : (
            placedProducts.map((product) => (
              <div
                key={product.uniqueId}
                className={`placed-product-item ${
                  selectedProductId === product.uniqueId ? "selected" : ""
                }`}
                onClick={() => onPlacedProductClick?.(product.uniqueId)}
              >
                <div className="placed-product-preview">
                  <img
                    src={product.modelItem.thumbnail ?? ""}
                    alt={product.modelItem.name}
                    className="placed-product-image"
                    onError={(e) => {
                      const target = e.target as HTMLImageElement;
                      target.style.display = "none";
                      const placeholder =
                        target.nextElementSibling as HTMLElement;
                      if (placeholder) placeholder.style.display = "flex";
                    }}
                  />
                  <div className="placed-product-placeholder">
                    <span className="placeholder-icon">
                      {getProductIcon(product.modelItem.category)}
                    </span>
                  </div>
                </div>

                <div className="placed-product-info">
                  <div className="placed-product-name">
                    {product.modelItem.name}
                  </div>
                  <div className="placed-product-details">
                    {product.modelItem.category}
                    {product.selectedColorId &&
                      product.modelItem.availableColors && (
                        <>
                          {" • "}
                          {
                            product.modelItem.availableColors.find(
                              (c) => c.id === product.selectedColorId
                            )?.name
                          }
                        </>
                      )}
                  </div>
                </div>
              </div>
            ))
          )}
        </div>
      )}
    </div>
  );
}

export type { ModelItem, ModelCategory, PlacedProduct };
