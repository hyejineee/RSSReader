package com.hyejineee.rssreader

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import com.hyejineee.rssreader.databinding.ActivityMainBinding
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
        "https://www.npr.org/rss/rss.php?id=1001",
        "http://rss.cnn.com/rss/cnn_topstories.rss",
        "http://feeds.foxnews.com/foxnews/politics?format=xml",
        "httpa://feeds.foxnews.com/foxnews/politics?format=xml",
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        asyncLoadNews()


    }

    private fun asyncLoadNews() = GlobalScope.launch{
        val requests = mutableListOf<Deferred<List<String>>>()
        feeds.mapTo(requests){
            asyncFetchHeadlines(it, dispatcher)
        }

        requests.forEach { it.join() }

        val headlines = requests
            .filter{ !it.isCancelled } // 취소 중이거나 최소됨 상태가 아닌 디퍼드만
            .flatMap { it.getCompleted() }

        val failed = requests.filter{it.isCancelled}.size

        launch(Dispatchers.Main){
            binding.tvMainNumberOfProcessing.text = "Found ${headlines.size} News" +
                    "in ${requests.size} feeds"

            if(failed > 0){
                binding.tvMainFailedNumberOfProcessing.text = "Failed to fetch ${failed} feeds"
            }
        }
    }

    private fun asyncFetchHeadlines(feed: String, dispatcher: CoroutineDispatcher) =
        GlobalScope.async(dispatcher) {
            val builder = documentFactory.newDocumentBuilder()
            val xml = builder.parse(feed)
            val news = xml.getElementsByTagName("channel").item(0)

            (0 until news.childNodes.length)
                .asSequence()
                .map { news.childNodes.item(it) }
                .filter { Node.ELEMENT_NODE == it.nodeType }
                .map { it as Element }
                .filter { "item" == it.tagName }
                .map {
                    it.getElementsByTagName("title").item(0).textContent
                }
                .toList()
        }

}