package com.example.oshao.coreservice;

/**
 * Created by oshao on 1/17/2017.
 */

public interface ApplicationKey {

    interface UiActivity {

        String ACTION_SLEEP_TIME_UIACTIVITY = "com.example.oshao.uiactivity.sleeptime";
        String KEY_SLEEP_TIME_UIACTIVITY = "com.example.oshao.uiactivity.sleeptime";

    }

    interface BackgroundThread {

        String ACTION_EPC_BACKGROUNDTHREAD = "com.example.oshao.backgroundthread.epc";

        String KEY_EPC_BACKGROUNDTHREAD = "com.example.oshao.backgroundthread.epc";

    }

    interface CoreService {

        String ACTION_LOOP_COUNT_CORESERVICE = "com.example.oshao.coreservice.actionloopcount";

        String KEY_LOOP_COUNT_CORESERVICE = "key_loop_count";

        String ACTION_READER_COUNT_CORESERVICE = "com.example.oshao.coreservice.actionreadercount";

        String KEY_READER_COUNT_CORESERVICE = "key_reader_count";

    }

}
