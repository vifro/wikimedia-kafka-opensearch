package extentedapi.streams.processor

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.kstream.*
import java.io.IOException
import java.time.Duration

class WebsiteCountStreamBuilder(
    private val inputStream: KStream<String, String>
) : StreamBuilder {
    companion object {
        private const val WEBSITE_COUNT_STORE = "website-count-store"
        private const val WEBSITE_COUNT_TOPIC = "wikimedia.stats.website"
        private val OBJECT_MAPPER = ObjectMapper()
    }

    override fun setup() {
        val timeWindows = TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(1L))
        inputStream
            .selectKey { _, changeJson ->
                try {
                    val jsonNode = OBJECT_MAPPER.readTree(changeJson)
                    jsonNode.get("server_name")?.asText() ?: "unknown"
                } catch (e: IOException) {
                    "parse-error"
                }
            }
            .groupByKey()
            .windowedBy(timeWindows)
            .count(Materialized.`as`(WEBSITE_COUNT_STORE))
            .toStream()
            .mapValues { key, value ->
                val kvMap = mapOf(
                    "website" to key.key(),
                    "count" to value
                )
                try {
                    OBJECT_MAPPER.writeValueAsString(kvMap)
                } catch (e: JsonProcessingException) {
                    null
                }
            }
            .to(
                WEBSITE_COUNT_TOPIC, Produced.with(
                    WindowedSerdes.timeWindowedSerdeFrom(String::class.java, timeWindows.size()),
                    Serdes.String()
                )
            )
    }
}