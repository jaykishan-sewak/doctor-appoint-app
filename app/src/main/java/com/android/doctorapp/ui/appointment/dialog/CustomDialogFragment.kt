package com.android.doctorapp.ui.appointment.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import com.android.doctorapp.databinding.CustomDialogBinding

class CustomDialogFragment(context: Context, private val listener: OnButtonClickListener) :
    Dialog(context) {

    interface OnButtonClickListener {
        fun oClick(text: String)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = CustomDialogBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)
        binding.btnReasonSubmit.setOnClickListener {
            dismiss()
            listener.oClick(binding.etReason.text.toString())
        }
    }


}