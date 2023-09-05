package http

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dto.UserRequest
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

private val client = OkHttpClient.Builder().build()

private const val BASE_URL = "http://localhost:8080"

private val gson = Gson()
private val mapType = object : TypeToken<Map<String, String>>() {}.type

// Json 문자열을 Map 으로 변환
private fun toMap(bodyString: String): Map<String, String>  = gson.fromJson(bodyString, mapType)

fun createRequest(url: String, method: String, body: RequestBody? = null): Request {

    val builder = Request.Builder()
        .url(url)

    when (method.uppercase()) {
        "GET" -> builder.get()
        "POST" -> body?.let { builder.post(it) }
    }

    return builder.build()
}

private fun baseCallV1(uri: String, method: String, body: RequestBody? = null, callback: (response: Response) -> Unit) {
    val request = createRequest("$BASE_URL$uri", method, body)

    Thread {
        client.newCall(request).execute().use(callback)
    }.start()
}

fun callSaveUserV1() {
    baseCallV1("/users", "POST", UserRequest(name = "hong", age = 25).toJsonBody().toRequestBody("application/json".toMediaType())) {
        it?.body?.string()?.let { bosyString ->
            val body: Map<String, String> = toMap(bosyString)

            callGetAgeV1(body["id"]?.toInt() ?: throw IllegalArgumentException("id is null"))
        }
    }
}

fun callGetAgeV1(id: Int) {
    baseCallV1("/users/age?id=$id", "GET") {
        it?.body?.string()?.let { bosyString ->
            val body: Map<String, String> = toMap(bosyString)

            println("result: ${body["age"]}")
        }
    }
}

private fun baseCallV2(uri: String, method: String, body: RequestBody? = null): Response {
    val request = createRequest("$BASE_URL$uri", method, body)
    return client.newCall(request).execute()
}

fun callSaveUserV2(): Map<String, String>  {
    val response = baseCallV2("/users", "POST", UserRequest(name = "hong", age = 25).toJsonBody().toRequestBody("application/json".toMediaType()))
    return response.body?.string()?.let { toMap(it) } ?: emptyMap()
}

fun callGetAgeV2(id: Int): Map<String, String> {
    val response = baseCallV2("/users/age?id=$id", "GET")
    return response.body?.string()?.let { toMap(it) } ?: emptyMap()
}

//suspend fun main(): Unit = runBlocking {
//    val saveUserApiResponse = async(Dispatchers.IO) { callSaveUserV2() }
//    val getAgeApiResponse = async { callGetAgeV2(saveUserApiResponse.await()["id"]?.toInt() ?: throw IllegalArgumentException("id is null")) }
//
//    println("result: ${getAgeApiResponse.await()["age"]}")
//}

private suspend fun baseCallV3(uri: String, method: String, body: RequestBody? = null): Response = CoroutineScope(Dispatchers.IO).async {
    val request = createRequest("$BASE_URL$uri", method, body)
    client.newCall(request).execute()
}.await()

suspend fun callSaveUserV3(): Map<String, String> = coroutineScope {
    async {
        val response = baseCallV3("/users", "POST", UserRequest(name = "hong", age = 25).toJsonBody().toRequestBody("application/json".toMediaType()))
        response.body?.string()?.let { toMap(it) } ?: emptyMap()
    }.await()
}

suspend fun callGetAgeV3(id: Int): Map<String, String> = coroutineScope {
    async {
        val response = baseCallV3("/users/age?id=$id", "GET")
        response.body?.string()?.let { toMap(it) } ?: emptyMap()
    }.await()
}

fun main(): Unit = runBlocking {
    val saveUserApiResponse: Map<String, String> = callSaveUserV3()
    val getAgeApiResponse = callGetAgeV3(saveUserApiResponse["id"]?.toInt() ?: throw IllegalArgumentException("id is null"))

    println("result: ${getAgeApiResponse["age"]}")
}