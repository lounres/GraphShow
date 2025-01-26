package dev.lounres.graphShow.common.ui.implementation.graphCanvas

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.toSize
import dev.lounres.graphShow.common.ui.components.graphCanvas.GraphShowCanvasComponent
import dev.lounres.graphShow.common.ui.components.graphCanvas.PointerState
import dev.lounres.kone.collections.iterables.next
import dev.lounres.kone.collections.list.toKoneList
import dev.lounres.kone.collections.utils.lastThatOrNull
import dev.lounres.kone.computationalGeometry.Vector2
import dev.lounres.kone.computationalGeometry.invoke
import dev.lounres.kone.computationalGeometry.minus
import dev.lounres.kone.computationalGeometry.plus
import dev.lounres.kone.computationalGeometry.times
import dev.lounres.kone.misc.composeCanvas.KoneCanvas
import dev.lounres.kone.misc.composeCanvas.KoneCanvasState
import dev.lounres.kone.misc.composeCanvas.koneCanvasEuclideanKategory2Scope
import dev.lounres.kone.scope
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Clock
import kotlin.math.exp
import kotlin.time.Duration.Companion.seconds


internal val clock = Clock.System
internal val maximalDUrationBetweenClicks = 0.5.seconds

@OptIn(ExperimentalComposeUiApi::class)
@Composable
public fun GraphShowCanvasUI(
    modifier: Modifier = Modifier,
    component: GraphShowCanvasComponent,
) {
    val vertices by component.graph.verticesStateFlow.collectAsState()
    val edges by component.graph.edgesStateFlow.collectAsState()
    var koneCanvasState by remember { mutableStateOf(KoneCanvasState()) }
    val pointerState = component.pointerStateStateFlow.collectAsState().value
    KoneCanvas(
        modifier = modifier
            .pointerInput(vertices, edges) {
                koneCanvasEuclideanKategory2Scope {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            val lastChange = event.changes.last()
                            val lastOffset = lastChange.position
                            val size = size.toSize()
                            val offset = Vector2(lastOffset.x - size.width / 2, size.height / 2 - lastOffset.y) * koneCanvasState.zoom
                            val lastPosition = koneCanvasState.offset + offset
                            when {
                                event.type == PointerEventType.Press && event.button == PointerButton.Primary -> {
                                    component.pointerStateStateFlow.update { pointerState ->
                                        when (pointerState) {
                                            PointerState.Free,
                                            is PointerState.Pressed,
                                            is PointerState.Clicked.Edge,
                                            is PointerState.Dragging -> scope {
                                                val capturedVertex = vertices.lastThatOrNull {
                                                    it.capturesPointer(koneCanvasState, lastPosition)
                                                }
                                                
                                                if (capturedVertex != null)
                                                    return@scope PointerState.Pressed.Vertex(
                                                        currentPosition = lastOffset,
                                                        vertex = capturedVertex,
                                                    )
                                                
                                                val capturedEdge = edges.lastThatOrNull {
                                                    it.capturesPointer(koneCanvasState, lastPosition)
                                                }

                                                if (capturedEdge != null)
                                                    return@scope PointerState.Pressed.Edge(
                                                        edge = capturedEdge,
                                                    )
                                                
                                                PointerState.Pressed.Canvas(
                                                    currentPosition = lastOffset,
                                                )
                                            }
                                            is PointerState.Clicked.Vertex -> scope {
                                                val capturedVertex = vertices.lastThatOrNull {
                                                    it.capturesPointer(koneCanvasState, lastPosition)
                                                }
                                                
                                                if (capturedVertex != null) {
                                                    component.graph.addEdge(
                                                        tail = pointerState.vertex,
                                                        head = capturedVertex,
                                                    )
                                                    return@scope PointerState.Pressed.Vertex(
                                                        currentPosition = lastOffset,
                                                        vertex = capturedVertex,
                                                    )
                                                }
                                                
                                                val capturedEdge = edges.lastThatOrNull {
                                                    it.capturesPointer(koneCanvasState, lastPosition)
                                                }
                                                
                                                if (capturedEdge != null)
                                                    return@scope PointerState.Pressed.Edge(
                                                        edge = capturedEdge,
                                                    )
                                                
                                                PointerState.Pressed.Canvas(
                                                    currentPosition = lastOffset,
                                                )
                                            }
                                            is PointerState.Clicked.Canvas -> scope {
                                                val capturedVertex = vertices.lastThatOrNull {
                                                    it.capturesPointer(koneCanvasState, lastPosition)
                                                }
                                                
                                                if (capturedVertex != null)
                                                    return@scope PointerState.Pressed.Vertex(
                                                        currentPosition = lastOffset,
                                                        vertex = capturedVertex,
                                                    )
                                                
                                                val capturedEdge = edges.lastThatOrNull {
                                                    it.capturesPointer(koneCanvasState, lastPosition)
                                                }
                                                
                                                if (capturedEdge != null)
                                                    return@scope PointerState.Pressed.Edge(
                                                        edge = capturedEdge,
                                                    )
                                                
                                                if (clock.now() - pointerState.releasedInstant < maximalDUrationBetweenClicks)
                                                    return@scope PointerState.CanvasSecondClick(
                                                        currentPosition = lastPosition
                                                    )
                                                
                                                PointerState.Dragging.Canvas(
                                                    currentOffset = lastOffset,
                                                )
                                            }
                                            is PointerState.CanvasSecondClick -> pointerState
                                            PointerState.InitializingVertex,
                                            is PointerState.CreatingVertex -> {
                                                PointerState.CreatingVertex(
                                                    currentPosition = lastPosition
                                                )
                                            }
                                            PointerState.InitializingEdge -> PointerState.InitializingEdge
                                            is PointerState.CreatingEdge -> {
                                                PointerState.CreatingEdge(
                                                    startVertex = pointerState.startVertex,
                                                    currentPosition = lastPosition
                                                )
                                            }
                                            PointerState.Removing -> scope {
                                                val capturedVertex = vertices.lastThatOrNull {
                                                    it.capturesPointer(koneCanvasState, lastPosition)
                                                }
                                                
                                                if (capturedVertex != null) {
                                                    capturedVertex.remove()
                                                    return@scope PointerState.Removing
                                                }
                                                
                                                val capturedEdge = edges.lastThatOrNull {
                                                    it.capturesPointer(koneCanvasState, lastPosition)
                                                }
                                                
                                                if (capturedEdge != null) {
                                                    capturedEdge.remove()
                                                    return@scope PointerState.Removing
                                                }
                                                
                                                PointerState.Removing
                                            }
                                        }
                                    }
                                }
                                
                                event.type == PointerEventType.Release -> {
                                    component.pointerStateStateFlow.update { pointerState ->
                                        when (pointerState) {
                                            PointerState.Free -> PointerState.Free
                                            is PointerState.Pressed.Canvas ->
                                                PointerState.Clicked.Canvas(
                                                    releasedInstant = clock.now(),
                                                )
                                            is PointerState.Pressed.Vertex ->
                                                PointerState.Clicked.Vertex(
                                                    vertex = pointerState.vertex,
                                                    currentPosition = lastPosition,
                                                )
                                            is PointerState.Pressed.Edge ->
                                                PointerState.Clicked.Edge(
                                                    edge = pointerState.edge,
                                                )
                                            is PointerState.Clicked -> pointerState
                                            is PointerState.Dragging.Canvas -> PointerState.Free
                                            is PointerState.Dragging.Vertex ->
                                                PointerState.Clicked.Vertex(
                                                    vertex = pointerState.vertex,
                                                    currentPosition = lastPosition,
                                                )
                                            PointerState.InitializingVertex -> PointerState.Free
                                            is PointerState.CanvasSecondClick -> {
                                                component.graph.addVertex(lastPosition)
                                                PointerState.Free
                                            }
                                            is PointerState.CreatingVertex -> {
                                                component.graph.addVertex(lastPosition)
                                                PointerState.CreatingVertex(
                                                    currentPosition = lastPosition
                                                )
                                            }
                                            PointerState.InitializingEdge -> {
                                                val lastCapturedVertex = component.graph.vertices.toKoneList().lastThatOrNull { it.capturesPointer(koneCanvasState, lastPosition) }
                                                if (lastCapturedVertex != null)
                                                    PointerState.CreatingEdge(
                                                        startVertex = lastCapturedVertex,
                                                        currentPosition = lastPosition,
                                                    )
                                                else PointerState.InitializingEdge
                                            }
                                            is PointerState.CreatingEdge -> {
                                                val lastCapturedVertex = component.graph.vertices.toKoneList().lastThatOrNull { it.capturesPointer(koneCanvasState, lastPosition) }
                                                if (lastCapturedVertex == null)
                                                    PointerState.CreatingEdge(
                                                        startVertex = pointerState.startVertex,
                                                        currentPosition = lastPosition,
                                                    )
                                                else {
                                                    component.graph.addEdge(pointerState.startVertex, lastCapturedVertex)
                                                    PointerState.InitializingEdge
                                                }
                                            }
                                            PointerState.Removing -> PointerState.Removing
                                        }
                                    }
                                }
                                
                                event.type == PointerEventType.Move -> {
                                    component.pointerStateStateFlow.update { pointerState ->
                                        when (pointerState) {
                                            PointerState.Free -> pointerState
                                            is PointerState.Pressed.Canvas -> {
                                                val (oldOffset, oldZoom) = koneCanvasState
                                                val offset = lastOffset - pointerState.currentPosition
                                                koneCanvasState =
                                                    KoneCanvasState(
                                                        offset = oldOffset - Vector2(offset.x, -offset.y) * oldZoom,
                                                        zoom = oldZoom
                                                    )
                                                PointerState.Dragging.Canvas(
                                                    currentOffset = lastOffset,
                                                )
                                            }
                                            is PointerState.Pressed.Vertex -> {
                                                val (_, oldZoom) = koneCanvasState
                                                val lastPosition = lastOffset
                                                val offset = lastPosition - pointerState.currentPosition
                                                pointerState.vertex.position += Vector2(offset.x, -offset.y) * oldZoom
                                                PointerState.Dragging.Vertex(
                                                    currentPosition = lastPosition,
                                                    vertex = pointerState.vertex,
                                                )
                                            }
                                            is PointerState.Pressed.Edge -> pointerState
                                            is PointerState.Clicked.Canvas -> pointerState
                                            is PointerState.Clicked.Vertex ->
                                                PointerState.Clicked.Vertex(
                                                    vertex = pointerState.vertex,
                                                    currentPosition = lastPosition,
                                                )
                                            is PointerState.Clicked.Edge -> pointerState
                                            is PointerState.Dragging.Canvas -> {
                                                val (oldOffset, oldZoom) = koneCanvasState
                                                val offset = lastOffset - pointerState.currentOffset
                                                koneCanvasState =
                                                    KoneCanvasState(
                                                        offset = oldOffset - Vector2(offset.x, -offset.y) * oldZoom,
                                                        zoom = oldZoom
                                                    )
                                                PointerState.Dragging.Canvas(
                                                    currentOffset = lastOffset,
                                                )
                                            }
                                            is PointerState.Dragging.Vertex -> {
                                                val (_, oldZoom) = koneCanvasState
                                                val lastPosition = lastOffset
                                                val offset = lastPosition - pointerState.currentPosition
                                                pointerState.vertex.position += Vector2(offset.x, -offset.y) * oldZoom
                                                PointerState.Dragging.Vertex(
                                                    currentPosition = lastPosition,
                                                    vertex = pointerState.vertex,
                                                )
                                            }
                                            is PointerState.CanvasSecondClick ->
                                                PointerState.CanvasSecondClick(
                                                    currentPosition = lastPosition,
                                                )
                                            PointerState.InitializingVertex,
                                            is PointerState.CreatingVertex, -> {
                                                PointerState.CreatingVertex(
                                                    currentPosition = lastPosition,
                                                )
                                            }
                                            PointerState.InitializingEdge -> PointerState.InitializingEdge
                                            is PointerState.CreatingEdge -> {
                                                PointerState.CreatingEdge(
                                                    startVertex = pointerState.startVertex,
                                                    currentPosition = lastPosition,
                                                )
                                            }
                                            PointerState.Removing -> PointerState.Removing
                                        }
                                    }
                                }
                                
                                event.type == PointerEventType.Scroll -> {
                                    val zoomDelta = exp(lastChange.scrollDelta.y / 10)
                                    
                                    val (oldOffset, oldZoom) = koneCanvasState
                                    val newZoom = oldZoom * zoomDelta
                                    val pointerOffset = lastChange.position.let {
                                        Vector2(
                                            -size.width / 2 + it.x,
                                            size.height / 2 - it.y
                                        )
                                    }
                                    
                                    koneCanvasState =
                                        KoneCanvasState(
                                            offset = oldOffset + pointerOffset * (oldZoom - newZoom),
                                            zoom = newZoom
                                        )
                                }
                            }
                        }
                    }
                }
            },
        koneCanvasState = koneCanvasState,
    ) {
        when (pointerState) {
            PointerState.Free -> {
                for (edge in edges) drawEdge(koneCanvasState, edge)
                for (vertex in vertices) drawVertex(koneCanvasState, vertex)
            }
            is PointerState.Pressed.Canvas -> {
                for (edge in edges) drawEdge(koneCanvasState, edge)
                for (vertex in vertices) drawVertex(koneCanvasState, vertex)
            }
            is PointerState.Pressed.Vertex -> {
                for (edge in edges) drawEdge(koneCanvasState, edge)
                for (vertex in vertices)
                    if (vertex === pointerState.vertex) drawHighlightedVertex(koneCanvasState, vertex)
                    else drawVertex(koneCanvasState, vertex)
            }
            is PointerState.Pressed.Edge -> {
                for (edge in edges)
                    if (edge === pointerState.edge) drawHighlightedEdge(koneCanvasState, edge)
                    else drawEdge(koneCanvasState, edge)
                for (vertex in vertices) drawVertex(koneCanvasState, vertex)
            }
            is PointerState.Clicked.Canvas -> {
                for (edge in edges) drawEdge(koneCanvasState, edge)
                for (vertex in vertices) drawVertex(koneCanvasState, vertex)
            }
            is PointerState.Clicked.Vertex -> {
                for (edge in edges) drawEdge(koneCanvasState, edge)
                drawNewEdge(
                    canvasState = koneCanvasState,
                    tail = pointerState.vertex.position,
                    head = pointerState.currentPosition,
                )
                for (vertex in vertices)
                    if (vertex === pointerState.vertex) drawHighlightedVertex(koneCanvasState, vertex)
                    else drawVertex(koneCanvasState, vertex)
            }
            is PointerState.Clicked.Edge -> {
                for (edge in edges)
                    if (edge === pointerState.edge) drawHighlightedEdge(koneCanvasState, edge)
                    else drawEdge(koneCanvasState, edge)
                for (vertex in vertices) drawVertex(koneCanvasState, vertex)
            }
            is PointerState.Dragging.Canvas -> {
                for (edge in edges) drawEdge(koneCanvasState, edge)
                for (vertex in vertices) drawVertex(koneCanvasState, vertex)
            }
            is PointerState.Dragging.Vertex -> {
                for (edge in edges) drawEdge(koneCanvasState, edge)
                for (vertex in vertices)
                    if (vertex === pointerState.vertex) drawHighlightedVertex(koneCanvasState, vertex)
                    else drawVertex(koneCanvasState, vertex)
            }
            is PointerState.CanvasSecondClick -> {
                for (edge in edges) drawEdge(koneCanvasState, edge)
                for (vertex in vertices) drawVertex(koneCanvasState, vertex)
                drawNewVertex(koneCanvasState, pointerState.currentPosition)
            }
            PointerState.InitializingVertex -> {
                for (edge in edges) drawEdge(koneCanvasState, edge)
                for (vertex in vertices) drawVertex(koneCanvasState, vertex)
            }
            is PointerState.CreatingVertex -> {
                for (edge in edges) drawEdge(koneCanvasState, edge)
                for (vertex in vertices) drawVertex(koneCanvasState, vertex)
                drawNewVertex(koneCanvasState, pointerState.currentPosition)
            }
            PointerState.InitializingEdge -> {
                for (edge in edges) drawEdge(koneCanvasState, edge)
                for (vertex in vertices) drawVertex(koneCanvasState, vertex)
            }
            is PointerState.CreatingEdge -> {
                for (edge in edges) drawEdge(koneCanvasState, edge)
                drawNewEdge(koneCanvasState, pointerState.startVertex.position, pointerState.currentPosition)
                for (vertex in vertices)
                    if (vertex === pointerState.startVertex) drawHighlightedVertex(koneCanvasState, vertex)
                    else drawVertex(koneCanvasState, vertex)
            }
            PointerState.Removing -> {
                for (edge in edges) drawEdge(koneCanvasState, edge)
                for (vertex in vertices) drawVertex(koneCanvasState, vertex)
            }
        }
    }
}