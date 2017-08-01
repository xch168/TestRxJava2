package com.github.xch168.testrxjava2;

import com.google.gson.annotations.SerializedName;

/**
 * Created by XuCanHui on 2017/8/1.
 */

public class MobileAddress {

    @SerializedName("error_code")
    private int errorCode;
    @SerializedName("reason")
    private String reason;
    @SerializedName("result")
    private ResultBean result;


    public static class ResultBean {
        @SerializedName("mobilenumber")
        public String mobileNumber;
        @SerializedName("mobilearea")
        public String mobileArea;
        @SerializedName("mobiletype")
        public String mobileType;
        @SerializedName("areacode")
        public String areaCode;
        @SerializedName("postcode")
        public String postCode;
    }


    @Override
    public String toString() {
        return "mobile number:" + result.mobileNumber + " area:" + result.mobileArea + " type:" + result.mobileType + " areaCode:" + result.areaCode + " postCode:" + result.postCode;
    }
}
