
/*
 * Copyright © 2017 Onymos Inc and digi.me. All rights reserved.
 */

package com.digi.me.permission.service.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DataGetResponse {

    @SerializedName("fileContent")
    public List<File> fileContent;

    @SerializedName("fileList")
    public List<String> fileList;
}