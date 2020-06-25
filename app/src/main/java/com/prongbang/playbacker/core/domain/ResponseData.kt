package com.prongbang.playbacker.core.domain

import com.google.gson.annotations.SerializedName

class ResponseData<T>(
		@SerializedName("code")
		val code: Int = 0,
		@SerializedName("data")
		val `data`: Data<T> = Data(),
		@SerializedName("message")
		val message: String = ""
)