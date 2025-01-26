package dev.lounres.graphShow.desktop.ui.components

import dev.lounres.graphShow.common.ui.components.graphCanvas.GraphShowCanvasComponent
import dev.lounres.graphShow.common.ui.components.graphCanvas.PointerState
import kotlinx.coroutines.flow.MutableStateFlow


interface GraphShowWindowComponent {
    val pointerStateStateFlow: MutableStateFlow<PointerState>
    
    val isVerticesListOpen: MutableStateFlow<Boolean>
    val isEdgesListOpen: MutableStateFlow<Boolean>
    
    val graphShowCanvasComponent: GraphShowCanvasComponent
}