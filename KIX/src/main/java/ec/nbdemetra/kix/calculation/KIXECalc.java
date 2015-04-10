/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.nbdemetra.kix.calculation;

import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;

/**
 *
 * @author Thomas Witthohn
 */
public class KIXECalc {

    public static TsData unchain(TsData index) {
        TsData helpTs = transform(index);

        return index.times(100).div(helpTs);

    }

    public static TsData weightSum(TsData i1, TsData weight1, TsData i2, TsData weight2) {
        TsData w1 = transform(weight1);
        TsData w2 = transform(weight2);
        return (i1.times(w1).plus(i2.times(w2))).div(w1.plus(w2));

    }

    //TODO: ordentlicher Name
    private static TsData transform(TsData index) {
        int startYear = index.getStart().getYear();
        int endYear = index.getLastPeriod().getYear();
        TsFrequency frequency = index.getFrequency();

        int count = frequency.intValue() * (index.getEnd().getYear() - startYear);
        TsData retVal = new TsData(new TsDomain(frequency, startYear + 1, 0, count));

        //Logic
        for (int i = 1; i <= endYear - startYear; ++i) {
            double helper = index.get(new TsPeriod(frequency, startYear + i - 1, frequency.intValue() - 1));

            for (int k = 0; k < frequency.intValue(); ++k) {
                retVal.set(new TsPeriod(frequency, startYear + i, k), helper);

            }
        }
        return retVal;
    }

}
