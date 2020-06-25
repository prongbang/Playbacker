package com.prongbang.playbacker.main.domain


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class LiveStream(
		@SerializedName("address")
		val address: String = "",
		@SerializedName("avatar")
		val avatar: String = "",
		@SerializedName("channelId")
		val channelId: String = "",
		@SerializedName("channelName")
		val channelName: String = "",
		@SerializedName("createAt")
		val createAt: String = "",
		@SerializedName("firstName")
		val firstName: String = "",
		@SerializedName("hls")
		val hls: String = "",
		@SerializedName("hlsPlayback")
		val hlsPlayback: String = "",
		@SerializedName("id")
		val id: String = "",
		@SerializedName("isLive")
		val isLive: Boolean = false,
		@SerializedName("lastName")
		val lastName: String = "",
		@SerializedName("location")
		val location: String = "",
		@SerializedName("mpeg")
		val mpeg: String = "",
		@SerializedName("mqttSubscribeTopic")
		val mqttSubscribeTopic: String = "",
		@SerializedName("mqttTcp")
		val mqttTcp: String = "",
		@SerializedName("mqttWs")
		val mqttWs: String = "",
		@SerializedName("rtmp")
		val rtmp: String = "",
		@SerializedName("rtsp")
		val rtsp: String = "",
		@SerializedName("streamName")
		val streamName: String = "",
		@SerializedName("thumbnail")
		val thumbnail: String = "",
		@SerializedName("updateAt")
		val updateAt: String = "",
		@SerializedName("userId")
		val userId: String = "",
		@SerializedName("views")
		val views: Int = 0
) : Parcelable {
	val isLiving get() = if (isLive) "LIVE" else ""
	val locationName get() = if (location.isEmpty()) "Unknown Location" else location
	val fullName get() = "$firstName $lastName"
	val liveUrl get() = if (isLive) hls else hlsPlayback
	val useController get() = !isLive
}