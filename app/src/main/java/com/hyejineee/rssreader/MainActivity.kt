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

@ObsoleteCoroutinesApi
@DelicateCoroutinesApi
class MainActivity : AppCompatActivity() {

    private lateinit var binding:ActivityMainBinding

    private val dispatcher = newSingleThreadContext(name = "ServiceCall")

    private val documentFactory = DocumentBuilderFactory.newInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        GlobalScope.launch(dispatcher){
            val headlines = fetchHeadlines()

            GlobalScope.launch(Dispatchers.Main){
                binding.tvMainNumberOfProcessing.text = "Found ${headlines.size} News."
            }
        }


    }

    private fun fetchHeadlines(): List<String> {
        val builder = documentFactory.newDocumentBuilder()
        val xml = builder.parse("https://www.npr.org/rss/rss.php?id=1001")
        val news = xml.getElementsByTagName("channel").item(0)

        return (0 until news.childNodes.length)
            .asSequence()
            .map {
                news.childNodes.item(it)
            }
            .filter { Node.ELEMENT_NODE == it.nodeType }
            .map {
                it as Element
            }
            .filter { "item" == it.tagName }
            .map {

                it.getElementsByTagName("title").item(0).textContent

            }
            .toList()
    }
}