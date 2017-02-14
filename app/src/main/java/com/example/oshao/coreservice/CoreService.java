package com.example.oshao.coreservice;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by oshao on 1/12/2017.
 */

public class CoreService extends Service implements ApplicationKey {

    private final String ACTION_SLEEP_TIME_UIACTIVITY = "com.example.oshao.uiactivity.sleeptime";
    private final String KEY_SLEEP_TIME_UIACTIVITY = "com.example.oshao.uiactivity.sleeptime";

    private final String ACTION_EPC_BACKGROUNDTHREAD = "com.example.oshao.backgroundthread.epc";
    private final String KEY_EPC_BACKGROUNDTHREAD = "com.example.oshao.backgroundthread.epc";

    private final String ACTION_READER_COUNT_CORESERVICE = "com.example.oshao.coreservice.actionreadercount";
    private final String KEY_READER_COUNT_CORESERVICE = "key_reader_count";

    private final String ACTION_SLEEP_TIME_CORESERVICE = "com.example.oshao.coreservice.actionsleeptime";
    private final String KEY_SLEEP_TIME_CORESERVICE = "key_sleep_time";

    private final String ACTION_LOOP_COUNT_CORESERVICE = "com.example.oshao.coreservice.actionloopcount";
    private final String KEY_LOOP_COUNT_CORESERVICE = "key_loop_count";

    private ExecutorService pool = Executors.newScheduledThreadPool(10);

    private int readerCount = 0;
    private Callable<Integer> callableRederCount;
    private Future<Integer> taskReaderCount;

    private int sleepTime = 0;
    private Callable<Integer> callableSleepTime;
    private Future<Integer> taskSleepTime;

    private int loopCount = 0;

    @Override
    public void onCreate() {
        super.onCreate();

        CoreServiceReceiver coreServiceReceiver = new CoreServiceReceiver();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BackgroundThread.ACTION_EPC_BACKGROUNDTHREAD);
        intentFilter.addAction(UiActivity.ACTION_SLEEP_TIME_UIACTIVITY);
        registerReceiver(coreServiceReceiver, intentFilter);

        //startReaderCountThread();
//        startSleepTimeThread();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startReaderCountThread() {

        if (taskReaderCount != null && !taskReaderCount.isDone() || pool.isShutdown()) return;

        callableRederCount = new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {

                while (readerCount < 1000) {
                    Intent intent = new Intent(ACTION_READER_COUNT_CORESERVICE);
                    intent.putExtra(KEY_READER_COUNT_CORESERVICE, readerCount);
                    sendBroadcast(intent);
                    Thread.sleep(400);
                }

                return null;

            }
        };
        taskReaderCount = pool.submit(callableRederCount);
    }

    private void startSleepTimeThread() {
        if (taskSleepTime != null && !taskSleepTime.isDone() || pool.isShutdown()) return;

        callableSleepTime = new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {

                while (sleepTime <= 1000) {

//                    Intent intent = new Intent(ACTION_SLEEP_TIME_CORESERVICE);
//                    intent.putExtra(KEY_SLEEP_TIME_CORESERVICE, sleepTime);
//                    sendBroadcast(intent);
                    loopCount++;
                    Thread.sleep(sleepTime);

                    Intent intent = new Intent(CoreService.ACTION_LOOP_COUNT_CORESERVICE);
                    intent.putExtra(CoreService.KEY_LOOP_COUNT_CORESERVICE, loopCount);
                    Log.v("CoreService", "Sending Loop Count" + loopCount);
                    sendBroadcast(intent);

                }

                return null;
            }
        };

        taskSleepTime = pool.submit(callableSleepTime);

    }

    public class CoreServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case BackgroundThread.ACTION_EPC_BACKGROUNDTHREAD:
                    readerCount++;
                    // directly send intent(tagEPC) to UI
                    Intent in = new Intent(CoreService.ACTION_READER_COUNT_CORESERVICE);
                    intent.putExtra(CoreService.KEY_READER_COUNT_CORESERVICE, readerCount);
                    sendBroadcast(in);
                    break;
                case UiActivity.ACTION_SLEEP_TIME_UIACTIVITY:
                    sleepTime = intent.getIntExtra(UiActivity.KEY_SLEEP_TIME_UIACTIVITY, 0);
                    long interval = System.currentTimeMillis() - intent.getLongExtra("mainactivitytime", 0);
                    // print Time
                    Log.v("Timing", "Message: " + sleepTime + " Time: " + interval);
                    Log.v("CoreService", "" + sleepTime);
            }
        }
    }
}
