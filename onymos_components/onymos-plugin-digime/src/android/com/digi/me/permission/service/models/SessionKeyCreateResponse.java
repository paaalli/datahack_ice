/*
 * Copyright © 2017 digi.me. All rights reserved.
 */

package com.digi.me.permission.service.models;

import com.google.gson.annotations.SerializedName;

public class SessionKeyCreateResponse {
    @SerializedName("sessionKey")
    public String sessionKey;

    @SerializedName("expiry")
    public long expiry;
}