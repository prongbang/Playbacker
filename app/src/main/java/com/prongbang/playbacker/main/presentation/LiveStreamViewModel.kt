package com.prongbang.playbacker.main.presentation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prongbang.playbacker.core.domain.Results
import com.prongbang.playbacker.main.domain.GetLiveStreamListUseCase
import com.prongbang.playbacker.main.domain.LiveStream
import kotlinx.coroutines.launch

class LiveStreamViewModel(
		private val getLiveStreamListUseCase: GetLiveStreamListUseCase
) : ViewModel() {

	val liveStreamListLoading = MutableLiveData<Boolean>()
	val liveStreamListSuccess = MutableLiveData<List<LiveStream>>()
	val liveStreamListFailure = MutableLiveData<String>()

	fun getLiveStreamList() {
		viewModelScope.launch {
			liveStreamListLoading.postValue(true)
			when (val it = getLiveStreamListUseCase.execute()) {
				is Results.Success -> {
					liveStreamListSuccess.postValue(it.value.data.list)
					liveStreamListLoading.postValue(false)
				}
				is Results.Error -> {
					liveStreamListFailure.postValue(it.throwable.message)
					liveStreamListLoading.postValue(false)
				}
			}
		}
	}
}