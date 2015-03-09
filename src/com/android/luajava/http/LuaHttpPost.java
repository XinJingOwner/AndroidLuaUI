package com.android.luajava.http;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

public class LuaHttpPost {
    private List<NameValuePair> mHeaders = new ArrayList<NameValuePair>();
    private List<NameValuePair> mParameters = new ArrayList<NameValuePair>();

    public void addHeader(String name, String value) {
        mHeaders.add(new BasicNameValuePair(name, value));
    }

    public void setParameter(String name, String value) {
        mParameters.add(new BasicNameValuePair(name, value));
    }

    /**
     *
     * @param url
     * @return http output, empty string if error
     */
    public String execute(String url) {
        HttpHelper http = HttpHelper.getInstance();
        String output =  http.post(url, mParameters, mHeaders);
        return output == null ? "" : output;
    }
}
