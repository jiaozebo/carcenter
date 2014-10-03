package com.harbinpointech.carcenter.data;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
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

    public static boolean hasLogined() {
        return !TextUtils.isEmpty(JSESSIONID);
    }

    public static void logout() {
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
     *
     * @param params
     * @param carName   车牌号
     * @param getCarImg 是否获取图片
     * @return
     */
    public static int getCarBaseInfos(JSONObject[] params, String carName, boolean getCarImg) throws JSONException, IOException {
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
     *
     * @param params
     * @param carName 车牌号
     * @return
     * @throws JSONException
     * @throws IOException
     */
    public static int getCarPluginInfos(JSONObject[] params, String carName) throws JSONException, IOException {
        params[0] = new JSONObject(String.format("{\"%s\":\"%s\"}", "carName", carName));
        int result = doPost(URL + "MobileGetCarDeviceInfo", params);
        if (result == 200) {
            return 0;
        } else {
            return result;
        }
    }

    /**
     * @param params
     * @return
     * @throws JSONException
     * @throws IOException
     */
    public static int getAllUsers(JSONObject[] params) throws JSONException, IOException {
        int result = doPost(URL + "GetAllUsers", params);
        if (result == 200) {
            return 0;
        } else {
            return result;
        }
    }

    /**
     * @param message
     * @param users
     * @return
     * @throws JSONException
     * @throws IOException
     */
    public static int sendMessage(String message, String... users) throws JSONException, IOException {
        JSONObject json = new JSONObject();
        json.put("message", new String(message.getBytes(), "ISO8859-1"));
        JSONArray ja = new JSONArray();
        for (String user : users) {
//            ja.put(new JSONObject(String.format("{\"%s\":\"%s\"}", "id", String.valueOf(user))));
            ja.put(user);
        }
        json.put("userIDs", ja);
        int result = doPost(URL + "SendMessage", new JSONObject[]{json});
        if (result == 200) {
            return 0;
        } else {
            return result;
        }
    }


    public static int recvMessage(JSONObject[] params) throws JSONException, IOException {
        int result = doPost(URL + "ReceiveMessage", params);
        if (result == 200) {
            return 0;
        } else {
            return result;
        }
    }

    public static int createMessageGroup(String name) throws JSONException, IOException {
        JSONObject obj = new JSONObject();
        obj.put("name", name);
        JSONObject[] params = new JSONObject[]{obj};
        int result = doPost(URL + "CreateMessageGroup", params);
        if (result == 200) {
            return 0;
        } else {
            return result;
        }
    }


    //
    public static int singin(String carName, double latitude, double longitude) throws JSONException, IOException {
        JSONObject[] params = new JSONObject[]{new JSONObject(String.format("{\"%s\":\"%s\", \"%s\":\"%.02f\",\"%s\":\"%.02f\"}", "carName", carName, "lat", latitude, "lng", longitude))};
        int result = doPost(URL + "Signin", params);
        if (result == 200) {
            return params[0].getBoolean("d") ? 0 : 1;
        } else {
            return result;
        }
    }

    public static byte[] MD5(byte[] input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(input);
        return md.digest();
    }


    public static void gps2lnglat(double[] data) {
        for (int i = 0; i < data.length; i++) {
            double d = data[i];
            d /= 100;
            int d1 = (int) d;
            double d2 = d - d1;
            d2 *= 100;
            d2 /= 60;
            d = d1 + d2;
            data[i] = d;
        }
    }

    private static int doPost(String url, JSONObject[] json) throws IOException, JSONException {

//        PrintStream ps = new PrintStream(new FileOutputStream("/sdcard/car.txt", true));

        HttpPost request = new HttpPost(url);
        request.setHeader("Accept", "application/json");
        request.setHeader("Content-type", "application/json");
        if (json[0] != null) {
            StringEntity entity = new StringEntity(json[0].toString());
            request.setEntity(entity);
        } else {
        }
        Log.w("WEB_HELPER_request", url + ", " + json[0]);
//        ps.println("REQUEST:    *******");
//        ps.println(url);
//        ps.println(json[0]);
        if (!TextUtils.isEmpty(JSESSIONID)) {
            request.setHeader("Cookie", "ASP.NET_SessionId=" + JSESSIONID);
        }
        DefaultHttpClient httpClient = new DefaultHttpClient();

        HttpResponse response = httpClient.execute(request);
        StatusLine sl = response.getStatusLine();
        if (sl == null) {
//            ps.println("RESPONSE:    *******");
//            ps.println(url);
//            ps.println(response);
//            ps.close();
            return -1;
        }

        if (sl.getStatusCode() == HttpURLConnection.HTTP_OK) {
            if (response.getEntity() == null) {
//                ps.println("RESPONSE:    *******");
//                ps.println(url);
//                ps.println(response);
//                ps.close();
                return -1;
            }

            if (!hasLogined()) {
             /* 获取cookieStore */
                CookieStore cookieStore = httpClient.getCookieStore();
                List<Cookie> cookies = cookieStore.getCookies();
                for (int i = 0; i < cookies.size(); i++) {
                    if ("ASP.NET_SessionId".equals(cookies.get(i).getName())) {
                        JSESSIONID = cookies.get(i).getValue();
                        break;
                    }
                }
            }
            String jsonStr = EntityUtils.toString(response.getEntity());
//            ps.println("RESPONSE:    ");
//            ps.println(url);
//            ps.println(jsonStr);
//            ps.close();
            json[0] = new JSONObject(jsonStr);
            Log.w("WEB_HELPER_response", json[0].toString());
        }
        return sl.getStatusCode();
    }

    public static int fixPoint(String url, double[] data) throws IOException, JSONException {

//        PrintStream ps = new PrintStream(new FileOutputStream("/sdcard/car.txt", true));

        HttpGet request = new HttpGet(String.format("%s?%s=%s&%s=%s&%s=%s&%s=%s", url, "from", "0", "to", "4", "x", String.valueOf(data[1]), "y", String.valueOf(data[0])));
//        request.setHeader("Accept", "application/json");
//        request.setHeader("Content-type", "application/json");
//        if (json[0] != null) {
//            StringEntity entity = new StringEntity(json[0].toString());
//            request.setEntity(entity);
//        } else {
//        }
//        Log.w("WEB_HELPER_request", url + ", " + json[0]);
//        ps.println("REQUEST:    *******");
//        ps.println(url);
//        ps.println(json[0]);
//        if (!TextUtils.isEmpty(JSESSIONID)) {
//            request.setHeader("Cookie", "ASP.NET_SessionId=" + JSESSIONID);
//        }
//        HttpParams p = request.getParams();
//        p.setIntParameter("from", 0);
//        p.setIntParameter("to", 4);
//        p.setDoubleParameter("x",  data[0]);
//        p.setDoubleParameter("y",data[1]);
//        request.setParams(p);
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
            JSONObject json = new JSONObject(jsonStr);
            int result = json.getInt("error");
            if (result == 0) {
//                {"error":0,"x":"MTE4LjM2MjYyNzA1MjQ4","y":"MzguMTYwNzUwMjI5MTM3"}
                double dx = data[1];
                double dy = data[0];

                String x = json.getString("x");
                String y = json.getString("y");
                x = new String(Base64.decode(x, 0));
                data[1] = Double.parseDouble(x);

                y = new String(Base64.decode(y, 0));
                data[0] = Double.parseDouble(y);

                Log.i(JSESSIONID, String.format("%.08f,%.08f->%.08f,%.08f", dx, dy, data[1], data[0]));
            }
            return result;
        }
        return -1;
    }

    public static void AddRepaireRecord() {
    }

    public static void mobileGetRepaireRecords2() {
    }
}
