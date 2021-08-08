package com.hyejineee.rssreader

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.hyejineee.rssreader.databinding.ActivityMainBinding
import com.hyejineee.rssreader.model.Article
import com.hyejineee.rssreader.model.Feed
import kotlinx.coroutines.*
import org.w3c.dom.Element
import org.w3c.dom.Node
import javax.xml.parsers.DocumentBuilderFactory

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
@DelicateCoroutinesApi
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val dispatcher = newFixedThreadPoolContext(2, name = "IO")

    private val documentFactory = DocumentBuilderFactory.newInstance()

    private val feeds = listOf(
        Feed("npr", "https://www.npr.org/rss/rss.php?id=1001"),
        Feed("cnn","http://rss.cnn.com/rss/cnn_topstories.rss"),
        Feed("fox","http://feeds.foxnews.com/foxnews/politics?format=xml"),
        Feed("inv","httpa://feeds.foxnews.com/foxnews/politics?format=xml"),
    )

    private val articleAdapter = ArticleAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.listMainArticles.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = articleAdapter
        }

        asyncLoadNews()

    }

    private fun asyncLoadNews() = GlobalScope.launch{
        val requests = mutableListOf<Deferred<List<Article>>>()
        feeds.mapTo(requests){
            asyncFetchArticles(it, dispatcher)
        }

        requests.forEach { it.join() }

        val articles = requests
            .filter{ !it.isCancelled } // 취소 중이거나 최소됨 상태가 아닌 디퍼드만
            .flatMap { it.getCompleted() }

        val failed = requests.filter{it.isCancelled}.size

        launch(Dispatchers.Main){
           (binding.listMainArticles.adapter as ArticleAdapter).addAll(articles)
            binding.progressMain.visibility = View.GONE
        }
    }

    private fun asyncFetchArticles(feed: Feed, dispatcher: CoroutineDispatcher) =
        GlobalScope.async(dispatcher) {

            delay(1000)
            val builder = documentFactory.newDocumentBuilder()
            val xml = builder.parse(feed.url)
            val news = xml.getElementsByTagName("channel").item(0)

            (0 until news.childNodes.length)
                .asSequence()
                .map { news.childNodes.item(it) }
                .filter { Node.ELEMENT_NODE == it.nodeType }
                .map { it as Element }
                .filter { "item" == it.tagName }
                .map {
                    val title = it.getElementsByTagName("title").item(0).textContent
                    val summary = it.getElementsByTagName("description").item(0).textContent
                    Article(title = title, summary = summary, feed = feed.name)
                }
                .toList()
        }

}