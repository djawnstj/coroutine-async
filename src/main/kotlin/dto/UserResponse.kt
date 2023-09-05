package dto


import com.google.gson.annotations.SerializedName

data class UserResponse(
    @SerializedName("code")
    val code: Int,
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("age")
    val age: Int? = null
) {
    override fun toString(): String {
        return "UserResponse(code=$code${if (id != null) ", id=$id" else ""}${if (name != null) ", name=$name" else ""}${if (age != null) ", age=$age" else ""})"
    }
}