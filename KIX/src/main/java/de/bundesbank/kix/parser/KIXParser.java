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
import ec.tstoolkit.timeseries.regression.TsVariables;
import ec.tstoolkit.timeseries.simplets.TsData;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = IParser.class)
public class KIXParser extends AbstractParser {

    @Override
    public TsData compute(String[] formula, TsVariables indices, TsVariables weights) {
        isFormulaLengthPossible(formula, 3, 1);
        isYear1900OrLater(formula[formula.length - 1]);
        getMissingDataParts(formula, indices, weights);
        //checkData(formula);

        int refYear = Integer.parseInt(formula[formula.length - 1]);

        boolean displayFirstYear = NbPreferences.forModule(KIXOptionsPanelController.class).getBoolean(KIX2_DEFAULT_METHOD, true);

        ICalc calculator = null;
        for (int i = 1; i < formula.length; i += 3) {
            TsData addData = extractData(indices.get(formula[i]));
            TsData addWeights = extractData(weights.get(formula[i + 1]));

            if (calculator == null) {
                calculator = new KIXCalc(addData, addWeights, refYear, displayFirstYear);
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
