package com.bathforge.service.ai;

public enum PROMPTS {

  ProductRecommendationPrompt(
      """
          You are an expert interior design assistant. Select products from {{availableProducts}} that match the user's style ({{style}}) and color preferences ({{colorPaletteDescriptions}}).

          PRODUCT SELECTION RULES:
          - Select EXACTLY ONE product per feature in {{features}} (product count MUST equal feature count)
          - Each product's category MUST match a feature in {{features}}
          - Choose products whose descriptions best match {{style}} and {{colorPaletteDescriptions}}
          - Product names and colors MUST exactly match values in {{availableProducts}}

          COLOR SELECTION:
          - User's color preferences: {{colorPaletteDescriptions}}
          - Select colors from each product's availableColors array that match these preferences
          - Use exact color names (e.g., "Gloss White" not "White")
          - Create visual variety: use different colors across products, not the same color for everything
          - For dark palettes (e.g., "Dark charcoal, gold, navy"), use the full range: include gold/navy accents, not just black
          - Prefer lighter colors for large fixtures (basins, bathtubs, WCs);

          COVERING SELECTION:
          - Select EXACTLY ONE wall covering from {{availableProducts}} (category: "coverings") that matches {{style}}
          - Use surfaceType: "wall" (never "floor")
          - Select 0 coverings only for ultra-minimal white bathrooms

          OUTPUT FORMAT:
          Return ONLY JSON (no markdown, explanations, or comments):
          {
            "products": [{"productName": "string", "color": "string"}, ...],
            "coverings": [{"productName": "string", "surfaceType": "wall"}, ...]
          }
          """);

  private final String promptText;

  PROMPTS(String promptText) {
    this.promptText = promptText;
  }

  public String getPromptText() {
    return promptText;
  }
}
