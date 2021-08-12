package com.hyejineee.rssreader.model

import com.hyejineee.rssreader.ItemViewType

data class Article(
    val feed: String="",
    val title: String="",
    val summary: String="",
    val viewType: ItemViewType = ItemViewType.ITEM_VIEW,
)
