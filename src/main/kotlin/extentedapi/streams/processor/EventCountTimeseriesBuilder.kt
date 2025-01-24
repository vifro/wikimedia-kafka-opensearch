package extentedapi.streams.processor

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.kstream.*
import java.time.Duration

class EventCountTimeseriesBuilder(
    private val inputStream: KStream<String, String>
) : StreamBuilder  {
    companion object {
        private const val TIMESERIES_TOPIC = "wikimedia.stats.timeseries"
        private const val TIMESERIES_STORE = "event-count-store"
        private val OBJECT_MAPPER = ObjectMapper()
    }

    override fun setup() {
        val timeWindows = TimeWindows.ofSizeWithNoGrace(Duration.ofSeconds(10))
        inputStream
            .selectKey { _, _ -> "key-to-group" }
            .groupByKey()
            .windowedBy(timeWindows)
            .count(Materialized.`as`(TIMESERIES_STORE))
            .toStream()
            .mapValues { readOnlyKey, value ->
                val kvMap = mapOf(
                    "start_time" to readOnlyKey.window().startTime().toString(),
                    "end_time" to readOnlyKey.window().endTime().toString(),
                    "window_size" to timeWindows.size(),
                    "event_count" to value
                )
                try {
                    OBJECT_MAPPER.writeValueAsString(kvMap)
                } catch (e: JsonProcessingException) {
                    null
                }
            }
            .to(
                TIMESERIES_TOPIC, Produced.with(
                    WindowedSerdes.timeWindowedSerdeFrom(String::class.java, timeWindows.size()),
                    Serdes.String()
                )
            )
    }
}