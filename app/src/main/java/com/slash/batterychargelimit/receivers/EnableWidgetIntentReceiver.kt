package com.slash.batterychargelimit.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.Toast

import com.slash.batterychargelimit.R
import com.slash.batterychargelimit.SharedMethods
import eu.chainfire.libsuperuser.Shell

import com.slash.batterychargelimit.Constants.STOP_CHARGING
import com.slash.batterychargelimit.Constants.PLUGGED_IN
import com.slash.batterychargelimit.Constants.SETTINGS
import com.slash.batterychargelimit.Constants.INTENT_TOGGLE_ACTION
import com.slash.batterychargelimit.EnableWidget

class EnableWidgetIntentReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == INTENT_TOGGLE_ACTION) {
            val settings = context.getSharedPreferences(SETTINGS, 0)
            if (Shell.SU.available()) {
                if (settings.getBoolean(PLUGGED_IN, false)) {
                    val stop = !settings.getBoolean(STOP_CHARGING, false)
                    if (stop) {
                        SharedMethods.changeState(context, SharedMethods.CHARGE_OFF)
                    } else {
                        SharedMethods.changeState(context, SharedMethods.CHARGE_ON)
                    }
                    settings.edit().putBoolean(STOP_CHARGING, stop).apply()
                    updateWidget(context, stop)
                }
            } else {
                Toast.makeText(context, R.string.root_denied, Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {

        fun updateWidget(context: Context, enable: Boolean) {
            val remoteViews = RemoteViews(context.packageName, R.layout.widget_button)

            remoteViews.setImageViewResource(R.id.enable, getImage(enable))
            remoteViews.setOnClickPendingIntent(R.id.enable, EnableWidget.buildButtonPendingIntent(context))

            EnableWidget.pushWidgetUpdate(context, remoteViews)
        }

        fun getImage(enabled: Boolean): Int {
            return if (enabled) {
                R.drawable.widget_enabled
            } else {
                R.drawable.widget_disabled
            }
        }
    }
}
