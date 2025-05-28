package com.karpeko.coffee.ui.orders.history;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LifecycleOwner;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import java.util.concurrent.TimeUnit;

public class OrderWorkScheduler {
    private static final String WORK_TAG = "ORDER_CHECK_WORKER";
    private static final String WORK_UNIQUE_NAME = "uniqueOrderCheckWork";

    public static void scheduleOrderCheck(Context context) {
        // 1. Создаем constraints (ограничения)
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        // 2. Создаем периодический запрос
        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
                OrderCheckWorker.class,
                15, TimeUnit.MINUTES
        )
                .setConstraints(constraints)
                .addTag(WORK_TAG)
                .build();

        // 3. Запускаем Worker с логированием
        WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                        WORK_UNIQUE_NAME,
                        ExistingPeriodicWorkPolicy.REPLACE,
                        workRequest
                );

        // 4. Проверка статуса (добавьте этот код там, где запускаете Worker)
        checkWorkerStatus(context);
    }

    // Метод для проверки состояния Worker
    public static void checkWorkerStatus(Context context) {
        WorkManager.getInstance(context)
                .getWorkInfosByTagLiveData(WORK_TAG)
                .observe((LifecycleOwner) context, workInfos -> {
                    if (workInfos == null || workInfos.isEmpty()) {
                        Log.d("WORKER_STATUS", "Worker не найден");
                        return;
                    }

                    for (WorkInfo info : workInfos) {
                        Log.d("WORKER_STATUS",
                                String.format("Worker ID: %s, State: %s",
                                        info.getId(), info.getState()));

                        // Дополнительная диагностика
                        if (info.getState() == WorkInfo.State.BLOCKED) {
                            Log.w("WORKER_STATUS", "Worker заблокирован из-за: " +
                                    info.getOutputData().getString("error"));
                        }
                    }
                });
    }
}
