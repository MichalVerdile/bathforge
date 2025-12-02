import type { ComponentType } from "react";
import {
  MdBathtub,
  MdWash,
  MdShower,
  MdSpa,
  MdLayers,
  MdLocalFireDepartment,
  MdPlumbing,
} from "react-icons/md";
import { FaToilet, FaChair, FaFaucet } from "react-icons/fa";

export interface StyleOption {
  id:
    | "modern"
    | "traditional"
    | "minimalist"
    | "luxury"
    | "industrial"
    | "scandinavian";
  name: string;
  icon: string;
  description: string;
}

export interface ColorPalette {
  id:
    | "spa-serenity"
    | "modern-monochrome"
    | "natural-warmth"
    | "urban-chic"
    | "luxe-dark"
    | "sage-stone";
  name: string;
  colors: {
    primary: string;
    secondary: string;
    accent1: string;
    accent2: string;
  };
}

export interface FeatureOption {
  id:
    | "bathtubs"
    | "basins"
    | "wcs"
    | "shower"
    | "furniture"
    | "accessoires"
    | "fittings"
    | "coverings"
    | "towel_radiators"
    | "fittings_bathtubs";
  name: string;
  icon: ComponentType<{ size?: number }>;
  description: string;
  category: string;
}

export const STYLE_OPTIONS: StyleOption[] = [
  {
    id: "modern",
    name: "Modern",
    icon: "▢",
    description: "Clean lines, minimalist design, and contemporary fixtures",
  },
  {
    id: "traditional",
    name: "Traditional",
    icon: "◈",
    description: "Classic elegance with timeless fixtures and warm tones",
  },
  {
    id: "minimalist",
    name: "Minimalist",
    icon: "○",
    description: "Simple, functional design with minimal decoration",
  },
  {
    id: "luxury",
    name: "Luxury",
    icon: "◆",
    description: "High-end materials, premium fixtures, and spa-like ambiance",
  },
  {
    id: "industrial",
    name: "Industrial",
    icon: "▨",
    description: "Raw materials, exposed elements, and urban aesthetics",
  },
  {
    id: "scandinavian",
    name: "Scandinavian",
    icon: "△",
    description: "Light, airy spaces with natural materials and simple forms",
  },
];

export const COLOR_PALETTES: ColorPalette[] = [
  {
    id: "spa-serenity",
    name: "Spa Serenity",
    colors: {
      primary: "#F8F9FA",
      secondary: "#C9D6DF",
      accent1: "#8BA5B7",
      accent2: "#A89F91",
    },
  },
  {
    id: "modern-monochrome",
    name: "Modern Monochrome",
    colors: {
      primary: "#FFFFFF",
      secondary: "#E8E9EB",
      accent1: "#6B7280",
      accent2: "#1F2937",
    },
  },
  {
    id: "natural-warmth",
    name: "Natural Warmth",
    colors: {
      primary: "#F5F1E8",
      secondary: "#E8DCC8",
      accent1: "#C19A6B",
      accent2: "#8B6F47",
    },
  },
  {
    id: "urban-chic",
    name: "Urban Chic",
    colors: {
      primary: "#F5F5F5",
      secondary: "#E0E0E0",
      accent1: "#9E9E9E",
      accent2: "#616161",
    },
  },
  {
    id: "luxe-dark",
    name: "Luxe Dark",
    colors: {
      primary: "#F9F7F4",
      secondary: "#1C2833",
      accent1: "#C9A961",
      accent2: "#5D6D7E",
    },
  },
  {
    id: "sage-stone",
    name: "Sage & Stone",
    colors: {
      primary: "#F4F6F5",
      secondary: "#C8D5C7",
      accent1: "#8A9A8B",
      accent2: "#6E7D7E",
    },
  },
];

export const FEATURE_OPTIONS: FeatureOption[] = [
  {
    id: "bathtubs",
    name: "Bathtubs",
    icon: MdBathtub,
    description: "Relaxing soaking tubs",
    category: "bathtubs",
  },
  {
    id: "basins",
    name: "Basins",
    icon: MdWash,
    description: "Bathroom basins and sinks",
    category: "basins",
  },
  {
    id: "wcs",
    name: "WCs",
    icon: FaToilet,
    description: "Modern toilets",
    category: "wcs",
  },
  {
    id: "shower",
    name: "Shower",
    icon: MdShower,
    description: "Walk-in or enclosed showers",
    category: "shower",
  },
  {
    id: "furniture",
    name: "Furniture",
    icon: FaChair,
    description: "Cabinets, vanities and storage furniture",
    category: "furniture",
  },
  {
    id: "accessoires",
    name: "Accessories",
    icon: MdSpa,
    description: "Mirrors, hooks and small accessories",
    category: "accessoires",
  },
  {
    id: "fittings",
    name: "Fittings",
    icon: FaFaucet,
    description: "Taps, mixers and hardware",
    category: "fittings",
  },
  {
    id: "towel_radiators",
    name: "Towel Radiators",
    icon: MdLocalFireDepartment,
    description: "Heated towel rails and radiators",
    category: "towel_radiators",
  },
  {
    id: "fittings_bathtubs",
    name: "Bathtub Fittings",
    icon: MdPlumbing,
    description: "Plumbing and fittings for bathtubs",
    category: "fittings_bathtubs",
  },
];

const buildLabelMap = <T extends { id: string; name: string }>(
  items: readonly T[]
) =>
  items.reduce<Record<string, string>>((acc, item) => {
    acc[item.id] = item.name;
    return acc;
  }, {});

export const STYLE_LABELS = buildLabelMap(STYLE_OPTIONS) as Record<
  StyleOption["id"],
  string
>;
export const COLOR_LABELS = buildLabelMap(COLOR_PALETTES) as Record<
  ColorPalette["id"],
  string
>;
export const FEATURE_LABELS = buildLabelMap(FEATURE_OPTIONS) as Record<
  FeatureOption["id"],
  string
>;

export type StyleId = StyleOption["id"];
export type ColorPaletteId = ColorPalette["id"];
export type FeatureId = FeatureOption["id"];
