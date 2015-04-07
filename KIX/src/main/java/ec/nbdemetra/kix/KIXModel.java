/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.nbdemetra.kix;

import ec.tss.Ts;
import ec.tss.TsCollection;
import ec.tss.TsFactory;
import ec.tstoolkit.MetaData;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.timeseries.TsException;
import ec.tstoolkit.timeseries.regression.ITsVariable;
import ec.tstoolkit.timeseries.regression.TsVariables;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import java.awt.HeadlessException;
import java.util.ArrayList;
import java.util.Locale;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 * Implementation of the IKIXModel interface <br>
 * Capable to aggregate chain indices and calculate growth shares
 * <p>
 * For further information:
 *
 * @author Thomas Witthohn
 */
public class KIXModel implements IKIXModel {

    private TsVariables indices, weights;
    private String[][] request;
    private String[][] formulaNames;
    private TsCollection outputTsCollection;

    /**
     *
     * @param inputuser
     * @param vars
     * @return
     */
    @Override
    public TsCollection parser(String inputString, TsVariables indices, TsVariables weights) {
        this.indices = indices;
        this.weights = weights;
        formatInput(inputString);
        TsData[] outputTsData = new TsData[request.length];
        outputTsCollection = TsFactory.instance.createTsCollection();

        for (int j = 0; j < request.length; j++) {
            try {
                String[] formula;
                if (request[j].length != 0) {
                    formula = request[j];
                } else {
                    formula = new String[]{""};
                }
                switch (formula[0].toUpperCase(Locale.ENGLISH)) {
                    case "KIX":
                        outputTsData[j] = doKIX(formula, j);
                        break;
                    case "WBG":
                        outputTsData[j] = doWBG(formula, j);
                        break;
                    case "":
                        throw new InputException("No control character found in formula "
                                + String.valueOf(j + 1)
                                + ". Please use the syntax described in the tooltip or the help.");
                    default:
                        throw new InputException(formula[0].toUpperCase() + " in formula "
                                + String.valueOf(j + 1)
                                + " is an invalid control character. Please use the syntax described in the tooltip or the help.");
                }
            } catch (InputException | NumberFormatException | HeadlessException | TsException e) {
                NotifyDescriptor nd = new NotifyDescriptor.Message(e.getMessage());
                DialogDisplayer.getDefault().notify(nd);
            } finally {
                if (outputTsData[j] == null) {
                    //TODO Variante finden die einen leeren TsData "besser" erzeugt
                    outputTsData[j] = new TsData(TsFrequency.Yearly, 2005, 0, 1);
                }
            }
        }

        fillTsCollection(outputTsData);
        return outputTsCollection;

    }

    /**
     *
     * @param formula
     * @param j
     * @return
     * @throws ec.nbdemetra.kix.KIXModel.InputException
     */
    private TsData doKIX(String[] formula, int j) throws InputException {
        checkYearKIX(formula[formula.length - 1], j);
        check(formula, j);
        checkData(formula, j);

        TsData weightedSumData = null;
        TsData weightedSumWeights = null;
        TsData indexData = null;
        TsData indexWeights = null;
        TsData addData;
        TsData addWeights;

        int refYear = Integer.parseInt(formula[formula.length - 1]);

        for (int i = 1; i < formula.length; i += 3) {

            addData = extractData(indices.get(formula[i]));
            addWeights = extractData(weights.get(formula[i + 1]));

            if (i == 1) {
                indexData = addData.clone();
                indexWeights = aggregateOverTheYear(addData, addWeights);

                weightedSumData = unchain(addData);
                weightedSumWeights = mid(addWeights, 1);

            } else {
                TsData[] tempNewIndex;
                tempNewIndex = weightsum(indexData, indexWeights, addData,
                        aggregateOverTheYear(addData, addWeights), formula[i - 1]);
                indexData = tempNewIndex[0];
                indexWeights = tempNewIndex[1];

                TsData[] tempWeightSum;
                tempWeightSum = weightsum(weightedSumData, weightedSumWeights,
                        unchain(addData), mid(addWeights, 1), formula[i - 1]);
                weightedSumData = tempWeightSum[0];
                weightedSumWeights = tempWeightSum[1];

            }

        }
        return scaleToRefYear(chainSum(weightedSumData), indexData, refYear);
    }

