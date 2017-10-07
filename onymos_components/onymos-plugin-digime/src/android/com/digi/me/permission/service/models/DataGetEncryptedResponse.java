/*
 * Copyright © 2017 Onymos Inc and digi.me. All rights reserved.
 */

package com.digi.me.permission.service.models; 

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DataGetEncryptedResponse {

    @SerializedName("fileContent")
    public String fileContent;

    @SerializedName("fileList")
    public List<String> fileList;
}

