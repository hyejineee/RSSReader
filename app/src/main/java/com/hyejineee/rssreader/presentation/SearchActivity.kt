package com.hyejineee.rssreader.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.hyejineee.rssreader.ArticleAdapter
import com.hyejineee.rssreader.R
import com.hyejineee.rssreader.databinding.ActivitySearchBinding
import com.hyejineee.rssreader.search.Searcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private val articleAdapter = ArticleAdapter()

    private val searcher = Searcher()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search)

        binding.listSearchAcResult.apply {
            layoutManager = LinearLayoutManager(this@SearchActivity)
            adapter = articleAdapter
        }

        binding.buttonSearchAcSearch.setOnClickListener {
            articleAdapter.clear()
            GlobalScope.launch {
                search()
            }
        }
    }

    private suspend fun search() {
        val query = binding.edittvSearchAcSearch.text.toString()

        val channel = searcher.search(query)

        while (!channel.isClosedForReceive) {
            val article = channel.receive()

            GlobalScope.launch(Dispatchers.Main) {
                articleAdapter.add(article)
            }
        }
    }
}