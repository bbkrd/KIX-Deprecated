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

import de.bundesbank.kix.parser.IParser;
import ec.tss.Ts;
import ec.tss.TsCollection;
import ec.tss.TsFactory;
import ec.tstoolkit.MetaData;
import ec.tstoolkit.timeseries.TsException;
import ec.tstoolkit.timeseries.regression.TsVariables;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * @author Thomas Witthohn
 */
public class Repository {

    private FormulaInput[] formulaInputs;
    private TsCollection outputTsCollection;

    @NbBundle.Messages({
        "# {0} - formula control character",
        "# {1} - formula number",
        "ERR_InvalidControlCharacter={0} in formula {1} is an invalid control character. Please use the syntax described in the help.",
        "# {0} - formula number",
        "ERR_ErrorInCalculation=Error(s) in formula {0}:\n"})
    /**
     *
     * @param inputString
     * @param indices
     * @param weights
     *
     * @return
     */
    public final TsCollection calculate(@Nonnull String inputString, @Nonnull TsVariables indices, @Nonnull TsVariables weights) {
        convertStringToFormulaInput(inputString);
        TsData[] outputTsData = new TsData[formulaInputs.length];
        outputTsCollection = TsFactory.instance.createTsCollection();
        StringBuilder errorMessage = new StringBuilder();

        for (int i = 0; i <= formulaInputs.length - 1; i++) {
            try {
                String controlCharacter = formulaInputs[i].getControlCharacter();

                Optional<? extends IParser> optionalParser = Lookup.getDefault().lookupAll(IParser.class).stream().filter(x -> x.isValidControlCharacter(controlCharacter)).findFirst();
                if (optionalParser.isPresent()) {
                    IParser x = optionalParser.get();
                    TsData tsData = x.compute(formulaInputs[i].getFormula(), indices, weights);
                    String parserErrorMessage = x.getErrorMessage();
                    if (parserErrorMessage == null || parserErrorMessage.isEmpty()) {
                        outputTsData[i] = tsData;
                    } else {
                        errorMessage.append(Bundle.ERR_ErrorInCalculation(i + 1)).append(parserErrorMessage).append("\n");
                    }

                } else {
                    errorMessage.append(Bundle.ERR_InvalidControlCharacter(controlCharacter.toUpperCase(Locale.ENGLISH), i + 1)).append("\n");
                }
            } catch (TsException e) {
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

    private void convertStringToFormulaInput(String input) {
        input = Pattern.compile("^\\s*#.*", Pattern.MULTILINE).matcher(input).replaceAll("");
        input = input.replaceAll("[\n]+", "\n").trim();
        String[] splitInput = input.split("\n");

        formulaInputs = new FormulaInput[splitInput.length];

        for (int i = 0; i < splitInput.length; i++) {
            String line = splitInput[i].replaceAll("\\s*", "");
            String formulaName, formula;
            if (line.contains("=")) {
                formulaName = line.substring(0, line.indexOf('='));
                formula = line.substring(line.indexOf('='));
            } else {
                formulaName = "Formula " + (i + 1);
                formula = line;
            }
            String controlCharacter = formula.split(",", 2)[0];
            formulaInputs[i] = new FormulaInput(formulaName, controlCharacter, formula);
        }
    }

    private void fillTsCollection(TsData[] outputTsData) {
        for (int i = 0; i < outputTsData.length; i++) {
            if (outputTsData[i] != null) {
                MetaData metaData = new MetaData();
                String name = formulaInputs[i].getName();
                metaData.put("formula", formulaInputs[i].getFormula());

                Ts t = TsFactory.instance.createTs(name, metaData, outputTsData[i]);
                outputTsCollection.add(t);
            }
        }
    }

//    //TODO komplette Implementierung
//    @NbBundle.Messages({
//        "# {0} - formula number",
//        "ERR_CHECKDATA_NotDefinedFirstPeriod=Some data of formula {0} is not defined from their first period onward.",
//        "# {0} - index series name",
//        "# {1} - weighted series name",
//        "# {2} - formula number",
//        "ERR_CHECKDATA_IndexStartsBeforeWeights=The index series {0} begins before the corresponding weight series {1} in formula {2}.",
//        "# {0} - index series name",
//        "ERR_CHECKDATA_IndexNoStartDate=Index series {0} has no defined start date.",
//        "# {0} - weighted series name",
//        "ERR_CHECKDATA_WeightNoStartDate=Weight series {0} has no defined start date."
//    })
//    /**
//     *
//     * @param formula
//     * @param j
//     *
//     */
//    private void checkData(String[] formula, int j) {
//        StringBuilder errortext = new StringBuilder();
//        for (int i = 1; i < formula.length; i += 3) {
//            if (indices.get(formula[i]).getDefinitionDomain() == null) {
//                errortext.append(Bundle.ERR_CHECKDATA_IndexNoStartDate(formula[i]));
//            }
//            if (weights.get(formula[i + 1]).getDefinitionDomain() == null) {
//                errortext.append(Bundle.ERR_CHECKDATA_WeightNoStartDate(formula[i + 1]));
//            }
//            TsPeriod indexStart = indices.get(formula[i]).getDefinitionDomain().getStart();
//            TsPeriod weightStart = weights.get(formula[i + 1]).getDefinitionDomain().getStart();
//
//            if (!(indexStart.getPosition() == 0)
//                    || !(weightStart.getPosition() == 0)) {
//                //TODO Prüfung vervollständigen/verifizieren
//                errortext.append(Bundle.ERR_CHECKDATA_NotDefinedFirstPeriod(j));
//            }
//            if (indexStart.isBefore(weightStart)) {
//                errortext.append(Bundle.ERR_CHECKDATA_IndexStartsBeforeWeights(formula[i], formula[i + 1], j));
//            }
//        }
//        if (errortext.length() > 0) {
//        }
//    }
//
//    /**
//     *
//     * @param formula
//     * @param j
//     *
//     * @throws ec.nbdemetra.kix.KIXModel.InputException
//     */
//    private void checkWBG(String[] formula, int j, TsVariables indices) {
//
//        
//        if (!(indices.get(formula[4]).getDefinitionDomain().getStart().isNotBefore(indices.get(formula[1]).getDefinitionDomain().getStart()))) {
//            throw new InputException("The contributing index series (iContr) should not begin after the total index series (iTotal) in formula "
//                    + j);
//        }
//    }
//
//    private void checkNaN(TsData data, String name) {
//        if (data.hasMissingValues()) {
//            throw new InputException("Missing values in " + name);
//        }
//    }
//
//    private void checkLag(TsData data, int lag, int j) {
//        if (data.getFrequency() == TsFrequency.Monthly && lag != 1 && lag != 3 && lag != 6 && lag != 12) {
//            throw new InputException("In formula" + j + ": Only lag 1,3,6 or 12 allowed for monthly data");
//        }
//        if (data.getFrequency() == TsFrequency.Quarterly && lag != 1 && lag != 2 && lag != 4) {
//            throw new InputException("In formula" + j + ": Only lag 1,2 or 4 allowed for quarterly data");
//        }
//    }
//
//    private String addMissingWeights(String input) {
//        return input.replaceAll("i((?:\\d)+)(?=,(((([\\+\\-]){1}|i(\\d)+),)|(\\d)+))", "i$1,w$1");
//    }
}
