package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "special_content",
    indices = [
        Index(value = ["topicId"])
    ]
)
data class SpecialContentEntity(
    @PrimaryKey
    val id: String,
    val topicId: String,
    val title: String,
    val subtitle: String,
    val contentBody: String,
    val formulaSheet: String? = null,
    val exampleCount: Int = 0,
    val orderIndex: Int = 0
)
