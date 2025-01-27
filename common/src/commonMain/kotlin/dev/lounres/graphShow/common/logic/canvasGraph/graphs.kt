package dev.lounres.graphShow.common.logic.canvasGraph

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.lounres.kone.collections.iterables.next
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.list.KoneMutableListNode
import dev.lounres.kone.collections.list.emptyKoneList
import dev.lounres.kone.collections.list.implementations.KoneGCLinkedList
import dev.lounres.kone.collections.list.implementations.KoneGCLinkedListProducer
import dev.lounres.kone.collections.list.toKoneList
import dev.lounres.kone.collections.set.KoneMutableLinkedNoddedReifiedSet
import dev.lounres.kone.collections.set.KoneMutableLinkedSetNode
import dev.lounres.kone.collections.set.KoneMutableSetNode
import dev.lounres.kone.collections.set.KoneReifiedSet
import dev.lounres.kone.collections.set.addAllFrom
import dev.lounres.kone.collections.set.buildKoneReifiedSet
import dev.lounres.kone.collections.set.implementations.KoneListBackedMutableLinkedNoddedReifiedSet
import dev.lounres.kone.collections.set.koneMutableReifiedSetOf
import dev.lounres.kone.collections.utils.mapTo
import dev.lounres.kone.comparison.absoluteReifiedEquality
import dev.lounres.kone.computationalGeometry.Point2
import dev.lounres.kone.graphs.EdgeEnds
import dev.lounres.kone.graphs.ReducibleGraph
import dev.lounres.kone.graphs.RemovableGraphEdge
import dev.lounres.kone.graphs.RemovableGraphVertex
import dev.lounres.kone.graphs.minus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlin.js.JsName


public interface PlaneGraphVertex : RemovableGraphVertex<PlaneGraphVertex, PlaneGraphEdge> {
    public var position: Point2<Float>
}

public interface PlaneGraphEdge : RemovableGraphEdge<PlaneGraphVertex, PlaneGraphEdge>

@Serializable(with = PlaneGraphKSerializer::class)
public interface PlaneGraph : ReducibleGraph<PlaneGraphVertex, PlaneGraphEdge> {
    public fun addVertex(point2: Point2<Float>): PlaneGraphVertex
    public fun addEdge(tail: PlaneGraphVertex, head: PlaneGraphVertex): PlaneGraphEdge
    
    public val verticesStateFlow: StateFlow<KoneList<PlaneGraphVertex>>
    public val edgesStateFlow: StateFlow<KoneList<PlaneGraphEdge>>
}

@JsName("newPlaneGraph")
public fun PlaneGraph(): PlaneGraph = PlaneGraphImpl()

internal class PlaneGraphVertexImpl(
    graph: PlaneGraphImpl,
    position: Point2<Float>,
) : PlaneGraphVertex {
    
    private var _graph: PlaneGraphImpl? = graph
    internal val graph: PlaneGraphImpl get() = _graph!!
    
    override var position: Point2<Float> by mutableStateOf(position)
    
    private var _verticesSetNode: KoneMutableLinkedSetNode<PlaneGraphVertexImpl>? = graph.vertices.addNode(this)
    internal var verticesSetNode: KoneMutableLinkedSetNode<PlaneGraphVertexImpl>
        get() = _verticesSetNode!!
        set(value) { _verticesSetNode = value }
    
    private var _outgoingIncidentEdges: KoneGCLinkedList<PlaneGraphEdgeImpl>? = KoneGCLinkedList()
    internal val outgoingIncidentEdges: KoneGCLinkedList<PlaneGraphEdgeImpl> get() = _outgoingIncidentEdges!!
    private var _incomingIncidentEdges: KoneGCLinkedList<PlaneGraphEdgeImpl>? = KoneGCLinkedList()
    internal val incomingIncidentEdges: KoneGCLinkedList<PlaneGraphEdgeImpl> get() = _incomingIncidentEdges!!
    
    override val incidentEdges: KoneReifiedSet<PlaneGraphEdge>
        get() = buildKoneReifiedSet(absoluteReifiedEquality()) {
            addAllFrom(outgoingIncidentEdges)
            addAllFrom(incomingIncidentEdges)
        }
    
    override val adjacentVertices: KoneReifiedSet<PlaneGraphVertex>
        get() = incidentEdges.mapTo(koneMutableReifiedSetOf(absoluteReifiedEquality())) { it.ends - this }
    
    override fun remove() {
        for (edge in outgoingIncidentEdges) edge.detachFromStart()
        outgoingIncidentEdges.dispose()
        _outgoingIncidentEdges = null
        for (edge in incomingIncidentEdges) edge.detachFromEnd()
        incomingIncidentEdges.dispose()
        _incomingIncidentEdges = null
        verticesSetNode.remove()
        _verticesSetNode = null
        _graph!!.updateStates()
    }
}

