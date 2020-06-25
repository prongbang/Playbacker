package com.prongbang.playbacker.main.presentation

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.prongbang.playbacker.main.domain.LiveStream

class LiveStreamAdapter : ListAdapter<LiveStream, LiveStreamViewHolder>(DIFF_COMPARATOR) {

	var onItemClick: (LiveStream) -> Unit = {}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LiveStreamViewHolder {
		return LiveStreamViewHolder.newInstance(parent)
	}

	override fun onBindViewHolder(holder: LiveStreamViewHolder, position: Int) {
		holder.bind(getItem(position), onItemClick)
	}

	companion object {
		private val DIFF_COMPARATOR = object : DiffUtil.ItemCallback<LiveStream>() {
			override fun areItemsTheSame(oldItem: LiveStream, newItem: LiveStream): Boolean =
					oldItem == newItem

			override fun areContentsTheSame(oldItem: LiveStream, newItem: LiveStream): Boolean =
					oldItem.id == newItem.id
		}
	}

}