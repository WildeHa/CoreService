package com.example.oshao.coreservice;

import java.util.HashMap;

import hk.lscm.blindcane.*;
import hk.lscm.blindcane.ws.bean.GeneralRequest;

/**
 * Created by oshao on 1/20/2017.
 */

public class RequestMessageModifier {

    public static GeneralRequest modifyMapPointDataRequest(String username, String password) {

        GeneralRequest request = new GeneralRequest();

        request.setAuthParams(new HashMap<String, String>());
        request.getAuthParams().put(ApplicationContext.User.AuthParamsKey.USER_ID, username);
        request.getAuthParams().put(ApplicationContext.User.AuthParamsKey.PASSWORD, password);
        request.setRequestCode(ApplicationContext.GeneralRequest.RequestCode.GET_ALL_RESOURCES);

        return request;
    }
}
