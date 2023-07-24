package com.adrian.feriaapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BoletaAdapter(private val direcciones: List<Direccion>) :
    RecyclerView.Adapter<BoletaAdapter.DireccionViewHolder>() {

    private var direccionSelectedListener: OnDireccionSelectedListener? = null
    private var direccionElegida: Direccion? = null
    var selectedDireccionId: Int? = null

    fun setOnDireccionSelectedListener(listener: OnDireccionSelectedListener) {
        direccionSelectedListener = listener


    }
    fun obtenerDireccionElegida(): Direccion? {
        return direccionElegida
    }
    interface OnDireccionSelectedListener {
        fun onDireccionSelected(direccion: Direccion) {

        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DireccionViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.list_item_direccion, parent, false)
        return DireccionViewHolder(view)
    }

    override fun onBindViewHolder(holder: DireccionViewHolder, position: Int) {
        val direccion = direcciones[position]
        holder.bind(direccion)
        holder.choiceBtn.setOnClickListener {
            direccionSelectedListener?.onDireccionSelected(direccion)
        }
    }

    override fun getItemCount(): Int {
        return direcciones.size
    }

    private var selectedPosition = -1
    private var selectedDireccion: Direccion? = null

    inner class DireccionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewDireccion: TextView = itemView.findViewById(R.id.boletadireccion)
        private val textViewComuna: TextView = itemView.findViewById(R.id.boletaComuna)
        val choiceBtn: RadioButton = itemView.findViewById(R.id.choiceBtn)

        fun bind(direccion: Direccion) {
            textViewDireccion.text = direccion.direccion
            textViewComuna.text = direccion.comuna
            val idDireccion = direccion.id
            choiceBtn.isChecked = direccion == selectedDireccion

            itemView.setOnClickListener {
                itemView.setOnClickListener {
                    selectedDireccion = direccion
                    selectedDireccionId = direccion.id // Asignar el ID de la dirección seleccionada
                    notifyDataSetChanged()
                }
            }
    }

    private fun selectDireccion(direccion: Direccion, position: Int) {
        selectedDireccion = direccion
        selectedPosition = position
        notifyDataSetChanged()
        direccionSelectedListener?.onDireccionSelected(direccion)
    }


    }
}