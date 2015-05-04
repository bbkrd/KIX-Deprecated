/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.nbdemetra.kix;

import ec.tss.TsCollection;
import ec.tstoolkit.timeseries.regression.TsVariable;
import ec.tstoolkit.timeseries.regression.TsVariables;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.utilities.DefaultNameValidator;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Thomas Witthohn
 */
public class KIXModelTest {

    public KIXModelTest() {
    }
    private final String VALIDATOR = ",= +-";
    private final double[] i1Data = {89.78, 93.71, 90.85, 95.20, 95.47, 98.29, 100.15, 104.76, 107.69, 109.16, 112.40, 122.26,
                                     119.73, 120.83, 122.45, 128.06, 128.39, 127.53, 125.12, 118.38, 102.32, 100.80, 105.21, 109.90};
    private final double[] w1Data = {175.09, 184.14, 178.95, 187.45, 188.09, 194.33, 197.97, 207.50, 214.56, 219.10, 225.54, 245.62,
                                     241.17, 244.79, 247.22, 257.58, 259.71, 260.26, 256.27, 240.33, 204.06, 199.94, 208.43, 218.29};
    private final double[] i2Data = {81.19, 93.96, 95.71, 97.31, 89.17, 94.84, 104.22, 111.00, 99.15, 115.17, 113.44, 132.40, 112.77,
                                     118.62, 121.56, 131.39, 121.06, 121.56, 125.29, 139.41, 128.07, 129.15, 125.46, 135.75};
    private final double[] w2Data = {25.15, 29.51, 30.30, 30.85, 28.51, 30.68, 32.95, 36.13, 31.86, 37.94, 37.00, 42.43, 36.29,
                                     38.60, 40.18, 43.60, 40.62, 40.57, 42.46, 46.63, 40.28, 39.44, 39.73, 44.00};
    private final TsVariables indices = new TsVariables("i", new DefaultNameValidator(VALIDATOR));
    private final TsVariables weights = new TsVariables("w", new DefaultNameValidator(VALIDATOR));
    private final KIXModel instance = new KIXModel();

    private void quarterlyData() {
        indices.clear();
        weights.clear();
        indices.set("i1", new TsVariable(new TsData(TsFrequency.Quarterly, 2004, 0, i1Data, true)));
        indices.set("i2", new TsVariable(new TsData(TsFrequency.Quarterly, 2004, 0, i2Data, true)));
        weights.set("w1", new TsVariable(new TsData(TsFrequency.Quarterly, 2004, 0, w1Data, true)));
        weights.set("w2", new TsVariable(new TsData(TsFrequency.Quarterly, 2004, 0, w2Data, true)));
    }

    @Test
    public void TestKIX_Add2Ts() {
        String inputString = "KIX,i1,w1,+,i2,w2,2005";
        quarterlyData();
        double[] expResult = {88.59, 93.74, 91.52, 95.49, 94.60, 97.81, 100.71, 105.62, 106.50, 110.00, 112.55, 123.68,
                              118.76, 120.52, 122.33, 128.52, 127.37, 126.70, 125.15, 121.32, 105.96, 104.81, 108.07, 113.56};
        TsCollection result = instance.parser(inputString, indices, weights);

        for (int i = 0; i < expResult.length; i++) {
            assertEquals(expResult[i], result.get(0).getTsData().getValues().internalStorage()[i], 0.005);
        }
    }

    @Test
    public void TestKIX_Subtract2Ts() {
        String inputString = "KIX,i1,w1,-,i2,w2,2005";
        quarterlyData();
        double[] expResult = {91.42, 93.66, 89.92, 94.8, 96.67, 98.95, 99.37, 103.57, 109.35, 107.99, 112.2, 120.29, 121.07,
                              121.25, 122.62, 127.42, 129.81, 128.68, 125.08, 114.3, 97.24, 95.21, 101.21, 104.8};
        TsCollection result = instance.parser(inputString, indices, weights);

        for (int i = 0; i < expResult.length; i++) {
            assertEquals(expResult[i], result.get(0).getTsData().getValues().internalStorage()[i], 0.005);
        }
    }