    /**
     *
     * @param formula
     * @param j
     * @return
     * @throws ec.nbdemetra.kix.KIXModel.InputException
     */
    private TsData doWBG(String[] formula, int j) throws InputException {
        check(formula, j);
        checkData(formula, j);
        checkWBG(formula, j);

        int lag = Integer.parseInt(formula[formula.length - 1]);
        TsData TsWBTData = extractData(indices.get(formula[1]));
        TsData TsWBTWeights = extractData(weights.get(formula[2]));
        TsData TsAllData = extractData(indices.get(formula[4]));
        TsData TsAllWeights = extractData(weights.get(formula[5]));

        TsAllData = normalizeToYear(TsAllData, TsAllData.getStart().getYear());
        TsData TsRemainData;
        TsData TsRemainWeights;
        if (formula[3].equalsIgnoreCase("-")) {
            TsData[] tempNewIndex = weightsum(unchain(TsAllData), mid(TsAllWeights, 1),
                    unchain(TsWBTData), mid(TsWBTWeights, 1), "+");
            TsRemainData = tempNewIndex[0];
            TsRemainWeights = TsAllWeights.plus(TsWBTWeights);
        } else {
            TsData[] tempNewIndex = weightsum(unchain(TsAllData), mid(TsAllWeights, 1),
                    unchain(TsWBTData), mid(TsWBTWeights, 1), "-");
            TsRemainData = tempNewIndex[0];
            TsRemainWeights = TsAllWeights.minus(TsWBTWeights);
        }

        TsData TsWBTDataLagDiff = weightsum(unchain(TsWBTData.lead(lag), TsWBTData),
                mid(TsWBTWeights, 1), TsRemainData, mid(TsRemainWeights, 1), formula[3])[0];
        TsWBTDataLagDiff = chainSum(TsWBTDataLagDiff, unchain(TsAllData));

        TsData TsAllDataLagDiff = ((TsAllData.lag(lag).minus(TsAllData)).div(TsAllData).times(100)).lead(lag);
        TsWBTDataLagDiff = ((TsWBTDataLagDiff.lag(lag).minus(TsAllData)).div(TsAllData).times(100)).lead(lag);

        TsData TsReturnData = TsAllDataLagDiff.minus(TsWBTDataLagDiff);
        return TsReturnData;
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
    private TsData unchain(TsData inputTsData) {
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
    private TsData unchain(TsData inputTsData, TsData helper) {
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
    private TsData[] weightsum(TsData weightedSumD, TsData weightedSumW, TsData addD, TsData addW, String operation) {
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
     * @param s
     * @param lag
     * @return
     */
    private TsData mid(TsData s, int lag) {
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
     *
     * @param s
     * @return
     */
    private TsData chainSum(TsData s) {
        return chainSum(s, s);
    }

    /**
     *
     * @param s
     * @param retVal
     * @return
     */
    private TsData chainSum(TsData s, TsData retVal) {
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
        retVal = retVal.times(s).div(100);
        return retVal;
    }

    /**
     *
     * @param addData
     * @param addWeights
     * @return
     */
    private TsData aggregateOverTheYear(TsData addData, TsData addWeights) {
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
     * @param chainedSum
     * @param indexD
     * @param refYear
     * @return
     */
    private TsData scaleToRefYear(TsData chainedSum, TsData indexD, Integer refYear) {
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
     * Returns a new time series which is normalized to the new reference year
     * (average of 100 in the reference year).
     *
     * @param indexData time series to normalize
     * @param refyear   reference year in which the new time series averages at
     *                  100
     * @return A new time series with an average of 100 in <b>year</b>.
     */
    private TsData normalizeToYear(TsData indexData, Integer refyear) {
        TsData returnData = indexData.clone();
        double factor = mid(indexData, 0).get(new TsPeriod(indexData.getFrequency(), refyear, 1));
        returnData = returnData.div(factor).times(100);
        return returnData;
    }

    /**
     * Checks if every part in the formula has a corresponding data part in the
     * data list and informs the user about formulas with missing data parts.
     *
     * @param formula String array with all parts of the requested formula
     * @param j       the count of the formula
     * @throws ec.nbdemetra.kix.KIXModel.InputException
     */
    private void check(String[] formula, int j) throws InputException {
        if (formula.length % 3 != 1) {
            throw new InputException("Formula "
                    + String.valueOf(j + 1) + " is not following the KIX syntax.");
        }

        String start = "The following data of formula " + String.valueOf(j + 1)
                + " could not be found: ";
        StringBuilder errortext = new StringBuilder(start);

        for (int i = 1; i < formula.length; i += 3) {
            if (!indices.contains(formula[i])) {
                errortext.append(formula[i]).append(", ");
            }
            if (!weights.contains(formula[i + 1])) {
                errortext.append(formula[i + 1]).append(", ");
            }
        }
        if (!errortext.toString().equals(start)) {
            errortext.delete(errortext.length() - 2, errortext.length());
            throw new InputException(errortext.toString());
        }
    }

    //TODO komplette Implementierung
    /**
     *
     * @param formula
     * @param j
     * @throws ec.nbdemetra.kix.KIXModel.InputException
     */
    private void checkData(String[] formula, int j) throws InputException {
        StringBuilder errortext = new StringBuilder();
        for (int i = 1; i < formula.length; i += 3) {
            if (!(indices.get(formula[i]).getDefinitionDomain().getStart().getPosition() == 0)
                    || !(weights.get(formula[i + 1]).getDefinitionDomain().getStart().getPosition() == 0)) {
                //TODO Prüfung vervollständigen/verifizieren
                errortext.append("Some data of formula ")
                        .append(String.valueOf(j + 1))
                        .append(" is not defined from their first period onward .");
            }
            if (indices.get(formula[i]).getDefinitionDomain().getStart().isBefore(weights.get(formula[i + 1]).getDefinitionDomain().getStart())) {
                errortext.append("The index series ")
                        .append(formula[i]).append("begins before the corresponding weight series ")
                        .append(formula[i + 1]).append("in formula ")
                        .append(String.valueOf(j + 1)).append(".");
            }
        }
        if (errortext.length() > 0) {
            throw new InputException(errortext.toString());
        }
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
    private void checkYearKIX(String year, int j) throws InputException {
        if (!tryParseInt(year)) {
            throw new InputException("The reference year (" + year + ") has to be numeric in formula " + String.valueOf(j + 1));
        }
        if (Integer.parseInt(year) < 1950) {
            throw new InputException("The reference year (" + year + ") has to be after 1949 in formula " + String.valueOf(j + 1));
        }
    }

    /**
     *
     * @param formula
     * @param j
     * @throws ec.nbdemetra.kix.KIXModel.InputException
     */
    private void checkWBG(String[] formula, int j) throws InputException {
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

    /**
     * Returns true if the String can be parsed to Integer.
     *
     * @param value String to test
     * @return <code>true</code> if value can be parsed to Integer;
     *         <code>false</code> otherwise.
     */
    boolean tryParseInt(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    private TsData extractData(ITsVariable input) {
        ArrayList<DataBlock> data = new ArrayList();
        data.add(new DataBlock(input.getDefinitionDomain().getLength()));
        input.data(input.getDefinitionDomain(), data);
        return new TsData(input.getDefinitionDomain().getStart(), data.get(0));
    }

    private void formatInput(String input) {
        String[] splitInput = input.split("\n");
        formulaNames = new String[splitInput.length][];
        request = new String[splitInput.length][];

        int counter = 0;
        for (String a : splitInput) {
            formulaNames[counter] = a.split("=", 2);
            if (formulaNames[counter].length == 2) {
                request[counter] = formulaNames[counter][1].split(",");
            } else {
                request[counter] = formulaNames[counter][0].split(",");
            }
            counter++;
        }
        for (String[] formula : request) {
            for (int j = 0; j < formula.length; j++) {
                formula[j] = formula[j].trim();
            }
        }

    }

    private void fillTsCollection(TsData[] outputTsData) {
        for (int i = 0; i < outputTsData.length; i++) {
            Ts t;
            if (formulaNames[i].length == 2) {
                MetaData metaData = new MetaData();
                metaData.put("titel", formulaNames[i][0]);
                metaData.put("formula", formulaNames[i][1]);
                t = TsFactory.instance.createTs(formulaNames[i][0], metaData, outputTsData[i]);
            } else {
                MetaData metaData = new MetaData();
                metaData.put("formula", formulaNames[i][0]);
                t = TsFactory.instance.createTs("Formula " + String.valueOf(i + 1), metaData, outputTsData[i]);
            }
            outputTsCollection.add(t);
        }
    }

    /**
     * Exceptions related to user input in KIX
     */
    private static class InputException extends RuntimeException {

        public InputException() {
        }

        public InputException(String s) {
            super(s);
        }
    }

}
