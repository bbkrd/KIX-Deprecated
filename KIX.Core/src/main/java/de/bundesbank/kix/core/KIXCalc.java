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
package de.bundesbank.kix.core;

import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;

/**
 *
 * @author Thomas Witthohn
 */
public class KIXCalc {

    /**
     *
     * @param addData
     * @param addWeights
     * @return
     */
    public static TsData aggregateOverTheYear(TsData addData, TsData addWeights) {
        TsData retData = addData.clone();

        int startYear = addData.getStart().getYear();
        int endYear = addData.getLastPeriod().getYear();
        int lastPeriodPosition = addData.getLastPeriod().getPosition();
        TsFrequency frequence = addData.getFrequency();
        for (int i = 0; i <= endYear - startYear; i++) {

            double helperCurr = 0;
            double helperPrev = 0;
            double helperWeight = 0;
            double result;

            for (int j = 0; j < frequence.intValue(); j++) {
                if (i == 0) {
                    if (Double.isFinite(addData.get(new TsPeriod(frequence, startYear, j)))
                            && Double.isFinite(addWeights.get(new TsPeriod(frequence, startYear, j)))) {
                        helperCurr += addData.get(new TsPeriod(frequence, startYear, j));
                        helperPrev += addData.get(new TsPeriod(frequence, startYear, j));
                        helperWeight += addWeights.get(new TsPeriod(frequence, startYear, j));
                    }
                } else {
                    if (Double.isFinite(addData.get(new TsPeriod(frequence, startYear + i - 1, j)))
                            && Double.isFinite(addWeights.get(new TsPeriod(frequence, startYear + i, j)))) {
                        helperCurr += addData.get(new TsPeriod(frequence, startYear + i, j));
                        helperPrev += addData.get(new TsPeriod(frequence, startYear + i - 1, j));
                        helperWeight += addWeights.get(new TsPeriod(frequence, startYear + i - 1, j));
                    }
                }
            }

            result = helperCurr / helperPrev * helperWeight;
            if (i == endYear - startYear) {
                for (int k = 0; k <= lastPeriodPosition; k++) {
                    retData.set(new TsPeriod(frequence, startYear + i, k), result);
                }
            } else {
                for (int k = 0; k < frequence.intValue(); k++) {
                    retData.set(new TsPeriod(frequence, startYear + i, k), result);
                }
            }
        }

        return retData;
    }

    /**
     *
     * @param s
     * @return
     */
    public static TsData chainSum(TsData s) {
        return chainSum(s, s);
    }

    /**
     *
     * @param s
     * @param retVal
     * @return
     */
    public static TsData chainSum(TsData s, TsData retVal) {
        retVal = mid(retVal, true);
        int startYear = retVal.getStart().getYear();
        int endYear = retVal.getLastPeriod().getYear();
        int lastPeriodPosition = s.getLastPeriod().getPosition();
        TsFrequency frequence = retVal.getFrequency();

        for (int i = 1; i <= endYear - startYear; i++) {

            if (i == endYear - startYear) {
                for (int j = 0; j <= lastPeriodPosition; j++) {
                    if (Double.isFinite(retVal.get(new TsPeriod(frequence, startYear + i - 1, j)))) {
                        double prevyearval = retVal.get(new TsPeriod(frequence, startYear + i - 1, j));
                        double thisyearval = retVal.get(new TsPeriod(frequence, startYear + i, j));
                        double result = (thisyearval * prevyearval / 100);
                        retVal.set(new TsPeriod(frequence, startYear + i, j), result);
                    }
                }
            } else {
                for (int j = 0; j < frequence.intValue(); j++) {
                    if (Double.isFinite(retVal.get(new TsPeriod(frequence, startYear + i - 1, j)))) {
                        double prevyearval = retVal.get(new TsPeriod(frequence, startYear + i - 1, j));
                        double thisyearval = retVal.get(new TsPeriod(frequence, startYear + i, j));
                        double result = (thisyearval * prevyearval / 100);
                        retVal.set(new TsPeriod(frequence, startYear + i, j), result);
                    }
                }
            }
        }
        return retVal.times(s).div(100);
    }

