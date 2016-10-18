/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.kix.core;

import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Thomas Witthohn
 */
public class KIXCalcTest {

    public KIXCalcTest() {
    }
    TsPeriod start = new TsPeriod(TsFrequency.Quarterly, 2004, 0);

    double[] aData = {89.78, 93.71, 90.85, 95.20, 95.47, 98.29, 100.15, 104.76, 107.69, 109.16, 112.40, 122.26, 119.73, 120.83, 122.45, 128.06, 128.39, 127.53, 125.12, 118.38, 102.32, 100.80, 105.21, 109.90};
    TsData a = new TsData(start, aData, true);

    public void testChainSum_TsData() {
        System.out.println("chainSum");
        double[] weightedSumData = {95.95, 101.52, 99.12, 103.42, 102.45, 105.93,
            109.07, 114.39, 106.83, 110.35, 112.90, 124.07, 104.93, 106.49, 108.08,
            113.56, 103.94, 103.40, 102.13, 99.01, 84.68, 83.76, 86.37, 90.75};
        TsData weightedSum = new TsData(new TsPeriod(TsFrequency.Quarterly, 2004, 0), weightedSumData, true);

        double[] expResultData = {95.95, 101.52, 99.12, 103.42, 102.45, 105.93,
            109.07, 114.39, 115.33, 119.13, 121.88, 133.94, 128.62, 130.52, 132.48,
            139.19, 137.94, 137.21, 135.53, 131.39, 114.76, 113.51, 117.04, 122.98};

        TsData result = KIXCalc.chainSum(weightedSum);
        Assert.assertArrayEquals(expResultData, result.getValues().internalStorage(), 0.01);
    }

    @Test
    public void testUnchain_TsData() {
        System.out.println("unchain");

        double[] expResultData = {97.18, 101.43, 98.34, 103.05, 103.34, 106.39, 108.41, 113.40, 108.05, 109.52, 112.77, 122.67, 106.07, 107.05, 108.48, 113.45, 104.58, 103.88, 101.92, 96.43, 81.95, 80.73, 84.27, 88.02};
        TsData result = KIXCalc.unchain(a);
        Assert.assertArrayEquals(expResultData, result.getValues().internalStorage(), 0.005);
    }

    @Test
    public void testMid() {
        System.out.println("mid");

        double[] expResultData = {92.39, 92.39, 92.39, 92.39, 99.67, 99.67, 99.67, 99.67, 112.88, 112.88, 112.88, 112.88, 122.77, 122.77, 122.77, 122.77, 124.86, 124.86, 124.86, 124.86, 104.56, 104.56, 104.56, 104.56};
        TsData result = KIXCalc.mid(a, false);
        Assert.assertArrayEquals(expResultData, result.getValues().internalStorage(), 0.0051);
    }

}
