import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Serializable
data class ScoreResponse(
    val player1: String,
    val player2: String,
    val score: List<Int>, // [очки 1, очки 2]
    val sets: List<Int>,  // [партии 1, партии 2]
    val current_set: Int,
    val server: Int,
    val status: String,
    val winner: Int? = null
)

object ApiClient {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    private const val BASE_URL = "http://127.0.0.1:8000/api/match"

    fun startMatch(sport: String, p1: String, p2: String, server: Int, onResult: (ScoreResponse?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response: ScoreResponse = client.post("$BASE_URL/start") {
                    contentType(ContentType.Application.Json)
                    setBody(mapOf("sport" to sport, "player1" to p1, "player2" to p2, "server_index" to server))
                }.body()
                onResult(response)
            } catch (e: Exception) { e.printStackTrace(); onResult(null) }
        }
    }

    fun addPoint(playerIndex: Int, onResult: (ScoreResponse?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response: ScoreResponse = client.post("$BASE_URL/point") {
                    contentType(ContentType.Application.Json)
                    setBody(mapOf("player_index" to playerIndex))
                }.body()
                onResult(response)
            } catch (e: Exception) { e.printStackTrace(); onResult(null) }
        }
    }
}