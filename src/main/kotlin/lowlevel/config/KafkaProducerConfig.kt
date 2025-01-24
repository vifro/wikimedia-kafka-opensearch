import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import java.util.*


data class KafkaProducerConfig(
    val bootstrapServers: String = System.getenv("KAFKA_BOOTSTRAP_SERVERS") ?: "127.0.0.1:9092",
    val schemaRegistryUrl: String = System.getenv("SCHEMA_REGISTRY_URL") ?: "http://schema-registry:8081"
) {
    fun toProperties() = Properties().apply {
        setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
        setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.qualifiedName)
        setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer::class.qualifiedName)
        val kafkaVersion: Int = org.apache.kafka.common.utils.AppInfoParser.getVersion().split(".")[0].toInt()
        if (kafkaVersion < 3) {
            // Create Idempotent Producer by adding configs missing in earlier versions.
            setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true")
            setProperty(ProducerConfig.ACKS_CONFIG, "all")
            setProperty(ProducerConfig.RETRIES_CONFIG, Int.MAX_VALUE.toString())
        }

        /**
         *  High throughput producer, at expense of a bit of latency and CPU.
         *      * linger.ms: default 0, but increase to 5 to add more messages to batch, at cost of latency of course
         *      * batch.size:  If batch Filled before linger.ms, increase the batch size. Default 16 kB.
         *      * Compression: Snappy , good for text and json.
         */
        setProperty(ProducerConfig.LINGER_MS_CONFIG, "20")
        setProperty(ProducerConfig.BATCH_SIZE_CONFIG, (1024 * 32).toString())
        setProperty(ProducerConfig.COMPRESSION_TYPE_CONFIG, CompressionType.SNAPPY.toString())


        // Partitioner kafka >= 2.4 = sticky partitioner (a lot lower latency, increasing by larger partition)
        // buffer.memory = 32MB standard. If filled buffer, send() will be blocked.
        // max.block.ms=60000, send() block. Throws error after 60 seconds.
    }
}

enum class CompressionType {
    // Best practise to set compression level at the producer level. Which is the default.
    PRODUCER, NONE, LZ4, SNAPPY;

    override fun toString(): String = when (this) {
        PRODUCER -> "producer"
        NONE -> "none"
        LZ4 -> "lz4"
        SNAPPY -> "snappy"
    }
}