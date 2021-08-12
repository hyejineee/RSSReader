package com.hyejineee.rssreader

interface ViewType{
    fun get():Int
}

enum class ItemViewType:ViewType {

    ITEM_VIEW {
        override fun get(): Int =1
    },
    LOADING_ITEM_VIEW {
        override fun get(): Int =2
    }

}