package com.uclan.remotecamera.androidApp.utility

import android.app.AlertDialog
import android.content.Context

class GenericAlert {
    fun create(context: Context, title: String, msg: String): AlertDialog {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setMessage(msg)

        builder.setPositiveButton("Okay") { dialog, _ ->
            dialog.dismiss()
        }

        return builder.create()
    }
}