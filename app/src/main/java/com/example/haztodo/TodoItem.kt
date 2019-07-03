package com.example.haztodo

import java.util.*

data class TodoItem(var id: Int?, val createdDate: Date = Date(), var description: String = "")