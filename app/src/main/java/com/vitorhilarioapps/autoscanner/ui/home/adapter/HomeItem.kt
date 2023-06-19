package com.vitorhilarioapps.autoscanner.ui.home.adapter

import com.vitorhilarioapps.autoscanner.ui.scanner.model.Filters

sealed class HomeItem {
    data class Header(val tittle: String, val icon: String) : HomeItem()
    data class Card(val tittle: String, val topic1: String, val topic2: String, val topic3: String, val icon: String) : HomeItem()
    data class FilterList(val data: List<Filters>) : HomeItem()
    data class Author(val tittle: String) : HomeItem()
    data class ImagePicker(val icon: String) : HomeItem()
}