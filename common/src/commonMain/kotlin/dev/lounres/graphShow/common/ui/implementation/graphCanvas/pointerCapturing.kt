package dev.lounres.graphShow.common.ui.implementation.graphCanvas

import dev.lounres.graphShow.common.logic.canvasGraph.PlaneGraphEdge
import dev.lounres.graphShow.common.logic.canvasGraph.PlaneGraphVertex
import dev.lounres.kone.computationalGeometry.Point2
import dev.lounres.kone.computationalGeometry.Vector2
import dev.lounres.kone.computationalGeometry.dot
import dev.lounres.kone.computationalGeometry.invoke
import dev.lounres.kone.computationalGeometry.lengthSquared
import dev.lounres.kone.computationalGeometry.minus
import dev.lounres.kone.computationalGeometry.utils.cross
import dev.lounres.kone.misc.composeCanvas.KoneCanvasState
import dev.lounres.kone.misc.composeCanvas.koneCanvasEuclideanKategory2Scope


internal fun PlaneGraphVertex.capturesPointer(canvasState: KoneCanvasState, pointerPoint: Point2<Float>): Boolean =
    koneCanvasEuclideanKategory2Scope {
        (position - pointerPoint).lengthSquared < (20f * canvasState.zoom).let { it * it }
    }

internal fun PlaneGraphEdge.capturesPointer(canvasState: KoneCanvasState, pointerPoint: Point2<Float>): Boolean = koneCanvasEuclideanKategory2Scope {
    val (tail, head) = ends
    val start = tail.position
    val direction: Vector2<Float> = head.position - start
    val pointerVector: Vector2<Float> = pointerPoint - start
    
    pointerVector dot direction < direction.lengthSquared &&
            (pointerVector cross direction).let { it * it } < (2.5f * canvasState.zoom).let { it * it } * direction.lengthSquared
}