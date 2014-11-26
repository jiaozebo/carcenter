package com.harbinpointech.carcenter.test;

import android.test.InstrumentationTestCase;
import android.util.Log;

import com.harbinpointech.carcenter.util.GPSConvertor;

/**
 * Created by John on 2014/11/26.
 */
public class OffsetTest extends InstrumentationTestCase {

    public void testOffset() {
//        117.278715&y=31.864298

        double[] data = new double[]{31.864298, 117.278715};
        GPSConvertor.gps2bd(data);
        Log.d("offset",data[0] + "," +data[1]);
    }
}
