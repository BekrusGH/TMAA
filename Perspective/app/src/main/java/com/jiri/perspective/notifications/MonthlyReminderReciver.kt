package com.jiri.perspective.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.jiri.perspective.MainActivity
import com.jiri.perspective.R

// Receiver, který se spustí ve chvíli, kdy dorazí naplánovaný reminder.
// Jeho úkolem je zobrazit notifikaci a naplánovat další.
class MonthlyReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        // Pro jistotu vytvoříme notification channel i tady.
        NotificationHelper.createMonthlyReminderChannel(context)

        // Na Androidu 13+ musíme mít oprávnění POST_NOTIFICATIONS.
        // Pokud chybí, notifikaci nezobrazíme.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        // Intent pro otevření appky po kliknutí na notifikaci.
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        // PendingIntent = zabalený Intent, který se spustí později po kliknutí na notifikaci.
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Sestavení notifikace.
        val notification = NotificationCompat.Builder(
            context,
            NotificationHelper.MONTHLY_REMINDER_CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.monthly_reminder_title))
            .setContentText(context.getString(R.string.monthly_reminder_body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        // Zobrazení notifikace.
        NotificationManagerCompat.from(context).notify(
            NotificationHelper.MONTHLY_REMINDER_NOTIFICATION_ID,
            notification
        )

        // Po zobrazení reminderu naplánujeme další.
        MonthlyReminderScheduler.scheduleNext(context)
    }
}