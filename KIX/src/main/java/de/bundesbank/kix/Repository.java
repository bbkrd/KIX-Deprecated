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
                    x.clearErrorMessage();
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
            String line = splitInput[i];
            String formulaName, formula;
            if (line.contains("=")) {
                formulaName = line.substring(0, line.indexOf('=')).trim();
                formula = line.substring(line.indexOf('=') + 1).trim();
            } else {
                formulaName = "Formula " + (i + 1);
                formula = line.trim();
            }
            String controlCharacter = formula.split(",", 2)[0].trim();
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
}
