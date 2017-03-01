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
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import static org.junit.Assert.assertArrayEquals;
import org.junit.Test;

/**
 *
 * @author Thomas Witthohn
 */
public class KIXCalcTest {

    public KIXCalcTest() {
    }
    TsPeriod start = new TsPeriod(TsFrequency.Quarterly, 2004, 0);

    double[] aData = {89.78, 93.71, 90.85, 95.20, 95.47, 98.29, 100.15, 104.76,
        107.69, 109.16, 112.40, 122.26, 119.73, 120.83, 122.45, 128.06,
        128.39, 127.53, 125.12, 118.38, 102.32, 100.80, 105.21, 109.90};
    TsData a = new TsData(start, aData, true);

    @Test
    public void testChainSum_TsData() {
        double[] weightedSumData = {95.9455678781636, 101.5235478245180, 99.1155647893106, 103.4153195080070,
            102.4501592599300, 105.9303326930090, 109.0691592281930, 114.3861870989490,
            106.8300670944030, 110.3457312146300, 112.8987184226080, 124.0662022472970,
            104.9318938491780, 106.4883823302890, 108.0821970617710, 113.5583078139040,
            103.9447394183710, 103.3979307247260, 102.1310926275610, 99.0086432985022,
            84.6808550674616, 83.7602791011846, 86.3681995465272, 90.7496460061146};

        TsData weightedSum = new TsData(new TsPeriod(TsFrequency.Quarterly, 2004, 0), weightedSumData, true);

        double[] expResultData = {95.9455678781636, 101.5235478245180, 99.1155647893106, 103.4153195080070,
            102.4501592599300, 105.9303326930090, 109.0691592281930, 114.3861870989490,
            115.3326289430720, 119.1281033492460, 121.8842817769350, 133.9405811242200,
            128.6164900765570, 130.5242997799890, 132.4778607906210, 139.1900063392590,
            137.9369188286730, 137.2112918579180, 135.5301702854790, 131.3866124483150,
            114.7563178624400, 113.5087878496830, 117.0429437972970, 122.9805156630170
        };

        TsData result = KIXCalc.chainSum(weightedSum);
        assertArrayEquals(expResultData, result.internalStorage(), 0.00005);
    }

    @Test
    public void testUnchain_TsData() {
        double[] expResultData = {97.18, 101.43, 98.34, 103.05, 103.34, 106.39, 108.41, 113.40,
            108.05, 109.52, 112.77, 122.67, 106.07, 107.05, 108.48, 113.45,
            104.58, 103.88, 101.92, 96.43, 81.95, 80.73, 84.27, 88.02};
        TsData result = KIXCalc.unchain(a);
        assertArrayEquals(expResultData, result.internalStorage(), 0.005);
    }

    @Test
    public void testMid() {
        double[] expResultData = {92.385, 92.385, 92.385, 92.385, 99.6675, 99.6675, 99.6675, 99.6675,
            112.8775, 112.8775, 112.8775, 112.8775, 122.7675, 122.7675, 122.7675, 122.7675,
            124.855, 124.855, 124.855, 124.855, 104.5575, 104.5575, 104.5575, 104.5575};
        TsData result = KIXCalc.mid(a, false);
        assertArrayEquals(expResultData, result.internalStorage(), 1E-13);
    }

}
