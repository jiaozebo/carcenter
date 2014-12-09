package com.harbinpointech.carcenter.util;


/**
 * Created by John on 2014/11/21.
 */
public class GPSConvertor {
    private static final double PI = 3.14159265358979324;
    private static final double x_pi;

    static {
        x_pi = PI * 3000.0 / 180.0;
    }


    public static void gps2bd(double[] latlng) {
        gcj_encrypt(latlng);
        bd_encrypt(latlng);
    }

    /**
     * WGS-84 to GCJ-02
     */
    private static void gcj_encrypt(final double[] latlng) {
        double lat = latlng[0], lng = latlng[1];
        if (outOfChina(lat, lng))
            return;
        delta(latlng);
        latlng[0] += lat;
        latlng[1] += lng;
    }

    /**
     * GCJ-02 to WGS-84
     */
    private static void gcj_decrypt(double[] latlng) {
        double lat = latlng[0], lng = latlng[1];
        if (outOfChina(lat, lng))
            return;
        delta(latlng);
        latlng[0] = lat - latlng[0];
        latlng[1] = lng - latlng[1];
    }

    //GCJ-02 to BD-09
    private static void bd_encrypt(double[] $latlng) {
        double $x = $latlng[1];
        double $y = $latlng[0];
        double $z = Math.sqrt($x * $x + $y * $y) + 0.00002 * Math.sin($y * x_pi);
        double $theta = Math.atan2($y, $x) + 0.000003 * Math.cos($x * x_pi);
        double $bdLon = $z * Math.cos($theta) + 0.0065;
        double $bdLat = $z * Math.sin($theta) + 0.006;
        $latlng[0] = $bdLat;
        $latlng[1] = $bdLon;
    }

    //BD-09 to GCJ-02
    private static void bd_decrypt(double[] $latlng) {
        double $x = $latlng[1] - 0.0065;
        double $y = $latlng[0] - 0.006;
        double $z = Math.sqrt($x * $x + $y * $y) - 0.00002 * Math.sin($y * x_pi);
        double $theta = Math.atan2($y, $x) - 0.000003 * Math.cos($x * x_pi);
        $latlng[1] += $z * Math.cos($theta);
        $latlng[0] += $z * Math.sin($theta);
    }
//    //GCJ-02 to WGS-84 exactly
//    public function gcj_decrypt_exact($gcjLat, $gcjLon) {
//        $initDelta = 0.01;
//        $threshold = 0.000000001;
//        $dLat = $initDelta;
//        $dLon = $initDelta;
//        $mLat = $gcjLat - $dLat;
//        $mLon = $gcjLon - $dLon;
//        $pLat = $gcjLat + $dLat;
//        $pLon = $gcjLon + $dLon;
//        $wgsLat = 0;
//        $wgsLon = 0;
//        $i = 0;
//        while (TRUE) {
//            $wgsLat = ($mLat + $pLat) / 2;
//            $wgsLon = ($mLon + $pLon) / 2;
//            $tmp = $this -> gcj_encrypt($wgsLat, $wgsLon);
//            $dLat = $tmp['lat'] - $gcjLat;
//            $dLon = $tmp['lon'] - $gcjLon;
//            if ((abs($dLat) < $threshold) && (abs($dLon) < $threshold))
//                break;
//
//            if ($dLat > 0) $pLat = $wgsLat;
//            else $mLat = $wgsLat;
//            if ($dLon > 0) $pLon = $wgsLon;
//            else $mLon = $wgsLon;
//
//            if (++$i > 10000) break;
//        }
//        //console.log(i);
//        return array('lat' = > $wgsLat,'lon' =>$wgsLon);
//    }
//
//    //GCJ-02 to BD-09
//    public function bd_encrypt($gcjLat, $gcjLon) {
//        $x = $gcjLon;
//        $y = $gcjLat;
//        $z = sqrt($x * $x + $y * $y) + 0.00002 * sin($y * $this -> x_pi);
//        $theta = atan2($y, $x) + 0.000003 * cos($x * $this -> x_pi);
//        $bdLon = $z * cos($theta) + 0.0065;
//        $bdLat = $z * sin($theta) + 0.006;
//        return array('lat' = > $bdLat,'lon' =>$bdLon);
//    }
//
//    //BD-09 to GCJ-02
//    public function bd_decrypt($bdLat, $bdLon) {
//        $x = $bdLon - 0.0065;
//        $y = $bdLat - 0.006;
//        $z = sqrt($x * $x + $y * $y) - 0.00002 * sin($y * $this -> x_pi);
//        $theta = atan2($y, $x) - 0.000003 * cos($x * $this -> x_pi);
//        $$gcjLon = $z * cos($theta);
//        $gcjLat = $z * sin($theta);
//        return array('lat' = > $gcjLat,'lon' =>$gcjLon);
//    }
//
//    //WGS-84 to Web mercator
//    //$mercatorLat -> y $mercatorLon -> x
//    public function mercator_encrypt($wgsLat, $wgsLon) {
//        $x = $wgsLon * 20037508.34 / 180.;
//        $y = log(tan((90. + $wgsLat) * PI / 360.)) / (PI / 180.);
//        $y = $y * 20037508.34 / 180.;
//        return array('lat' = > $y,'lon' =>$x);
//        /*
//        if ((abs($wgsLon) > 180 || abs($wgsLat) > 90))
//            return NULL;
//        $x = 6378137.0 * $wgsLon * 0.017453292519943295;
//        $a = $wgsLat * 0.017453292519943295;
//        $y = 3189068.5 * log((1.0 + sin($a)) / (1.0 - sin($a)));
//        return array('lat' => $y, 'lon' => $x);
//        //*/
//    }
//
//    // Web mercator to WGS-84
//    // $mercatorLat -> y $mercatorLon -> x
//    public function mercator_decrypt($mercatorLat, $mercatorLon) {
//        $x = $mercatorLon / 20037508.34 * 180.;
//        $y = $mercatorLat / 20037508.34 * 180.;
//        $y = 180 / PI * (2 * atan(exp($y * PI / 180.)) - PI / 2);
//        return array('lat' = > $y,'lon' =>$x);
//        /*
//        if (abs($mercatorLon) < 180 && abs($mercatorLat) < 90)
//            return NULL;
//        if ((abs($mercatorLon) > 20037508.3427892) || (abs($mercatorLat) > 20037508.3427892))
//            return NULL;
//        $a = $mercatorLon / 6378137.0 * 57.295779513082323;
//        $x = $a - (floor((($a + 180.0) / 360.0)) * 360.0);
//        $y = (1.5707963267948966 - (2.0 * atan(exp((-1.0 * $mercatorLat) / 6378137.0)))) * 57.295779513082323;
//        return array('lat' => $y, 'lon' => $x);
//        //*/
//    }

