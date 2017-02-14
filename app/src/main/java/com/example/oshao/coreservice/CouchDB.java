package com.example.oshao.coreservice;

import android.os.AsyncTask;
import android.text.style.TextAppearanceSpan;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseOptions;
import com.couchbase.lite.Document;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.Reducer;
import com.couchbase.lite.UnsavedRevision;
import com.couchbase.lite.View;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.support.LazyJsonObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;

import hk.lscm.blindcane.core.bean.MapPoint;
import hk.lscm.blindcane.core.bean.MapPointPath;
import hk.lscm.blindcane.core.bean.RegionalString;
import hk.lscm.blindcane.core.bean.Tag;
import hk.lscm.blindcane.ws.bean.GeneralResponse;
import hk.lscm.blindcane.ws.bean.Resource;

/**
 * Created by oshao on 1/20/2017.
 */

public class CouchDB {

    private static String TAG = "CouchDB";

    private Database database;

    private final String DATA_BASE_NAME = "testing";

    private final String JSON_FILE_DOCUMENT_NAME = "json";
    private final String MAP_FILE_DOCUMENT_NAME = "map";


    //Json properties key
    private final String RESPONSE_CODE = "responseCode";
    private final String RESPONSE_VALUE = "responseValues";
    private final String RESOURCE = "resource";

    private Manager manager;

    public Map<String, List<String>> map = new HashMap<>();

    public String documentId;
    Document documentJson;
    Document documentMap;
    Document documentTest;

    View view;
    View singleView;


    protected void startCBLite() throws Exception {

        Manager.enableLogging(TAG, Log.VERBOSE);
        Manager.enableLogging(com.couchbase.lite.util.Log.TAG, Log.VERBOSE);
        Manager.enableLogging(com.couchbase.lite.util.Log.TAG_SYNC_ASYNC_TASK, Log.VERBOSE);
        Manager.enableLogging(com.couchbase.lite.util.Log.TAG_SYNC, Log.VERBOSE);
        Manager.enableLogging(com.couchbase.lite.util.Log.TAG_QUERY, Log.VERBOSE);
        Manager.enableLogging(com.couchbase.lite.util.Log.TAG_VIEW, Log.VERBOSE);
        Manager.enableLogging(com.couchbase.lite.util.Log.TAG_DATABASE, Log.VERBOSE);


        manager = new Manager(new AndroidContext(GlobalVirable.getContext()), Manager.DEFAULT_OPTIONS);

        DatabaseOptions options = new DatabaseOptions();
        options.setCreate(true);

        database = manager.openDatabase(DATA_BASE_NAME, options);
        documentJson = new Document(database, JSON_FILE_DOCUMENT_NAME);
        documentMap = new Document(database, MAP_FILE_DOCUMENT_NAME);
        documentTest = new Document(database, "test");

        view = database.getView("test");
        view.setMapReduce(new Mapper() {
            @Override
            public void map(Map<String, Object> document, Emitter emitter) {

                List<String> keySet = new ArrayList<String>();
                keySet.add((String) document.get("id"));
                keySet.add(String.valueOf(document.get("lastUpdateTime")));
                //keySet.add(Double.toString((Double) document.get("lastUpdateTime")));


                emitter.emit(keySet, document);
            }
        }, new Reducer() {
            @Override
            public Object reduce(List<Object> keys, List<Object> values, boolean rereduce) {
                return values.get(0);
            }
        }, "3");

        singleView = database.getView("singleView");
        singleView.setMap(new Mapper() {
            @Override
            public void map(Map<String, Object> document, Emitter emitter) {
                List<String> keySet = new ArrayList<String>();
                keySet.add((String) document.get("id"));
                keySet.add(String.valueOf(document.get("lastUpdateTime")));
                //keySet.add(Double.toString((Double) document.get("lastUpdateTime")));

                Gson gson = new Gson();
                String jsonStr = gson.toJson(document, Map.class);

                emitter.emit(keySet, jsonStr);
            }
        }, "8");

        Log.d(TAG, "=-=-=-=-You have create a db -=-=-=-");
    }

