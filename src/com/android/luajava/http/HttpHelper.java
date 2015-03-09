package com.android.luajava.http;

import android.util.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class HttpHelper {

    private static final String TAG = "HttpHelper";
    private static final int CONNECT_TIME_OUT = 3000;
    private static final int READ_TIME_OUT = 6000;
    private static final List<NameValuePair> EMPTY_HEADERS = new ArrayList<NameValuePair>();

    private HttpClient mClient = null;
    private static HttpHelper mInstance = null;

    public static HttpHelper getInstance() {
        if (mInstance == null)
            mInstance = new HttpHelper();

        return mInstance;
    }

    private HttpHelper() {
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, CONNECT_TIME_OUT);
        HttpConnectionParams.setSoTimeout(params, READ_TIME_OUT);
        HttpConnectionParams.setSocketBufferSize(params, 8192);

        mClient = new DefaultHttpClient(params);
    }

//    public String get(String url) {
//    }
//
//    public String get(String url, List<NameValuePair> params) {
//
//    }

    /**
     *
     * @param url
     * @param params not null
     * @param headers not null
     * @return http-get output, null if exception
     */
    public String get(String url, List<NameValuePair> params, List<NameValuePair> headers) {
        Log.d(TAG, "http-get, url: " + url);

        if (mClient == null)
            return null;

        StringBuilder paramsStr = new StringBuilder();
        boolean firstParam = true;
        for (NameValuePair param : params) {
            if (firstParam) {
                paramsStr.append('?');
                firstParam = false;
            } else
                paramsStr.append('&');

            paramsStr.append(param.getName())
                    .append('=')
                    .append(param.getValue());
        }

        String encodedParamsStr = null;
        try {
            encodedParamsStr = URLEncoder.encode(paramsStr.toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
        Log.d(TAG, "encodedParamsStr = '" + encodedParamsStr + "'");

        String response = null;
        try {
            HttpEntity entity = executeHttpGet(url + encodedParamsStr, headers);
            if (entity != null) {
                response = EntityUtils.toString(entity);
            }
        } catch (URISyntaxException e) {
            Log.w(TAG, "Error execute http GET: " + e);
        } catch (ClientProtocolException e) {
            Log.w(TAG, "Error execute http GET: " + e);
        } catch (IOException e) {
            Log.w(TAG, "Error execute http GET: " + e);
        }

        return response;
    }

    /* Not used now */
    public byte[] GetBinary(String url) {
        byte[] binContent = null;

        try {
            if (mClient == null)
                return null;

            HttpEntity rsp_entity = executeHttpGet(url, EMPTY_HEADERS);

            if (rsp_entity != null) {
                binContent = EntityUtils.toByteArray(rsp_entity);
            }

        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return binContent;
    }

    private HttpEntity executeHttpGet(String url, List<NameValuePair> headers)
            throws IOException, URISyntaxException {
        HttpGet httpGet = new HttpGet(new URI(url));
        for (NameValuePair header : headers) {
            httpGet.addHeader(header.getName(), header.getValue());
        }
        HttpResponse rsp = mClient.execute(httpGet);

        HttpEntity rsp_entity = null;
        if (rsp.getStatusLine().getStatusCode() == 200)
            rsp_entity = rsp.getEntity();

        Log.d(TAG, "Status: " + rsp.getStatusLine().toString());
        return rsp_entity;
    }

    /* Not used now */
    public String post(String url, String content) {

        String rsp_content = null;
        HttpResponse rsp = null;
        HttpEntity rsp_entity = null;

        try {
            if (mClient == null)
                return null;

            HttpPost httpPost = new HttpPost(url);

            if (content != null) {
                httpPost.setEntity(new StringEntity(content));
            }
            rsp = mClient.execute(httpPost);
            rsp_entity = rsp.getEntity();

            CookieStore cookies = ((AbstractHttpClient) mClient)
                    .getCookieStore();
            Log.d(TAG, "cookies: " + cookies.getCookies());

            Log.d(TAG, rsp.getStatusLine().toString());

            if (rsp.getStatusLine().getStatusCode() == 200)
                rsp_content = EntityUtils.toString(rsp_entity);
            else
                rsp_content = null;

        } catch (ClientProtocolException e) {
        } catch (IOException e) {
        }

        return rsp_content;
    }

    public String post(String url, List<NameValuePair> params) {

        String rsp_content = null;
        HttpResponse rsp = null;
        HttpEntity rsp_entity = null;

        try {
            if (mClient == null)
                return null;

            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            rsp = mClient.execute(httpPost);
            rsp_entity = rsp.getEntity();
//            Log.d(TAG, "Status: " + rsp.getStatusLine().toString());

            if (rsp.getStatusLine().getStatusCode() == 200)
                rsp_content = EntityUtils.toString(rsp_entity);
        } catch (ClientProtocolException e) {
            Log.d(TAG, "Error execute http POST: " + e);
        } catch (IOException e) {
            Log.d(TAG, "Error execute http POST: " + e);
        }

        return rsp_content;
    }

    /**
     *
     * @param url
     * @param params not null
     * @param headers not null
     * @return
     */
    public String post(String url, List<NameValuePair> params, List<NameValuePair> headers) {

        String rsp_content = null;
        HttpResponse rsp = null;
        HttpEntity rsp_entity = null;

        if (mClient == null)
            return null;

        try {

            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            for (NameValuePair header : headers) {
                httpPost.addHeader(header.getName(), header.getValue());
            }
            rsp = mClient.execute(httpPost);
            rsp_entity = rsp.getEntity();

            if (rsp.getStatusLine().getStatusCode() == 200)
                rsp_content = EntityUtils.toString(rsp_entity);
        } catch (ClientProtocolException e) {
            Log.w(TAG, "Error execute http POST: " + e);
        } catch (IOException e) {
            Log.w(TAG, "Error execute http POST: " + e);
        }

        return rsp_content;
    }

    public void close() {
        mClient.getConnectionManager().shutdown();

        mInstance = null;
        mClient = null;
    }
}
