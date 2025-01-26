package dev.lounres.graphShow.web.ui.components

import dev.lounres.graphShow.common.logic.canvasGraph.PlaneGraph
import dev.lounres.graphShow.common.ui.components.graphCanvas.GraphShowCanvasComponent
import dev.lounres.graphShow.common.ui.components.graphCanvas.PointerState
import dev.lounres.graphShow.common.ui.components.graphCanvas.RealGraphShowCanvasComponent
import kotlinx.coroutines.flow.MutableStateFlow


class RealGraphShowWindowComponent(
    graph: PlaneGraph
): GraphShowWindowComponent {
    override val pointerStateStateFlow: MutableStateFlow<PointerState> = MutableStateFlow<PointerState>(PointerState.Free)
    
    override val isVerticesListOpen: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override val isEdgesListOpen: MutableStateFlow<Boolean> = MutableStateFlow(false)
    
    override val graphShowCanvasComponent: GraphShowCanvasComponent =
        RealGraphShowCanvasComponent(
            graph = graph,
            pointerStateStateFlow = pointerStateStateFlow,
        )
}