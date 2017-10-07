/*
 * Copyright © 2017 digi.me. All rights reserved.
 */

package com.digi.me.permission.service;

import com.digi.me.permission.service.models.DataGetEncryptedResponse;
import com.digi.me.permission.service.models.DataGetResponse;
import com.digi.me.permission.service.models.SessionKeyCreateResponse;
import com.digi.me.permission.service.models.SessionTokenBody;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface PermissionService {

    @Headers({
        "content-type: application/json",
        "Cache-Control: no-cache"
    })
    @POST("v1/permission-access/session")
    Call<SessionKeyCreateResponse> getSessionToken(@Body SessionTokenBody sessionTokenBody);

    @Headers({
        "Content-type: application/json",
        "Cache-Control: no-cache"
    })
    @GET("v1/permission-access/query/{sessionKey}")
    Call<DataGetResponse> listDataFiles(@Path("sessionKey") String sessionKey);

    @Headers({
        "Content-type: application/json",
        "Cache-Control: no-cache"
    })
    @GET("v1/permission-access/query/{sessionKey}/{fileName}")
    Call<DataGetEncryptedResponse> getDataFile(@Path("sessionKey") String sessionKey, @Path("fileName") String fileName);

    @GET("v1/permission-access/query/{sessionKey}/{fileName}")
    Call<DataGetResponse> getDataFileUnencrypted(@Path("sessionKey") String sessionKey, @Path("fileName") String fileName);
}

