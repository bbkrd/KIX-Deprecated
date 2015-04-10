/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.nbdemetra.kix.calculation;

import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import org.junit.Test;

/**
 *
 * @author Thomas Witthohn
 */
public class KIXECalcTest {

    public KIXECalcTest() {
    }

    @Test
    public void TestUnchainQuarterlyLastQuarter() {
        double[] Data = {75.1, 78.1, 79.9, 79.7, 81.3, 86.1, 89.2, 90.8, 92.4, 95.6, 98.8, 103.5, 102.0,
                         107.4, 110.6, 113.9, 117.5, 121.1, 124.8, 128.4, 132.4, 136.2, 140.6, 144.7, 149.0};
        TsData a = new TsData(TsFrequency.Quarterly, 2007, 3, Data, true);

        TsData result = KIXECalc.unchain(a);

        double[] expResultData = {103.99467, 106.39148, 106.12517, 108.25566, 105.90406, 109.71710, 111.68512, 113.65314,
                                  103.46320, 106.92641, 112.01299, 110.38961, 105.29412, 108.43137, 111.66667, 115.19608,
                                  103.06383, 106.21277, 109.27660, 112.68085, 102.87009, 106.19335, 109.29003, 112.53776};
        assertEquals(expResultData, result.getValues().internalStorage());
    }

    @Test
    public void TestUnchainQuarterlyFirstQuarter() {
        double[] data = {78.1, 79.9, 79.7, 81.3, 86.1, 89.2, 90.8, 92.4, 95.6, 98.8, 103.5, 102.0,
                         107.4, 110.6, 113.9, 117.5, 121.1, 124.8, 128.4, 132.4, 136.2, 140.6, 144.7, 149.0};
        TsData a = new TsData(TsFrequency.Quarterly, 2008, 0, data, true);

        TsData result = KIXECalc.unchain(a);

        double[] expResultData = {105.90406, 109.71710, 111.68512, 113.65314, 103.46320, 106.92641, 112.01299, 110.38961, 105.29412, 108.43137,
                                  111.66667, 115.19608, 103.06383, 106.21277, 109.27660, 112.68085, 102.87009, 106.19335, 109.29003, 112.53776};
        assertEquals(expResultData, result.getValues().internalStorage());
    }

    @Test
    public void TestWeightSum() {
        double[] data1 = {103.99467, 106.39148, 106.12517, 108.25566, 105.90406, 109.71710, 111.68512, 113.65314,
                          103.46320, 106.92641, 112.01299, 110.38961, 105.29412, 108.43137, 111.66667, 115.19608,
                          103.06383, 106.21277, 109.27660, 112.68085, 102.87009, 106.19335, 109.29003, 112.53776};
        TsData a = new TsData(TsFrequency.Quarterly, 2008, 0, data1, true);

        double[] weight1 = {455.0, 443.0, 431.0, 419.0, 408.0, 380.0, 352.0, 324.0, 290.0, 313.0, 336.0, 359.0,
                            395.6, 387.0, 378.4, 369.8, 368.5, 350.0, 331.5, 313.0, 290.9, 311.8, 306.8, 301.8};
        TsData wa = new TsData(TsFrequency.Quarterly, 2007, 3, weight1, true);

        double[] data2 = {99.64912, 99.82456, 98.07018, 96.66667, 98.45735, 96.91470, 95.37205, 93.82940, 99.12959,
                          98.35590, 95.26112, 94.10058, 100.30832, 99.48613, 98.45838, 97.22508, 99.26004, 98.20296,
                          97.14588, 95.98309, 99.33921, 98.34802, 97.24670, 97.02643};
        TsData b = new TsData(TsFrequency.Quarterly, 2008, 0, data2, true);

        double[] weight2 = {176.0, 181.0, 186.0, 191.0, 195.0, 230.0, 265.0, 300.0, 366.0, 380.0, 394.0, 408.0,
                            430.5, 463.0, 495.5, 528.0, 558.0, 576.0, 594.0, 612.0, 648.6, 672.3, 698.0, 723.7};
        TsData wb = new TsData(TsFrequency.Quarterly, 2007, 3, weight2, true);

        TsData result = KIXECalc.weightSum(a, wa, b, wb);

        double[] expResultData = {102.78260, 104.55982, 103.87845, 105.02323, 103.49592, 105.57702, 106.40975, 107.24248,
                                  101.04537, 102.14469, 102.66667, 101.30152, 102.69590, 103.76980, 104.78352, 105.83097,
                                  100.77294, 101.38873, 101.97067, 102.62435, 100.43248, 100.77719, 100.97571, 101.82925};
        assertEquals(expResultData, result.getValues().internalStorage());

    }

    private void assertEquals(double[] expected, double[] actual) {
        assertEquals(expected, actual, 0.005);
    }

    private void assertEquals(double[] expected, double[] actual, double delta) {
        org.junit.Assert.assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            org.junit.Assert.assertEquals(expected[i], actual[i], 0.005);
        }
    }
}
