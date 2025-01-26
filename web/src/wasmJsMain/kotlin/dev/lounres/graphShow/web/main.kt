package dev.lounres.graphShow.web

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeViewport
import dev.lounres.graphShow.common.logic.canvasGraph.PlaneGraph
import dev.lounres.graphShow.common.ui.components.graphCanvas.PointerState
import dev.lounres.graphShow.common.ui.implementation.graphCanvas.GraphShowCanvasUI
import dev.lounres.graphShow.web.resources.Res
import dev.lounres.graphShow.web.resources.addEdge
import dev.lounres.graphShow.web.resources.addVertex
import dev.lounres.graphShow.web.resources.showEdges
import dev.lounres.graphShow.web.resources.showVertices
import dev.lounres.graphShow.web.ui.components.GraphShowWindowComponent
import dev.lounres.graphShow.web.ui.components.RealGraphShowWindowComponent
import dev.lounres.kone.collections.list.koneListOf
import dev.lounres.kone.computationalGeometry.Point2
import kotlinx.browser.document
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource


fun main() {
    val graph = PlaneGraph()
    
    with(graph) {
        val vertices = koneListOf(
            addVertex(Point2(-50.0f, -50.0f)),
            addVertex(Point2(50.0f, -50.0f)),
            addVertex(Point2(-50.0f, 50.0f)),
            addVertex(Point2(50.0f, 50.0f)),
            addVertex(Point2(150.0f, -50.0f)),
            addVertex(Point2(250.0f, -50.0f)),
            addVertex(Point2(150.0f, 50.0f)),
            addVertex(Point2(250.0f, 50.0f)),
        )
        
        val edges = koneListOf(
            addEdge(vertices[0u], vertices[1u]),
            addEdge(vertices[1u], vertices[2u]),
            addEdge(vertices[2u], vertices[0u]),
            addEdge(vertices[3u], vertices[1u]),
        )
    }
    
    val graphShowWindowComponent: GraphShowWindowComponent =
        RealGraphShowWindowComponent(
            graph = graph,
        )
    
    @OptIn(ExperimentalComposeUiApi::class)
    ComposeViewport(document.body!!) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            val pointerState by graphShowWindowComponent.pointerStateStateFlow.collectAsState()
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
                        is PointerState.CreatingEdge -> false
                    },
                    onCheckedChange = {
                        graphShowWindowComponent.pointerStateStateFlow.update { pointerState ->
                            when (pointerState) {
                                PointerState.Free,
                                is PointerState.Pressed,
                                is PointerState.Clicked,
                                is PointerState.Dragging,
                                is PointerState.CanvasSecondClick -> PointerState.InitializingVertex
                                PointerState.InitializingVertex,
                                is PointerState.CreatingVertex -> PointerState.Free
                                is PointerState.CreatingEdge,
                                PointerState.InitializingEdge -> PointerState.InitializingVertex
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
                        is PointerState.CanvasSecondClick -> false
                        PointerState.InitializingVertex,
                        is PointerState.CreatingVertex -> false
                        PointerState.InitializingEdge,
                        is PointerState.CreatingEdge -> true
                    },
                    onCheckedChange = {
                        graphShowWindowComponent.pointerStateStateFlow.update { pointerState ->
                            when (pointerState) {
                                PointerState.Free,
                                is PointerState.Pressed,
                                is PointerState.Clicked,
                                is PointerState.Dragging,
                                is PointerState.CanvasSecondClick -> PointerState.InitializingEdge
                                PointerState.InitializingVertex,
                                is PointerState.CreatingVertex -> PointerState.InitializingEdge
                                PointerState.InitializingEdge,
                                is PointerState.CreatingEdge -> PointerState.Free
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
                    component = graphShowWindowComponent.graphShowCanvasComponent
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
                            val vertices by graph.verticesStateFlow.collectAsState()
                            LazyColumn(
                                modifier = Modifier.fillMaxHeight().weight(1f),
                                state = scrollState,
                            ) {
                                items(vertices.size.toInt()) {
                                    if (it != 0) Spacer(modifier = Modifier.height(4.dp))
                                    val interactionSource = remember { MutableInteractionSource() }
                                    rememberCoroutineScope().launch {
                                        interactionSource.interactions.collect {
                                            when (it) {
                                                is HoverInteraction.Enter -> {}
                                                is HoverInteraction.Exit -> {}
                                            }
                                        }
                                    }
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .hoverable(interactionSource),
                                        shape = RoundedCornerShape(16.dp),
                                        shadowElevation = 2.dp,
                                    ) {
                                        val vertex = vertices[it.toUInt()]
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                        ) {
                                            Text(
                                                modifier = Modifier.align(Alignment.CenterVertically),
                                                text = "Вершина ${it + 1}"
                                            )
                                            Spacer(modifier = Modifier.weight(1f))
                                            IconButton(
                                                onClick = { vertex.remove() }
                                            ) {
                                                Icon(
                                                    Icons.Default.Delete,
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
                            val edges by graph.edgesStateFlow.collectAsState()
                            LazyColumn(
                                modifier = Modifier.fillMaxHeight().weight(1f),
                                state = scrollState,
                            ) {
                                items(edges.size.toInt()) {
                                    if (it != 0) Spacer(modifier = Modifier.height(4.dp))
                                    val interactionSource = remember { MutableInteractionSource() }
                                    rememberCoroutineScope().launch {
                                        interactionSource.interactions.collect {
                                            when (it) {
                                                is HoverInteraction.Enter -> {}
                                                is HoverInteraction.Exit -> {}
                                            }
                                        }
                                    }
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .hoverable(interactionSource),
                                        shape = RoundedCornerShape(16.dp),
                                        shadowElevation = 2.dp,
                                    ) {
                                        val vertex = edges[it.toUInt()]
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                        ) {
                                            Text(
                                                modifier = Modifier.align(Alignment.CenterVertically),
                                                text = "Ребро ${it + 1}"
                                            )
                                            Spacer(modifier = Modifier.weight(1f))
                                            IconButton(
                                                onClick = { vertex.remove() }
                                            ) {
                                                Icon(
                                                    Icons.Default.Delete,
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