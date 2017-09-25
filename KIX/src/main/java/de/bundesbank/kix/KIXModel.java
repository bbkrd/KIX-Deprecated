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
package de.bundesbank.kix;

import de.bundesbank.kix.core.*;
import de.bundesbank.kix.options.KIXOptionsPanelController;
import static de.bundesbank.kix.options.KIXOptionsPanelController.KIX2_DEFAULT_METHOD;
import static de.bundesbank.kix.options.KIXOptionsPanelController.KIXE_DEFAULT_METHOD;
import de.bundesbank.kix.options.UnchainingMethod;
import static de.bundesbank.kix.options.UnchainingMethod.PRAGMATIC;
import static de.bundesbank.kix.options.UnchainingMethod.PURISTIC;
import ec.tss.Ts;
import ec.tss.TsCollection;
import ec.tss.TsFactory;
import ec.tstoolkit.MetaData;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.timeseries.TsException;
import ec.tstoolkit.timeseries.regression.ITsVariable;
import ec.tstoolkit.timeseries.regression.TsVariable;
import ec.tstoolkit.timeseries.regression.TsVariables;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Pattern;
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

    @NbBundle.Messages({
        "# {0} - formula control character",
        "# {1} - formula number",
        "ERR_InvalidControlCharacter={0} in formula {1}  is an invalid control character. Please use the syntax described in the help."})
    /**
     *
     * @param inputString
     * @param indices
     * @param weights
     *
     * @return
     */
    @Override
    public TsCollection parser(@Nonnull String inputString, @Nonnull TsVariables indices, @Nonnull TsVariables weights) {
        this.indices = indices;
        this.weights = weights;
        formatInput(inputString);
        TsData[] outputTsData = new TsData[request.length];
        outputTsCollection = TsFactory.instance.createTsCollection();
        StringBuilder errorMessage = new StringBuilder();

        for (int j = 1; j <= request.length; j++) {
            try {
                String[] formula;
                if (request[j - 1].length != 0) {
                    formula = request[j - 1];
                } else {
                    formula = new String[]{""};
                }
                switch (ControlCharacter.fromString(formula[0])) {
                    case KIX:
                        outputTsData[j - 1] = doKIX(formula, j);
                        break;
                    case WBG:
                        outputTsData[j - 1] = doWBG(formula, j);
                        break;
                    case UNC:
                        outputTsData[j - 1] = doUNC(formula, j);
                        break;
                    case CHA:
                        outputTsData[j - 1] = doCHA(formula, j);
                        break;
                    case KIXE:
                        outputTsData[j - 1] = doKIXE(formula, j);
                        break;
                    case WBGE:
                        outputTsData[j - 1] = doWBGE(formula, j);
                        break;
                    case UNCE:
                        outputTsData[j - 1] = doUNCE(formula, j);
                        break;
                    case CHAE:
                        outputTsData[j - 1] = doCHAE(formula, j);
                        break;
                    case FBI:
                        outputTsData[j - 1] = doFBI(formula, j);
                        break;
                    default:
                        errorMessage.append(Bundle.ERR_InvalidControlCharacter(formula[0].toUpperCase(Locale.ENGLISH), j)).append("\n");
                }
            } catch (InputException | TsException e) {
                errorMessage.append(e.getMessage()).append("\n");
            }
        }

        if (errorMessage.length() > 0) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(errorMessage.toString());
            DialogDisplayer.getDefault().notify(nd);
        }

        fillTsCollection(outputTsData);
        return outputTsCollection;

    }

    /**
     *
     * @param formula
     * @param j
     *
     * @return
     *
     * @throws ec.nbdemetra.kix.KIXModel.InputException
     */
    private TsData doKIX(String[] formula, int j) {
        generalFormulaCheck(formula, j, 3, 1);
        yearCheck(formula[formula.length - 1], j);
        dataAvailabilityCheck(formula, j);
        checkData(formula, j);

        int refYear = Integer.parseInt(formula[formula.length - 1]);

        String unchainingMethod = NbPreferences.forModule(KIXOptionsPanelController.class).get(KIX2_DEFAULT_METHOD, PRAGMATIC.name());
        boolean puristic = UnchainingMethod.fromString(unchainingMethod) == PURISTIC;

        ICalc calculator = null;
        for (int i = 1; i < formula.length; i += 3) {
            TsData addData = extractData(indices.get(formula[i]));
            TsData addWeights = extractData(weights.get(formula[i + 1]));

            if (calculator == null) {
                calculator = new KIXCalc(addData, addWeights, refYear, puristic);
            } else if (formula[i - 1].equals("+")) {
                calculator.plus(addData, addWeights);
            } else {
                calculator.minus(addData, addWeights);
            }
        }

        if (calculator != null) {
            return calculator.getResult();
        } else {
            throw new InputException("Error in formular " + j);
        }
    }

    /**
     *
     * @param formula
     * @param j
     *
     * @return
     *
     * @throws ec.nbdemetra.kix.KIXModel.InputException
     */
    private TsData doWBG(String[] formula, int j) {
        generalFormulaCheck(formula, j, 3, 1);
        dataAvailabilityCheck(formula, j);
        checkData(formula, j);
        checkWBG(formula, j, indices);

        int lag = Integer.parseInt(formula[formula.length - 1]);
        TsData TsWBTData = extractData(indices.get(formula[1]));
        TsData TsWBTWeights = extractData(weights.get(formula[2]));
        TsData TsAllData = extractData(indices.get(formula[4]));
        TsData TsAllWeights = extractData(weights.get(formula[5]));

        ICalc calc = new WBGCalc(TsAllData, TsAllWeights, lag);

        if (formula[3].equalsIgnoreCase("-")) {
            calc.minus(TsWBTData, TsWBTWeights);
        } else {
            calc.plus(TsWBTData, TsWBTWeights);
        }

        return calc.getResult();
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
        return AnnualOverlapCalc.unchain(TsToUnchain);
    }

    private TsData doCHA(String[] formula, int j) {
        TsData TsToChain;
        if (indices.contains(formula[1])) {
            TsToChain = extractData(indices.get(formula[1]));
        } else if (weights.contains(formula[1])) {
            TsToChain = extractData(weights.get(formula[1]));
        } else {
            throw new InputException(formula[1] + "in formula " + j + " doesn't exist");
        }
        if (formula.length > 2 && tryParseInt(formula[2])) {
            return AnnualOverlapCalc.chainSum(TsToChain).index(TsPeriod.year(Integer.parseInt(formula[2])), 100);
        }
        return AnnualOverlapCalc.chainSum(TsToChain);
    }

    private TsData doKIXE(String[] formula, int j) {
        yearCheck(formula[formula.length - 1], j);
        generalFormulaCheck(formula, j, 3, 1);
        dataAvailabilityCheck(formula, j);

        int refYear = Integer.parseInt(formula[formula.length - 1]);
        TsData addData;
        TsData addWeights;

        String unchainingMethod = NbPreferences.forModule(KIXOptionsPanelController.class).get(KIXE_DEFAULT_METHOD, PURISTIC.name());
        boolean puristic = UnchainingMethod.fromString(unchainingMethod) == PURISTIC;

        ICalc calculator = null;
        for (int i = 1; i < formula.length; i += 3) {

            addData = extractData(indices.get(formula[i]));
            addWeights = extractData(weights.get(formula[i + 1]));

            checkNaN(addData, formula[i]);
            checkNaN(addWeights, formula[i + 1]);

            if (calculator == null) {
                calculator = new KIXECalc(addData, addWeights, refYear, puristic);
            } else if (formula[i - 1].equals("+")) {
                calculator.plus(addData, addWeights);
            } else {
                calculator.minus(addData, addWeights);
            }
        }

        if (calculator != null) {
            return calculator.getResult();
        } else {
            throw new InputException("Error in formular " + j);
        }
    }

    private TsData doWBGE(String[] formula, int j) {
        generalFormulaCheck(formula, j, 3, 0);
        dataAvailabilityCheck(formula, j);

        int lag = Integer.parseInt(formula[5]);
        TsData TsWBTData = extractData(indices.get(formula[1]));
        TsData TsWBTWeights = extractData(weights.get(formula[2]));
        TsData TsAllData = extractData(indices.get(formula[3]));
        TsData TsAllWeights = extractData(weights.get(formula[4]));

        checkNaN(TsWBTData, formula[1]);
        checkNaN(TsWBTWeights, formula[2]);
        checkNaN(TsAllData, formula[3]);
        checkNaN(TsAllWeights, formula[4]);

        checkLag(TsWBTData, lag, j);

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

    private TsData doCHAE(String[] formula, int j) {
        TsData TsToChain;
        if (indices.contains(formula[1])) {
            TsToChain = extractData(indices.get(formula[1]));
        } else if (weights.contains(formula[1])) {
            TsToChain = extractData(weights.get(formula[1]));
        } else {
            throw new InputException(formula[1] + "in formula " + j + " doesn't exist");
        }
        if (formula.length > 2 && tryParseInt(formula[2])) {
            return KIXECalc.chain(TsToChain).index(TsPeriod.year(Integer.parseInt(formula[2])), 100);
        }
        return KIXECalc.chain(TsToChain);
    }

    /**
     * Checks if every part in the formula has a corresponding data part in the
     * data list and informs the user about formulas with missing data parts.
     *
     * @param formula String array with all parts of the requested formula
     * @param j       the count of the formula
     *
     * @throws ec.nbdemetra.kix.KIXModel.InputException
     */
    private void dataAvailabilityCheck(String[] formula, int j) {
        String start = "The following data of formula " + j
                + " could not be found: ";
        StringBuilder errortext = new StringBuilder(start);

        int increment = ControlCharacter.fromString(formula[0]).getNumber();
        for (int i = 1; i + increment - 1 < formula.length; i += increment) {
            if (!indices.contains(formula[i])) {
                errortext.append(formula[i]).append(", ");
            }
            if (!weights.contains(formula[i + 1]) && !formula[i + 1].matches("[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?")) {
                errortext.append(formula[i + 1]).append(", ");
            }
        }
        if (!errortext.toString().equals(start)) {
            errortext.delete(errortext.length() - 2, errortext.length());
            throw new InputException(errortext.toString());
        }
    }

    private void generalFormulaCheck(String[] formula, int formulaNumber, int modulo, int expected) {
        if (formula.length % modulo != expected) {
            throw new InputException("Formula "
                    + formulaNumber + " is not following the correct syntax.");
        }
    }

    //TODO komplette Implementierung
    @NbBundle.Messages({
        "# {0} - formula number",
        "ERR_CHECKDATA_NotDefinedFirstPeriod=Some data of formula {0} is not defined from their first period onward.",
        "# {0} - index series name",
        "# {1} - weighted series name",
        "# {2} - formula number",
        "ERR_CHECKDATA_IndexStartsBeforeWeights=The index series {0} begins before the corresponding weight series {1} in formula {2}.",
        "# {0} - index series name",
        "ERR_CHECKDATA_IndexNoStartDate=Index series {0} has no defined start date.",
        "# {0} - weighted series name",
        "ERR_CHECKDATA_WeightNoStartDate=Weight series {0} has no defined start date."
    })
    /**
     *
     * @param formula
     * @param j
     *
     * @throws ec.nbdemetra.kix.KIXModel.InputException
     */
    private void checkData(String[] formula, int j) {
        StringBuilder errortext = new StringBuilder();
        for (int i = 1; i < formula.length; i += 3) {
            if (indices.get(formula[i]).getDefinitionDomain() == null) {
                errortext.append(Bundle.ERR_CHECKDATA_IndexNoStartDate(formula[i]));
            }
            if (weights.get(formula[i + 1]).getDefinitionDomain() == null) {
                errortext.append(Bundle.ERR_CHECKDATA_WeightNoStartDate(formula[i + 1]));
            }
            TsPeriod indexStart = indices.get(formula[i]).getDefinitionDomain().getStart();
            TsPeriod weightStart = weights.get(formula[i + 1]).getDefinitionDomain().getStart();

            if (!(indexStart.getPosition() == 0)
                    || !(weightStart.getPosition() == 0)) {
                //TODO Prüfung vervollständigen/verifizieren
                errortext.append(Bundle.ERR_CHECKDATA_NotDefinedFirstPeriod(j));
            }
            if (indexStart.isBefore(weightStart)) {
                errortext.append(Bundle.ERR_CHECKDATA_IndexStartsBeforeWeights(formula[i], formula[i + 1], j));
            }
        }
        if (errortext.length() > 0) {
            throw new InputException(errortext.toString());
        }
    }

    private TsData extractData(ITsVariable input) {
        if (input instanceof TsVariable) {
            return ((TsVariable) input).getTsData();
        }
        ArrayList<DataBlock> data = new ArrayList<>();
        TsDomain domain = input.getDefinitionDomain();
        data.add(new DataBlock(domain.getLength()));
        input.data(domain, data);
        return new TsData(domain.getStart(), data.get(0));
    }

    private void formatInput(String input) {
        input = Pattern.compile("^\\s*#.*", Pattern.MULTILINE).matcher(input).replaceAll("");
        input = input.replaceAll("[\n]+", "\n").trim();
        String[] splitInput = input.split("\n");
        formulaNames = new String[splitInput.length][];
        request = new String[splitInput.length][];

        int counter = 0;
        for (String line : splitInput) {
            line = line.replaceAll("\\s*", "");
            formulaNames[counter] = line.split("=", 2);
            int formulaPosition = formulaNames[counter].length - 1;
            String formula = formulaNames[counter][formulaPosition].toLowerCase(Locale.ENGLISH);
            //TODO efficient and flexible way instead of hardcoded magic strings
            if (formula.startsWith("kix") || formula.startsWith("wbg") || formula.startsWith("cli") || formula.startsWith("pcg")) {
                formulaNames[counter][formulaPosition] = addMissingWeights(formula);
                request[counter] = formulaNames[counter][formulaPosition].split(",");
            } else {
                request[counter] = formula.split(",");
            }
            counter++;
        }
        for (String[] formula : request) {
            if (formula != null) {
                for (int j = 0; j < formula.length; j++) {
                    formula[j] = formula[j].trim();
                }
            }
        }

    }

    /**
     * Checks if the parameter <b>year</b> can be parsed to Integer and if the
     * year is 1900 or later.
     *
     * @param year the string representation of the year
     * @param j    the count of the formula
     *
     * @throws ec.nbdemetra.kix.KIXModel.InputException exception message
     *                                                  informs the user about the formula with the false year
     */
    private void yearCheck(String year, int j) {
        if (!tryParseInt(year)) {
            throw new InputException("The reference year (" + year + ") has to be numeric in formula " + j);
        }
        if (Integer.parseInt(year) < 1900) {
            throw new InputException("The reference year (" + year + ") has to be after 1899 in formula " + j);
        }
    }

    /**
     *
     * @param formula
     * @param j
     *
     * @throws ec.nbdemetra.kix.KIXModel.InputException
     */
    private void checkWBG(String[] formula, int j, TsVariables indices) {
        if (formula.length != 7) {
            throw new InputException("Formula "
                    + j + " is not following the WBG syntax.");
        }
        if (Integer.parseInt(formula[6]) < 1 || Integer.parseInt(formula[6]) > indices.get(formula[1]).getDefinitionFrequency().intValue()) {
            throw new InputException("The number of lags has to be at between 1 and "
                    + indices.get(formula[1]).getDefinitionFrequency().intValue()
                    + "(maximum lag one year) in formula "
                    + j);
        }
        if (!(indices.get(formula[4]).getDefinitionDomain().getStart().isNotBefore(indices.get(formula[1]).getDefinitionDomain().getStart()))) {
            throw new InputException("The contributing index series (iContr) should not begin after the total index series (iTotal) in formula "
                    + j);
        }
    }

    private void checkNaN(TsData data, String name) {
        if (data.hasMissingValues()) {
            throw new InputException("Missing values in " + name);
        }
    }

    private void checkLag(TsData data, int lag, int j) {
        if (data.getFrequency() == TsFrequency.Monthly && lag != 1 && lag != 3 && lag != 6 && lag != 12) {
            throw new InputException("In formula" + j + ": Only lag 1,3,6 or 12 allowed for monthly data");
        }
        if (data.getFrequency() == TsFrequency.Quarterly && lag != 1 && lag != 2 && lag != 4) {
            throw new InputException("In formula" + j + ": Only lag 1,2 or 4 allowed for quarterly data");
        }
    }

    private void fillTsCollection(TsData[] outputTsData) {
        for (int i = 0; i < outputTsData.length; i++) {
            if (outputTsData[i] != null) {
                MetaData metaData = new MetaData();
                String name;
                if (formulaNames[i].length == 2) {
                    name = formulaNames[i][0];
                    metaData.put("name", name);
                    metaData.put("formula", formulaNames[i][1]);
                } else {
                    name = "Formula " + String.valueOf(i + 1);
                    metaData.put("formula", formulaNames[i][0]);
                }
                Ts t = TsFactory.instance.createTs(name, metaData, outputTsData[i]);
                outputTsCollection.add(t);
            }
        }
    }

    private String addMissingWeights(String input) {
        input = input.replaceAll("i((?:\\d)+)(?=,(((([\\+\\-]){1}|i(\\d)+),)|(\\d)+))", "i$1,w$1");
        return input;
    }

    private TsData doFBI(String[] formula, int j) {
        ICalc calc = null;

        generalFormulaCheck(formula, j, 3, 0);
        dataAvailabilityCheck(formula, j);

        for (int i = 1; i < formula.length; i += 3) {

            TsData addData = extractData(indices.get(formula[i]));
            if (formula[i + 1].matches("[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?")) {
                double addWeights = Double.parseDouble(formula[i + 1]);
                if (calc == null) {
                    calc = new FBICalc(addData, addWeights);
                } else if (formula[i - 1].equals("+")) {
                    calc.plus(addData, addWeights);
                } else {
                    calc.minus(addData, addWeights);
                }
            } else {
                TsData addWeights = extractData(weights.get(formula[i + 1]));
                if (calc == null) {
                    calc = new FBICalc(addData, addWeights);
                } else if (formula[i - 1].equals("+")) {
                    calc.plus(addData, addWeights);
                } else {
                    calc.minus(addData, addWeights);
                }
            }

        }

        if (calc != null) {
            return calc.getResult();
        } else {
            throw new InputException("Error in formular " + j);
        }
    }

    /**
     * Returns true if the String can be parsed to Integer.
     *
     * @param value String to test
     *
     * @return <code>true</code> if value can be parsed to Integer;
     *         <code>false</code> otherwise.
     */
    private boolean tryParseInt(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }
}
