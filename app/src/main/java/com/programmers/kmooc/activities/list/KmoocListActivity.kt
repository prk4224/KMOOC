package com.programmers.kmooc.activities.list

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.programmers.kmooc.KmoocApplication
import com.programmers.kmooc.activities.detail.KmoocDetailActivity
import com.programmers.kmooc.databinding.ActivityKmookListBinding
import com.programmers.kmooc.models.Lecture
import com.programmers.kmooc.utils.toVisibility
import com.programmers.kmooc.viewmodels.KmoocListViewModel
import com.programmers.kmooc.viewmodels.KmoocListViewModelFactory

class KmoocListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityKmookListBinding
    private lateinit var viewModel: KmoocListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val kmoocRepository = (application as KmoocApplication).kmoocRepository
        viewModel = ViewModelProvider(this, KmoocListViewModelFactory(kmoocRepository)).get(
            KmoocListViewModel::class.java
        )

        binding = ActivityKmookListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = LecturesAdapter()
            .apply { onClick = this@KmoocListActivity::startDetailActivity }

        binding.lectureList.adapter = adapter

        viewModel.lectureList.observe(this) { lectureList ->
            adapter.updateLectures(lectureList.lectures)
            binding.pullToRefresh.isRefreshing = false
        }

        // pullToRefresh (아래로 당기면) list를 갱신해서 새로고침
        binding.pullToRefresh.setOnRefreshListener {
            viewModel.list()
        }

        // viewModel이 실행되면 progressBar를 제거
        viewModel.progressVisible.observe(this) { visible ->
            binding.progressBar.visibility = visible.toVisibility()
        }

        binding.lectureList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = binding.lectureList.layoutManager

                // progressVisible(다음 데이터)가 있을 경우
                if(viewModel.progressVisible.value != true){
                    val lastVisibleItem = (layoutManager as LinearLayoutManager)
                        .findLastCompletelyVisibleItemPosition()

                    // 마지막에 보여준 아이템의 position이 전체 아이템 보다 5개 모자란 경우
                    // viewModel의 next함수 호출
                    if(layoutManager.itemCount <= lastVisibleItem + 5){
                        viewModel.next()
                    }
                }
            }
        })

        viewModel.list()
    }

    private fun startDetailActivity(lecture: Lecture) {
        startActivity(
            Intent(this, KmoocDetailActivity::class.java)
                .apply { putExtra(KmoocDetailActivity.INTENT_PARAM_COURSE_ID, lecture.id) }
        )
    }
}
