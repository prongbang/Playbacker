package com.prongbang.playbacker.main.presentation

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.prongbang.playbacker.R
import com.prongbang.playbacker.player.presentation.PlayerActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

	private val liveStreamViewModel: LiveStreamViewModel by viewModel()
	private val liveStreamAdapter: LiveStreamAdapter by inject()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		initView()
		initLoad()
	}

	private fun initView() {
		recyclerView.apply {
			layoutManager = LinearLayoutManager(this@MainActivity)
			adapter = liveStreamAdapter
		}

		liveStreamAdapter.apply {
			onItemClick = {
				PlayerActivity.navigate(this@MainActivity, it)
			}
		}

		swipeRefresh.setOnRefreshListener {
			swipeRefresh.isRefreshing = false
			initLoad()
		}
	}

	private fun initLoad() {
		getLiveStreamList()
	}

	private fun getLiveStreamList() {
		liveStreamViewModel.apply {
			getLiveStreamList()

			liveStreamListLoading.observe(this@MainActivity, Observer {
				progressBar.visibility = if (it) View.VISIBLE else View.GONE
			})

			liveStreamListSuccess.observe(this@MainActivity, Observer {
				liveStreamAdapter.submitList(it)
			})

			liveStreamListFailure.observe(this@MainActivity, Observer {
				Toast.makeText(this@MainActivity, it, Toast.LENGTH_SHORT)
						.show()
			})
		}
	}

}