package com.prongbang.playbacker.core.domain

sealed class Results<out R> {
	data class Success<out T>(val value: T) : Results<T>()
	data class Error(val throwable: Throwable) : Results<Nothing>()
	object Loading : Results<Nothing>()
}