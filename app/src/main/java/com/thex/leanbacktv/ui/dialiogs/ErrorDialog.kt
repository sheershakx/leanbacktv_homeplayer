package com.thex.leanbacktv.ui.dialiogs

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.thex.leanbacktv.R
import com.thex.leanbacktv.databinding.LayoutDialogErrorBinding

class ErrorDialog() : DialogFragment() {
    lateinit var binding: LayoutDialogErrorBinding
    private lateinit var errorMessage: String
    private lateinit var errorTitle: String
    private lateinit var context: Activity


    constructor(errorTitle: String, errorMessage: String, context: Activity) : this() {
        this.errorTitle = errorTitle
        this.errorMessage = errorMessage
        this.context = context
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = LayoutDialogErrorBinding.inflate(layoutInflater)
        return binding.root

        // return inflater.inflate(R.layout.layout_dialog_error, container)
    }

    override fun getTheme(): Int {
        return R.style.ErrorDialogTheme
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.errorContainer.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.dialog_bg
            )
        )
        binding.errorTitle.text = errorTitle
        binding.errorDesc.text = errorMessage


        binding.errorBtnOk.setOnClickListener {
            dismiss()
            context.finish()


        }
    }


}