internal class PlaneGraphEdgeImpl(
    graph: PlaneGraphImpl,
    val start: PlaneGraphVertexImpl,
    val end: PlaneGraphVertexImpl,
) : PlaneGraphEdge {
    
    private var _graph: PlaneGraphImpl? = graph
    
    private var _ends: EdgeEnds<PlaneGraphVertex>? = EdgeEnds(start, end)
    override val ends: EdgeEnds<PlaneGraphVertex> get() = _ends!!
    
    private var startVertexNode: KoneMutableListNode<PlaneGraphEdgeImpl>? = start.outgoingIncidentEdges.addNode(this)
    private var endVertexNode: KoneMutableListNode<PlaneGraphEdgeImpl>? = end.incomingIncidentEdges.addNode(this)
    
    private var edgesSetNode: KoneMutableSetNode<PlaneGraphEdgeImpl>? = graph.edges.addNode(this)
    
    override val adjacentEdges: KoneReifiedSet<PlaneGraphEdge>
        get() = buildKoneReifiedSet(absoluteReifiedEquality()) {
            addAllFrom(start.incidentEdges)
            addAllFrom(end.incidentEdges)
        }
    
    internal fun detachFromStart() {
        startVertexNode = null
        endVertexNode!!.remove()
        endVertexNode = null
        edgesSetNode!!.remove()
        edgesSetNode = null
        _ends = null
    }
    
    internal fun detachFromEnd() {
        startVertexNode!!.remove()
        startVertexNode = null
        endVertexNode = null
        edgesSetNode!!.remove()
        edgesSetNode = null
        _ends = null
    }
    
    override fun remove() {
        startVertexNode!!.remove()
        startVertexNode = null
        endVertexNode!!.remove()
        endVertexNode = null
        edgesSetNode!!.remove()
        edgesSetNode = null
        _ends = null
        _graph!!.updateStates()
    }
}

internal class PlaneGraphImpl : PlaneGraph {
    override val vertices: KoneMutableLinkedNoddedReifiedSet<PlaneGraphVertexImpl> =
        KoneListBackedMutableLinkedNoddedReifiedSet(absoluteReifiedEquality(), KoneGCLinkedListProducer)
    override val edges: KoneMutableLinkedNoddedReifiedSet<PlaneGraphEdgeImpl> =
        KoneListBackedMutableLinkedNoddedReifiedSet(absoluteReifiedEquality(), KoneGCLinkedListProducer)
    
    override fun addVertex(position: Point2<Float>): PlaneGraphVertexImpl = PlaneGraphVertexImpl(this, position).also { updateStates() }
    
    override fun addEdge(tail: PlaneGraphVertex, head: PlaneGraphVertex): PlaneGraphEdgeImpl =
        when {
            tail !is PlaneGraphVertexImpl || tail.graph !== this -> error("Cannot add edge between edges not from this graph")
            head !is PlaneGraphVertexImpl || head.graph !== this -> error("Cannot add edge between edges not from this graph")
            else -> PlaneGraphEdgeImpl(this, tail, head)
        }.also { updateStates() }
    
    private val _verticesStateFlow: MutableStateFlow<KoneList<PlaneGraphVertex>> = MutableStateFlow(emptyKoneList())
    override val verticesStateFlow: StateFlow<KoneList<PlaneGraphVertex>> get() = _verticesStateFlow
    private val _edgesStateFlow: MutableStateFlow<KoneList<PlaneGraphEdge>> = MutableStateFlow(emptyKoneList())
    override val edgesStateFlow: StateFlow<KoneList<PlaneGraphEdge>> get() = _edgesStateFlow
    
    internal fun updateStates() {
        _verticesStateFlow.value = vertices.toKoneList()
        _edgesStateFlow.value = edges.toKoneList()
    }
}