    public GeneralResponse initial() {

        GeneralResponse response = null;
        MapPointRetriver mapPointRetriver = new MapPointRetriver();
        try {

            response = mapPointRetriver.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, RequestMessageModifier.modifyMapPointDataRequest("admin", "admin")).get();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        if (response != null) {
            Log.v(TAG, "Response is not null");
            return response;
        } else {

            Log.e(TAG, "Response is null, Try again");

            return null;

        }
    }

    public Resource getResource() {

        Document document = database.getDocument("json");
        Object o = document.getProperty(RESOURCE);
        Gson gson = new Gson();
        String jsonstr = gson.toJson(o, Map.class);
        Resource resource = gson.fromJson(jsonstr, Resource.class);
        Log.v(TAG, "-=-=-=Document ID is " + resource.getMapPoints().get(0).getDomain());

        return resource;

    }


    public void queryAllJson() throws CouchbaseLiteException {

        Query query = database.createAllDocumentsQuery();
        query.setAllDocsMode(Query.AllDocsMode.ALL_DOCS);
        QueryEnumerator result = query.run();
        for (Iterator<QueryRow> it = result; it.hasNext(); ) {

            QueryRow row = it.next();
            Document document = row.getDocument();
            Log.v(TAG, "" + document.getProperties());
        }
    }

    public void insertJson(String responseCode, Map<String, List<String>> responseValues, Resource resource) {


        Map<String, Object> properties = new HashMap<>();

        properties.put(RESPONSE_CODE, responseCode);
        properties.put(RESPONSE_VALUE, responseValues);
        properties.put(RESOURCE, resource);


        try {
            documentJson.putProperties(properties);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }

        documentId = documentJson.getId();
    }

