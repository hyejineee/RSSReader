package com.hyejineee.rssreader.search

import com.hyejineee.rssreader.model.Article
import com.hyejineee.rssreader.model.Feed
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import org.w3c.dom.Element
import org.w3c.dom.Node
import javax.xml.parsers.DocumentBuilderFactory

class Searcher {

    private val feeds = listOf(
        Feed("npr", "https://www.npr.org/rss/rss.php?id=1001"),
        Feed("cnn","http://rss.cnn.com/rss/cnn_topstories.rss"),
        Feed("fox","http://feeds.foxnews.com/foxnews/politics?format=xml"),
//        Feed("inv","httpa://feeds.foxnews.com/foxnews/politics?format=xml"),
    )

    private val dispatcher = newFixedThreadPoolContext(2, name = "IO")

    private val documentFactory = DocumentBuilderFactory.newInstance()

    fun search(query: String): ReceiveChannel<Article> {
        val channel = Channel<Article>(150)

        feeds.forEach {
            GlobalScope.launch(dispatcher) {
                search(it, channel, query)
            }
        }

        return channel
    }

    private suspend fun search(
        feed: Feed,
        channel:SendChannel<Article>,
        query: String
    ){
        val builder = documentFactory.newDocumentBuilder()
        val xml = builder.parse(feed.url)
        val news = xml.getElementsByTagName("channel").item(0)

        (0 until news.childNodes.length)
            .asSequence()
            .map { news.childNodes.item(it) }
            .filter { Node.ELEMENT_NODE == it.nodeType }
            .map{it as Element}
            .filter { "item" == it.tagName }
            .filter {
                it.getElementsByTagName("title").item(0).textContent.contains(query) ||
                        it.getElementsByTagName("description").item(0).textContent.contains(query)
            }
            .map{
                val title = it.getElementsByTagName("title").item(0).textContent
                var summary = it.getElementsByTagName("description").item(0).textContent
                if(summary.contains("<div")){
                    summary = summary.substring(0, summary.indexOf("<div"))
                }
                Article(feed.name, title, summary)
            }
            .toList()
            .forEach {
                channel.send(it)
                ResultCounter.increment()
            }

    }
}


