package com.example.haztodo

import android.app.Activity
import android.view.inputmethod.InputMethodManager


object Utils {
    fun closeKeyboard(activity: Activity) {
        val inputManager = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        activity.currentFocus?.let {
            inputManager.hideSoftInputFromWindow(
                it.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        }
    }

    fun openKeyboard(activity: Activity) {
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm?.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)
    }
}