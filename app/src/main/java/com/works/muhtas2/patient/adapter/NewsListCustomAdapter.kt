package com.works.muhtas2.patient.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.works.muhtas2.R
import com.works.muhtas2.patient.models.NewsData

class NewsListCustomAdapter(
    private val context: Activity,
    private val list: List<NewsData>
) : ArrayAdapter<NewsData>(context, R.layout.news_list, list) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val rootView = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.news_list, parent, false)

        val rNewsTitle = rootView.findViewById<TextView>(R.id.r_NewsTitle)
        val rNewsImg = rootView.findViewById<ImageView>(R.id.r_NewsImg)

        val news = list[position]

        // Manejo seguro de datos nulos
        rNewsTitle.text = news.title ?: "Sin título disponible"

        // Cargar imagen con Glide de forma segura
        Glide.with(context)
            .load(news.img)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.ic_dialog_alert)
            .into(rNewsImg)

        return rootView
    }
}
