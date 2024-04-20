package com.example.readlocal.popup

import android.app.AlertDialog
import android.content.Context
import android.widget.EditText

fun showEditFileNameDialog(context: Context, fileName: String, onNameUpdated: (String) -> Unit) {
    val editText = EditText(context)
    editText.setText(fileName)
    AlertDialog.Builder(context)
        .setTitle("Edit File Name")
        .setView(editText)
        .setPositiveButton("Save") { dialog, _ ->
            val newName = editText.text.toString()
            onNameUpdated(newName)
            dialog.dismiss()
        }
        .setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        .show()
}
