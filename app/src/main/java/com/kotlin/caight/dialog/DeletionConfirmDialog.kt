package com.kotlin.caight.dialog

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import com.kotlin.caight.R
import kotlinx.android.synthetic.main.dialog_deletion_confirm.*

class DeletionConfirmDialog(@param:StringRes private val titleId: Int, private val confirmKey: String, var listener: OnDeletionConfirmListener? = null) : DialogFragment()
{
    interface OnDeletionConfirmListener
    {
        fun onConfirm()
        fun onCancel()
    }

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility", "InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
    {
        val inflater = requireActivity().layoutInflater
        val dialog: AlertDialog = AlertDialog.Builder(activity)
            .setTitle(titleId)
            .setView(inflater.inflate(R.layout.dialog_deletion_confirm, null))
            .setPositiveButton(R.string.act_confirm) { _, _ ->
                if (listener != null)
                {
                    if (confirmKeyEditText!!.text.toString() == confirmKey)
                    {
                        listener!!.onConfirm()
                    }
                }
            }
            .setNegativeButton(R.string.act_cancel) { _, _ ->
                if (listener != null)
                {
                    listener!!.onCancel()
                }
            }
            .setCancelable(true)
            .create()

        /*
         * Initialize GUI Components
         */
        // confirmKeyTextView
        confirmKeyTextView.text = "'$confirmKey'"

        // confirmKeyEditText
        confirmKeyEditText.hint = confirmKey
        return dialog
    }
}