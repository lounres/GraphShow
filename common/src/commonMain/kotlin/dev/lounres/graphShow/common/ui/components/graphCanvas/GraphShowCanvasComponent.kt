package dev.lounres.graphShow.common.ui.components.graphCanvas

import dev.lounres.graphShow.common.logic.canvasGraph.PlaneGraph
import kotlinx.coroutines.flow.MutableStateFlow


public interface GraphShowCanvasComponent {
    public val graph: PlaneGraph
    public val pointerStateStateFlow : MutableStateFlow<PointerState>
}