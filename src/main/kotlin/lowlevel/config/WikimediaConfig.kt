package lowlevel.config

data class WikimediaConfig(
    val wikimediaStreamUrl: String = System.getenv("WIKIMEDIA_STREAM_URL")
        ?: "https://stream.wikimedia.org/v2/stream/recentchange"
)