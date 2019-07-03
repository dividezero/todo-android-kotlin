package com.example.haztodo

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import ru.gildor.coroutines.okhttp.await


class RequestService(private val TODOURL: String) {
    private val client = OkHttpClient.Builder().build()
    private val gson = Gson()
    private val JSON = MediaType.parse("application/json; charset=utf-8")

    suspend fun getTodos(): ArrayList<TodoItem> {
        val request = Request.Builder()
            .url(TODOURL)
            .build()

        val response = client.newCall(request).await()
        if (response.code() != 200) {
            throw UnsuccsefulRequestException("Result code ${response.code()}")
        }

        val mapType = object : TypeToken<Map<Int, String>>() {}.type
        if (response.body() != null) {
            val todoMap: Map<Int, String> = gson.fromJson(response.body()!!.string(), mapType)
            return ArrayList(todoMap.map { entry -> fromJSON(entry.value, entry.key) })
        }
        return ArrayList()
    }

    suspend fun putTodo(todo: TodoItem): TodoItem {
        val body = RequestBody.create(JSON, toJSON(todo))
        val request = Request.Builder()
            .url("${TODOURL}/${todo.id}")
            .put(body)
            .build()

        val result = client.newCall(request).await()
        if (result.code() != 200) {
            throw UnsuccsefulRequestException("Result code ${result.code()}")
        }
        return gson.fromJson(result.body()!!.string(), TodoItem::class.java)
    }

    suspend fun deleteTodo(todo: TodoItem): TodoItem {
        val request = Request.Builder()
            .url("${TODOURL}/${todo.id}")
            .delete()
            .build()

        val result = client.newCall(request).await()
        if (result.code() != 200) {
            throw UnsuccsefulRequestException("Result code ${result.code()}")
        }

        return gson.fromJson(result.body()!!.string(), TodoItem::class.java)
    }

    suspend fun createTodo(todo: TodoItem): TodoItem {
        val body = RequestBody.create(JSON, toJSON(todo))
        val request = Request.Builder()
            .url(TODOURL)
            .post(body)
            .build()

        val result = client.newCall(request).await()
        if (result.code() != 200) {
            throw UnsuccsefulRequestException("Result code ${result.code()}")
        }
        return gson.fromJson(result.body()!!.string(), TodoItem::class.java)
    }

    fun toJSON(todo: TodoItem): String {
        return gson.toJson(todo)
    }

    fun fromJSON(json: String, id: Int?): TodoItem {
        val todo: TodoItem = gson.fromJson(json, TodoItem::class.java)
        if (id != null) {
            todo.id = id
        }
        return todo
    }

    class UnsuccsefulRequestException(message: String) : Exception(message)
}