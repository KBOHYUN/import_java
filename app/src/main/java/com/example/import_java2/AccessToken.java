package com.example.import_java2;

import com.google.gson.annotations.SerializedName;

public class AccessToken {

    @SerializedName("access_token")
    String token;

    @SerializedName("now")
    int now;

    @SerializedName("expired_at")
    int expired_at;


    public String getToken() {
        return this.token;
    }

}