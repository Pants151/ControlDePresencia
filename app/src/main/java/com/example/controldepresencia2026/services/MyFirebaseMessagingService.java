package com.example.controldepresencia2026.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.example.controldepresencia2026.MainActivity;
import com.example.controldepresencia2026.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String titulo = "Aviso de Presencia";
        String mensaje = "Tienes una nueva notificación";

        // Si el mensaje viene en el formato "Notification"
        if (remoteMessage.getNotification() != null) {
            titulo = remoteMessage.getNotification().getTitle();
            mensaje = remoteMessage.getNotification().getBody();
        }
        // Si el mensaje viene en el formato "Data"
        else if (remoteMessage.getData().size() > 0) {
            if(remoteMessage.getData().containsKey("title")) {
                titulo = remoteMessage.getData().get("title");
            }
            if(remoteMessage.getData().containsKey("body")) {
                mensaje = remoteMessage.getData().get("body");
            }
        }

        mostrarNotificacion(titulo, mensaje);
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
    }

    private void mostrarNotificacion(String titulo, String mensaje) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "CANAL_PRESENCIA")
                .setSmallIcon(R.mipmap.ic_launcher) // Asegurarse de que existe
                .setContentTitle(titulo)
                .setContentText(mensaje)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Crear canal si es necesario
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("CANAL_PRESENCIA",
                    "Recordatorios de Fichaje",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0, notificationBuilder.build());
    }
}
