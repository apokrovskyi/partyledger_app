package com.apokrovskyi.partyledger

import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.LayoutRes

class Util {
    companion object {
        fun inflateDialog(inflater: LayoutInflater, @LayoutRes resource: Int): View {
            val result = inflater.inflate(R.layout.dialog_frame, null)
            val container = result.findViewById<FrameLayout>(R.id.dialog_body)
            inflater.inflate(resource, container)
            return result
        }
    }
}