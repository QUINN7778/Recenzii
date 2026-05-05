package com.sianov.stepan.utils

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sianov.stepan.data.model.CastMember

class ListConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: String?): List<String> {
        if (value == null) return emptyList()
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }

    @TypeConverter
    fun toStringList(list: List<String>?): String? {
        return gson.toJson(list)
    }

    @TypeConverter
    fun fromCastMemberList(value: String?): List<CastMember> {
        if (value == null) return emptyList()
        val type = object : TypeToken<List<CastMember>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }

    @TypeConverter
    fun toCastMemberList(list: List<CastMember>?): String? {
        return gson.toJson(list)
    }
}
