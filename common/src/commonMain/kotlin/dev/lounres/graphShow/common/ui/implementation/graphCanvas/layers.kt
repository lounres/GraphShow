package dev.lounres.graphShow.common.ui.implementation.graphCanvas

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import dev.lounres.graphShow.common.logic.canvasGraph.PlaneGraphEdge
import dev.lounres.graphShow.common.logic.canvasGraph.PlaneGraphVertex
import dev.lounres.kone.computationalGeometry.Point2
import dev.lounres.kone.misc.composeCanvas.KoneCanvasState


internal fun DrawScope.drawVertex(canvasState: KoneCanvasState, vertex: PlaneGraphVertex) {
    drawCircle(
        color = Color.Black,
        center = Offset(vertex.position.x, vertex.position.y),
        radius = 20f * canvasState.zoom,
    )
    drawCircle(
        color = Color.White,
        center = Offset(vertex.position.x, vertex.position.y),
        radius = 15f * canvasState.zoom,
    )
}

internal fun DrawScope.drawHighlightedVertex(canvasState: KoneCanvasState, vertex: PlaneGraphVertex) {
    drawCircle(
        color = Color.Red,
        center = Offset(vertex.position.x, vertex.position.y),
        radius = 20f * canvasState.zoom,
    )
    drawCircle(
        color = Color.White,
        center = Offset(vertex.position.x, vertex.position.y),
        radius = 15f * canvasState.zoom,
    )
}

internal fun DrawScope.drawNewVertex(canvasState: KoneCanvasState, coordinates: Point2<Float>) {
    drawCircle(
        color = Color.Gray,
        center = Offset(coordinates.x, coordinates.y),
        radius = 20f * canvasState.zoom,
    )
    drawCircle(
        color = Color.White,
        center = Offset(coordinates.x, coordinates.y),
        radius = 15f * canvasState.zoom,
    )
}

internal fun DrawScope.drawEdge(canvasState: KoneCanvasState, edge: PlaneGraphEdge) {
    val (tail, head) = edge.ends
    drawLine(
        color = Color.Black,
        start = tail.position.let { Offset(it.x, it.y) },
        end = head.position.let { Offset(it.x, it.y) },
        strokeWidth = 5.0f * canvasState.zoom,
    )
}

internal fun DrawScope.drawHighlightedEdge(canvasState: KoneCanvasState, edge: PlaneGraphEdge) {
    val (tail, head) = edge.ends
    drawLine(
        color = Color.Red,
        start = tail.position.let { Offset(it.x, it.y) },
        end = head.position.let { Offset(it.x, it.y) },
        strokeWidth = 5.0f * canvasState.zoom,
    )
}

internal fun DrawScope.drawNewEdge(canvasState: KoneCanvasState, tail: Point2<Float>, head: Point2<Float>) {
    drawLine(
        color = Color.Gray,
        start = tail.let { Offset(it.x, it.y) },
        end = head.let { Offset(it.x, it.y) },
        strokeWidth = 5.0f * canvasState.zoom,
    )
}