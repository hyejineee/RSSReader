package com.hyejineee.rssreader.presentation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager

import androidx.recyclerview.widget.RecyclerView
import com.hyejineee.rssreader.ArticleAdapter
import com.hyejineee.rssreader.ItemViewType
import com.hyejineee.rssreader.R
import com.hyejineee.rssreader.databinding.ActivityMainBinding
import com.hyejineee.rssreader.producer.ArticleProducer
import kotlinx.coroutines.*

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
@DelicateCoroutinesApi
class MainActivity : AppCompatActivity(), ArticleAdapter.ArticleLoader {

    private lateinit var binding: ActivityMainBinding

    private val articleAdapter = ArticleAdapter(loader = this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.listMainArticles.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = articleAdapter


            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val lastVisiblePosition =
                        (recyclerView.layoutManager as LinearLayoutManager)
                            .findLastCompletelyVisibleItemPosition()

                    val lastItemPosition = articleAdapter.itemCount - 1

                    if (lastItemPosition == lastVisiblePosition &&
                        articleAdapter.getItemViewType(lastItemPosition) != ItemViewType.LOADING_ITEM_VIEW.get()
                    ) {
                        articleAdapter.loading()
                        recyclerView.scrollToPosition(articleAdapter.itemCount-1)

                    }
                }
            })
        }

        GlobalScope.launch{
            loadMore()
        }

    }


    override suspend fun loadMore() {


        val producer = ArticleProducer.producer


        if(!producer.isClosedForReceive){
            val articles = producer.receive()

            GlobalScope.launch(Dispatchers.Main){
                delay(2000)
                articleAdapter.stopLoading()
                articleAdapter.addAll(articles)
            }
        }
    }
}