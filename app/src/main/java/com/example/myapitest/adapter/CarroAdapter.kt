package com.example.myapitest.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapitest.R
import com.example.myapitest.model.Carro
import com.example.myapitest.model.CarroValor
import com.example.myapitest.ui.loadUrl

class CarroAdapter (
    private val carros: List<CarroValor>,
    private val itemClickListener: (CarroValor) -> Unit
) : RecyclerView.Adapter<CarroAdapter.ItemViewHolder>() {

    class ItemViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.image)
        val modelView: TextView = view.findViewById(R.id.model)
        val yearView: TextView = view.findViewById(R.id.year)
        val licenseView: TextView = view.findViewById(R.id.license)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_car_layout, parent, false)
        return ItemViewHolder(view)
    }

    override fun getItemCount(): Int = carros.size

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val carro = carros[position]
        holder.itemView.setOnClickListener {
            itemClickListener.invoke(carro)
        }

        holder.modelView.text = carro.name
        holder.yearView.text = carro.year
        holder.licenseView.text = carro.licence

        holder.imageView.loadUrl(carro.imageUrl)
        /*
        Picasso.get()
            .load(carro.imageUrl)
            .placeholder(R.drawable.ic_cloud_download)
            .error(R.drawable.ic_error)
            .transform(CircleTransform())
            .into(holder.imageView)
         */
    }
}