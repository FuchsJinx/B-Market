package com.karpeko.coffee.notification;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.karpeko.coffee.MainActivity;
import com.karpeko.coffee.R;

import java.util.Calendar;
import java.util.Locale;

public class OrderNotificationHelper {
    public static final String CHANNEL_ID = "ORDERS_CHANNEL";
    private static final int MORNING_NOTIFICATION_ID = 1001;
    private static final int EVENING_NOTIFICATION_ID = 1002;

    private final Context context;
    private final NotificationManagerCompat notificationManager;

    public OrderNotificationHelper(Context context) {
        this.context = context.getApplicationContext();
        this.notificationManager = NotificationManagerCompat.from(this.context);
        createNotificationChannel();
    }

    // Создаем канал уведомлений (для Android 8.0+)
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Уведомления о заказах",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Уведомления о статусе заказов и ежедневные напоминания");
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    // Уведомление о создании заказа
    @SuppressLint("MissingPermission")
    public void showOrderCreatedNotification(String orderId, double totalAmount, int itemsCount) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("order_id", orderId);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                orderId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String amountText = String.format(Locale.getDefault(), "%.2f руб.", totalAmount);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.main_icon)
                .setContentTitle("Заказ оформлен")
                .setContentText("Сумма: " + amountText)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Ваш заказ #" + orderId + " успешно оформлен!\n" +
                                "Количество товаров: " + itemsCount + "\n" +
                                "Общая сумма: " + amountText))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        try {
            notificationManager.notify(orderId.hashCode(), builder.build());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    // Настройка ежедневных уведомлений
    public void scheduleDailyNotifications() {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Утреннее уведомление (9:00)
        setDailyNotification(alarmManager, 8, 0, MORNING_NOTIFICATION_ID,
                "Доброе утро!", "Начните его с чашечки ароматного кофе!");

        // Вечернее уведомление (20:00)
        setDailyNotification(alarmManager, 18, 0, EVENING_NOTIFICATION_ID,
                "Добрый вечер!", "Отдохните после тяжелого дня с чашечкой кофе и интересной книгой в нашей кофейне!");
    }

    private void setDailyNotification(AlarmManager alarmManager, int hour, int minute,
                                      int notificationId, String title, String message) {
        Intent notificationIntent = new Intent(context, NotificationReceiver.class);
        notificationIntent.putExtra("title", title);
        notificationIntent.putExtra("message", message);
        notificationIntent.putExtra("notification_id", notificationId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        // Если время уже прошло сегодня, планируем на следующий день
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        // Устанавливаем повтор ежедневно
        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent
        );
    }

    // Метод для отмены всех уведомлений
    public void cancelAllNotifications() {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent morningIntent = new Intent(context, NotificationReceiver.class);
        PendingIntent morningPendingIntent = PendingIntent.getBroadcast(
                context,
                MORNING_NOTIFICATION_ID,
                morningIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(morningPendingIntent);

        Intent eveningIntent = new Intent(context, NotificationReceiver.class);
        PendingIntent eveningPendingIntent = PendingIntent.getBroadcast(
                context,
                EVENING_NOTIFICATION_ID,
                eveningIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(eveningPendingIntent);

        notificationManager.cancelAll();
    }
}