    @Test
    public void TestKIXE_Add2Ts() {
        String inputString = "KIXE,i1,w1,+,i2,w2,2005";
        quarterlyData();
        double[] expResult = {94.59634, 97.81158, 100.71445, 105.62539, 106.46924, 109.99229, 112.53047, 123.67321, 118.79002, 120.54374,
                              122.34560, 128.53707, 127.35740, 126.68997, 125.14942, 121.36334, 105.96989, 104.81759, 108.08206, 113.56458};
        TsCollection result = instance.parser(inputString, indices, weights);

        for (int i = 0; i < expResult.length; i++) {
            assertEquals(expResult[i], result.get(0).getTsData().getValues().internalStorage()[i], 0.005);
        }
    }

    @Test
    public void TestKIXE_Add2Ts2() {
        String inputString = "KIXE,i1,w1,+,i2,w2,2007";

        indices.clear();
        weights.clear();
        indices.set("i1", new TsVariable(new TsData(TsFrequency.Quarterly, 2004, 0, i1Data, true)));
        indices.set("i2", new TsVariable(new TsData(TsFrequency.Quarterly, 2006, 1, i2Data, true)));
        weights.set("w1", new TsVariable(new TsData(TsFrequency.Quarterly, 2004, 0, w1Data, true)));
        weights.set("w2", new TsVariable(new TsData(TsFrequency.Quarterly, 2006, 1, w2Data, true)));

        double[] expResult = {93.18165, 95.93406, 97.74947, 102.24898, 105.10875, 106.54351, 109.70585, 119.32951,
                              117.35039, 117.19164, 119.37548, 125.53398, 126.74698, 124.38076, 124.47468,
                              118.38063, 107.10292, 103.05270, 107.68883, 112.16246, 121.23252, 111.70111,
                              112.16246, 115.60410, 128.63251, 118.16919, 119.16569, 115.76096, 125.25546};
        TsCollection result = instance.parser(inputString, indices, weights);
        double[] resultDouble = result.get(0).getTsData().getValues().internalStorage();
        assertEquals(expResult.length, resultDouble.length);
        for (int i = 0; i < expResult.length; i++) {
            org.junit.Assert.assertEquals(expResult[i], resultDouble[i], 0.005);
        }
    }

    @Test
    public void TestKIXE_Add3Ts() {
        String inputString = "KIXE,i1,w1,+,i2,w2,+,i2,w2,2007";

        indices.clear();
        weights.clear();
        indices.set("i1", new TsVariable(new TsData(TsFrequency.Quarterly, 2004, 0, i1Data, true)));
        indices.set("i2", new TsVariable(new TsData(TsFrequency.Quarterly, 2004, 0, i2Data, true)));
        weights.set("w1", new TsVariable(new TsData(TsFrequency.Quarterly, 2004, 0, w1Data, true)));
        weights.set("w2", new TsVariable(new TsData(TsFrequency.Quarterly, 2004, 0, w2Data, true)));

        double[] expResult = {93.91761, 97.43114, 101.12217, 106.25976, 105.53401, 110.60108, 112.61085,
                              124.71687, 118.05069, 120.29964, 122.23897, 128.86989, 126.55549, 126.03296,
                              125.14638, 123.58184, 108.69291, 107.81738, 110.21964, 116.29706};
        TsCollection result = instance.parser(inputString, indices, weights);
        double[] resultDouble = result.get(0).getTsData().getValues().internalStorage();
        assertEquals(expResult.length, resultDouble.length);
        for (int i = 0; i < expResult.length; i++) {
            org.junit.Assert.assertEquals(expResult[i], resultDouble[i], 0.000005);
        }
    }

}
