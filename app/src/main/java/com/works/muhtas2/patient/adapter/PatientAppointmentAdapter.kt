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
import com.works.muhtas2.patient.models.PatientAppointmentData

class PatientAppointmentAdapter(
    private val context: Activity,
    private val list: List<PatientAppointmentData>
) : ArrayAdapter<PatientAppointmentData>(context, R.layout.custom_appointment, list) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val rootView = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.custom_appointment, parent, false)

        val rAppName = rootView.findViewById<TextView>(R.id.r_appPName)
        val rAppDate = rootView.findViewById<TextView>(R.id.r_appPDate)
        val rAppHour = rootView.findViewById<TextView>(R.id.r_appPHour)
        val rAppNote = rootView.findViewById<TextView>(R.id.r_appPNote)
        val rAppField = rootView.findViewById<TextView>(R.id.r_appPField)
        val rAppImg = rootView.findViewById<ImageView>(R.id.r_appPImg)

        val appointment = list[position]

        // Usar los nombres de variables correctos según tu modelo `PatientAppointmentData`
        rAppName.text = "${appointment.doctorFirstName ?: "Sin nombre"}"
        rAppDate.text = "Fecha: ${appointment.date ?: "No disponible"}"
        rAppHour.text = "Hora: ${appointment.hour ?: "No disponible"}"
        rAppNote.text = "Nota: ${appointment.note ?: "Sin nota"}"
        rAppField.text = appointment.speciality ?: "Especialidad no registrada"

        // Cargar imagen del doctor con Glide, asegurando placeholders y manejo de errores
        Glide.with(context)
            .load(appointment.doctorImageUrl) // Asegúrate de que `doctorImg` es el nombre correcto
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.ic_dialog_alert)
            .into(rAppImg)

        return rootView
    }
}
