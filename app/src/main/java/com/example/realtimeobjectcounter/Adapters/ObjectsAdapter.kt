package com.example.realtimeobjectcounter.Adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.realtimeobjectcounter.Models.ObjectsModel
import com.example.realtimeobjectcounter.databinding.RvObjectsItemBinding

class ObjectsAdapter : ListAdapter<ObjectsModel, ObjectsAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RvObjectsItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bindObjectsData(currentItem)
    }

    inner class ViewHolder(private val binding: RvObjectsItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bindObjectsData(currentItem: ObjectsModel?) {
            binding.apply {
                currentItem?.let { currentItem ->
                    Glide.with(imgObj).load(currentItem.image).into(imgObj)
                    tvObjName.text = currentItem.name
                    tvObjCount.text = "${currentItem.count} objects"
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ObjectsModel>() {
        override fun areItemsTheSame(oldItem: ObjectsModel, newItem: ObjectsModel) =
            oldItem.key == newItem.key

        override fun areContentsTheSame(oldItem: ObjectsModel, newItem: ObjectsModel) =
            oldItem == newItem
    }
}