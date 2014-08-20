package com.harbinpointech.carcenter.data;

import android.text.TextUtils;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Created by John on 2014/8/8.
 */
public class WebHelper {

    public static final String URL = "http://182.254.136.208:81/WCF/Service.svc/";
    //    public static final String URL = "http://192.168.1.101:81/service.svc/";

    public static boolean hasLogined(){
        return !TextUtils.isEmpty(JSESSIONID);
    }

    public static void logout(){
        JSESSIONID = null;
    }

    private static String JSESSIONID = null;

    public static int login(String usr, String password) throws JSONException, IOException, NoSuchAlgorithmException {
        JSESSIONID = null;
        byte[] md5Pwd = MD5(password.getBytes());
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < md5Pwd.length; i++) {
            sb.append(String.format(i == md5Pwd.length - 1 ? "%02X" : "%02X-", md5Pwd[i]));
        }
        String pwdEncrypt = sb.toString();
        JSONObject[] jsons = new JSONObject[]{new JSONObject(String.format("{\"%s\":\"%s\",\"%s\":\"%s\"}", "name", usr, "password", pwdEncrypt))};
        int result = doPost(URL + "Login", jsons);
        if (result == 200) {
            return jsons[0].getBoolean("d") ? 0 : 1;
        } else {
            return result;
        }
    }

    public static int getCars(JSONObject[] params) throws IOException, JSONException {
        JSONObject[] param = new JSONObject[]{null};
        int result = doPost(URL + "MobileGetCars", param);
        if (result == 200) {
            params[0] = param[0];
            result = 0;
        }
        return result;
    }

    /**
     * public ServiceBaseInfo MobileGetCarBaseInfos(string carName, bool isGetImg)
     * @param params
     * @param carName   车牌号
     * @param getCarImg 是否获取图片
     * @return
     */
    public static int getCarBaseInfos(JSONObject[]params, String carName, boolean getCarImg) throws JSONException, IOException {
        params[0] = new JSONObject(String.format("{\"%s\":\"%s\",\"%s\":\"%s\"}", "carName", carName, "isGetImg", getCarImg));
        int result = doPost(URL + "MobileGetCarBaseInfos", params);
        if (result == 200) {
            return 0;
        } else {
            return result;
        }
    }

    /**
     * public ServiceDevice[] MobileGetCarDeviceInfo(string CarName)
     * @param params
     * @param carName   车牌号
     * @return
     * @throws JSONException
     * @throws IOException
     */
    public static int getCarPluginInfos(JSONObject[]params, String carName) throws JSONException, IOException {
        params[0] = new JSONObject(String.format("{\"%s\":\"%s\"}", "carName", carName));
        int result = doPost(URL + "MobileGetCarDeviceInfo", params);
        if (result == 200) {
            return 0;
        } else {
            return result;
        }
    }

    public static byte[] MD5(byte[] input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(input);
        return md.digest();
    }

    private static int doPost(String url, JSONObject[] json) throws IOException, JSONException {
        HttpPost request = new HttpPost(url);
        request.setHeader("Accept", "application/json");
        request.setHeader("Content-type", "application/json");
        if (json[0] != null) {
            StringEntity entity = new StringEntity(json[0].toString());
            request.setEntity(entity);
        } else {
        }
        Log.w("WEB_HELPER_request",url + ", " + json[0]);
        if (!TextUtils.isEmpty(JSESSIONID)) {
            request.setHeader("Cookie", "ASP.NET_SessionId=" + JSESSIONID);
        }
        DefaultHttpClient httpClient = new DefaultHttpClient();

        HttpResponse response = httpClient.execute(request);
        StatusLine sl = response.getStatusLine();
        if (sl == null) {
            return -1;
        }

        if (sl.getStatusCode() == HttpURLConnection.HTTP_OK) {
            if (response.getEntity() == null) {
                return -1;
            }

             /* 获取cookieStore */
            CookieStore cookieStore = httpClient.getCookieStore();
            List<Cookie> cookies = cookieStore.getCookies();
            for (int i = 0; i < cookies.size(); i++) {
                if ("ASP.NET_SessionId".equals(cookies.get(i).getName())) {
                    JSESSIONID = cookies.get(i).getValue();
                    break;
                }
            }

            String jsonStr = EntityUtils.toString(response.getEntity());
            json[0] = new JSONObject(jsonStr);
            Log.w("WEB_HELPER_response",json[0].toString());
        }
        return sl.getStatusCode();
    }
}