    public void insertMap(Resource resource) {

        Map<String, Object> properties = new HashMap<>();

        List<MapPoint> list;
        if (resource != null) {

            list = resource.getMapPoints();

            for (MapPoint mapPoint : list) {

                List<Object> values = new ArrayList<>();
                values.add(mapPoint.getDomain());
                values.add(mapPoint.getLastUpdateTime());

                properties.put(mapPoint.getId(), values);
            }
        }

        try {
            documentMap.putProperties(properties);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    public void deleteDocument(String id) {

        try {
            //database.getDocument(id).delete();
            database.delete();
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }

    }

    public Map<String, List<String>> modifyDocument(Resource resource) {

        Map<String, List<String>> result = new HashMap<>();

        List<MapPoint> list = resource.getMapPoints();

        for (MapPoint mapPoint : list) {

            List<String> values = new ArrayList<>();

            values.add(mapPoint.getDomain());
            values.add(Long.toString(mapPoint.getLastUpdateTime()));

            result.put(mapPoint.getId(), values);

        }

        return result;

    }

    public void generateMapPoint() throws CouchbaseLiteException {


        Map<String, Object> properties = new HashMap<>();

        for (int i = 0; i < 1000; i++) {

            MapPoint mapPoint = new MapPoint();
            mapPoint.setId(Integer.toString(ThreadLocalRandom.current().nextInt(0, 101)));
            mapPoint.setDomain("lscm");
            mapPoint.setLastUpdateTime(System.currentTimeMillis());
            mapPoint.setName(new HashMap<String, RegionalString>());
            RegionalString rs = new RegionalString();
            rs.setLanguageCode("TC");
            rs.setContent("ABCDEFG");
            mapPoint.getName().put("TC", rs);

            //with Gson
            Gson gson = new Gson();
            String str = gson.toJson(mapPoint);
            Map m = gson.fromJson(str, Map.class);

            //with fastJson
            String jsonStr = JSON.toJSONString(mapPoint);
            Map map = JSON.parseObject(jsonStr, Map.class);

//            Document d = database.getDocument(mapPoint.getId());
            Document d = database.getDocument(UUID.randomUUID().toString());
            d.putProperties(m);

//            properties.put(Integer.toString(i), mapPoint);
        }

//        documentTest.putProperties(properties);


    }

    public void getLatestRecord() {

        Log.d("lscm", "Database count:" + database.getDocumentCount());

        long startTime, endTime, timeDiff;

//        view.setMap(new Mapper() {
//            @Override
//            public void map(Map<String, Object> document, Emitter emitter) {
//
//                Log.v(TAG, "Starting Mapper");
//                //                for (int i = 0; i < 100; i++) {
////
////                    List<String> keySet = (List<String>) document.get("id");
////
////                    for (String key : keySet) {
////                        if (key.contains(Integer.toString(i)))
////
////                        {
////
////                        }
////                    }
////
////                    List<Double> timeList = new ArrayList<Double>();
////                    timeList.add((Double) document.get(Integer.toString(i)));
////                    Double lastUpdateTime = Collections.max(timeList);
////                    List<Object> keyList = new ArrayList<Object>();
////
////                    keyList.add(i);
////                    keyList.add(lastUpdateTime);
////
////                    e.emit(keyList, document);
////
////                    Log.d(TAG, "_+_+_+_+_+_+_+" + keyList.toString());
////
////                }
////                    String id = (String) document.get("id");
////
////                    e.emit(document.get("lastUpdateTime"),id );
////                    Log.v(TAG,"LastUpdate Time type : "+document.get("id"));
////                List<String> keySet = (List<String>) document.get("id");
////                for (String key : keySet) {
////                    e.emit(key, document);
////                }
////                Log.v(TAG,"Map reduced is"+keySet);
//                String id = (String) document.get("id");
//
////                List<Double> valueSet = new ArrayList<Double>();
////
////                for (int i = 0;i<1000;i++){
////                    if (Integer.toString(i).equals(id))
////                    {
////                        valueSet.add((Double) document.get("lastUpdateTime"));
////                        Log.v(TAG,"Wo caonima "+valueSet);
////
////                    }
////                }
//
////                Log.v(TAG,"The value set is "+ Collections.max(valueSet));
//
//                List<String> keySet = new ArrayList<String>();
//                keySet.add((String) document.get("id"));
//                keySet.add(Double.toString((Double) document.get("lastUpdateTime")));
//
//
//                emitter.emit(keySet, document);
//            }
//        }, "1");

//        view.setMapReduce(new Mapper() {
//            @Override
//            public void map(Map<String, Object> document, Emitter emitter) {
//
//                String id = (String) document.get("id");
//                emitter.emit(id, document.get("lastUpdateTime"));
//
//            }
//        }, new Reducer() {
//            @Override
//            public Object reduce(List<Object> keys, List<Object> values, boolean rereduce) {
//
//                return null;
//            }
//        }, "2");


//        startTime = System.currentTimeMillis();
//        MapPoint targetMap;
//
//        Query query = view.createQuery();
//        query.setDescending(true);
//        query.setGroupLevel(1);
////        query.setLimit(1);
//
//        try {
//            QueryEnumerator result = query.run();
//
//            for (Iterator<QueryRow> it = result; it.hasNext(); ) {
//                QueryRow row = it.next();
////                Log.v(TAG, "lastUpdateTime Finished : " + row.getValue() + "Key value is" + row.getKey());
//
//                String string = JSON.toJSONString(row.getValue());
//                targetMap = JSON.parseObject(string, MapPoint.class);
//
//
//                Log.v(TAG, "id:" + targetMap.getId() + "domain: " + targetMap.getDomain() + "LastUpdateTime: " + targetMap.getLastUpdateTime()
//                );
//
//            }
//        } catch (CouchbaseLiteException e) {
//            e.printStackTrace();
//        }
//
//        endTime = System.currentTimeMillis();
//        timeDiff = endTime - startTime;
//
//        Log.v(TAG, "Time interval for group query is :" + timeDiff);


        startTime = System.currentTimeMillis();

        String targetId = "1";

        Query q = singleView.createQuery();
        q.setDescending(true);
//        List<Object> keys = new ArrayList<Object>();
//        keys.add(targetId);
//        q.setGroupLevel(1);
        q.setStartKey(Arrays.asList(targetId, new HashMap<String, Object>()));
        q.setEndKey(Arrays.asList(targetId));
        q.setLimit(1);

        ObjectMapper om = new ObjectMapper();
        Gson gson = new Gson();

        MapPoint targetMp = null;

        try {
            QueryEnumerator result = q.run();

            for (Iterator<QueryRow> it = result; it.hasNext(); ) {
                QueryRow row = it.next();



                // GSON
//                targetMp =  gson.fromJson((String) row.getValue(), MapPoint.class);

                // fastJSON
                String jsonStr = JSON.toJSONString(row.getDocument().getProperties());
                targetMp = JSON.parseObject(jsonStr, MapPoint.class);
//                Log.v(TAG,"heihei-=-=-=-=-=-"+row.getValue());
                // Jackson
                //targetMp = om.convertValue(row.getDocum`ent().getProperties(), MapPoint.class);

                //Log.v(TAG, "lastUpdateTime Finished : " + row.getValue() + "Key value is" + row.getKey());

            }
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
//        catch (IOException ioe) {
//            ioe.printStackTrace();
//        }

        if (targetMp != null) {
            Log.v(TAG, "MapPoint value:" + targetMp.getDomain());
        }
        else
        {
            Log.v(TAG, "MapPoint is null");
        }

        endTime = System.currentTimeMillis();
        timeDiff = endTime - startTime;


        Log.v(TAG, "Time interval for single query is :" + timeDiff);
        try {
            Log.v(TAG, "Hidden value: " + targetMp.getAdditionalProperties().get("_rev"));
            Log.v(TAG, "Full object: " + om.writeValueAsString(targetMp));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }


//        Long lastUpdateTime = 0l;
//        String mapId = null;
//
//        View lastUpdateTimeView = database.getView("test");
//        lastUpdateTimeView.setMap(new Mapper() {
//            @Override
//            public void map(Map<String, Object> document, Emitter emitter) {
//
////                for (int i = 0; i < 1000; i++) {
//
////                    MapPoint mp = (MapPoint) documentTest.getProperty(Integer.toString(i));
////                    Log.v(TAG, "Map is " + mp);
////                    String id = mp.getId();
////                    Long lastUpdateTime = mp.getLastUpdateTime();
//
//                for (int i = 0; i < 1000; i++) {
//
//                    Object o = documentTest.getProperty(Integer.toString(i));
//                    Gson gson = new Gson();
//                    String test = gson.toJson(o, Map.class);
//                    MapPoint mapPoint = gson.fromJson(test, MapPoint.class);
//                    String id = mapPoint.getId();
//                    Long lastUpdateTime = mapPoint.getLastUpdateTime();
//                    emitter.emit(id, lastUpdateTime);
//
//                }
//
//
//            }
//
//        }, "0");
//
//        View domainMapId = database.getView("test2");
//        domainMapId.setMap(new Mapper() {
//            @Override
//            public void map(Map<String, Object> document, Emitter emitter) {
//                for (int i = 0; i < 1000; i++) {
//                    Object o = documentTest.getProperty(Integer.toString(i));
//                    Gson gson = new Gson();
//                    String test = gson.toJson(o, Map.class);
//                    MapPoint mapPoint = gson.fromJson(test, MapPoint.class);
//                    String domain = mapPoint.getDomain();
//                    String id = mapPoint.getId();
//                    emitter.emit(domain, id);
//                }
//            }
//        }, "0");
//
//
//        Query query = lastUpdateTimeView.createQuery();
//        query.setDescending(true);
//        query.setLimit(1);
//
//        try {
//            QueryEnumerator result = query.run();
//
//            for (Iterator<QueryRow> it = result; it.hasNext(); ) {
//                QueryRow row = it.next();
//                lastUpdateTime = (Long) row.getValue();
//                Log.v(TAG, "lastUpdateTime Finished : " + lastUpdateTime);
//            }
//        } catch (CouchbaseLiteException e) {
//            e.printStackTrace();
//        }
//
//        Query queryId = domainMapId.createQuery();
//        queryId.setDescending(true);
//        queryId.setLimit(1000);
//
//        try {
//            QueryEnumerator result = queryId.run();
//
//            for (Iterator<QueryRow> it = result; it.hasNext(); ) {
//                QueryRow row = it.next();
//                mapId = (String) row.getValue();
//
//                Log.v(TAG, "All the map id is   : " + mapId);
//            }
//        } catch (CouchbaseLiteException e) {
//            e.printStackTrace();
//        }
//
//
//        return lastUpdateTime;

    }


}
