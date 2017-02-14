package com.example.oshao.coreservice;

import android.os.AsyncTask;
import android.util.Log;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import hk.lscm.blindcane.*;
import hk.lscm.blindcane.ApplicationContext;
import hk.lscm.blindcane.ws.bean.GeneralRequest;
import hk.lscm.blindcane.ws.bean.GeneralResponse;

/**
 * Created by oshao on 1/19/2017.
 */

public class MapPointRetriver extends AsyncTask<GeneralRequest, Void, GeneralResponse> {

    private String url = "http://172.16.2.149:8080/BlindCaneServer/JSONService/SubmitGeneralRequest";
    private final String TAG = "MapPointRetriver";

    @Override
    protected GeneralResponse doInBackground(GeneralRequest... generalRequests) {

        GeneralRequest request = generalRequests[0];

        RestTemplate restTemplate = new RestTemplate();

        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

        try {
            return restTemplate.postForObject(url, request, GeneralResponse.class);
        } catch (Exception e) {
            return null;

        }
    }

    @Override
    protected void onPostExecute(GeneralResponse generalResponse) {

        if (generalResponse.getResponseCode() != null && generalResponse.getResponseCode().equals(ApplicationContext.GeneralResponse.ResponseCode.REQUEST_PROCESS_SUCCESS)) {

            Log.v(TAG, "=-=-=-Already connect to url-=-=-=");

        }
    }
}
