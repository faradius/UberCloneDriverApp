package com.alex.uberclonedriverapp.channel

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.alex.uberclonedriverapp.R

//Esta configuarión sirve para otros proyectos
class NotificationHelper(base: Context): ContextWrapper(base) {

    //Solo cambiar estas variables para el uso en otros proyectos
    private val CHANNEL_ID = "com.alex.uberclonedriverapp"
    private val CHANNEL_NAME = "UberCloneDriverApp"
    private var manager:NotificationManager? = null

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createChannels()
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    fun createChannels(){
        val notificationChannel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationChannel.enableLights(true)
        notificationChannel.enableVibration(true)
        notificationChannel.lightColor = Color.WHITE
        notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE

        getManager().createNotificationChannel(notificationChannel)
    }

    fun getManager():NotificationManager{
        if (manager == null){
            manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        }

        return manager as NotificationManager
    }

    fun getNotification(title: String, body: String): NotificationCompat.Builder{
        return  NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setColor(Color.GRAY)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body).setBigContentTitle(title))
    }

    fun getNotificationActions(title: String, body: String, acceptAction: NotificationCompat.Action, cancelAction: NotificationCompat.Action): NotificationCompat.Builder{
        return  NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setColor(Color.GRAY)
            .setSmallIcon(R.mipmap.ic_launcher)
            .addAction(acceptAction)
            .addAction(cancelAction)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body).setBigContentTitle(title))
    }
}