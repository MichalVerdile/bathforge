import React, { useRef, useEffect, useState, useCallback } from "react";

interface Vertex {
  x: number;
  y: number;
}

interface Simple2DEditorProps {
  vertices: Vertex[];
  setVertices: (vertices: Vertex[]) => void;
}

// Calculate canvas size based on viewport
const calculateCanvasSize = (): number => {
  const viewportHeight = window.innerHeight;
  const viewportWidth = window.innerWidth;
  const headerHeight = 100; // approximate header height
  const bottomButtonsHeight = 100; // approximate bottom buttons height
  const padding = 40;

  const availableHeight =
    viewportHeight - headerHeight - bottomButtonsHeight - padding;
  const availableWidth = viewportWidth - padding;

  // Use square canvas, take minimum dimension and cap at 800px
  const size = Math.min(availableHeight, availableWidth, 800);
  return Math.max(size, 300); // minimum size of 300px
};

export const Simple2DEditor: React.FC<Simple2DEditorProps> = ({
  vertices,
  setVertices,
}) => {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);
  const popupRef = useRef<HTMLDivElement>(null);
  const [draggingVertex, setDraggingVertex] = useState<number | null>(null);
  const [hoveredVertex, setHoveredVertex] = useState<number | null>(null);
  const [draggingWall, setDraggingWall] = useState<number | null>(null);
  const [hoveredWall, setHoveredWall] = useState<number | null>(null);
  const [editingWall, setEditingWall] = useState<number | null>(null);
  const [editingValue, setEditingValue] = useState<string>("");
  const [selectedVertex, setSelectedVertex] = useState<number | null>(null);
  const [canvasSize, setCanvasSize] = useState<number>(calculateCanvasSize());

  // Wall snapping configuration
  const SNAP_THRESHOLD = 5; // degrees - how close to horizontal/vertical to trigger snap

  // Handle window resize to update canvas size
  useEffect(() => {
    const handleResize = () => {
      const newSize = calculateCanvasSize();
      setCanvasSize(newSize);
    };

    window.addEventListener("resize", handleResize);
    return () => window.removeEventListener("resize", handleResize);
  }, []);

  // Calculate wall length in centimeters (assuming 1 pixel = 1 cm for now)
  const getWallLength = useCallback((p1: Vertex, p2: Vertex) => {
    const dx = p2.x - p1.x;
    const dy = p2.y - p1.y;
    return Math.sqrt(dx * dx + dy * dy);
  }, []);

  // Get wall midpoint for label positioning
  const getWallMidpoint = useCallback((p1: Vertex, p2: Vertex) => {
    return {
      x: (p1.x + p2.x) / 2,
      y: (p1.y + p2.y) / 2,
    };
  }, []);

  // Handle wall length editing
  const handleWallLengthEdit = useCallback(
    (wallIndex: number) => {
      const current = vertices[wallIndex];
      const next = vertices[(wallIndex + 1) % vertices.length];
      const currentLength = getWallLength(current, next);
      setEditingWall(wallIndex);
      setEditingValue(Math.round(currentLength).toString());
    },
    [vertices, getWallLength]
  );

  // Calculate angle of a wall in degrees
  const getWallAngle = useCallback((p1: Vertex, p2: Vertex) => {
    const dx = p2.x - p1.x;
    const dy = p2.y - p1.y;
    const angle = Math.atan2(dy, dx) * (180 / Math.PI);
    return angle;
  }, []);

  // Check if a wall should snap to horizontal or vertical
  const shouldSnapWall = useCallback(
    (p1: Vertex, p2: Vertex) => {
      const angle = getWallAngle(p1, p2);
      const absAngle = Math.abs(angle);

      // Check if close to horizontal (0° or 180°)
      if (absAngle <= SNAP_THRESHOLD || absAngle >= 180 - SNAP_THRESHOLD) {
        return "horizontal";
      }

      // Check if close to vertical (90° or 270°)
      if (
        Math.abs(absAngle - 90) <= SNAP_THRESHOLD ||
        Math.abs(absAngle - 270) <= SNAP_THRESHOLD
      ) {
        return "vertical";
      }

      return null;
    },
    [getWallAngle, SNAP_THRESHOLD]
  );

  // Apply wall snapping to a vertex position
  const applyWallSnapping = useCallback(
    (vertexIndex: number, newX: number, newY: number) => {
      const prevIndex =
        vertexIndex === 0 ? vertices.length - 1 : vertexIndex - 1;
      const nextIndex = (vertexIndex + 1) % vertices.length;

      const prevVertex = vertices[prevIndex];
      const nextVertex = vertices[nextIndex];

      // Check snapping for the wall before this vertex
      const prevWallSnap = shouldSnapWall(prevVertex, { x: newX, y: newY });
      if (prevWallSnap === "horizontal") {
        newY = prevVertex.y; // Snap to horizontal
      } else if (prevWallSnap === "vertical") {
        newX = prevVertex.x; // Snap to vertical
      }

      // Check snapping for the wall after this vertex
      const nextWallSnap = shouldSnapWall({ x: newX, y: newY }, nextVertex);
      if (nextWallSnap === "horizontal") {
        newY = nextVertex.y; // Snap to horizontal
      } else if (nextWallSnap === "vertical") {
        newX = nextVertex.x; // Snap to vertical
      }

      return { x: newX, y: newY };
    },
    [vertices, shouldSnapWall]
  );

  // Check if two line segments intersect
  const doLinesIntersect = useCallback(
    (p1: Vertex, p2: Vertex, p3: Vertex, p4: Vertex) => {
      const denom =
        (p4.y - p3.y) * (p2.x - p1.x) - (p4.x - p3.x) * (p2.y - p1.y);
      if (Math.abs(denom) < 1e-10) return false; // Lines are parallel

      const ua =
        ((p4.x - p3.x) * (p1.y - p3.y) - (p4.y - p3.y) * (p1.x - p3.x)) / denom;
      const ub =
        ((p2.x - p1.x) * (p1.y - p3.y) - (p2.y - p1.y) * (p1.x - p3.x)) / denom;

      return ua >= 0 && ua <= 1 && ub >= 0 && ub <= 1;
    },
    []
  );

  // Check if moving a vertex would create intersecting walls
  const wouldCreateIntersection = useCallback(
    (vertexIndex: number, newX: number, newY: number) => {
      const newVertices = [...vertices];
      newVertices[vertexIndex] = { x: newX, y: newY };

      // Check each wall against every other wall
      for (let i = 0; i < newVertices.length; i++) {
        const current = newVertices[i];
        const next = newVertices[(i + 1) % newVertices.length];

        for (let j = i + 2; j < newVertices.length; j++) {
          // Skip adjacent walls (they share a vertex)
          if (j === newVertices.length - 1 && i === 0) continue;

          const otherCurrent = newVertices[j];
          const otherNext = newVertices[(j + 1) % newVertices.length];

          if (doLinesIntersect(current, next, otherCurrent, otherNext)) {
            return true;
          }
        }
      }

      return false;
    },
    [vertices, doLinesIntersect]
  );

  // Apply new wall length
  const applyWallLength = useCallback(
    (wallIndex: number, newLengthCm: number) => {
      const current = vertices[wallIndex];
      const next = vertices[(wallIndex + 1) % vertices.length];

      // Calculate current wall direction
      const dx = next.x - current.x;
      const dy = next.y - current.y;
      const currentLength = Math.sqrt(dx * dx + dy * dy);

      if (currentLength === 0) return; // Avoid division by zero

      // Calculate new position for the next vertex
      const scale = newLengthCm / currentLength;
      const newNextX = current.x + dx * scale;
      const newNextY = current.y + dy * scale;

      // Check if this would create intersections
      if (
        !wouldCreateIntersection(
          (wallIndex + 1) % vertices.length,
          newNextX,
          newNextY
        )
      ) {
        const newVertices = [...vertices];
        newVertices[(wallIndex + 1) % vertices.length] = {
          x: newNextX,
          y: newNextY,
        };
        setVertices(newVertices);
      }
    },
    [vertices, wouldCreateIntersection]
  );

  // Remove a vertex (minimum 3 vertices required for a valid polygon)
  const removeVertex = useCallback(
    (vertexIndex: number) => {
      if (vertices.length <= 3) return; // Don't allow removal if only 3 vertices remain

      const newVertices = [...vertices];
      newVertices.splice(vertexIndex, 1);
      setVertices(newVertices);
      setSelectedVertex(null);
    },
    [vertices, setVertices]
  );

  const draw = useCallback(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const ctx = canvas.getContext("2d");
    if (!ctx) return;

    // Clear canvas
    ctx.clearRect(0, 0, canvas.width, canvas.height);

    // Draw walls (lines connecting vertices)
    for (let i = 0; i < vertices.length; i++) {
      const current = vertices[i];
      const next = vertices[(i + 1) % vertices.length];

      // Set wall style based on hover/drag state
      if (hoveredWall === i || draggingWall === i) {
        ctx.strokeStyle = "#cbd5e1";
        ctx.lineWidth = 6;
      } else {
        ctx.strokeStyle = "#94a3b8";
        ctx.lineWidth = 4;
      }

      ctx.lineCap = "round";
      ctx.beginPath();
      ctx.moveTo(current.x, current.y);
      ctx.lineTo(next.x, next.y);
      ctx.stroke();
    }

    // Draw vertices (circles)
    vertices.forEach((vertex, index) => {
      ctx.beginPath();
      ctx.arc(vertex.x, vertex.y, 8, 0, 2 * Math.PI);

      if (hoveredVertex === index) {
        ctx.fillStyle = "#cbd5e1";
      } else {
        ctx.fillStyle = "#94a3b8";
      }
      ctx.fill();

      ctx.strokeStyle = "#334155";
      ctx.lineWidth = 2;
      ctx.stroke();
    });

    // Draw wall length labels
    vertices.forEach((vertex, index) => {
      const next = vertices[(index + 1) % vertices.length];
      const midpoint = getWallMidpoint(vertex, next);
      const length = getWallLength(vertex, next);

      // Style for labels
      ctx.fillStyle = "#e2e8f0";
      ctx.font = "12px Inter, system-ui, sans-serif";
      ctx.textAlign = "center";
      ctx.textBaseline = "middle";

      // Draw background circle for better visibility
      ctx.beginPath();
      ctx.arc(midpoint.x, midpoint.y, 20, 0, 2 * Math.PI);
      ctx.fillStyle = "rgba(15, 23, 42, 0.8)";
      ctx.fill();

      // Draw text
      ctx.fillStyle = "#e2e8f0";
      ctx.fillText(`${Math.round(length)} cm`, midpoint.x, midpoint.y);
    });
  }, [
    vertices,
    hoveredVertex,
    hoveredWall,
    draggingWall,
    getWallMidpoint,
    getWallLength,
  ]);

  useEffect(() => {
    draw();
  }, [draw]);

  // Auto-focus input when popup opens
  useEffect(() => {
    if (editingWall !== null && inputRef.current) {
      // Small delay to ensure the popup is rendered
      setTimeout(() => {
        inputRef.current?.focus();
        inputRef.current?.select(); // Select all text for easy replacement
      }, 0);
    }
  }, [editingWall]);

  // Handle clicks outside the popup to close it
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        editingWall !== null &&
        popupRef.current &&
        !popupRef.current.contains(event.target as Node)
      ) {
        setEditingWall(null);
        setEditingValue("");
      }
    };

    if (editingWall !== null) {
      // Use a small delay to prevent immediate closure when opening the popup
      const timeoutId = setTimeout(() => {
        document.addEventListener("mouseup", handleClickOutside);
      }, 100);

      return () => {
        clearTimeout(timeoutId);
        document.removeEventListener("mouseup", handleClickOutside);
      };
    }
  }, [editingWall]);

  const getMousePos = useCallback((event: React.MouseEvent) => {
    const canvas = canvasRef.current;
    if (!canvas) return { x: 0, y: 0 };

    const rect = canvas.getBoundingClientRect();
    return {
      x: event.clientX - rect.left,
      y: event.clientY - rect.top,
    };
  }, []);

  const getVertexAt = useCallback(
    (x: number, y: number) => {
      for (let i = 0; i < vertices.length; i++) {
        const vertex = vertices[i];
        const distance = Math.sqrt((x - vertex.x) ** 2 + (y - vertex.y) ** 2);
        if (distance <= 12) {
          // 8px radius + 4px tolerance
          return i;
        }
      }
      return null;
    },
    [vertices]
  );

  // Get wall at mouse position (with tolerance for easier clicking)
  const getWallAt = useCallback(
    (x: number, y: number) => {
      for (let i = 0; i < vertices.length; i++) {
        const current = vertices[i];
        const next = vertices[(i + 1) % vertices.length];

        // Calculate distance from point to line segment
        const A = x - current.x;
        const B = y - current.y;
        const C = next.x - current.x;
        const D = next.y - current.y;

        const dot = A * C + B * D;
        const lenSq = C * C + D * D;

        if (lenSq === 0) continue; // Skip zero-length walls

        let param = dot / lenSq;
        param = Math.max(0, Math.min(1, param)); // Clamp to line segment

        const xx = current.x + param * C;
        const yy = current.y + param * D;

        const distance = Math.sqrt((x - xx) ** 2 + (y - yy) ** 2);

        if (distance <= 15) {
          // 15px tolerance for wall clicking
          return i;
        }
      }
      return null;
    },
    [vertices]
  );

  const handleMouseDown = useCallback(
    (event: React.MouseEvent) => {
      const mousePos = getMousePos(event);
      const vertexIndex = getVertexAt(mousePos.x, mousePos.y);
      const wallIndex = getWallAt(mousePos.x, mousePos.y);

      // Handle right-click for vertex removal
      if (event.button === 2 && vertexIndex !== null) {
        event.preventDefault();
        if (vertices.length > 3) {
          removeVertex(vertexIndex);
        }
        return;
      }

      // Handle left-click for vertex selection
      if (event.button === 0 && vertexIndex !== null) {
        setSelectedVertex(vertexIndex);
      }

      // Check if clicking on a wall label
      if (wallIndex !== null && vertexIndex === null) {
        const current = vertices[wallIndex];
        const next = vertices[(wallIndex + 1) % vertices.length];
        const midpoint = getWallMidpoint(current, next);
        const distanceToLabel = Math.sqrt(
          (mousePos.x - midpoint.x) ** 2 + (mousePos.y - midpoint.y) ** 2
        );

        if (distanceToLabel <= 20) {
          // Within label click area
          handleWallLengthEdit(wallIndex);
          return;
        }
      }

      // Prioritize vertex dragging over wall dragging
      if (vertexIndex !== null) {
        setDraggingVertex(vertexIndex);
        setDraggingWall(null);
      } else if (wallIndex !== null) {
        setDraggingWall(wallIndex);
        setDraggingVertex(null);
      }
    },
    [
      getMousePos,
      getVertexAt,
      getWallAt,
      vertices,
      getWallMidpoint,
      handleWallLengthEdit,
      removeVertex,
    ]
  );

  const handleMouseMove = useCallback(
    (event: React.MouseEvent) => {
      const mousePos = getMousePos(event);

      // Update hover state (prioritize vertex over wall)
      const vertexIndex = getVertexAt(mousePos.x, mousePos.y);
      const wallIndex =
        vertexIndex === null ? getWallAt(mousePos.x, mousePos.y) : null;

      setHoveredVertex(vertexIndex);
      setHoveredWall(wallIndex);

      // Handle vertex dragging
      if (draggingVertex !== null) {
        // Apply wall snapping to the new position
        const snappedPosition = applyWallSnapping(
          draggingVertex,
          mousePos.x,
          mousePos.y
        );

        // Check if the new position would create intersecting walls
        if (
          !wouldCreateIntersection(
            draggingVertex,
            snappedPosition.x,
            snappedPosition.y
          )
        ) {
          const newVertices = [...vertices];
          newVertices[draggingVertex] = snappedPosition;
          setVertices(newVertices);
        }
        // If it would create intersection, don't move the vertex
      }

      // Handle wall dragging
      if (draggingWall !== null) {
        const current = vertices[draggingWall];
        const next = vertices[(draggingWall + 1) % vertices.length];

        // Calculate the direction perpendicular to the wall
        const wallDx = next.x - current.x;
        const wallDy = next.y - current.y;
        const wallLength = Math.sqrt(wallDx * wallDx + wallDy * wallDy);

        if (wallLength > 0) {
          // Calculate how far the mouse has moved perpendicular to the wall
          const mouseDx = mousePos.x - current.x;
          const mouseDy = mousePos.y - current.y;

          // Project mouse movement onto perpendicular direction
          const perpendicularDx = -wallDy / wallLength;
          const perpendicularDy = wallDx / wallLength;

          const projection =
            mouseDx * perpendicularDx + mouseDy * perpendicularDy;

          // Move both vertices by the same amount in the perpendicular direction
          const newVertices = [...vertices];
          const moveX = projection * perpendicularDx;
          const moveY = projection * perpendicularDy;

          newVertices[draggingWall] = {
            x: current.x + moveX,
            y: current.y + moveY,
          };
          newVertices[(draggingWall + 1) % vertices.length] = {
            x: next.x + moveX,
            y: next.y + moveY,
          };

          // Check if this would create intersections
          let wouldIntersect = false;
          for (let i = 0; i < newVertices.length; i++) {
            if (
              wouldCreateIntersection(i, newVertices[i].x, newVertices[i].y)
            ) {
              wouldIntersect = true;
              break;
            }
          }

          if (!wouldIntersect) {
            setVertices(newVertices);
          }
        }
      }
    },
    [
      getMousePos,
      getVertexAt,
      getWallAt,
      draggingVertex,
      draggingWall,
      vertices,
      setVertices,
      wouldCreateIntersection,
      applyWallSnapping,
    ]
  );

  const handleMouseUp = useCallback(() => {
    setDraggingVertex(null);
    setDraggingWall(null);
  }, []);

  const handleMouseLeave = useCallback(() => {
    setDraggingVertex(null);
    setDraggingWall(null);
    setHoveredVertex(null);
    setHoveredWall(null);
  }, []);

  // Handle keyboard events for wall length editing and vertex removal
  const handleKeyDown = useCallback(
    (event: React.KeyboardEvent) => {
      // Handle wall length editing
      if (editingWall !== null) {
        if (event.key === "Enter") {
          const newLength = parseFloat(editingValue);
          if (!isNaN(newLength) && newLength > 0) {
            applyWallLength(editingWall, newLength);
          }
          setEditingWall(null);
          setEditingValue("");
        } else if (event.key === "Escape") {
          setEditingWall(null);
          setEditingValue("");
        }
        return;
      }

      // Handle vertex removal with Delete key
      if (
        event.key === "Delete" &&
        selectedVertex !== null &&
        vertices.length > 3
      ) {
        removeVertex(selectedVertex);
      }
    },
    [
      editingWall,
      editingValue,
      applyWallLength,
      selectedVertex,
      vertices.length,
      removeVertex,
    ]
  );

  const handleDoubleClick = useCallback(
    (event: React.MouseEvent) => {
      const mousePos = getMousePos(event);

      // Only create vertex if we're not hovering over an existing vertex
      const vertexIndex = getVertexAt(mousePos.x, mousePos.y);
      if (vertexIndex !== null) return; // Don't create vertex on existing vertex

      // Find which wall we're double-clicking on
      const wallIndex = getWallAt(mousePos.x, mousePos.y);
      if (wallIndex === null) return; // Not on a wall

      // Create new vertex at the click position
      const newVertex: Vertex = { x: mousePos.x, y: mousePos.y };
      const newVertices = [...vertices];

      // Insert the new vertex after the wall's starting vertex
      newVertices.splice(wallIndex + 1, 0, newVertex);
      setVertices(newVertices);
    },
    [getMousePos, getVertexAt, getWallAt, vertices, setVertices]
  );

  const handleContextMenu = useCallback((event: React.MouseEvent) => {
    event.preventDefault(); // Prevent default context menu
  }, []);

  return (
    <div
      style={{
        width: "100%",
        height: "100%",
        background: "#0f172a",
        position: "relative",
      }}
    >
      <style>
        {`
          .no-spinner::-webkit-outer-spin-button,
          .no-spinner::-webkit-inner-spin-button {
            -webkit-appearance: none;
            margin: 0;
          }
          .no-spinner[type=number] {
            -moz-appearance: textfield;
          }
        `}
      </style>
      <canvas
        ref={canvasRef}
        width={canvasSize}
        height={canvasSize}
        onMouseDown={handleMouseDown}
        onMouseMove={handleMouseMove}
        onMouseUp={handleMouseUp}
        onMouseLeave={handleMouseLeave}
        onDoubleClick={handleDoubleClick}
        onContextMenu={handleContextMenu}
        onKeyDown={handleKeyDown}
        tabIndex={0}
        style={{
          cursor:
            draggingVertex !== null
              ? "grabbing"
              : draggingWall !== null
              ? "grabbing"
              : hoveredVertex !== null
              ? "grab"
              : hoveredWall !== null
              ? "grab"
              : "default",
          display: "block",
          margin: "auto",
          outline: "none",
        }}
      />

      {/* Input overlay for wall length editing */}
      {editingWall !== null && (
        <div
          ref={popupRef}
          style={{
            position: "absolute",
            top: "50%",
            left: "50%",
            transform: "translate(-50%, -50%)",
            background: "rgba(15, 23, 42, 0.98)",
            border: "1px solid rgba(148, 163, 184, 0.2)",
            borderRadius: "12px",
            padding: "20px 24px",
            zIndex: 1000,
            boxShadow:
              "0 20px 25px -5px rgba(0, 0, 0, 0.3), 0 10px 10px -5px rgba(0, 0, 0, 0.1)",
            backdropFilter: "blur(8px)",
          }}
        >
          <div
            style={{
              color: "#f1f5f9",
              fontSize: "14px",
              marginBottom: "12px",
              fontWeight: "500",
              textAlign: "center",
            }}
          >
            Enter wall length (cm)
          </div>
          <div style={{ display: "flex", justifyContent: "center" }}>
            <input
              ref={inputRef}
              type="number"
              value={editingValue}
              onChange={(e) => setEditingValue(e.target.value)}
              onKeyDown={handleKeyDown}
              style={{
                background: "#1e293b",
                border: "1px solid rgba(148, 163, 184, 0.3)",
                borderRadius: "8px",
                color: "#f1f5f9",
                padding: "12px 16px",
                fontSize: "16px",
                width: "140px",
                outline: "none",
                textAlign: "center",
                fontWeight: "500",
                transition: "border-color 0.2s ease",
                // Hide the default number input spinners
                MozAppearance: "textfield",
                WebkitAppearance: "none",
                appearance: "none",
              }}
              className="no-spinner"
              onFocus={(e) => {
                e.target.style.borderColor = "rgba(59, 130, 246, 0.5)";
              }}
              onBlur={(e) => {
                e.target.style.borderColor = "rgba(148, 163, 184, 0.3)";
              }}
            />
          </div>
          <div
            style={{
              color: "#94a3b8",
              fontSize: "11px",
              marginTop: "8px",
              textAlign: "center",
              opacity: 0.8,
            }}
          >
            Press Enter to apply, Escape to cancel
          </div>
        </div>
      )}
    </div>
  );
};
