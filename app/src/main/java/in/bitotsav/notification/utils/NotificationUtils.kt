package `in`.bitotsav.notification.utils

import `in`.bitotsav.R
import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.media.RingtoneManager
import android.os.Bundle
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.NavDeepLinkBuilder
import java.util.*

enum class Channel(val id: String, val channelName: String) {
    ANNOUNCEMENT("announcement", "Announcement"),
    EVENT("event", "Event"),
    PM("pm", "Private Message"),
    RESULT("result", "Result"),
    STARRED("starred", "Starred")
}

/**
 * Create and show a notification
 */
fun displayNotification(
    title: String,
    content: String,
    timestamp: Long,
    channel: Channel,
    pendingIntent: PendingIntent,
    context: Context
) {
    val uniqueId = (Math.log(Date().time.toDouble()) * 1000000000000000L % Integer.MAX_VALUE).toInt()

    val bigTextStyle = NotificationCompat.BigTextStyle()
    bigTextStyle.bigText(content)

    val channelId = channel.id
    val notificationBuilder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(getIconByChannel(channel))
        .setLargeIcon(AppCompatResources.getDrawable(context, R.mipmap.ic_launcher_round)!!.toBitmap())
        .setStyle(bigTextStyle)
        .setContentTitle(title)
        .setContentText(content)
        .setDefaults(Notification.DEFAULT_ALL)
        .setWhen(timestamp)
        .setShowWhen(true)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)

    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.notify(uniqueId, notificationBuilder.build())
}

@TargetApi(26)
fun createNotificationChannels(context: Context) {
    // TODO [Feature]: Add a workaround for anomalous behavior of notification channels on MIUI
    val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    Channel.values().forEach {
        val channel = NotificationChannel(it.id, it.channelName, NotificationManager.IMPORTANCE_DEFAULT).apply {
            setSound(defaultSoundUri, null)
            enableLights(true)
            enableVibration(true)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        notificationManager.createNotificationChannel(channel)
    }
}

private fun getIconByChannel(channel: Channel): Int {
    return when (channel) {
        Channel.ANNOUNCEMENT -> R.drawable.ic_announcement_white_24dp
        Channel.EVENT -> R.drawable.ic_schedule_white_24dp
        Channel.PM -> R.drawable.ic_priority_high_white_24dp
        Channel.RESULT -> R.drawable.ic_trophy_white_24dp
        Channel.STARRED -> R.drawable.ic_star_fill_white_24dp
    }
}

fun getFeedPendingIntent(context: Context) =
    NavDeepLinkBuilder(context)
        .setGraph(R.navigation.nav_bitotsav)
        .setDestination(R.id.destFeed)
        .createPendingIntent()

fun getEventDetailPendingIntent(context: Context, eventId: Int) =
    NavDeepLinkBuilder(context)
        .setGraph(R.navigation.nav_bitotsav)
        .setDestination(R.id.destEventDetail)
        .setArguments(Bundle().apply {
            putInt("eventId", eventId)
        })
        .createPendingIntent()