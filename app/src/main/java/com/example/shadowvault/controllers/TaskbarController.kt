package com.example.shadowvault

import android.view.View
import android.widget.TextView

class TaskbarController(
    private val topTaskbar: View,
    private val bottomTaskbar: View,
    private val selectedCountText: TextView
) {

    fun show(selectedCount: Int) {
        selectedCountText.text = "$selectedCount selected"

        // Top
        if (topTaskbar.visibility != View.VISIBLE) {
            topTaskbar.visibility = View.VISIBLE
            topTaskbar.post {
                topTaskbar.translationY = -topTaskbar.height.toFloat()
                topTaskbar.animate()
                    .translationY(0f)
                    .setDuration(250)
                    .start()
            }
        }

        // Bottom
        if (bottomTaskbar.visibility != View.VISIBLE) {
            bottomTaskbar.visibility = View.VISIBLE
            bottomTaskbar.post {
                bottomTaskbar.translationY = bottomTaskbar.height.toFloat()
                bottomTaskbar.animate()
                    .translationY(0f)
                    .setDuration(250)
                    .start()
            }
        }
    }

    fun hide() {
        // Top
        if (topTaskbar.visibility == View.VISIBLE) {
            topTaskbar.animate()
                .translationY(-topTaskbar.height.toFloat())
                .setDuration(250)
                .withEndAction { topTaskbar.visibility = View.GONE }
                .start()
        }

        // Bottom
        if (bottomTaskbar.visibility == View.VISIBLE) {
            bottomTaskbar.animate()
                .translationY(bottomTaskbar.height.toFloat())
                .setDuration(250)
                .withEndAction { bottomTaskbar.visibility = View.GONE }
                .start()
        }
    }
}
