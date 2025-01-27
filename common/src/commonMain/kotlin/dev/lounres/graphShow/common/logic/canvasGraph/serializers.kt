package dev.lounres.graphShow.common.logic.canvasGraph

import dev.lounres.kone.collections.array.KoneUIntArray
import dev.lounres.kone.collections.array.koneUIntArrayOf
import dev.lounres.kone.collections.list.KoneList
import dev.lounres.kone.collections.list.indices
import dev.lounres.kone.collections.map.KoneMapEntry
import dev.lounres.kone.collections.map.associate
import dev.lounres.kone.collections.map.get
import dev.lounres.kone.collections.utils.forEach
import dev.lounres.kone.collections.utils.map
import dev.lounres.kone.collections.utils.withIndex
import dev.lounres.kone.comparison.absoluteEquality
import dev.lounres.kone.computationalGeometry.Point2
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.serializer


internal object PlaneGraphKSerializer : KSerializer<PlaneGraph> {
    private val verticesSerializer = serializer<KoneList<Point2<Float>>>()
    private val edgesSerializer = serializer<KoneList<KoneUIntArray>>()
    
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("PlaneGraph") {
        element("vertices", verticesSerializer.descriptor)
        element("edges", edgesSerializer.descriptor)
    }
    
    override fun serialize(encoder: Encoder, value: PlaneGraph) {
        val vertexIndices = value.vertices.withIndex().associate(absoluteEquality()) { KoneMapEntry(it.value, it.index) }
        encoder.encodeStructure(descriptor) {
            encodeSerializableElement(descriptor, 0, verticesSerializer, value.vertices.map { it.position })
            encodeSerializableElement(
                descriptor = descriptor,
                index = 1,
                serializer = edgesSerializer,
                value = value.edges.map {
                    val (tail, head) = it.ends
                    koneUIntArrayOf(vertexIndices[tail], vertexIndices[head])
                }
            )
        }
    }
    
    @OptIn(ExperimentalSerializationApi::class)
    override fun deserialize(decoder: Decoder): PlaneGraph =
        decoder.decodeStructure(descriptor) {
            val verticesDescriptions: KoneList<Point2<Float>>
            val edgesDescriptions: KoneList<KoneUIntArray>
            if (decodeSequentially()) { // sequential decoding protocol
                verticesDescriptions = decodeSerializableElement(descriptor, 0, verticesSerializer)
                edgesDescriptions = decodeSerializableElement(descriptor, 1, edgesSerializer)
            } else {
                var verticesDescriptionsBuilder: KoneList<Point2<Float>>? = null
                var edgesDescriptionsBuilder: KoneList<KoneUIntArray>? = null
                while (true) {
                    when (val index = decodeElementIndex(descriptor)) {
                        0 -> verticesDescriptionsBuilder = decodeSerializableElement(descriptor, 0, verticesSerializer)
                        1 -> edgesDescriptionsBuilder = decodeSerializableElement(descriptor, 1, edgesSerializer)
                        CompositeDecoder.DECODE_DONE -> break
                        else -> error("Cannot deserialize PlaneGraph: got index $index out of range [0; 2)")
                    }
                }
                verticesDescriptions = verticesDescriptionsBuilder ?: error("Cannot deserialize PlaneGraph: did not receive element with index 0")
                edgesDescriptions = edgesDescriptionsBuilder ?: error("Cannot deserialize PlaneGraph: did not receive element with index 1")
            }
            PlaneGraph().apply {
                val vertices = verticesDescriptions.map { addVertex(it) }
                edgesDescriptions.forEach {
                    require(it.size == 2u) { "Cannot deserialize PlaneGraph: got edge with number of ends different from 2" }
                    val tailIndex = it[0u]
                    val headIndex = it[1u]
                    require(tailIndex in vertices.indices) { "Cannot deserialize PlaneGraph: got edge tail index out of bounds" }
                    require(headIndex in vertices.indices) { "Cannot deserialize PlaneGraph: got edge head index out of bounds" }
                    addEdge(vertices[tailIndex], vertices[headIndex])
                }
            }
        }
}