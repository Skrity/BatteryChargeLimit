package com.slash.batterychargelimit.receivers

import android.content.*
import android.preference.PreferenceManager
import android.util.Log
import com.slash.batterychargelimit.Constants
import com.slash.batterychargelimit.Constants.POWER_CHANGE_TOLERANCE_MS
import com.slash.batterychargelimit.Constants.PLUGGED_IN
import com.slash.batterychargelimit.ForegroundService
import com.slash.batterychargelimit.SharedMethods
import com.slash.batterychargelimit.settings.SettingsFragment

/**
 * Created by harsha on 30/1/17.
 *
 * This BroadcastReceiver handles the change of the power supply state.
 * Because control files like charging_enabled are causing fake events, there is a time window POWER_CHANGE_TOLERANCE_MS
 * milliseconds where the respective "changes" of the power supply will be ignored.
 *
 * 21/4/17 milux: Changed to avoid service (re)start because of fake power on event
 */

class PowerConnectionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        //Ignore new events after power change or during state fixing
        if (!PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(SettingsFragment.KEY_IMMEDIATE_POWER_INTENT_HANDLING, false)
                && SharedMethods.isChangePending(
                Math.max(POWER_CHANGE_TOLERANCE_MS, BatteryReceiver.backOffTime * 2))) {
            if (action == Intent.ACTION_POWER_CONNECTED) {
                //Ignore connected event only if service is running
                if (ForegroundService.isRunning) {
                    Log.d("Power State", "ACTION_POWER_CONNECTED ignored")
                    return
                }
            } else if (action == Intent.ACTION_POWER_DISCONNECTED) {
                Log.d("Power State", "ACTION_POWER_DISCONNECTED ignored")
                return
            }
        }
        if (action == Intent.ACTION_POWER_CONNECTED) {
            Log.d("Power State", "ACTION_POWER_CONNECTED")
            context.getSharedPreferences(Constants.SETTINGS, 0).edit().putBoolean(PLUGGED_IN, true).apply()
            SharedMethods.startService(context)
        } else if (action == Intent.ACTION_POWER_DISCONNECTED) {
            Log.d("Power State", "ACTION_POWER_DISCONNECTED")
            context.getSharedPreferences(Constants.SETTINGS, 0).edit().putBoolean(PLUGGED_IN, false).apply()
            context.getSharedPreferences(Constants.SETTINGS, 0).edit().putBoolean(Constants.STOP_CHARGING, false).apply()
            EnableWidgetIntentReceiver.updateWidget(context, false)
            SharedMethods.stopService(context, false)
        }
    }
}
