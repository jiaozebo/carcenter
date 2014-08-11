package com.harbinpointech.carcenter.data;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by John on 2014/8/8.
 */
public class WebHelper {

    public static final String URL = "http://182.254.136.208:81/WCF/Service.svc/";

    public static int login(String usr, String password) throws JSONException, IOException, NoSuchAlgorithmException {
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

    public static int getAllCarPositions(JSONObject[] carPositions) throws IOException, JSONException {
        JSONObject[] param = new JSONObject[]{null};
        int result = doPost(URL + "GetCarTrajectory", param);
        if (result == 200) {
            carPositions[0] = param[0];
        }
        return result;
    }

    public static int getCars(JSONObject[] params) throws IOException, JSONException {
        JSONObject[] param = new JSONObject[]{null};
        int result = doPost(URL + "GetCars", param);
        if (result == 200) {
            params[0] = param[0];
        }
        result = 0;
//        param[0] = new JSONObject("{\"name\":\"car1\",\"lat\":}");

        return result;
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
            request.setEntity(null);
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
            String jsonStr = EntityUtils.toString(response.getEntity());
            json[0] = new JSONObject(jsonStr);
        }
        return sl.getStatusCode();
    }
}
