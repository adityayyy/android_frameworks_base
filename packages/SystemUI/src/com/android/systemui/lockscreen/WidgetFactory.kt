/*
 * Copyright (C) 2025 The AxionAOSP Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.systemui.lockscreen

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.GridLayout
import androidx.core.content.ContextCompat
import com.android.systemui.animation.view.LaunchableImageView
import com.android.systemui.res.R
import com.google.android.flexbox.FlexboxLayout

class WidgetFactory(
    private val context: Context,
    private val controller: LockScreenWidgetsController
) {
    private val darkColor = ContextCompat.getColor(context, LsWidgetsRes.COLOR_BG_DARK)
    private val lightColor = ContextCompat.getColor(context, LsWidgetsRes.COLOR_BG_LIGHT)
    private val white = Color.WHITE

    private val isNightMode: Boolean get() =
        (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES

    private val Context.scaleRatio: Float
        get() {
            val displayMetrics = resources.displayMetrics
            val sw = minOf(displayMetrics.widthPixels, displayMetrics.heightPixels) / displayMetrics.density
            return sw / 420f
        }

    fun createWidgetView(action: WidgetAction): LaunchableImageView {
        return LaunchableImageView(context).apply {
            isFocusable = true
            isClickable = true
            setOnClickListener { action.onClick(controller) }
            action.onLongClick?.let { longClick ->
                setOnLongClickListener { v -> longClick(controller, v) }
            }
        }
    }

    fun updateWidgetState(view: LaunchableImageView, action: WidgetAction, active: Boolean) {
        view.setBackgroundResource(0)
        val iconRes = if (active) action.activeRes else action.inactiveRes
        val bgRes = if (controller.dozing) {
                if (active) {
                    LsWidgetsRes.WIDGET_BG_DOZING_ACTIVE
                } else {
                    LsWidgetsRes.WIDGET_BG_DOZING_INACTIVE
                }
            } else {
                if (active) {
                    LsWidgetsRes.WIDGET_BG_ACTIVE
                } else {
                    if (isNightMode) { 
                        LsWidgetsRes.WIDGET_BG_DARK
                    } else { 
                        LsWidgetsRes.WIDGET_BG_LIGHT
                    }
                }
            }
        val iconTint = when {
            controller.dozing || active -> white
            isNightMode -> lightColor
            else -> darkColor
        }
        view.setImageResource(iconRes)
        view.setBackgroundResource(bgRes)
        view.imageTintList = ColorStateList.valueOf(iconTint)
    }

    fun updateWidgetSize(view: LaunchableImageView, position: Int, total: Int) {
        val scaleRatio = context.scaleRatio
        val widgetSize = (context.resources.getDimensionPixelSize(LsWidgetsRes.WIDGET_CIRCLE_SIZE) * scaleRatio).toInt()
        val spacing = (context.resources.getDimensionPixelSize(LsWidgetsRes.WIDGET_MARGIN_HORIZONTAL) * scaleRatio).toInt()
        val iconPadding = (context.resources.getDimensionPixelSize(LsWidgetsRes.WIDGET_ICON_PADDING) * scaleRatio).toInt()
        val leftMargin = if (position == 0) 0 else spacing
        val rightMargin = if (position == total - 1) 0 else spacing
        view.layoutParams = FlexboxLayout.LayoutParams(widgetSize, widgetSize).apply {
            setMargins(leftMargin, spacing, rightMargin, spacing)
            flexGrow = 0f
            flexShrink = 0f
        }
        view.setPadding(iconPadding, iconPadding, iconPadding, iconPadding)
    }
}
