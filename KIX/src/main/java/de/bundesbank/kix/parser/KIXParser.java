/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.kix.parser;

import de.bundesbank.kix.InputException;
import de.bundesbank.kix.core.ICalc;
import de.bundesbank.kix.core.KIXCalc;
import de.bundesbank.kix.options.KIXOptionsPanelController;
import static de.bundesbank.kix.options.KIXOptionsPanelController.KIX2_DEFAULT_METHOD;
import de.bundesbank.kix.options.UnchainingMethod;
import static de.bundesbank.kix.options.UnchainingMethod.PRAGMATIC;
import static de.bundesbank.kix.options.UnchainingMethod.PURISTIC;
import ec.tstoolkit.timeseries.regression.TsVariables;
import ec.tstoolkit.timeseries.simplets.TsData;
import org.openide.util.NbPreferences;

public class KIXParser extends AbstractParser {

    @Override
    public TsData compute(String[] formula, TsVariables indices, TsVariables weights) {
        isFormulaLengthPossible(formula, 3, 1);
        isYear1900OrLater(formula[formula.length - 1]);
        getMissingDataParts(formula, indices, weights);
        //checkData(formula);

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
            throw new InputException("Error in formular");
        }
    }

}
