package com.myphuoc.sheets;

public class UserModal {
    private String Access_Point;
    private String RSSI;

    public String getAccess_Point(){
        return Access_Point;
    }

    public void setAccess_Point(String Access_Point){
        this.Access_Point = Access_Point;
    }

    public String getRSSI(){
        return RSSI;
    }

    public void setRSSI(String RSSI){
        this.RSSI = RSSI;
    }

    public UserModal(String Access_Point, String RSSI){
        this.Access_Point = Access_Point;
        this.RSSI = RSSI;
    }
}

