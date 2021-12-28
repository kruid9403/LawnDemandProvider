package com.jeremykruid.lawndemandprovider.model.adapter

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jeremykruid.lawndemandprovider.R

class StartJobPhotoAdapter(private val context: Context, private val photoList: MutableList<Bitmap>,
                           private val action: PhotoClicked):
    RecyclerView.Adapter<StartJobPhotoAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        fun bind(bitmap: Bitmap, position: Int){
            Glide.with(context).load(bitmap).into(itemView.findViewById(R.id.start_job_item_image))
            itemView.findViewById<ImageView>(R.id.start_job_item_image).setOnClickListener {
                action.photoClicked(bitmap, position)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context)
                .inflate(R.layout.start_job_photo_recycler_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val photo = photoList[position]
        holder.bind(photo, position)
    }

    override fun getItemCount(): Int {
        return photoList.size
    }

    interface PhotoClicked{
        fun photoClicked(bitmap: Bitmap, position: Int)
    }
}