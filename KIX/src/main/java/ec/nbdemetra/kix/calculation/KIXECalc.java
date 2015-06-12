/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.nbdemetra.kix.calculation;

import ec.nbdemetra.kix.InputException;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsObservation;
import ec.tstoolkit.timeseries.simplets.TsPeriod;

/**
 *
 * @author Thomas Witthohn
 */
public class KIXECalc {

    public static TsData contributionToGrowth(TsData uA, TsData wA, TsData uTotal, TsData wTotal, int lag) {
        TsDomain domain = uA.lead(lag).getDomain().intersection(uA.getDomain());
        TsData retVal = new TsData(domain, 0);
        TsFrequency frequency = retVal.getFrequency();
        switch (lag) {
            case 1:
                for (TsObservation tsObservation : retVal) {
                    double value;
                    TsPeriod period = tsObservation.getPeriod();
                    int year = period.getYear();
                    TsPeriod lastPeriodOneYearAgo = new TsPeriod(frequency, year - 1, frequency.intValue() - 1);

                    if (period.getPosition() == 0) {
                        value = (uA.get(period) - 100) * (wA.get(lastPeriodOneYearAgo) / wTotal.get(lastPeriodOneYearAgo));
                    } else {
                        TsPeriod previousPeriod = new TsPeriod(frequency, year, period.getPosition() - 1);
                        value = ((uA.get(period) - uA.get(previousPeriod)) / uTotal.get(previousPeriod)) * (wA.get(lastPeriodOneYearAgo) / wTotal.get(lastPeriodOneYearAgo)) * 100;
                    }
                    retVal.set(period, value);
                }
                break;

            case 2:
            case 6:
                break;

            case 3:
                break;

            case 4:
            case 12:
                for (TsObservation tsObservation : retVal) {
                    TsPeriod period = tsObservation.getPeriod();
                    int year = period.getYear();
                    TsPeriod samePeriodOneYearAgo = new TsPeriod(frequency, year - 1, period.getPosition());
                    TsPeriod lastPeriodOneYearAgo = new TsPeriod(frequency, year - 1, frequency.intValue() - 1);
                    TsPeriod lastPeriodTwoYearsAgo = new TsPeriod(frequency, year - 2, frequency.intValue() - 1);

                    double value = (wA.get(lastPeriodOneYearAgo) / wTotal.get(lastPeriodOneYearAgo))
                            * (uTotal.get(lastPeriodOneYearAgo) / uTotal.get(samePeriodOneYearAgo))
                            * (uA.get(period) - 100)
                            + (uA.get(lastPeriodOneYearAgo) - uA.get(samePeriodOneYearAgo)) / uTotal.get(samePeriodOneYearAgo)
                            * (wA.get(lastPeriodTwoYearsAgo) / wTotal.get(lastPeriodTwoYearsAgo)) * 100;

                    retVal.set(period, value);
                }
                break;
        }

        return retVal;
    }

    /**
     *
     * @param chainedTs the value of chainedTs
     * @param factor    the value of factor
     * @param refYear   the value of refYear
     */
    public static TsData scaleToRefYear(TsData chainedTs, double factor, int refYear) {
        return chainedTs.times(factor).div(meanInRefYear(chainedTs, refYear));
    }

    /**
     *
     * @param factor
     * @param weightFactor
     * @param index
     * @param weightIndex_InRefYear
     * @param refYear
     * @return
     */
    public static double subtractFromFactor(double factor, double weightFactor, TsData index, double weightIndex_InRefYear, int refYear) {
        return (factor * weightFactor - meanInRefYear(index, refYear) * weightIndex_InRefYear) / (weightFactor - weightIndex_InRefYear);
    }

    /**
     *
     * @param factor
     * @param weightFactor
     * @param index
     * @param weightIndex_InRefYear
     * @param refYear
     * @return
     */
    public static double addToFactor(double factor, double weightFactor, TsData index, double weightIndex_InRefYear, int refYear) {
        return (factor * weightFactor + meanInRefYear(index, refYear) * weightIndex_InRefYear) / (weightFactor + weightIndex_InRefYear);
    }

