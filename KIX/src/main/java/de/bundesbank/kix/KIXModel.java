/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.kix;

import de.bundesbank.kix.calculation.KIXCalc;
import de.bundesbank.kix.calculation.KIXECalc;
import de.bundesbank.kix.options.KIXOptionsPanelController;
import de.bundesbank.kix.options.UnchainingMethod;
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
import java.util.ArrayList;
import java.util.Locale;
import javax.annotation.Nonnull;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

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
     * @param inputString
     * @param indices
     * @param weights
     * @return
     */
    @Override
    public TsCollection parser(@Nonnull String inputString, @Nonnull TsVariables indices, @Nonnull TsVariables weights) {
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
                    case "UNC":
                        outputTsData[j] = doUNC(formula, j);
                        break;
                    case "KIXE":
                        outputTsData[j] = doKIXE(formula, j);
                        break;
                    case "WBGE":
                        outputTsData[j] = doWBGE(formula, j);
                        break;
                    case "UNCE":
                        outputTsData[j] = doUNCE(formula, j);
                        break;
                    case "":
                        throw new InputException("No control character found in formula "
                                + String.valueOf(j + 1)
                                + ". Please use the syntax described in the tooltip or the help.");
                    default:
                        throw new InputException(formula[0].toUpperCase(Locale.ENGLISH) + " in formula "
                                + String.valueOf(j + 1)
                                + " is an invalid control character. Please use the syntax described in the tooltip or the help.");
                }
            } catch (InputException | TsException e) {
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
        checkYear(formula[formula.length - 1], j);
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
                indexWeights = KIXCalc.aggregateOverTheYear(addData, addWeights);

                weightedSumData = KIXCalc.unchain(addData);
                weightedSumWeights = KIXCalc.mid(addWeights, 1);

            } else {
                TsData[] tempNewIndex;
                tempNewIndex = KIXCalc.weightsum(indexData, indexWeights, addData,
                        KIXCalc.aggregateOverTheYear(addData, addWeights), formula[i - 1]);
                indexData = tempNewIndex[0];
                indexWeights = tempNewIndex[1];

                TsData[] tempWeightSum;
                tempWeightSum = KIXCalc.weightsum(weightedSumData, weightedSumWeights,
                        KIXCalc.unchain(addData), KIXCalc.mid(addWeights, 1), formula[i - 1]);
                weightedSumData = tempWeightSum[0];
                weightedSumWeights = tempWeightSum[1];

            }

        }
        TsData returnValue = KIXCalc.scaleToRefYear(KIXCalc.chainSum(weightedSumData), indexData, refYear);
        String unchainingMethod = NbPreferences.forModule(KIXOptionsPanelController.class).get(KIXOptionsPanelController.KIX2_DEFAULT_METHOD, UnchainingMethod.PRAGMATIC.toString());
        if (UnchainingMethod.valueOf(unchainingMethod) == UnchainingMethod.PURISTIC) {

            return returnValue.drop(returnValue.getFrequency().intValue() - returnValue.getStart().getPosition(), 0);
        }
        return returnValue;
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

        TsAllData = KIXCalc.normalizeToYear(TsAllData, TsAllData.getStart().getYear());
        TsData TsRemainData;
        TsData TsRemainWeights;
        if (formula[3].equalsIgnoreCase("-")) {
            TsData[] tempNewIndex = KIXCalc.weightsum(KIXCalc.unchain(TsAllData), KIXCalc.mid(TsAllWeights, 1),
                    KIXCalc.unchain(TsWBTData), KIXCalc.mid(TsWBTWeights, 1), "+");
            TsRemainData = tempNewIndex[0];
            TsRemainWeights = TsAllWeights.plus(TsWBTWeights);
        } else {
            TsData[] tempNewIndex = KIXCalc.weightsum(KIXCalc.unchain(TsAllData), KIXCalc.mid(TsAllWeights, 1),
                    KIXCalc.unchain(TsWBTData), KIXCalc.mid(TsWBTWeights, 1), "-");
            TsRemainData = tempNewIndex[0];
            TsRemainWeights = TsAllWeights.minus(TsWBTWeights);
        }

        TsData TsWBTDataLagDiff = KIXCalc.weightsum(KIXCalc.unchain(TsWBTData.lead(lag), TsWBTData),
                KIXCalc.mid(TsWBTWeights, 1), TsRemainData, KIXCalc.mid(TsRemainWeights, 1), formula[3])[0];
        TsWBTDataLagDiff = KIXCalc.chainSum(TsWBTDataLagDiff, KIXCalc.unchain(TsAllData));

        TsData TsAllDataLagDiff = ((TsAllData.lag(lag).minus(TsAllData)).div(TsAllData).times(100)).lead(lag);
        TsWBTDataLagDiff = ((TsWBTDataLagDiff.lag(lag).minus(TsAllData)).div(TsAllData).times(100)).lead(lag);

        TsData TsReturnData = TsAllDataLagDiff.minus(TsWBTDataLagDiff);
        return TsReturnData;
    }

    private TsData doUNC(String[] formula, int j) {
        TsData TsToUnchain;
        if (indices.contains(formula[1])) {
            TsToUnchain = extractData(indices.get(formula[1]));
        } else if (weights.contains(formula[1])) {
            TsToUnchain = extractData(weights.get(formula[1]));
        } else {
            throw new InputException(formula[1] + "in formula " + j + " doesn't exist");
        }
        return KIXCalc.unchain(TsToUnchain);
    }

    private TsData doKIXE(String[] formula, int j) {
        checkYear(formula[formula.length - 1], j);
        check(formula, j);

        int refYear = Integer.parseInt(formula[formula.length - 1]);
        double factor = 0;
        double factorWeight = 0;
        TsData weightedIndex = null;
        TsData weightedIndexWeights = null;
        TsData addData;
        TsData addWeights;

        for (int i = 1; i < formula.length; i += 3) {

            addData = extractData(indices.get(formula[i]));
            addWeights = extractData(weights.get(formula[i + 1]));

            checkNaN(addData, formula[i]);
            checkNaN(addWeights, formula[i + 1]);

            if (weightedIndex == null || weightedIndexWeights == null) {
                weightedIndex = KIXECalc.unchain(addData);
                weightedIndexWeights = addWeights;
                factor = KIXECalc.addToFactor(factor, factorWeight, addData, KIXECalc.weightInRefYear(addData, addWeights, refYear), refYear);
                factorWeight += KIXECalc.weightInRefYear(addData, addWeights, refYear);
            } else {
                if ("-".equals(formula[i - 1])) {
                    weightedIndex = KIXECalc.subtractFromWeightSum(weightedIndex, weightedIndexWeights, KIXECalc.unchain(addData), addWeights);
                    weightedIndexWeights = weightedIndexWeights.minus(addWeights);
                    factor = KIXECalc.subtractFromFactor(factor, factorWeight, addData, KIXECalc.weightInRefYear(addData, addWeights, refYear), refYear);
                    factorWeight -= KIXECalc.weightInRefYear(addData, addWeights, refYear);
                } else {
                    weightedIndex = KIXECalc.addToWeightSum(weightedIndex, weightedIndexWeights, KIXECalc.unchain(addData), addWeights);
                    weightedIndexWeights = weightedIndexWeights.plus(addWeights);
                    factor = KIXECalc.addToFactor(factor, factorWeight, addData, KIXECalc.weightInRefYear(addData, addWeights, refYear), refYear);
                    factorWeight += KIXECalc.weightInRefYear(addData, addWeights, refYear);
                }
            }

        }
        TsData returnValue = KIXECalc.scaleToRefYear(KIXECalc.chain(weightedIndex), factor, refYear);
        String unchainingMethod = NbPreferences.forModule(KIXOptionsPanelController.class).get(KIXOptionsPanelController.KIXE_DEFAULT_METHOD, UnchainingMethod.PURISTIC.toString());
        if (UnchainingMethod.valueOf(unchainingMethod) == UnchainingMethod.PURISTIC) {

            return returnValue.drop(returnValue.getFrequency().intValue() - returnValue.getStart().getPosition(), 0);
        }
        return returnValue;
    }

    private TsData doWBGE(String[] formula, int j) {
//        check(formula, j);
//        checkData(formula, j);

        int lag = Integer.parseInt(formula[5]);
        TsData TsWBTData = extractData(indices.get(formula[1]));
        TsData TsWBTWeights = extractData(weights.get(formula[2]));
        TsData TsAllData = extractData(indices.get(formula[3]));
        TsData TsAllWeights = extractData(weights.get(formula[4]));

        checkNaN(TsWBTData, formula[1]);
        checkNaN(TsWBTWeights, formula[2]);
        checkNaN(TsAllData, formula[3]);
        checkNaN(TsAllWeights, formula[4]);

        checkLag(TsWBTData, lag);

        return KIXECalc.contributionToGrowth(KIXECalc.unchain(TsWBTData), TsWBTWeights, KIXECalc.unchain(TsAllData), TsAllWeights, lag);
    }

    private TsData doUNCE(String[] formula, int j) {
        TsData TsToUnchain;
        if (indices.contains(formula[1])) {
            TsToUnchain = extractData(indices.get(formula[1]));
        } else if (weights.contains(formula[1])) {
            TsToUnchain = extractData(weights.get(formula[1]));
        } else {
            throw new InputException(formula[1] + "in formula " + j + " doesn't exist");

        }
        return KIXECalc.unchain(TsToUnchain);
    }

    /**
     * Checks if every part in the formula has a corresponding data part in the
     * data list and informs the user about formulas with missing data parts.
     *
     * @param formula String array with all parts of the requested formula
     * @param j the count of the formula
     * @throws ec.nbdemetra.kix.KIXModel.InputException
     */
    private void check(String[] formula, int j) throws InputException {
        if (formula.length % 3 != 1) {
            throw new InputException("Formula "
                    + String.valueOf(j + 1) + " is not following the correct syntax.");
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
    @NbBundle.Messages({
        "# {0} - formula number",
        "ERR_CHECKDATA_NotDefinedFirstPeriod=Some data of formula {0} is not defined from their first period onward.",
        "# {0} - index series name",
        "# {1} - weighted series name",
        "# {2} - formula number",
        "ERR_CHECKDATA_IndexStartsBeforeWeights=The index series {0} begins before the corresponding weight series {1} in formula {2}."
    })
    /**
     *
     * @param formula
     * @param j
     * @throws ec.nbdemetra.kix.KIXModel.InputException
     */
    private void checkData(String[] formula, int j) throws InputException {
        StringBuilder errortext = new StringBuilder();
        for (int i = 1; i < formula.length; i += 3) {
            TsPeriod indexStart = indices.get(formula[i]).getDefinitionDomain().getStart();
            TsPeriod weightStart = weights.get(formula[i + 1]).getDefinitionDomain().getStart();
            if (!(indexStart.getPosition() == 0)
                    || !(weightStart.getPosition() == 0)) {
                //TODO Prüfung vervollständigen/verifizieren
                errortext.append(Bundle.ERR_CHECKDATA_NotDefinedFirstPeriod(j + 1));
            }
            if (indexStart.isBefore(weightStart)) {
                errortext.append(Bundle.ERR_CHECKDATA_IndexStartsBeforeWeights(formula[i], formula[i + 1], j + 1));
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
     * <code>false</code> otherwise.
     */
    private boolean tryParseInt(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    private TsData extractData(ITsVariable input) {
        ArrayList<DataBlock> data = new ArrayList<>();
        data.add(new DataBlock(input.getDefinitionDomain().getLength()));
        input.data(input.getDefinitionDomain(), data);
        return new TsData(input.getDefinitionDomain().getStart(), data.get(0));
    }

    private void formatInput(String input) {
        input = input.replaceAll("[\n]+", "\n");
        if (input.startsWith("\n")) {
            input = input.replaceFirst("\n", "");
        }
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

    /**
     * Checks if the parameter <b>year</b> can be parsed to Integer and if the
     * year is 1950 or later.
     *
     * @param year the string representation of the year
     * @param j the count of the formula
     * @throws ec.nbdemetra.kix.KIXModel.InputException exception message
     * informs the user about the formula with the false year
     */
    private void checkYear(String year, int j) throws InputException {
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
    private void checkWBG(String[] formula, int j, TsVariables indices) throws InputException {
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

    private void checkNaN(TsData data, String name) throws InputException {
        if (data.getValues().hasMissingValues()) {
            throw new InputException("Missing values in " + name);
        }
    }

    private void checkLag(TsData data, int lag) throws InputException {
        if (data.getFrequency() == TsFrequency.Monthly && lag != 1 && lag != 3 && lag != 6 && lag != 12) {
            throw new InputException("Only lag 1,3,6 or 12 allowed for monthly data");
        }
        if (data.getFrequency() == TsFrequency.Quarterly && lag != 1 && lag != 2 && lag != 4) {
            throw new InputException("Only lag 1,2 or 4 allowed for quarterly data");
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