    /**
     *
     * @param s
     * @param previousYearAverage
     * @return
     */
    public static TsData mid(TsData s, boolean previousYearAverage) {
        //Declaration
        int lag = previousYearAverage ? 1 : 0;
        TsData retval = s.clone();
        int startYear = s.getStart().getYear();
        int endYear = s.getLastPeriod().getYear();
        int lastPeriodPosition = s.getLastPeriod().getPosition();
        TsFrequency frequence = s.getFrequency();

        //Logic
        for (int i = 0; i <= endYear - startYear; i++) {
            double helper = 0;
            int counter = 0;

            for (int j = 0; j < frequence.intValue(); j++) {
                if (i - lag <= 0) {
                    if (Double.isFinite(s.get(new TsPeriod(frequence, startYear, j)))) {
                        helper += s.get(new TsPeriod(frequence, startYear, j));
                        counter++;
                    }
                } else {
                    if (Double.isFinite(s.get(new TsPeriod(frequence, startYear + i - lag, j)))) {
                        helper += s.get(new TsPeriod(frequence, startYear + i - lag, j));
                        counter++;
                    }
                }
            }
            helper /= counter;

            if (i == endYear - startYear) {
                for (int k = 0; k <= lastPeriodPosition; k++) {
                    retval.set(new TsPeriod(frequence, startYear + i, k), helper);
                }
            } else {
                for (int k = 0; k < frequence.intValue(); k++) {
                    retval.set(new TsPeriod(frequence, startYear + i, k), helper);
                }
            }
        }

        //Return
        return retval;
    }

    /**
     * Returns a new time series which is normalized to the new reference year
     * (average of 100 in the reference year).
     *
     * @param indexData time series to normalize
     * @param refyear reference year in which the new time series averages at
     * 100
     * @return A new time series with an average of 100 in <b>year</b>.
     */
    public static TsData normalizeToYear(TsData indexData, Integer refyear) {
        TsData returnData = indexData.clone();
        double factor = mid(indexData, false).get(new TsPeriod(indexData.getFrequency(), refyear, 0));
        return returnData.div(factor).times(100);
    }

    /**
     * Returns an unchained time series. Each value of <code>inputTsData</code>
     * is therefor divided by the prior-year average and multiplyed by 100.
     * Calls unchain(inputTsData, inputTsData).
     *
     * @param inputTsData the time series
     * @return A new unchained time series is returned.
     * @see #unchain(TsData, TsData)
     */
    public static TsData unchain(TsData inputTsData) {
        return unchain(inputTsData, inputTsData);
    }

    /**
     * Returns an unchained time series. Each value of <code>inputTsData</code>
     * is therefor divided by the prior-year average of <code>helper</code> and
     * multiplyed by 100.
     *
     * @param inputTsData the time series to be unchained
     * @param helper the time series used for the prior-year averages
     * @return A new unchained time series is returned.
     */
    public static TsData unchain(TsData inputTsData, TsData helper) {
        return inputTsData.times(100).div(mid(helper, true));
    }

    /**
     *
     * @param weightedSumD
     * @param weightedSumW
     * @param addD
     * @param addW
     * @param operation
     * @return
     */
    public static TsData[] weightsum(TsData weightedSumD, TsData weightedSumW, TsData addD, TsData addW, String operation) {
        TsData[] tempWeightSum = new TsData[2];
        if (operation.equalsIgnoreCase("-")) {
            tempWeightSum[0] = (TsData.multiply(weightedSumD, weightedSumW)
                    .minus(TsData.multiply(addD, addW)))
                    .div(TsData.subtract(weightedSumW, addW));
            tempWeightSum[1] = weightedSumW.minus(addW);
        } else {
            tempWeightSum[0] = (TsData.multiply(weightedSumD, weightedSumW)
                    .plus(TsData.multiply(addD, addW)))
                    .div(TsData.add(weightedSumW, addW));
            tempWeightSum[1] = weightedSumW.plus(addW);
        }

        return tempWeightSum;
    }

    private double factor, factorWeight;
    private final boolean puristic;
    private final int referenzYear;
    private TsData weightedSumData;
    private TsData weightedSumWeights;

    public KIXCalc(TsData indexData, TsData indexWeights, int referenzYear) {
        this(indexData, indexWeights, referenzYear, false);
    }

