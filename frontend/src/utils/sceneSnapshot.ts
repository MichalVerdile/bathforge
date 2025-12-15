import * as THREE from "three";

/**
 * Captures a snapshot of the Three.js scene as a base64 encoded PNG image
 * @param renderer - The Three.js WebGL renderer
 * @param scene - The Three.js scene to capture
 * @param camera - The Three.js camera
 * @param width - Optional width for the snapshot (defaults to renderer width)
 * @param height - Optional height for the snapshot (defaults to renderer height)
 * @returns Base64 encoded PNG image data URL
 */
export function captureSceneSnapshot(
  renderer: THREE.WebGLRenderer,
  scene: THREE.Scene,
  camera: THREE.Camera,
  width?: number,
  height?: number
): string {
  // Store current renderer size
  const currentSize = new THREE.Vector2();
  renderer.getSize(currentSize);

  // Set snapshot size if specified
  if (width && height) {
    renderer.setSize(width, height);
  }

  // Render the scene
  renderer.render(scene, camera);

  // Capture the canvas as base64 image
  const canvas = renderer.domElement;
  const dataUrl = canvas.toDataURL("image/png");

  // Restore original renderer size
  renderer.setSize(currentSize.x, currentSize.y);

  return dataUrl;
}

/**
 * Captures a high-quality snapshot with custom settings
 */
export function captureHighQualitySnapshot(
  renderer: THREE.WebGLRenderer,
  scene: THREE.Scene,
  camera: THREE.Camera,
  options: {
    width?: number;
    height?: number;
    quality?: number; // 0-1 for JPEG quality
    format?: "png" | "jpeg";
  } = {}
): string {
  const {
    width = 1920,
    height = 1080,
    quality = 0.95,
    format = "png",
  } = options;

  // Store current settings
  const currentSize = new THREE.Vector2();
  renderer.getSize(currentSize);
  const currentPixelRatio = renderer.getPixelRatio();

  // Set high-quality settings
  renderer.setPixelRatio(2);
  renderer.setSize(width, height);

  // Render the scene
  renderer.render(scene, camera);

  // Capture canvas
  const canvas = renderer.domElement;
  const mimeType = format === "jpeg" ? "image/jpeg" : "image/png";
  const dataUrl = canvas.toDataURL(mimeType, quality);

  // Restore original settings
  renderer.setPixelRatio(currentPixelRatio);
  renderer.setSize(currentSize.x, currentSize.y);
  renderer.render(scene, camera);

  return dataUrl;
}

/**
 * Downloads a snapshot as a file
 */
export function downloadSnapshot(
  dataUrl: string,
  filename: string = "bathroom-scene.png"
): void {
  const link = document.createElement("a");
  link.download = filename;
  link.href = dataUrl;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
}
