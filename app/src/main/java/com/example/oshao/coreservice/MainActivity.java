package com.example.oshao.coreservice;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.couchbase.lite.CouchbaseLiteException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hk.lscm.blindcane.ws.bean.GeneralResponse;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = "MainActivity";

    CouchDB couchDB = new CouchDB();
    GeneralResponse response;

    Button buttonCreate;
    Button buttonDelete;
    Button buttonInsert;
    Button buttonRead;
    Button buttonUpdate;

    private Map<String, List<String>> map = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GlobalVirable.setContext(this);

        Intent intent = new Intent(this, CoreService.class);
        startService(intent);

        try {
            couchDB.startCBLite();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();


        buttonCreate = (Button) findViewById(R.id.button_create);
        buttonDelete = (Button) findViewById(R.id.button_delete);
        buttonInsert = (Button) findViewById(R.id.button_insert);
        buttonRead = (Button) findViewById(R.id.button_read);
        buttonUpdate = (Button) findViewById(R.id.button_update);

        buttonCreate.setOnClickListener(this);
        buttonDelete.setOnClickListener(this);
        buttonInsert.setOnClickListener(this);
        buttonRead.setOnClickListener(this);
        buttonUpdate.setOnClickListener(this);


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.button_create:
                response = couchDB.initial();
                break;
            case R.id.button_delete:

//                couchDB.deleteDocument("123");
                couchDB.deleteDocument("test");

                break;
            case R.id.button_insert:

//                if (response != null) {
//                    couchDB.insertJson(response.getResponseCode(), response.getResponseValues(), response.getResource());
//                } else {
//                    Log.e(TAG, "No element to insert in to DB");
//                }


                try {
                    couchDB.generateMapPoint();
                } catch (CouchbaseLiteException e) {
                    e.printStackTrace();
                }


                break;
            case R.id.button_read:
//                try {
//                    couchDB.queryAllJson();
//                } catch (CouchbaseLiteException e) {
//                    e.printStackTrace();
//                    Log.d(TAG,"Fail to read document in DB");
//                }
//                couchDB.getResource();
                couchDB.getLatestRecord();
                break;

            case R.id.button_update:
                map = couchDB.modifyDocument(couchDB.getResource());
                Log.v(TAG, "" + map);
                break;

        }
    }


}
