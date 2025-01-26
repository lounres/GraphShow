package dev.lounres.graphShow.common.ui.components.graphCanvas

import androidx.compose.ui.geometry.Offset
import dev.lounres.graphShow.common.logic.canvasGraph.PlaneGraphEdge
import dev.lounres.graphShow.common.logic.canvasGraph.PlaneGraphVertex
import dev.lounres.kone.computationalGeometry.Point2
import kotlinx.datetime.Instant


public sealed interface PointerState {
    public data object Free : PointerState
    public sealed interface Pressed : PointerState {
        public data class Canvas(
            val currentPosition: Offset,
        ) : Pressed
        public data class Vertex(
            val currentPosition: Offset,
            val vertex: PlaneGraphVertex,
        ) : Pressed
        public data class Edge(
            val edge: PlaneGraphEdge,
        ) : Pressed
    }
    public sealed interface Clicked : PointerState {
        public data class Canvas(
            val releasedInstant: Instant,
        ) : Clicked
        public data class Vertex(
            val vertex: PlaneGraphVertex,
            val currentPosition: Point2<Float>,
        ) : Clicked
        public data class Edge(
            val edge: PlaneGraphEdge,
        ) : Clicked
    }
    public sealed interface Dragging : PointerState {
        public data class Canvas(
            val currentOffset: Offset,
        ) : Dragging
        public data class Vertex(
            val currentPosition: Offset,
            val vertex: PlaneGraphVertex,
        ) : Dragging
    }
    public data class CanvasSecondClick(
        val currentPosition: Point2<Float>,
    ) : PointerState
    public data object InitializingVertex : PointerState
    public data class CreatingVertex(
        val currentPosition: Point2<Float>,
    ) : PointerState
    public data object InitializingEdge : PointerState
    public data class CreatingEdge(
        val startVertex: PlaneGraphVertex,
        val currentPosition: Point2<Float>,
    ) : PointerState
    public data object Removing : PointerState
}