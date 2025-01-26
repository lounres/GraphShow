package dev.lounres.graphShow.common.ui.components.graphCanvas

import dev.lounres.graphShow.common.logic.canvasGraph.PlaneGraph
import kotlinx.coroutines.flow.MutableStateFlow


public class RealGraphShowCanvasComponent(
    override val graph: PlaneGraph,
    override val pointerStateStateFlow: MutableStateFlow<PointerState>
) : GraphShowCanvasComponent