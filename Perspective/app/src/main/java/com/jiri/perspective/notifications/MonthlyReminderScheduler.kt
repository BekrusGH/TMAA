package com.jiri.perspective.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

// Scheduler pro měsíční reminder.
// Jeho úkolem je spočítat další termín a nastavit alarm.
object MonthlyReminderScheduler {

    fun scheduleNext(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = reminderPendingIntent(context)

        // Pro jistotu zrušíme předchozí alarm se stejným PendingIntent,
        // aby nevznikaly duplicity.
        alarmManager.cancel(pendingIntent)

        val triggerAtMillis = calculateNextTriggerTimeMillis()

        // Nastaví alarm na vypočítaný budoucí čas.
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    }

    // PendingIntent pro spuštění receiveru ve chvíli, kdy alarm doběhne.
    private fun reminderPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MonthlyReminderReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            1001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    // Spočítá další termín reminderu.
    // Aktuálně: 28. den v měsíci v 19:00.
    private fun calculateNextTriggerTimeMillis(): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 28)
            set(Calendar.HOUR_OF_DAY, 19)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Pokud už dnešní / aktuální měsíční termín minul,
        // posuneme reminder na další měsíc.
        if (target.timeInMillis <= now.timeInMillis) {
            target.add(Calendar.MONTH, 1)
            target.set(Calendar.DAY_OF_MONTH, 28)
            target.set(Calendar.HOUR_OF_DAY, 19)
            target.set(Calendar.MINUTE, 0)
            target.set(Calendar.SECOND, 0)
            target.set(Calendar.MILLISECOND, 0)
        }

        return target.timeInMillis
    }
}