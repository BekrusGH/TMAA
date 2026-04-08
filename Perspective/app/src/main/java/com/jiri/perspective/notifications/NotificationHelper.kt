package com.jiri.perspective.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import com.jiri.perspective.R

// Pomocný objekt pro notifikace.
// Drží konstanty a vytváří notification channel.
object NotificationHelper {
    const val MONTHLY_REMINDER_CHANNEL_ID = "monthly_reminder_channel"
    const val MONTHLY_REMINDER_NOTIFICATION_ID = 1001

    fun createMonthlyReminderChannel(context: Context) {
        // Notification channels jsou potřeba jen od Androidu 8.0+.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        // Vytvoření channelu pro měsíční reminder notifikace.
        val channel = NotificationChannel(
            MONTHLY_REMINDER_CHANNEL_ID,
            context.getString(R.string.monthly_reminder_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.monthly_reminder_channel_description)
        }

        // Registrace channelu v systému.
        NotificationManagerCompat.from(context).createNotificationChannel(channel)
    }
}