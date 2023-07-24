package com.adrian.feriaapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DireccionAdaptador(private val addresses: List<Direccion>) : RecyclerView.Adapter<DireccionAdaptador.AddressViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.direccion_list, parent, false)
        return AddressViewHolder(view)
    }

    override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {
        val address = addresses[position]
        holder.bind(address)
    }

    override fun getItemCount(): Int = addresses.size

    inner class AddressViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textCity: TextView = itemView.findViewById(R.id.ShowCity)
        private val textDistrict: TextView = itemView.findViewById(R.id.ShowDistrict)
        private val textStreet: TextView = itemView.findViewById(R.id.ShowAddres)

        fun bind(address: Direccion) {
            textCity.text = address.ciudad
            textDistrict.text = address.comuna
            textStreet.text = address.direccion
        }
    }

}
