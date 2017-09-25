/*
 * Copyright 2016 Deutsche Bundesbank
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package de.bundesbank.kix;

import de.bundesbank.kix.options.KIXOptionsPanelController;
import static de.bundesbank.kix.options.KIXOptionsPanelController.KIX2_DEFAULT_METHOD;
import static de.bundesbank.kix.options.KIXOptionsPanelController.KIXE_DEFAULT_METHOD;
import de.bundesbank.kix.options.UnchainingMethod;
import ec.tss.TsCollection;
import ec.tstoolkit.timeseries.regression.TsVariable;
import ec.tstoolkit.timeseries.regression.TsVariables;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.utilities.DefaultNameValidator;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import org.junit.Ignore;
import org.junit.Test;
import org.openide.util.NbPreferences;

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

    private void setMethod(String method, UnchainingMethod unchainingMethod) {
        NbPreferences.forModule(KIXOptionsPanelController.class).put(method, unchainingMethod.name());
    }

    @Test
    public void TestKIX_Add2TsPragmatic() {
        setMethod(KIX2_DEFAULT_METHOD, UnchainingMethod.PRAGMATIC);
        String inputString = "KIX,i1,w1,+,i2,w2,2005";
        quarterlyData();
        double[] expResult = {88.59, 93.74, 91.52, 95.49, 94.60, 97.81, 100.71, 105.62, 106.50, 110.00, 112.55, 123.68,
                              118.76, 120.52, 122.33, 128.52, 127.37, 126.70, 125.15, 121.32, 105.96, 104.81, 108.07, 113.56};
        TsCollection result = instance.parser(inputString, indices, weights);

        Assert.assertArrayEquals(expResult, result.get(0).getTsData().internalStorage(), 0.005);
    }

    @Test
    public void TestKIX_Add2TsPuristic() {
        setMethod(KIX2_DEFAULT_METHOD, UnchainingMethod.PURISTIC);
        String inputString = "KIX,i1,w1,+,i2,w2,2005";
        quarterlyData();
        double[] expResult = {94.60, 97.81, 100.71, 105.62, 106.50, 110.00, 112.55, 123.68,
                              118.76, 120.52, 122.33, 128.52, 127.37, 126.70, 125.15, 121.32, 105.96, 104.81, 108.07, 113.56};
        TsCollection result = instance.parser(inputString, indices, weights);

        for (int i = 0; i < expResult.length; i++) {
            assertEquals(expResult[i], result.get(0).getTsData().internalStorage()[i], 0.005);
        }
    }

    @Test
    public void TestKIX_Subtract2TsPragmatic() {
        setMethod(KIX2_DEFAULT_METHOD, UnchainingMethod.PRAGMATIC);
        String inputString = "KIX,i1,w1,-,i2,w2,2005";
        quarterlyData();
        double[] expResult = {91.42, 93.66, 89.92, 94.8, 96.67, 98.95, 99.37, 103.57, 109.35, 107.99, 112.2, 120.29, 121.07,
                              121.25, 122.62, 127.42, 129.81, 128.68, 125.08, 114.3, 97.24, 95.21, 101.21, 104.8};
        TsCollection result = instance.parser(inputString, indices, weights);

        for (int i = 0; i < expResult.length; i++) {
            assertEquals(expResult[i], result.get(0).getTsData().internalStorage()[i], 0.005);
        }
    }

    @Test
    public void TestKIX_Subtract2TsPuristic() {
        setMethod(KIX2_DEFAULT_METHOD, UnchainingMethod.PURISTIC);
        String inputString = "KIX,i1,w1,-,i2,w2,2005";
        quarterlyData();
        double[] expResult = {96.67, 98.95, 99.37, 103.57, 109.35, 107.99, 112.2, 120.29, 121.07,
                              121.25, 122.62, 127.42, 129.81, 128.68, 125.08, 114.3, 97.24, 95.21, 101.21, 104.8};
        TsCollection result = instance.parser(inputString, indices, weights);

        for (int i = 0; i < expResult.length; i++) {
            assertEquals(expResult[i], result.get(0).getTsData().internalStorage()[i], 0.005);
        }
    }

    @Test
    public void TestKIX_NullTestPragmatic() {
        setMethod(KIX2_DEFAULT_METHOD, UnchainingMethod.PRAGMATIC);
        String inputString = "KIX,i1,w1,+,i2,w2,-,i2,w2,2005";
        quarterlyData();
        double[] expResult = {89.78, 93.71, 90.85, 95.20, 95.47, 98.29, 100.15, 104.76, 107.69, 109.16, 112.40, 122.26,
                              119.73, 120.83, 122.45, 128.06, 128.39, 127.53, 125.12, 118.38, 102.32, 100.80, 105.21, 109.90};
        TsCollection result = instance.parser(inputString, indices, weights);

        for (int i = 0; i < expResult.length; i++) {
            assertEquals(expResult[i], result.get(0).getTsData().internalStorage()[i], 0.005);
        }
    }

    @Test
    public void TestKIX_NullTestPuristic() {
        setMethod(KIX2_DEFAULT_METHOD, UnchainingMethod.PURISTIC);
        String inputString = "KIX,i1,w1,+,i2,w2,-,i2,w2,2005";
        quarterlyData();
        double[] expResult = {95.47, 98.29, 100.15, 104.76, 107.69, 109.16, 112.40, 122.26,
                              119.73, 120.83, 122.45, 128.06, 128.39, 127.53, 125.12, 118.38, 102.32, 100.80, 105.21, 109.90};
        TsCollection result = instance.parser(inputString, indices, weights);

        for (int i = 0; i < expResult.length; i++) {
            assertEquals(expResult[i], result.get(0).getTsData().internalStorage()[i], 0.005);
        }
    }

    @Test
    public void TestKIXE_Add2TsPuristic() {
        setMethod(KIXE_DEFAULT_METHOD, UnchainingMethod.PURISTIC);
        String inputString = "KIXE,i1,w1,+,i2,w2,2005";
        quarterlyData();
        double[] expResult = {94.59634, 97.81158, 100.71445, 105.62539, 106.46924, 109.99229, 112.53047, 123.67321, 118.79002, 120.54374,
                              122.34560, 128.53707, 127.35740, 126.68997, 125.14942, 121.36334, 105.96989, 104.81759, 108.08206, 113.56458};
        TsCollection result = instance.parser(inputString, indices, weights);
        double[] resultDouble = result.get(0).getTsData().internalStorage();
        assertEquals(expResult.length, resultDouble.length);
        for (int i = 0; i < expResult.length; i++) {
            org.junit.Assert.assertEquals(expResult[i], resultDouble[i], 0.005);
        }
    }

    @Test
    public void TestKIXE_Add2Ts2Puristic() {
        setMethod(KIXE_DEFAULT_METHOD, UnchainingMethod.PURISTIC);
        String inputString = "KIXE,i1,w1,+,i2,w2,2007";

        indices.clear();
        weights.clear();
        indices.set("i1", new TsVariable(new TsData(TsFrequency.Quarterly, 2004, 0, i1Data, true)));
        indices.set("i2", new TsVariable(new TsData(TsFrequency.Quarterly, 2006, 1, i2Data, true)));
        weights.set("w1", new TsVariable(new TsData(TsFrequency.Quarterly, 2004, 0, w1Data, true)));
        weights.set("w2", new TsVariable(new TsData(TsFrequency.Quarterly, 2006, 1, w2Data, true)));

        double[] expResult = {117.35039, 117.19164, 119.37548, 125.53398, 126.74698, 124.38076,
                              124.47468, 118.38063, 107.10292, 103.05270, 107.68883, 112.16246};
        TsCollection result = instance.parser(inputString, indices, weights);
        double[] resultDouble = result.get(0).getTsData().internalStorage();
        assertEquals(expResult.length, resultDouble.length);
        for (int i = 0; i < expResult.length; i++) {
            org.junit.Assert.assertEquals(expResult[i], resultDouble[i], 0.005);
        }
    }

    @Test
    public void TestKIXE_Add3TsPuristic() {
        setMethod(KIXE_DEFAULT_METHOD, UnchainingMethod.PURISTIC);
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
        double[] resultDouble = result.get(0).getTsData().internalStorage();
        assertEquals(expResult.length, resultDouble.length);
        for (int i = 0; i < expResult.length; i++) {
            org.junit.Assert.assertEquals(expResult[i], resultDouble[i], 0.000005);
        }
    }

    @Test
    public void TestKIXE_AddAndSubtractTsPuristic() {
        setMethod(KIXE_DEFAULT_METHOD, UnchainingMethod.PURISTIC);
        String inputString = "KIXE,i1,w1,+,i2,w2,-,i2,w2,2007";

        indices.clear();
        weights.clear();
        indices.set("i1", new TsVariable(new TsData(TsFrequency.Quarterly, 2004, 0, i1Data, true)));
        indices.set("i2", new TsVariable(new TsData(TsFrequency.Quarterly, 2004, 0, i2Data, true)));
        weights.set("w1", new TsVariable(new TsData(TsFrequency.Quarterly, 2004, 0, w1Data, true)));
        weights.set("w2", new TsVariable(new TsData(TsFrequency.Quarterly, 2004, 0, w2Data, true)));

        double[] expResult = {95.47, 98.29, 100.15, 104.76, 107.69, 109.16, 112.40, 122.26,
                              119.73, 120.83, 122.45, 128.06, 128.39, 127.53, 125.12, 118.38, 102.32, 100.80, 105.21, 109.90};
        TsCollection result = instance.parser(inputString, indices, weights);
        double[] resultDouble = result.get(0).getTsData().internalStorage();
        assertEquals(expResult.length, resultDouble.length);
        for (int i = 0; i < expResult.length; i++) {
            org.junit.Assert.assertEquals(expResult[i], resultDouble[i], 0.000005);
        }
    }

    @Ignore
    @Test
    //TODO: Complet test (expected results)
    public void TestKIXE_Add2TsPragmatic() {
        setMethod(KIXE_DEFAULT_METHOD, UnchainingMethod.PRAGMATIC);
        String inputString = "KIXE,i1,w1,+,i2,w2,2005";
        quarterlyData();
        double[] expResult = {};

        TsCollection result = instance.parser(inputString, indices, weights);
        double[] resultDouble = result.get(0).getTsData().internalStorage();
        assertEquals(expResult.length, resultDouble.length);
        for (int i = 0; i < expResult.length; i++) {
            org.junit.Assert.assertEquals(expResult[i], resultDouble[i], 0.005);
        }
    }

    @Ignore
    @Test
    //TODO: Complet test (expected results)
    public void TestKIXE_Add2Ts2Pragmatic() {
        setMethod(KIXE_DEFAULT_METHOD, UnchainingMethod.PRAGMATIC);
        String inputString = "KIXE,i1,w1,+,i2,w2,2007";

        indices.clear();
        weights.clear();
        indices.set("i1", new TsVariable(new TsData(TsFrequency.Quarterly, 2004, 0, i1Data, true)));
        indices.set("i2", new TsVariable(new TsData(TsFrequency.Quarterly, 2006, 1, i2Data, true)));
        weights.set("w1", new TsVariable(new TsData(TsFrequency.Quarterly, 2004, 0, w1Data, true)));
        weights.set("w2", new TsVariable(new TsData(TsFrequency.Quarterly, 2006, 1, w2Data, true)));

        double[] expResult = {};

        TsCollection result = instance.parser(inputString, indices, weights);
        double[] resultDouble = result.get(0).getTsData().internalStorage();
        assertEquals(expResult.length, resultDouble.length);
        for (int i = 0; i < expResult.length; i++) {
            org.junit.Assert.assertEquals(expResult[i], resultDouble[i], 0.005);
        }
    }

    @Ignore
    @Test
    //TODO: Complet test (expected results)
    public void TestKIXE_Add3TsPragmatic() {
        setMethod(KIXE_DEFAULT_METHOD, UnchainingMethod.PRAGMATIC);
        String inputString = "KIXE,i1,w1,+,i2,w2,+,i2,w2,2007";

        indices.clear();
        weights.clear();
        indices.set("i1", new TsVariable(new TsData(TsFrequency.Quarterly, 2004, 0, i1Data, true)));
        indices.set("i2", new TsVariable(new TsData(TsFrequency.Quarterly, 2004, 0, i2Data, true)));
        weights.set("w1", new TsVariable(new TsData(TsFrequency.Quarterly, 2004, 0, w1Data, true)));
        weights.set("w2", new TsVariable(new TsData(TsFrequency.Quarterly, 2004, 0, w2Data, true)));

        double[] expResult = {};
        TsCollection result = instance.parser(inputString, indices, weights);
        double[] resultDouble = result.get(0).getTsData().internalStorage();
        assertEquals(expResult.length, resultDouble.length);
        for (int i = 0; i < expResult.length; i++) {
            org.junit.Assert.assertEquals(expResult[i], resultDouble[i], 0.000005);
        }
    }

    @Ignore
    @Test
    //TODO: Complet test (expected results)
    public void TestKIXE_AddAndSubtractTsPragmatic() {
        setMethod(KIXE_DEFAULT_METHOD, UnchainingMethod.PRAGMATIC);
        String inputString = "KIXE,i1,w1,+,i2,w2,-,i2,w2,2007";

        indices.clear();
        weights.clear();
        indices.set("i1", new TsVariable(new TsData(TsFrequency.Quarterly, 2004, 0, i1Data, true)));
        indices.set("i2", new TsVariable(new TsData(TsFrequency.Quarterly, 2004, 0, i2Data, true)));
        weights.set("w1", new TsVariable(new TsData(TsFrequency.Quarterly, 2004, 0, w1Data, true)));
        weights.set("w2", new TsVariable(new TsData(TsFrequency.Quarterly, 2004, 0, w2Data, true)));

        double[] expResult = {};
        TsCollection result = instance.parser(inputString, indices, weights);
        double[] resultDouble = result.get(0).getTsData().internalStorage();
        assertEquals(expResult.length, resultDouble.length);
        for (int i = 0; i < expResult.length; i++) {
            org.junit.Assert.assertEquals(expResult[i], resultDouble[i], 0.000005);
        }
    }

    @Test
    public void TestWBG_Add() {
        double[] a = {78.0876494023904, 79.6812749003984, 79.6812749003984, 81.2749003984064,
                      86.0557768924303, 89.2430278884462, 90.8366533864542, 92.4302788844621,
                      95.6175298804781, 98.8047808764940, 103.5856573705180, 101.9920318725100,
                      107.4103585657370, 110.5976095617530, 113.9442231075700, 117.4501992031870,
                      121.1155378486060, 124.7808764940240, 128.4462151394420, 132.4302788844620,
                      136.2549800796810, 140.5577689243030, 144.7011952191230, 149.0039840637450
        };

        double[] gewA = {392.0, 400.0, 400.0, 408.0, 496.8, 442.4, 461.7, 290.0,
                         456.0, 446.4, 442.0, 416.0, 411.14, 395.58, 378.95, 368.50,
                         342.00, 336.69, 306.28, 290.85, 290.70, 273.42, 254.24, 252.45
        };

        double[] alles = {85.9460112133865, 96.9568880948168, 101.7486585895130, 86.9655368505560,
                          89.1065406886119, 100.6271803886270, 106.2345713930590, 91.4514496541017,
                          92.7668279813041, 103.4411654246170, 109.4984829918590, 94.2935236022205,
                          96.5776233160815, 106.5605420449900, 112.0604990089810, 99.6010948882151,
                          99.9401584839644, 109.0750515454860, 113.9548226021790, 101.9086661372410,
                          101.7462386055560, 110.5700305355260, 115.0658729354940, 103.2949213935240
        };

        double[] gewAlles = {843.00, 951.00, 998.00, 853.00, 951.60, 1114.40, 1091.70, 906.00,
                             978.25, 1196.40, 1145.46, 1096.50, 1121.94, 1306.64, 1230.05, 1176.50,
                             1184.78, 1386.89, 1260.42, 1189.41, 1205.70, 1378.52, 1293.22, 1266.85
        };

        indices.clear();
        weights.clear();
        indices.set("i1", new TsVariable(new TsData(TsFrequency.Quarterly, 1995, 0, a, true)));
        indices.set("i2", new TsVariable(new TsData(TsFrequency.Quarterly, 1995, 0, alles, true)));
        weights.set("w1", new TsVariable(new TsData(TsFrequency.Quarterly, 1995, 0, gewA, true)));
        weights.set("w2", new TsVariable(new TsData(TsFrequency.Quarterly, 1995, 0, gewAlles, true)));

        String inputString = "WBG,i1,w1,+,i2,w2,1";

        double[] expResult = {0.948991696322642, 0.000000000000000, 0.801603206412826, 2.813599062133630,
                              1.830663615560650, 0.810536980749720, 0.767754318618030, 1.566877038470200,
                              1.544659656000100, 2.077893883101840, -0.654315807317998, 2.290369914939000,
                              1.315412790230850, 1.251789925690840, 1.247035184493320, 1.091800973294730,
                              1.088096857056910, 0.996970166865571, 1.037258620651170, 0.799454776733306,
                              0.900822403184864, 0.798233032353246, 0.796546263782846
        };
        TsCollection result = instance.parser(inputString, indices, weights);
        double[] resultDouble = result.get(0).getTsData().internalStorage();
        assertEquals(expResult.length, resultDouble.length);
        for (int i = 0; i < expResult.length; i++) {
            org.junit.Assert.assertEquals(expResult[i], resultDouble[i], 0.000005);
        }
    }
}