    // two point's distance
//    public static double distance(double $latA, double $lonA, double $latB, double $lonB) {
//        double $earthR = 6371000.;
//        double $x = Math.cos($latA * PI / 180.) * Math.cos($latB * PI / 180.) * Math.cos(($lonA - $lonB) * PI / 180);
//        double $y = Math.sin($latA * PI / 180.) * Math.sin($latB * PI / 180.);
//        double $s = $x + $y;
//        if ($s > 1) $s = 1;
//        if ($s < -1) $s = -1;
//        double $alpha = Math.acos($s);
//        double $distance = $alpha * $earthR;
//        return $distance;
//    }
    
    /*
    * 
    *     private function delta($lat, $lon)
    {
        // Krasovsky 1940
        //
        // a = 6378245.0, 1/f = 298.3
        // b = a * (1 - f)
        // ee = (a^2 - b^2) / a^2;
        $a = 6378245.0;//  a: 卫星椭球坐标投影到平面地图坐标系的投影因子。
        $ee = 0.00669342162296594323;//  ee: 椭球的偏心率。
        $dLat = $this->transformLat($lon - 105.0, $lat - 35.0);
        $dLon = $this->transformLon($lon - 105.0, $lat - 35.0);
        $radLat = $lat / 180.0 * $this->PI;
        $magic = sin($radLat);
        $magic = 1 - $ee * $magic * $magic;
        $sqrtMagic = sqrt($magic);
        $dLat = ($dLat * 180.0) / (($a * (1 - $ee)) / ($magic * $sqrtMagic) * $this->PI);
        $dLon = ($dLon * 180.0) / ($a / $sqrtMagic * cos($radLat) * $this->PI);
        return array('lat' => $dLat, 'lon' => $dLon);
    }

    * */

    private static void delta(double[] latlng) {
        double $lat = latlng[0], $lon = latlng[1];
        // Krasovsky 1940
        //
        // a = 6378245.0, 1/f = 298.3
        // b = a * (1 - f)
        // ee = (a^2 - b^2) / a^2;
        double $a = 6378245.0;//  a: 卫星椭球坐标投影到平面地图坐标系的投影因子。
        double $ee = 0.00669342162296594323;//  ee: 椭球的偏心率。
        double $dLat = transformLat($lon - 105.0, $lat - 35.0);
        double $dLon = transformLon($lon - 105.0, $lat - 35.0);
        double $radLat = $lat / 180.0 * PI;
        double $magic = Math.sin($radLat);
        $magic = 1 - $ee * $magic * $magic;
        double $sqrtMagic = Math.sqrt($magic);
        $dLat = ($dLat * 180.0) / (($a * (1 - $ee)) / ($magic * $sqrtMagic) * PI);
        $dLon = ($dLon * 180.0) / ($a / $sqrtMagic * Math.cos($radLat) * PI);
//        return array('lat' => $dLat, 'lon' => $dLon);

        latlng[0] = $dLat;
        latlng[1] = $dLon;
    }

    private static boolean outOfChina(double lat, double lng) {
        if (lng < 72.004 || lng > 137.8347)
            return true;
        if (lat < 0.8293 || lat > 55.8271)
            return true;
        return false;
    }

    private static double transformLat(double $x, double $y) {
        double $ret = -100.0 + 2.0 * $x + 3.0 * $y + 0.2 * $y * $y + 0.1 * $x * $y + 0.2 * Math.sqrt(Math.abs($x));
        $ret += (20.0 * Math.sin(6.0 * $x * PI) + 20.0 * Math.sin(2.0 * $x * PI)) * 2.0 / 3.0;
        $ret += (20.0 * Math.sin($y * PI) + 40.0 * Math.sin($y / 3.0 * PI)) * 2.0 / 3.0;
        $ret += (160.0 * Math.sin($y / 12.0 * PI) + 320 * Math.sin($y * PI / 30.0)) * 2.0 / 3.0;
        return $ret;
    }

    private static double transformLon(double $x, double $y) {
        double $ret = 300.0 + $x + 2.0 * $y + 0.1 * $x * $x + 0.1 * $x * $y + 0.1 * Math.sqrt(Math.abs($x));
        $ret += (20.0 * Math.sin(6.0 * $x * PI) + 20.0 * Math.sin(2.0 * $x * PI)) * 2.0 / 3.0;
        $ret += (20.0 * Math.sin($x * PI) + 40.0 * Math.sin($x / 3.0 * PI)) * 2.0 / 3.0;
        $ret += (150.0 * Math.sin($x / 12.0 * PI) + 300.0 * Math.sin($x / 30.0 * PI)) * 2.0 / 3.0;
        return $ret;
    }
}