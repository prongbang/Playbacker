package com.prongbang.playbacker.core.network

import org.koin.dsl.module

val networkModule = module {
	single { LazyServiceGenerator() }
}