package dto

import com.google.gson.Gson

data class UserRequest(
    val id: Int? = null,
    val name: String? = null,
    val age: Int? = null
) {
    fun toQueryStringMap(): Map<String, String> = buildMap {
        this@UserRequest.let { request ->
            request.id?.let { put("id", it.toString()) }
            request.name?.let { put("id", it) }
            request.age?.let { put("id", it.toString()) }
        }
    }

    fun toJsonBody(): String = Gson().toJson(this)
}
