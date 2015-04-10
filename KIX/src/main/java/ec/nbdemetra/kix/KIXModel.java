/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.nbdemetra.kix;

import static ec.nbdemetra.kix.calculation.KIXCalc.aggregateOverTheYear;
import static ec.nbdemetra.kix.calculation.KIXCalc.chainSum;
import static ec.nbdemetra.kix.calculation.KIXCalc.checkWBG;
import static ec.nbdemetra.kix.calculation.KIXCalc.checkYearKIX;
import static ec.nbdemetra.kix.calculation.KIXCalc.mid;
import static ec.nbdemetra.kix.calculation.KIXCalc.normalizeToYear;
import static ec.nbdemetra.kix.calculation.KIXCalc.scaleToRefYear;
import static ec.nbdemetra.kix.calculation.KIXCalc.unchain;
import static ec.nbdemetra.kix.calculation.KIXCalc.weightsum;
import ec.tss.Ts;
import ec.tss.TsCollection;
import ec.tss.TsFactory;
import ec.tstoolkit.MetaData;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.timeseries.TsException;
import ec.tstoolkit.timeseries.regression.ITsVariable;
import ec.tstoolkit.timeseries.regression.TsVariables;
import ec.tstoolkit.timeseries.simplets.TsData;
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
        checkWBG(formula, j, indices);

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
                        .append(" is not defined from their first period onward.");
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
     * Returns true if the String can be parsed to Integer.
     *
     * @param value String to test
     * @return <code>true</code> if value can be parsed to Integer;
     *         <code>false</code> otherwise.
     */
    public static boolean tryParseInt(String value) {
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
            if (outputTsData[i] != null) {
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
    }
}
