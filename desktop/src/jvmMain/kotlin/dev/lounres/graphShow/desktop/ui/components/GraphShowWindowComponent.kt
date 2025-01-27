package dev.lounres.graphShow.desktop.ui.components

import dev.lounres.graphShow.common.ui.components.graphCanvas.GraphShowCanvasComponent
import kotlinx.coroutines.flow.MutableStateFlow


interface GraphShowWindowComponent {
    val isVerticesListOpen: MutableStateFlow<Boolean>
    val isEdgesListOpen: MutableStateFlow<Boolean>
    
    val graphShowCanvasComponentStateFlow: MutableStateFlow<GraphShowCanvasComponent>
}