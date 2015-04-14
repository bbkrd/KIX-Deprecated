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

    static TsData scaleToRefYear(TsData chainedTs, double factor, int refYear) {
        return chainedTs.times(factor).div(meanInRefYear(chainedTs, refYear));
    }

    public static double subtractFromFactor(double factor, double weightFactor, TsData index, double weightIndex_InRefYear, int refYear) {
        return (factor * weightFactor - meanInRefYear(index, refYear) * weightIndex_InRefYear) / (weightFactor - weightIndex_InRefYear);
    }

    public static double addToFactor(double factor, double weightFactor, TsData index, double weightIndex_InRefYear, int refYear) {
        return (factor * weightFactor + meanInRefYear(index, refYear) * weightIndex_InRefYear) / (weightFactor + weightIndex_InRefYear);
    }

    public static TsData chain(TsData weightSum) {
        TsData retVal = weightSum.clone();
        double chainingFactor = 100;
        int startYear = retVal.getStart().getYear();
        int endYear = retVal.getLastPeriod().getYear();
        TsFrequency frequency = retVal.getFrequency();

        for (int i = 1; i <= endYear - startYear; ++i) {
            chainingFactor *= weightSum.get(new TsPeriod(frequency, startYear + i - 1, frequency.intValue() - 1)) / 100;
            if (Double.isNaN(chainingFactor)) {
                chainingFactor = 100;
            }
            for (int k = 0; k < frequency.intValue(); ++k) {
                double chainedValue = retVal.get(new TsPeriod(frequency, startYear + i, k)) * chainingFactor / 100;
                retVal.set(new TsPeriod(frequency, startYear + i, k), chainedValue);
            }
        }
        return retVal;
    }

    public static TsData unchain(TsData index) {
        TsData helpTs = transform(index);

        return index.times(100).div(helpTs);

    }

    public static double weightInRefYear(TsData index, TsData weight, int refYear) {
        TsFrequency frequency = index.getFrequency();
        double meanInRefYear = meanInRefYear(index, refYear);
        double lastValuePreviousYearIndex = index.get(new TsPeriod(frequency, refYear - 1, frequency.intValue() - 1));
        double lastValuePreviousYearWeight = weight.get(new TsPeriod(frequency, refYear - 1, frequency.intValue() - 1));
        return meanInRefYear / lastValuePreviousYearIndex * lastValuePreviousYearWeight;
    }

    public static TsData addToWeightSum(TsData i1, TsData weight1, TsData i2, TsData weight2) {
        TsDomain domain = i1.getDomain().union(i2.getDomain());
        i1 = i1.fittoDomain(domain);
        i2 = i2.fittoDomain(domain);
        TsData w1 = transform(weight1).fittoDomain(domain);
        TsData w2 = transform(weight2).fittoDomain(domain);

        i1.getValues().setMissingValues(0);
        i2.getValues().setMissingValues(0);
        w1.getValues().setMissingValues(0);
        w2.getValues().setMissingValues(0);

        return (i1.times(w1).plus(i2.times(w2))).div(w1.plus(w2));

    }

    public static TsData subtractFromWeightSum(TsData i1, TsData weight1, TsData i2, TsData weight2) {
        TsDomain domain = i1.getDomain().union(i2.getDomain());
        i1 = i1.fittoDomain(domain);
        i2 = i2.fittoDomain(domain);
        TsData w1 = transform(weight1).fittoDomain(domain);
        TsData w2 = transform(weight2).fittoDomain(domain);

        i1.getValues().setMissingValues(0);
        i2.getValues().setMissingValues(0);
        w1.getValues().setMissingValues(0);
        w2.getValues().setMissingValues(0);

        return (i1.times(w1).minus(i2.times(w2))).div(w1.minus(w2));

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
            double lastValuePreviousYear = index.get(new TsPeriod(frequency, startYear + i - 1, frequency.intValue() - 1));

            for (int k = 0; k < frequency.intValue(); ++k) {
                retVal.set(new TsPeriod(frequency, startYear + i, k), lastValuePreviousYear);

            }
        }
        return retVal;
    }

    private static double meanInRefYear(TsData data, int refYear) {
        double sum = 0;
        double counter = 0;
        TsFrequency frequency = data.getFrequency();
        for (int i = 0; i < frequency.intValue(); i++) {
            double value = data.get(new TsPeriod(frequency, refYear, i));
            if (!Double.isNaN(value)) {
                sum += data.get(new TsPeriod(frequency, refYear, i));
                counter++;
            }
        }
        return sum / counter;
    }

}
