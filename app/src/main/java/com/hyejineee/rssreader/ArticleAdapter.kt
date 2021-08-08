package com.hyejineee.rssreader

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hyejineee.rssreader.databinding.ItemArticlesBinding
import com.hyejineee.rssreader.model.Article

class ArticleAdapter(private val articles:MutableList<Article> = mutableListOf()): RecyclerView.Adapter<ArticleAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(private val binding: ItemArticlesBinding)
        :RecyclerView.ViewHolder(binding.root){

            fun bind(item : Article){
                binding.item = item
                binding.executePendingBindings()
            }

    }

    fun addAll(items:List<Article>){
        articles.addAll(items)
        this.notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder
    = ItemViewHolder(
        ItemArticlesBinding.inflate(LayoutInflater.from(parent.context))
    )

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(articles[position])
    }

    override fun getItemCount(): Int = articles.size
}