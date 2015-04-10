/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.nbdemetra.kix.calculation;

import ec.nbdemetra.kix.InputException;
import static ec.nbdemetra.kix.KIXModel.tryParseInt;
import ec.tstoolkit.timeseries.regression.TsVariables;
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
                    if (addData.get(new TsPeriod(frequence, startYear, j)) != Double.NaN
                            && addWeights.get(new TsPeriod(frequence, startYear, j)) != Double.NaN) {
                        helperCurr += addData.get(new TsPeriod(frequence, startYear, j));
                        helperPrev += addData.get(new TsPeriod(frequence, startYear, j));
                        helperWeight += addWeights.get(new TsPeriod(frequence, startYear, j));
                    }
                } else {
                    if (addData.get(new TsPeriod(frequence, startYear + i - 1, j)) != Double.NaN
                            && addWeights.get(new TsPeriod(frequence, startYear + i, j)) != Double.NaN) {
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
        retVal = mid(retVal, 1);
        int startYear = retVal.getStart().getYear();
        int endYear = retVal.getLastPeriod().getYear();
        int lastPeriodPosition = s.getLastPeriod().getPosition();
        TsFrequency frequence = retVal.getFrequency();

        for (int i = 1; i <= endYear - startYear; i++) {

            if (i == endYear - startYear) {
                for (int j = 0; j <= lastPeriodPosition; j++) {
                    if (retVal.get(new TsPeriod(frequence, startYear + i - 1, j)) != Double.NaN) {
                        double prevyearval = retVal.get(new TsPeriod(frequence, startYear + i - 1, j));
                        double thisyearval = retVal.get(new TsPeriod(frequence, startYear + i, j));
                        double result = (thisyearval * prevyearval / 100);
                        retVal.set(new TsPeriod(frequence, startYear + i, j), result);
                    }
                }
            } else {
                for (int j = 0; j < frequence.intValue(); j++) {
                    if (retVal.get(new TsPeriod(frequence, startYear + i - 1, j)) != Double.NaN) {
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
     * Checks if the parameter <b>year</b> can be parsed to Integer and if the
     * year is 1950 or later.
     *
     * @param year the string representation of the year
     * @param j    the count of the formula
     * @throws ec.nbdemetra.kix.KIXModel.InputException exception message
     *                                                  informs the user about the formula with the false year
     */
    public static void checkYearKIX(String year, int j) throws InputException {
        if (!tryParseInt(year)) {
            throw new InputException("The reference year (" + year + ") has to be numeric in formula " + String.valueOf(j + 1));
        }
        if (Integer.parseInt(year) < 1950) {
            throw new InputException("The reference year (" + year + ") has to be after 1949 in formula " + String.valueOf(j + 1));
        }
    }

    /**
     *
     * @param s
     * @param lag
     * @return
     */
    public static TsData mid(TsData s, int lag) {
        //Declaration
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
                if (i == 0) {
                    if (s.get(new TsPeriod(frequence, startYear, j)) != Double.NaN) {
                        helper += s.get(new TsPeriod(frequence, startYear, j));
                        counter++;
                    }
                } else {
                    if (s.get(new TsPeriod(frequence, startYear + i - lag, j)) != Double.NaN) {
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
     * @param refyear   reference year in which the new time series averages at
     *                  100
     * @return A new time series with an average of 100 in <b>year</b>.
     */
    public static TsData normalizeToYear(TsData indexData, Integer refyear) {
        TsData returnData = indexData.clone();
        double factor = mid(indexData, 0).get(new TsPeriod(indexData.getFrequency(), refyear, 1));
        returnData = returnData.div(factor).times(100);
        return returnData;
    }

    /**
     *
     * @param chainedSum
     * @param indexD
     * @param refYear
     * @return
     */
    public static TsData scaleToRefYear(TsData chainedSum, TsData indexD, Integer refYear) {
        TsData retData = indexD.clone();
        double midChainedSumAtRefYear, midIndexAtRefYear;
        int startYear = retData.getStart().getYear();
        int endYear = retData.getLastPeriod().getYear();
        int lastPeriodPosition = retData.getLastPeriod().getPosition();
        TsFrequency frequence = retData.getFrequency();

        midChainedSumAtRefYear = mid(chainedSum, 0).get(new TsPeriod(frequence, refYear, 1));
        midIndexAtRefYear = mid(indexD, 0).get(new TsPeriod(frequence, refYear, 1));

        for (int i = 0; i <= endYear - startYear; i++) {
            if (i == endYear - startYear) {
                for (int j = 0; j <= lastPeriodPosition; j++) {
                    double result;
                    result = chainedSum.get(new TsPeriod(frequence, startYear + i, j)) * midIndexAtRefYear / midChainedSumAtRefYear;
                    retData.set(new TsPeriod(frequence, startYear + i, j), result);
                }
            } else {
                for (int j = 0; j < frequence.intValue(); j++) {
                    double result;
                    result = chainedSum.get(new TsPeriod(frequence, startYear + i, j)) * midIndexAtRefYear / midChainedSumAtRefYear;
                    retData.set(new TsPeriod(frequence, startYear + i, j), result);
                }
            }
        }
        return retData;
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
     * @param helper      the time series used for the prior-year averages
     * @return A new unchained time series is returned.
     */
    public static TsData unchain(TsData inputTsData, TsData helper) {
        //Declaration
        TsData unchained;
        helper = mid(helper, 1);

        //Logic
        unchained = inputTsData.times(100).div(helper);

        //Return
        return unchained;
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
            tempWeightSum[0] = TsData.multiply(weightedSumD, weightedSumW).
                    minus(TsData.multiply(addD, addW)).div(TsData.subtract(weightedSumW, addW));
            tempWeightSum[1] = weightedSumW.minus(addW);
        } else {
            tempWeightSum[0] = TsData.multiply(weightedSumD, weightedSumW).
                    plus(TsData.multiply(addD, addW)).div(TsData.add(weightedSumW, addW));
            tempWeightSum[1] = weightedSumW.plus(addW);
        }

        return tempWeightSum;
    }

    /**
     *
     * @param formula
     * @param j
     * @throws ec.nbdemetra.kix.KIXModel.InputException
     */
    public static void checkWBG(String[] formula, int j, TsVariables indices) throws InputException {
        if (formula.length != 7) {
            throw new InputException("Formula "
                    + String.valueOf(j + 1) + " is not following the WBG syntax.");
        }
        if (Integer.parseInt(formula[6]) < 1 || Integer.parseInt(formula[6]) > indices.get(formula[1]).getDefinitionFrequency().intValue()) {
            throw new InputException("The number of lags has to be at between 1 and "
                    + indices.get(formula[1]).getDefinitionFrequency().intValue()
                    + "(maximum lag one year) in formula "
                    + String.valueOf(j + 1));
        }
        if (!(indices.get(formula[4]).getDefinitionDomain().getStart().isNotBefore(indices.get(formula[1]).getDefinitionDomain().getStart()))) {
            throw new InputException("The contributing index series (iContr) should not begin after the total index series (iTotal) in formula "
                    + String.valueOf(j + 1));
        }
    }
}