    /**
     *
     * @param weightSum
     * @return
     */
    public static TsData chain(TsData weightSum) {
        TsData retVal = weightSum.clone();
        double chainingFactor = 100;
        int startYear = retVal.getStart().getYear();
        int endYear = retVal.getLastPeriod().getYear();
        TsFrequency frequency = retVal.getFrequency();

        for (int i = 1; i <= endYear - startYear; ++i) {
            chainingFactor *= weightSum.get(new TsPeriod(frequency, startYear + i - 1, frequency.intValue() - 1)) / 100;
            if (i == endYear - startYear) {
                for (int k = 0; k <= retVal.getLastPeriod().getPosition(); ++k) {
                    double chainedValue = retVal.get(new TsPeriod(frequency, startYear + i, k)) * chainingFactor / 100;
                    retVal.set(new TsPeriod(frequency, startYear + i, k), chainedValue);
                }
            } else {
                for (int k = 0; k < frequency.intValue(); ++k) {
                    double chainedValue = retVal.get(new TsPeriod(frequency, startYear + i, k)) * chainingFactor / 100;
                    retVal.set(new TsPeriod(frequency, startYear + i, k), chainedValue);
                }
            }
        }
        return retVal;
    }

    /**
     * Each value in the time series is divided by the respective previous year final value (month or quarter) then multiplied by 100.
     *
     * @param index
     * @return
     */
    public static TsData unchain(TsData index) {
        return index.times(100).div(transform(index));

    }

    /**
     *
     * @param index
     * @param weight
     * @param refYear
     * @return
     */
    public static double weightInRefYear(TsData index, TsData weight, int refYear) {
        TsFrequency frequency = index.getFrequency();
        double meanInRefYear = meanInRefYear(index, refYear);
        double lastValuePreviousYearIndex = index.get(new TsPeriod(frequency, refYear - 1, frequency.intValue() - 1));
        double lastValuePreviousYearWeight = weight.get(new TsPeriod(frequency, refYear - 1, frequency.intValue() - 1));
        return meanInRefYear / lastValuePreviousYearIndex * lastValuePreviousYearWeight;
    }

    /**
     * Returns a weighted sum defined with the formula (index1 * weight1 + index2 * weight2 ) / (weight1 + weight2).
     * The new domain is the union of the input domains.
     * Missing values are set to Zero.
     *
     * @param index1
     * @param weight1
     * @param index2
     * @param weight2
     * @return new TsData
     */
    public static TsData addToWeightSum(TsData index1, TsData weight1, TsData index2, TsData weight2) {
        weight1 = transform(weight1);
        weight2 = transform(weight2);

        return (index1.times(weight1).plus(index2.times(weight2))).div(weight1.plus(weight2));

    }

    /**
     * Returns the sum of two time series.
     * The new domain is the union of the input domains.
     * Missing values are set to Zero.
     *
     * @param weight1 first summand
     * @param weight2 second summand
     * @return new TsData, sum of the input series
     */
    public static TsData addToWeight(TsData weight1, TsData weight2) {
        return weight1.plus(weight2);

    }

    /**
     * Returns a weighted sum defined with the formula (index1 * weight1 - index2 * weight2 ) / (weight1 - weight2).
     * The new domain is the union of the input domains.
     * Missing values are set to Zero.
     *
     * @param index1
     * @param weight1
     * @param index2
     * @param weight2
     * @return new TsData
     */
    public static TsData subtractFromWeightSum(TsData index1, TsData weight1, TsData index2, TsData weight2) {
        weight1 = transform(weight1);
        weight2 = transform(weight2);

        return (index1.times(weight1).minus(index2.times(weight2))).div(weight1.minus(weight2));

    }

    /**
     * Returns the difference of two time series.
     * The new domain is the union of the input domains.
     * Missing values are set to Zero.
     *
     * @param weight1 minuend
     * @param weight2 subtrahend
     * @return new TsData, difference of the input series
     */
    public static TsData subtractFromWeight(TsData weight1, TsData weight2) {
        return weight1.minus(weight2);

    }

    //TODO: ordentlicher Name
    /**
     *
     * @param index
     * @return
     */
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

    /**
     *
     * @param data
     * @param refYear
     * @return
     */
    private static double meanInRefYear(TsData data, int refYear) {
        double sum = 0;
        TsFrequency frequency = data.getFrequency();
        for (int i = 0; i < frequency.intValue(); i++) {
            sum += data.get(new TsPeriod(frequency, refYear, i));
        }
        return sum / frequency.intValue();
    }

    public static void checkLag(TsData data, int lag) throws InputException {
        if (data.getFrequency() == TsFrequency.Monthly && lag != 1 && lag != 3 && lag != 6 && lag != 12) {
            throw new InputException("Only lag 1,3,6 or 12 allowed for monthly data");
        }
        if (data.getFrequency() == TsFrequency.Quarterly && lag != 1 && lag != 2 && lag != 4) {
            throw new InputException("Only lag 1,2 or 4 allowed for quarterly data");
        }
    }
}
