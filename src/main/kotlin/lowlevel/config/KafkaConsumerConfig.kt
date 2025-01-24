import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerInterceptor
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import java.util.*


data class KafkaConsumerConfig(
    val bootstrapServers: String = System.getenv("KAFKA_BOOTSTRAP_SERVERS") ?: "127.0.0.1:9092",
    val groupId: String = "wikimedia-kafka-opensearch-1",
) {
    fun toProperties() = Properties().apply {
        setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
        setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.qualifiedName)
        setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.qualifiedName)
        setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId)
        setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest")

        // This means that you have to handle commit manually ( i.e  Batch programmatically)
        setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false")
    }
}