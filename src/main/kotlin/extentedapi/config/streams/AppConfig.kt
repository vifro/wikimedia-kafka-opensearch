package extentedapi.config.streams

data class AppConfig(
    val inputTopic: String,
    val bootstrapURL: String,
    val appId: String
)