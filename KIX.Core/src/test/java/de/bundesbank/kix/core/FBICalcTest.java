/*
 * Copyright 2017 Deutsche Bundesbank
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
package de.bundesbank.kix.core;

import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Thomas Witthohn
 */
public class FBICalcTest {

    public FBICalcTest() {
    }

    private static final double delta = 0.00001;
    private final double[] data1 = {103.99467, 106.39148, 106.12517, 108.25566, 105.90406, 109.71710, 111.68512, 113.65314,
        103.46320, 106.92641, 112.01299, 110.38961, 105.29412, 108.43137, 111.66667, 115.19608,
        103.06383, 106.21277, 109.27660, 112.68085, 102.87009, 106.19335, 109.29003, 112.53776};
    private final TsData a = new TsData(TsFrequency.Quarterly, 2008, 0, data1, true);

    private final double[] weight1 = {455.0, 443.0, 431.0, 419.0, 408.0, 380.0, 352.0, 324.0, 290.0, 313.0, 336.0, 359.0,
        395.6, 387.0, 378.4, 369.8, 368.5, 350.0, 331.5, 313.0, 290.9, 311.8, 306.8, 301.8};
    private final TsData weightsForA = new TsData(TsFrequency.Quarterly, 2008, 0, weight1, true);

    private final double[] data2 = {99.64912, 99.82456, 98.07018, 96.66667, 98.45735, 96.91470, 95.37205, 93.82940, 99.12959,
        98.35590, 95.26112, 94.10058, 100.30832, 99.48613, 98.45838, 97.22508, 99.26004, 98.20296,
        97.14588, 95.98309, 99.33921, 98.34802, 97.24670, 97.02643};
    private final TsData b = new TsData(TsFrequency.Quarterly, 2008, 0, data2, true);

    private final double[] weight2 = {176.0, 181.0, 186.0, 191.0, 195.0, 230.0, 265.0, 300.0, 366.0, 380.0, 394.0, 408.0,
        430.5, 463.0, 495.5, 528.0, 558.0, 576.0, 594.0, 612.0, 648.6, 672.3, 698.0, 723.7};
    private final TsData weightsForB = new TsData(TsFrequency.Quarterly, 2008, 0, weight2, true);

    @Test
    public void testAdd_TsData_TsData() {
        TsData indexSeriesTwo = b;
        TsData weightSeriesTwo = weightsForB;
        FBICalc instance = new FBICalc(a, weightsForA);
        instance.add(indexSeriesTwo, weightSeriesTwo);

        TsData result = instance.getResult();
        double[] expResult = {102.78260, 104.48665, 103.69692, 104.62698,
            103.49592, 104.88997, 104.67870, 104.12250,
            101.04536, 102.22685, 102.97157, 101.72478,
            102.69590, 103.55885, 104.17759, 104.62726,
            100.77293, 101.23043, 101.49092, 101.63325,
            100.43249, 100.83372, 100.92394, 101.59134};

        Assert.assertArrayEquals(expResult, result.internalStorage(), delta);
    }

    @Test
    public void testAdd_TsData_double() {

        TsData indexSeriesTwo = b;
        double weightOne = 1.5;
        double weightTwo = 2.5;
        FBICalc instance = new FBICalc(a, weightOne);
        instance.add(indexSeriesTwo, weightTwo);

        TsData result = instance.getResult();
        double[] expResult = {101.27870, 102.28716, 101.09080, 101.01254,
            101.24987, 101.71560, 101.48945, 101.26330,
            100.75469, 101.56984, 101.54307, 100.20897,
            102.17800, 102.84060, 103.41149, 103.96421,
            100.68646, 101.20664, 101.69490, 102.24475,
            100.66329, 101.29002, 101.76295, 102.84318};
        Assert.assertArrayEquals(expResult, result.internalStorage(), delta);

    }

    @Test
    public void testMinus_TsData_TsData() {
        TsData indexSeriesTwo = b;
        TsData weightSeriesTwo = weightsForB;
        FBICalc instance = new FBICalc(a, weightsForA);
        instance.minus(indexSeriesTwo, weightSeriesTwo);

        TsData result = instance.getResult();
        double[] expResult = {106.73595, 110.92817, 112.24039, 117.96398,
            112.72147, 129.34745, 161.37436, 361.44989,
            82.59345, 58.31755, -1.78420, -25.24150,
            43.79306, 53.93603, 55.77677, 55.21702,
            91.86322, 85.79839, 81.82651, 78.50350,
            96.46772, 91.56252, 87.80168, 85.93063};
        Assert.assertArrayEquals(expResult, result.internalStorage(), delta);
    }

    @Test
    public void testMinus_TsData_double() {
        TsData indexSeriesTwo = b;
        double weightOne = 1.5;
        double weightTwo = 2.5;
        FBICalc instance = new FBICalc(a, weightOne);
        instance.minus(indexSeriesTwo, weightTwo);

        TsData result = instance.getResult();
        double[] expResult = {93.13080, 89.97418, 85.98770, 79.28319,
            87.28729, 77.71110, 70.90245, 64.09379,
            92.62918, 85.50014, 70.13332, 69.66704,
            92.82962, 86.06827, 78.64595, 70.26858,
            93.55436, 86.18825, 78.94980, 70.93645,
            94.04289, 86.58003, 79.18171, 73.75944};
        Assert.assertArrayEquals(expResult, result.internalStorage(), delta);
    }

    @Test
    public void testAdd_TsData_TsData_NoIntersection() {
        TsData indexSeriesTwo = b;
        TsData weightTwo = weightsForB;

        TsData indexSeriesOne = new TsData(TsFrequency.Quarterly, 1980, 0, data1, true);
        TsData weightOne = new TsData(TsFrequency.Quarterly, 1980, 0, weight1, true);
        FBICalc instance = new FBICalc(indexSeriesOne, weightOne);
        instance.minus(indexSeriesTwo, weightTwo);

        TsData result = instance.getResult();

        double[] expResult = {};
        Assert.assertArrayEquals(expResult, result.internalStorage(), delta);
    }

}
