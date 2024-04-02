package com.duyh.exoplayerdemo


import com.google.gson.annotations.SerializedName

data class SongEntity(
    @SerializedName("album")
    val album: String = "",
    @SerializedName("artist")
    val artist: String = "",
    @SerializedName("duration")
    val duration: Int = 0,
    @SerializedName("genre")
    val genre: String = "",
    @SerializedName("id")
    val id: String = "",
    @SerializedName("image")
    val image: String = "",
    @SerializedName("site")
    val site: String = "",
    @SerializedName("source")
    val source: String = "",
    @SerializedName("title")
    val title: String = "",
    @SerializedName("totalTrackCount")
    val totalTrackCount: Int = 0 ,
    @SerializedName("trackNumber")
    val trackNumber: Int = 0
)