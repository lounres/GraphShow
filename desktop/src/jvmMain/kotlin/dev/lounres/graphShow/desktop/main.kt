package dev.lounres.graphShow.desktop

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.onClick
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.singleWindowApplication
import dev.lounres.graphShow.common.logic.canvasGraph.PlaneGraph
import dev.lounres.graphShow.common.logic.canvasGraph.PlaneGraphVertex
import dev.lounres.graphShow.common.ui.components.graphCanvas.PointerState
import dev.lounres.graphShow.common.ui.components.graphCanvas.RealGraphShowCanvasComponent
import dev.lounres.graphShow.common.ui.implementation.graphCanvas.GraphShowCanvasUI
import dev.lounres.graphShow.desktop.resources.*
import dev.lounres.graphShow.desktop.ui.components.GraphShowWindowComponent
import dev.lounres.graphShow.desktop.ui.components.RealGraphShowWindowComponent
import dev.lounres.kone.collections.interop.toList
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.compose.rememberFileSaverLauncher
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.jetbrains.compose.resources.painterResource


val json = Json

@OptIn(ExperimentalSerializationApi::class)
fun main() {
    val graphShowWindowComponent: GraphShowWindowComponent = RealGraphShowWindowComponent()
    
    singleWindowApplication {
        val graphShowCanvasComponent by graphShowWindowComponent.graphShowCanvasComponentStateFlow.collectAsState()
        val filePickerLauncher = rememberFilePickerLauncher(
            type = PickerType.File(listOf("graphshow.json")),
            mode = PickerMode.Single,
        ) {
            if (it == null) return@rememberFilePickerLauncher
            val graph = json.decodeFromStream<PlaneGraph>(it.file.inputStream())
            graphShowWindowComponent.graphShowCanvasComponentStateFlow.value =
                RealGraphShowCanvasComponent(
                    graph = graph,
                    pointerStateStateFlow = MutableStateFlow(PointerState.Free),
                )
        }
        val fileSaverLauncher = rememberFileSaverLauncher {}
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            val pointerState by graphShowCanvasComponent.pointerStateStateFlow.collectAsState()
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilledIconToggleButton(
                    checked = when (pointerState) {
                        PointerState.Free,
                        is PointerState.Pressed,
                        is PointerState.Clicked,
                        is PointerState.Dragging,
                        is PointerState.CanvasSecondClick -> false
                        PointerState.InitializingVertex,
                        is PointerState.CreatingVertex -> true
                        PointerState.InitializingEdge,
                        is PointerState.CreatingEdge,
                        PointerState.Removing -> false
                    },
                    onCheckedChange = {
                        graphShowCanvasComponent.pointerStateStateFlow.update { pointerState ->
                            when (pointerState) {
                                PointerState.Free,
                                is PointerState.Pressed,
                                is PointerState.Clicked,
                                is PointerState.Dragging,
                                is PointerState.CanvasSecondClick -> PointerState.InitializingVertex
                                PointerState.InitializingVertex,
                                is PointerState.CreatingVertex -> PointerState.Free
                                is PointerState.CreatingEdge,
                                PointerState.InitializingEdge,
                                PointerState.Removing -> PointerState.InitializingVertex
                            }
                        }
                    },
                    shape = RoundedCornerShape(percent = 20),
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.addVertex),
                        contentDescription = "Add vertex",
                    )
                }
                Spacer(Modifier.width(2.dp))
                FilledIconToggleButton(
                    checked = when (pointerState) {
                        PointerState.Free,
                        is PointerState.Pressed,
                        is PointerState.Clicked,
                        is PointerState.Dragging,
                        is PointerState.CanvasSecondClick,
                        PointerState.InitializingVertex,
                        is PointerState.CreatingVertex -> false
                        PointerState.InitializingEdge,
                        is PointerState.CreatingEdge -> true
                        PointerState.Removing -> false
                    },
                    onCheckedChange = {
                        graphShowCanvasComponent.pointerStateStateFlow.update { pointerState ->
                            when (pointerState) {
                                PointerState.Free,
                                is PointerState.Pressed,
                                is PointerState.Clicked,
                                is PointerState.Dragging,
                                is PointerState.CanvasSecondClick,
                                PointerState.InitializingVertex,
                                is PointerState.CreatingVertex -> PointerState.InitializingEdge
                                PointerState.InitializingEdge,
                                is PointerState.CreatingEdge -> PointerState.Free
                                PointerState.Removing -> PointerState.InitializingEdge
                            }
                        }
                    },
                    shape = RoundedCornerShape(percent = 20),
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.addEdge),
                        contentDescription = "Add edge",
                    )
                }
                Spacer(Modifier.width(2.dp))
                FilledIconToggleButton(
                    checked = when (pointerState) {
                        PointerState.Free,
                        is PointerState.Pressed,
                        is PointerState.Clicked,
                        is PointerState.Dragging,
                        is PointerState.CanvasSecondClick,
                        PointerState.InitializingVertex,
                        is PointerState.CreatingVertex,
                        PointerState.InitializingEdge,
                        is PointerState.CreatingEdge -> false
                        PointerState.Removing -> true
                    },
                    onCheckedChange = {
                        graphShowCanvasComponent.pointerStateStateFlow.update { pointerState ->
                            when (pointerState) {
                                PointerState.Free,
                                is PointerState.Pressed,
                                is PointerState.Clicked,
                                is PointerState.Dragging,
                                is PointerState.CanvasSecondClick,
                                PointerState.InitializingVertex,
                                is PointerState.CreatingVertex,
                                PointerState.InitializingEdge,
                                is PointerState.CreatingEdge -> PointerState.Removing
                                PointerState.Removing -> PointerState.Free
                            }
                        }
                    },
                    shape = RoundedCornerShape(percent = 20),
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove",
                    )
                }
                Spacer(Modifier.width(2.dp))
                FilledTonalIconButton(
                    onClick = {
                        fileSaverLauncher.launch(
                            bytes = json.encodeToString(graphShowCanvasComponent.graph).encodeToByteArray(),
                            baseName = "",
                            extension = "graphshow.json",
                        )
                    },
                    shape = RoundedCornerShape(percent = 20),
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.save),
                        contentDescription = "Save graph",
                    )
                }
                Spacer(Modifier.width(2.dp))
                FilledTonalIconButton(
                    onClick = {
                        filePickerLauncher.launch()
                    },
                    shape = RoundedCornerShape(percent = 20),
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.open),
                        contentDescription = "Open graph",
                    )
                }
                Spacer(Modifier.weight(1f))
                FilledIconToggleButton(
                    checked = graphShowWindowComponent.isVerticesListOpen.collectAsState().value,
                    onCheckedChange = {
                        graphShowWindowComponent.isVerticesListOpen.value = it
                    },
                    shape = RoundedCornerShape(percent = 20),
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.showVertices),
                        contentDescription = "Show list of vertices",
                    )
                }
                Spacer(Modifier.width(2.dp))
                FilledIconToggleButton(
                    checked = graphShowWindowComponent.isEdgesListOpen.collectAsState().value,
                    onCheckedChange = {
                        graphShowWindowComponent.isEdgesListOpen.value = it
                    },
                    shape = RoundedCornerShape(percent = 20),
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.showEdges),
                        contentDescription = "Show list of edges",
                    )
                }
            }
            HorizontalDivider()
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
            ) {
                GraphShowCanvasUI(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f),
                    component = graphShowWindowComponent.graphShowCanvasComponentStateFlow.collectAsState().value
                )
                if (graphShowWindowComponent.isVerticesListOpen.collectAsState().value) {
                    VerticalDivider()
                    Column(
                        modifier = Modifier
                            .width(256.dp)
                            .padding(8.dp),
                    ) {
                        Text(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally),
                            text = "Вершины",
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val scrollState = rememberLazyListState()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                        ) {
                            val vertices by graphShowCanvasComponent.graph.verticesStateFlow.collectAsState()
                            LazyColumn(
                                modifier = Modifier.fillMaxHeight().weight(1f),
                                state = scrollState,
                            ) {
                                itemsIndexed<PlaneGraphVertex>(items = vertices.toList()) { index, vertex ->
                                    if (index != 0) Spacer(modifier = Modifier.height(4.dp))
                                    val shape = RoundedCornerShape(16.dp)
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .onClick {
                                                graphShowCanvasComponent.pointerStateStateFlow.value =
                                                    PointerState.Clicked.Vertex(
                                                        vertex = vertex,
                                                        currentPosition = vertex.position,
                                                    )
                                            }
                                            .let {
                                                if (pointerState.let { it is PointerState.Clicked.Vertex && it.vertex === vertex })
                                                    it.border(2.dp, Color.Red, shape)
                                                else
                                                    it
                                            },
                                        shape = shape,
                                        shadowElevation = 2.dp,
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                        ) {
                                            Text(
                                                modifier = Modifier.align(Alignment.CenterVertically),
                                                text = "Вершина ${index + 1}"
                                            )
                                            Spacer(modifier = Modifier.weight(1f))
                                            IconButton(
                                                onClick = { vertex.remove() }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Удалить вершину",
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            VerticalScrollbar(
                                modifier = Modifier.fillMaxHeight(),
                                adapter = rememberScrollbarAdapter(scrollState),
                            )
                        }
                    }
                }
                if (graphShowWindowComponent.isEdgesListOpen.collectAsState().value) {
                    VerticalDivider()
                    Column(
                        modifier = Modifier
                            .width(256.dp)
                            .padding(8.dp),
                    ) {
                        Text(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally),
                            text = "Рёбра",
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val scrollState = rememberLazyListState()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                        ) {
                            val edges by graphShowCanvasComponent.graph.edgesStateFlow.collectAsState()
                            LazyColumn(
                                modifier = Modifier.fillMaxHeight().weight(1f),
                                state = scrollState,
                            ) {
                                itemsIndexed(edges.toList()) { index, edge ->
                                    if (index != 0) Spacer(modifier = Modifier.height(4.dp))
                                    val shape = RoundedCornerShape(16.dp)
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .onClick {
                                                graphShowCanvasComponent.pointerStateStateFlow.value =
                                                    PointerState.Clicked.Edge(
                                                        edge = edge,
                                                    )
                                            }
                                            .let {
                                                if (pointerState.let { it is PointerState.Clicked.Edge && it.edge === edge })
                                                    it.border(2.dp, Color.Red, shape)
                                                else
                                                    it
                                            },
                                        shape = RoundedCornerShape(16.dp),
                                        shadowElevation = 2.dp,
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                        ) {
                                            Text(
                                                modifier = Modifier.align(Alignment.CenterVertically),
                                                text = "Ребро ${index + 1}"
                                            )
                                            Spacer(modifier = Modifier.weight(1f))
                                            IconButton(
                                                onClick = { edge.remove() }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Удалить ребро",
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            VerticalScrollbar(
                                modifier = Modifier.fillMaxHeight(),
                                adapter = rememberScrollbarAdapter(scrollState),
                            )
                        }
                    }
                }
            }
        }
    }
}