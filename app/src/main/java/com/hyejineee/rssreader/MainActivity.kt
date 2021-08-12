package com.hyejineee.rssreader

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hyejineee.rssreader.databinding.ActivityMainBinding
import com.hyejineee.rssreader.model.Article
import com.hyejineee.rssreader.model.Feed
import com.hyejineee.rssreader.producer.ArticleProducer
import kotlinx.coroutines.*
import org.w3c.dom.Element
import org.w3c.dom.Node
import javax.xml.parsers.DocumentBuilderFactory

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