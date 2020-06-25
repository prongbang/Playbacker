package com.prongbang.playbacker.core.domain

import com.google.gson.annotations.SerializedName

class Data<T>(
		@SerializedName("count")
		val count: Int = 0,
		@SerializedName("limit")
		val limit: Int = 0,
		@SerializedName("list")
		val list: List<T> = listOf(),
		@SerializedName("page")
		val page: Int = 0,
		@SerializedName("size")
		val size: Int = 0
)