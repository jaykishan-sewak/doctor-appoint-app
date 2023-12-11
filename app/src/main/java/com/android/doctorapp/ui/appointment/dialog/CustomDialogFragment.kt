package com.android.doctorapp.ui.appointment.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.widget.doAfterTextChanged
import com.android.doctorapp.R
import com.android.doctorapp.databinding.CustomDialogBinding

class CustomDialogFragment(
    var isDarkTheme: Boolean?,
    context: Context,
    private val listener: OnButtonClickListener
) :
    Dialog(context) {

    interface OnButtonClickListener {
        fun oClick(text: String)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = CustomDialogBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)
        binding.btnReasonSubmit.isEnabled = false
        if (isDarkTheme == true) {
            binding.btnReasonSubmit.setTextColor(context.getColor(R.color.black))
        } else
            binding.btnReasonSubmit.setTextColor(context.getColor(R.color.white))

        binding.etReason.doAfterTextChanged {

            binding.btnReasonSubmit.isEnabled =
                binding.etReason.text?.isNotEmpty() == true && isValidReason(binding)
            if (binding.btnReasonSubmit.isEnabled) {
                binding.btnReasonSubmit.setTextColor(context.getColor(R.color.white))
                binding.btnReasonSubmit.setOnClickListener {
                    dismiss()
                    listener.oClick(binding.etReason.text.toString())
                }
            } else {
                if (isDarkTheme == true) {
                    binding.btnReasonSubmit.setTextColor(context.getColor(R.color.black))
                } else
                    binding.btnReasonSubmit.setTextColor(context.getColor(R.color.white))
            }
        }
    }

    private fun isValidReason(binding: CustomDialogBinding): Boolean {
        return if (binding.etReason.text?.toString()
                .isNullOrEmpty() || ((binding.etReason.text?.toString()?.length ?: 0) < 5)
        ) {
            binding.etReason.error = context.getString(R.string.enter_valid_Reason)
            false
        } else if (binding.etReason.text?.get(0)?.isLetter() != true) {
            binding.etReason.error = context.getString(R.string.enter_valid_Reason)
            false
        } else {
            binding.etReason.error = null
            binding.btnReasonSubmit.setTextColor(context.getColor(R.color.white))
            true
        }
    }

}