    public KIXCalc(TsData indexData, TsData indexWeights, int referenzYear, boolean puristic) {
        this.weightedSumData = unchain(indexData);
        this.weightedSumWeights = mid(indexWeights, true);

        this.puristic = puristic;
        this.referenzYear = referenzYear;

        TsPeriod referenzYearPeriod = new TsPeriod(indexData.getFrequency(), referenzYear, 1);
        factor = mid(indexData, false).get(referenzYearPeriod);
        factorWeight = calculateFactorWeight(indexData, indexWeights);
    }

    public void add(TsData addData, TsData addWeights) {
        addFactor(addData, addWeights);
        addWeightsum(addData, addWeights);
    }

    public TsData getResult() {
        TsPeriod referenzYearPeriod = new TsPeriod(weightedSumData.getFrequency(), referenzYear, 0);
        double meanInRefYear = mid(chainSum(weightedSumData), false).get(referenzYearPeriod);

        TsData indexData = chainSum(weightedSumData).times(factor).div(meanInRefYear);
        if (puristic) {
            return indexData.drop(indexData.getFrequency().intValue() - indexData.getStart().getPosition(), 0);
        }
        return indexData;
    }

    public void minus(TsData subtractData, TsData subtractWeights) {
        subtractFactor(subtractData, subtractWeights);
        subtractWeightsum(subtractData, subtractWeights);
    }

    private void addFactor(TsData addData, TsData addWeights) {
        TsPeriod referenzYearPeriod = new TsPeriod(addData.getFrequency(), referenzYear, 1);
        double midIndexAtRefYear = mid(addData, false).get(referenzYearPeriod);
        double addFactorWeight = calculateFactorWeight(addData, addWeights);

        factor = (factor * factorWeight + midIndexAtRefYear * addFactorWeight) / (factorWeight + addFactorWeight);
        factorWeight += addFactorWeight;
    }

    /**
     *
     * @param addData
     * @param addWeights
     */
    private void addWeightsum(TsData addData, TsData addWeights) {

        addData = unchain(addData);
        addWeights = mid(addWeights, true);
        weightedSumData = (weightedSumData.times(weightedSumWeights)
                .plus(addData.times(addWeights)))
                .div(weightedSumWeights.plus(addWeights));
        weightedSumWeights = weightedSumWeights.plus(addWeights);

    }

    private double calculateFactorWeight(TsData data, TsData weights) {
        double sumDataRefYear = 0, sumDataPreYear = 0, sumWeightPreYear = 0;
        for (int i = 0; i < data.getFrequency().intValue(); i++) {
            TsPeriod referenzYearPeriod = new TsPeriod(data.getFrequency(), referenzYear, i);
            TsPeriod previousYearPeriod = new TsPeriod(data.getFrequency(), referenzYear - 1, i);

            sumDataRefYear += data.get(referenzYearPeriod);
            sumDataPreYear += data.get(previousYearPeriod);
            sumWeightPreYear += weights.get(previousYearPeriod);
        }
        return sumDataRefYear * sumWeightPreYear / sumDataPreYear;
    }

    private void subtractFactor(TsData addData, TsData addWeights) {
        TsPeriod referenzYearPeriod = new TsPeriod(addData.getFrequency(), referenzYear, 1);
        double midIndexAtRefYear = mid(addData, false).get(referenzYearPeriod);
        double subtractFactorWeight = calculateFactorWeight(addData, addWeights);

        factor = (factor * factorWeight - midIndexAtRefYear * subtractFactorWeight) / (factorWeight - subtractFactorWeight);
        factorWeight -= subtractFactorWeight;
    }

    /**
     *
     * @param subtractData
     * @param subtractWeights
     */
    private void subtractWeightsum(TsData subtractData, TsData subtractWeights) {
        subtractData = unchain(subtractData);
        subtractWeights = mid(subtractWeights, true);
        weightedSumData = (weightedSumData.times(weightedSumWeights)
                .minus(subtractData.times(subtractWeights)))
                .div(weightedSumWeights.minus(subtractWeights));
        weightedSumWeights = weightedSumWeights.minus(subtractWeights);
    }
}
