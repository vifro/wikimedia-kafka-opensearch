package extentedapi.streams.processor

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.Materialized
import java.io.IOException


class BotCountStreamBuilder(
    private val inputStream: KStream<String, String>
) : StreamBuilder {
    companion object {
        private const val BOT_COUNT_STORE = "bot-count-store"
        private const val BOT_COUNT_TOPIC = "wikimedia.stats.bots"
        private val OBJECT_MAPPER = ObjectMapper()
    }

    override fun setup() {
        inputStream
            .mapValues { changeJson ->
                try {
                    val jsonNode = OBJECT_MAPPER.readTree(changeJson)
                    if (jsonNode.get("bot")?.asBoolean() == true) "bot" else "non-bot"
                } catch (e: IOException) {
                    "parse-error"
                }
            }
            .groupBy { _, value -> value }
            .count()
            .toStream()
            .mapValues { key, value -> "$key: $value" }
            .to(BOT_COUNT_TOPIC)
    }
}
