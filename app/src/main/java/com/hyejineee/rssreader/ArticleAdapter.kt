package com.hyejineee.rssreader

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hyejineee.rssreader.databinding.ItemArticlesBinding
import com.hyejineee.rssreader.model.Article
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ArticleAdapter(
    private val articles: MutableList<Article> = mutableListOf(),
    private val loader: ArticleLoader
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var loading = false

    inner class ItemViewHolder(private val binding: ItemArticlesBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Article) {
            binding.item = item
            binding.executePendingBindings()
        }
    }

    inner class LoadingItemViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
    }

    interface ArticleLoader {
        suspend fun loadMore()
    }

    fun addAll(items: List<Article>) {
        articles.addAll(items)
        this.notifyDataSetChanged()
    }

    fun loading() {
        loading = true
        articles.add(
            Article(viewType = ItemViewType.LOADING_ITEM_VIEW)
        )

        notifyItemInserted(articles.size - 1)

        GlobalScope.launch {
            loader.loadMore()
        }
    }

    fun stopLoading() {
        loading = false
        if (articles.size > 0 &&
            articles.last().viewType == ItemViewType.LOADING_ITEM_VIEW
        ) {
            articles.removeLast()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            ItemViewType.ITEM_VIEW.get() -> ItemViewHolder(
                ItemArticlesBinding.inflate(LayoutInflater.from(parent.context))
            )
            else -> LoadingItemViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.loading_item_articles, parent, false)
            )
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (holder is ItemViewHolder) {
            holder.bind(articles[position])
        }

    }

    override fun getItemCount(): Int = articles.size

    override fun getItemViewType(position: Int): Int = articles[position].viewType.get()

}