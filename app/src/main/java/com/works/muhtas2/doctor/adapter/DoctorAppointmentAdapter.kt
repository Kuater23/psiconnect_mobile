package com.works.muhtas2.doctor.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.works.muhtas2.R
import com.works.muhtas2.doctor.models.DoctorAppointmentData

class DoctorAppointmentAdapter(
    private val context: Activity,
    private val list: List<DoctorAppointmentData>
) : ArrayAdapter<DoctorAppointmentData>(context, R.layout.custom_doctor_appointment, list) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val rootView = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.custom_doctor_appointment, parent, false)

        val rAppName = rootView.findViewById<TextView>(R.id.r_appDName)
        val rAppDate = rootView.findViewById<TextView>(R.id.r_appDDate)
        val rAppHour = rootView.findViewById<TextView>(R.id.r_appDHour)
        val rAppNote = rootView.findViewById<TextView>(R.id.r_appDNote)
        val rAppImg = rootView.findViewById<ImageView>(R.id.r_appDImg)

        val appointment = list[position]
        rAppName.text = appointment.patientFirstName + " " + appointment.patientLastName
        rAppDate.text = "Fecha: ${appointment.date}"
        rAppHour.text = "Hora: ${appointment.hour}"
        rAppNote.text = "Nota: ${appointment.note ?: "Sin nota"}"

        // Cargar imagen si está disponible
        appointment.patientImageUrl?.let {
            Glide.with(context)
                .load(appointment.patientImageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery) // Placeholder genérico
                .error(android.R.drawable.ic_dialog_alert) // Imagen si falla la carga
                .into(rAppImg)
        } ?: rAppImg.setImageResource(android.R.drawable.ic_menu_gallery) // Imagen por defecto si no hay URL

        return rootView
    }
}
