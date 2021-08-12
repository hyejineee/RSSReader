package com.hyejineee.rssreader.producer

import com.hyejineee.rssreader.model.Article
import com.hyejineee.rssreader.model.Feed
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.newFixedThreadPoolContext
import org.w3c.dom.Element
import org.w3c.dom.Node
import javax.xml.parsers.DocumentBuilderFactory

object ArticleProducer {
    private val feeds = listOf(
        Feed("npr", "https://www.npr.org/rss/rss.php?id=1001"),
        Feed("cnn","http://rss.cnn.com/rss/cnn_topstories.rss"),
        Feed("fox","http://feeds.foxnews.com/foxnews/politics?format=xml"),
        Feed("inv","httpa://feeds.foxnews.com/foxnews/politics?format=xml"),
    )

    private val dispatcher = newFixedThreadPoolContext(2, name = "IO")

    private val documentFactory = DocumentBuilderFactory.newInstance()

    private fun fetchArticles(feed: Feed):List<Article> {


        val builder = documentFactory.newDocumentBuilder()
        val xml = builder.parse(feed.url)
        val news = xml.getElementsByTagName("channel").item(0)

        return (0 until news.childNodes.length)
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
            .take(10)
    }

    val producer = GlobalScope.produce {
        feeds.forEach{
            send(fetchArticles(it))
        }
    }


}