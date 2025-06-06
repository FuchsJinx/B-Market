package com.karpeko.coffee.ui.orders.history;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class OrderCheckWorker extends Worker {

    public OrderCheckWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            // 1. Настройка временных параметров
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date now = new Date();
            Date fifteenMinutesAgo = new Date(now.getTime() - 15 * 60 * 1000);
            Timestamp thresholdTimestamp = new Timestamp(fifteenMinutesAgo);

            // 2. Логирование параметров
            Log.d("ORDER_WORKER", "=== Начало обработки ===");
            Log.d("ORDER_WORKER", "Текущее время: " + sdf.format(now));
            Log.d("ORDER_WORKER", "Пороговое время: " + sdf.format(fifteenMinutesAgo));
            Log.d("ORDER_WORKER", "Timestamp порога: " + thresholdTimestamp.getSeconds() + " сек.");

            // 3. Получаем экземпляр Firestore
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // 4. Основной запрос
            Query mainQuery = db.collection("orders")
                    .whereEqualTo("status", "Создан")
                    .whereLessThanOrEqualTo("createdAt", thresholdTimestamp);

            // 5. Выполняем запрос
            return mainQuery.get().continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    Exception e = task.getException();
                    Log.e("ORDER_WORKER", "Ошибка основного запроса", e);

                    // Пробуем альтернативный запрос без временного условия
                    return db.collection("orders")
                            .whereEqualTo("status", "Создан")
                            .get()
                            .continueWith(altTask -> {
                                if (!altTask.isSuccessful()) {
                                    return Result.failure();
                                }

                                QuerySnapshot altSnapshot = altTask.getResult();
                                Log.d("ORDER_WORKER", "Альтернативный запрос найден: " + altSnapshot.size() + " заказов");
                                processOrders(altSnapshot, thresholdTimestamp);
                                return Result.success();
                            });
                }

                QuerySnapshot snapshot = task.getResult();
                Log.d("ORDER_WORKER", "Основной запрос найден: " + snapshot.size() + " заказов");

                // 6. Обработка результатов
                if (snapshot.isEmpty()) {
                    Log.d("ORDER_WORKER", "Нет подходящих заказов, запускаем диагностику");
                    runDiagnostics(db, thresholdTimestamp);
                } else {
                    processOrders(snapshot, thresholdTimestamp);
                }

                return Tasks.forResult(Result.success());
            }).getResult();
        } catch (Exception e) {
            Log.e("ORDER_WORKER", "Критическая ошибка в doWork()", e);
            return Result.failure();
        }
    }

    private void processOrders(QuerySnapshot snapshot, Timestamp threshold) {
        for (QueryDocumentSnapshot doc : snapshot) {
            try {
                String orderId = doc.getId();
                String status = doc.getString("status");
                Timestamp createdAt = doc.getTimestamp("createdAt");

                Log.d("ORDER_PROCESSING", String.format(
                        "Обработка заказа %s: статус=%s, создан=%s",
                        orderId,
                        status,
                        createdAt.toDate().toString()
                ));

                if (createdAt.compareTo(threshold) <= 0) {
                    Log.d("ORDER_PROCESSING", "Заказ просрочен, обновляем статус");
                    doc.getReference().update("status", "Отменен")
                            .addOnSuccessListener(aVoid ->
                                    Log.d("ORDER_PROCESSING", "Статус заказа " + orderId + " обновлен"))
                            .addOnFailureListener(e ->
                                    Log.e("ORDER_PROCESSING", "Ошибка обновления заказа " + orderId, e));
                }
            } catch (Exception e) {
                Log.e("ORDER_PROCESSING", "Ошибка обработки документа " + doc.getId(), e);
            }
        }
    }

    private void runDiagnostics(FirebaseFirestore db, Timestamp threshold) {
        // 1. Проверяем последние 10 заказов любого статуса
        db.collection("orders")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(snapshot -> {
                    Log.d("ORDER_DIAG", "=== Последние 10 заказов ===");
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Timestamp createdAt = doc.getTimestamp("createdAt");
                        Log.d("ORDER_DIAG", String.format(
                                "ID: %s | Статус: %s | Создан: %s | Тип createdAt: %s",
                                doc.getId(),
                                doc.getString("status"),
                                (createdAt != null) ? createdAt.toDate().toString() : "null",
                                (createdAt != null) ? createdAt.getClass().getSimpleName() : "null"
                        ));
                    }
                });

        // 2. Проверяем все заказы с нужным статусом
        db.collection("orders")
                .whereEqualTo("status", "Создан")
                .get()
                .addOnSuccessListener(snapshot -> {
                    Log.d("ORDER_DIAG", "=== Все заказы 'Создан' ===");
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Timestamp createdAt = doc.getTimestamp("createdAt");
                        boolean isOld = (createdAt != null) && (createdAt.compareTo(threshold) <= 0);

                        Log.d("ORDER_DIAG", String.format(
                                "ID: %s | Создан: %s | Подходит: %b",
                                doc.getId(),
                                (createdAt != null) ? createdAt.toDate().toString() : "null",
                                isOld
                        ));
                    }
                });
    }
}
