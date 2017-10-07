/*
 * Copyright © 2017 digi.me. All rights reserved.
 */

package com.digi.me.permission.service.models;

import com.google.gson.annotations.SerializedName;

public class RequestDataBody {
    @SerializedName("sessionKey")
    public String sessionKey;

    @SerializedName("fileName")
    public String fileName;

    public RequestDataBody(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public RequestDataBody(String sessionKey, String fileName) {
        this.sessionKey = sessionKey;
        this.fileName = fileName;
    }
}
