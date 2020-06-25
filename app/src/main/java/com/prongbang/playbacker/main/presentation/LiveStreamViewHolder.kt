package com.prongbang.playbacker.main.presentation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.prongbang.playbacker.databinding.ItemLiveStreamBinding
import com.prongbang.playbacker.main.domain.LiveStream

class LiveStreamViewHolder(
		private val binding: ItemLiveStreamBinding
) : RecyclerView.ViewHolder(binding.root) {

	fun bind(item: LiveStream, onItemClick: (LiveStream) -> Unit) {
		binding.apply {
			data = item
			executePendingBindings()
		}

		itemView.setOnClickListener {
			onItemClick.invoke(item)
		}
	}

	companion object {
		fun newInstance(parent: ViewGroup): LiveStreamViewHolder {
			val binding = ItemLiveStreamBinding.inflate(LayoutInflater.from(parent.context), parent,
					false)
			return LiveStreamViewHolder(binding)
		}
	}
}