package com.vitorhilarioapps.autoscanner.ui.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.vitorhilarioapps.autoscanner.ui.home.HomeActivity.Companion.assets_path
import com.squareup.picasso.Picasso
import com.vitorhilarioapps.autoscanner.databinding.*

class HomeAdapter(
    private val listData: List<HomeItem>,
    private val actionOpenImagePicker: () -> Unit
    ) : Adapter<ViewHolder>() {

    inner class HeaderHolder(binding: HomeHeaderItemBinding) : ViewHolder(binding.root) {
        fun bindHeader(data: HomeItem.Header) {
            // Nothing to bind yet
        }
    }

    inner class CardHolder(binding: HomeCardItemBinding) : ViewHolder(binding.root) {
        private val tvCardItemTittleInBanner = binding.tvCardItemTittleInBanner
        private val tvCardItemTopic1 = binding.tvCardItemTopic1
        private val tvCardItemTopic2 = binding.tvCardItemTopic2
        private val tvCardItemTopic3 = binding.tvCardItemTopic3
        private val ivCardItemBanner = binding.ivCardItemBanner

        fun bindCard(data: HomeItem.Card) {
            tvCardItemTittleInBanner.text = data.tittle
            tvCardItemTopic1.text = data.topic1
            tvCardItemTopic2.text = data.topic2
            tvCardItemTopic3.text = data.topic3

            Picasso.get().load(assets_path + data.icon).into(ivCardItemBanner)
        }
    }

    inner class FilterListHolder(binding: HomeFilterListItemBinding) : ViewHolder(binding.root) {
        private val rvFilterList = binding.rvFilterList

        fun bindFilterList(data: HomeItem.FilterList) {
            rvFilterList.adapter = FilterListAdapter(data.data)
        }
    }

    inner class AuthorHolder(binding: HomeAuthorItemBinding) : ViewHolder(binding.root) {
        fun bindHelper(data: HomeItem.Author) {
            // Nothing to bind yet
        }
    }

    inner class ImagePickerHolder(binding: HomeImagePickerItemBinding) : ViewHolder(binding.root) {
        private val cvImagePickerContainer = binding.cvImagePickerContainer
        private val ivImagePickerIconCard = binding.ivImagePickerIconCard

        fun bindImagePicker(data: HomeItem.ImagePicker) {
            Picasso.get().load(assets_path + data.icon).into(ivImagePickerIconCard)
            cvImagePickerContainer.setOnClickListener { actionOpenImagePicker() }
        }
    }

    override fun onCreateViewHolder(group: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(group.context)
        return when(viewType) {
            Item.HEADER.id -> HeaderHolder(HomeHeaderItemBinding.inflate(inflater, group, false))
            Item.CARD.id -> CardHolder(HomeCardItemBinding.inflate(inflater, group, false))
            Item.FILTER_LIST.id -> FilterListHolder(HomeFilterListItemBinding.inflate(inflater, group, false))
            Item.AUTHOR.id -> AuthorHolder(HomeAuthorItemBinding.inflate(inflater, group, false))
            Item.IMAGE_PICKER.id -> ImagePickerHolder(HomeImagePickerItemBinding.inflate(inflater, group, false))
            else -> throw IllegalArgumentException("Illegal View Holder")
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = listData[position]
        when(data) {
            is HomeItem.Header -> (holder as HeaderHolder).bindHeader(data)
            is HomeItem.Card -> (holder as CardHolder).bindCard(data)
            is HomeItem.FilterList -> (holder as FilterListHolder).bindFilterList(data)
            is HomeItem.Author -> (holder as AuthorHolder).bindHelper(data)
            is HomeItem.ImagePicker -> (holder as ImagePickerHolder).bindImagePicker(data)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(listData[position]) {
            is HomeItem.Header -> Item.HEADER.id
            is HomeItem.Card -> Item.CARD.id
            is HomeItem.FilterList -> Item.FILTER_LIST.id
            is HomeItem.Author -> Item.AUTHOR.id
            is HomeItem.ImagePicker -> Item.IMAGE_PICKER.id
        }
    }

    override fun getItemCount(): Int = listData.size

    enum class Item(val id: Int) {
        HEADER(1),
        CARD(2),
        FILTER_LIST(3),
        AUTHOR(4),
        IMAGE_PICKER(5)
    }
}