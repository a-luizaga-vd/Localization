package com.example.localization.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LocationResponse {

    @SerializedName("logitud")
    @Expose
    private String logitud;
    @SerializedName("latitud")
    @Expose
    private String latitud;
    @SerializedName("userName")
    @Expose
    private String userName;
    @SerializedName("string1")
    @Expose
    private String string1;
    @SerializedName("string2")
    @Expose
    private String string2;
    @SerializedName("string3")
    @Expose
    private String string3;

    public String getLogitud() {
        return logitud;
    }

    public void setLogitud(String logitud) {
        this.logitud = logitud;
    }

    public String getLatitud() {
        return latitud;
    }

    public void setLatitud(String latitud) {
        this.latitud = latitud;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getString1() {
        return string1;
    }

    public void setString1(String string1) {
        this.string1 = string1;
    }

    public String getString2() {
        return string2;
    }

    public void setString2(String string2) {
        this.string2 = string2;
    }

    public String getString3() {
        return string3;
    }

    public void setString3(String string3) {
        this.string3 = string3;
    }
}
