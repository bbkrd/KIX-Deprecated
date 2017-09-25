/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.kix.parser;

import de.bundesbank.kix.ControlCharacter;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.timeseries.regression.ITsVariable;
import ec.tstoolkit.timeseries.regression.TsVariables;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import java.util.ArrayList;

/**
 *
 * @author s4504tw
 */
public abstract class AbstractParser implements IParser {

    protected TsData extractData(ITsVariable input) {
        ArrayList<DataBlock> data = new ArrayList<>();
        TsDomain domain = input.getDefinitionDomain();
        data.add(new DataBlock(domain.getLength()));
        input.data(domain, data);
        return new TsData(domain.getStart(), data.get(0));
    }

    /**
     * Returns true if the String can be parsed to Integer.
     *
     * @param value String to test
     *
     * @return <code>true</code> if value can be parsed to Integer;
     *         <code>false</code> otherwise.
     */
    protected boolean tryParseInt(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    protected boolean isFormulaLengthPossible(String[] formula, int variableAmount, int constantAmount) {
        return formula.length % variableAmount == constantAmount;
    }

    /**
     * Checks if the parameter <b>year</b> can be parsed to Integer and if the
     * year is 1900 or later.
     *
     * @param year the string representation of the year
     */
    protected boolean isYear1900OrLater(String year) {
        return tryParseInt(year) && Integer.parseInt(year) >= 1900;
    }

    /**
     * Checks if every part in the formula has a corresponding data part in the
     * data list and returns the names of missing data parts.
     *
     * @param formula String array with all parts of the requested formula
     * @param indices
     * @param weights
     */
    protected String getMissingDataParts(String[] formula, TsVariables indices, TsVariables weights) {
        StringBuilder errortext = null;

        int increment = ControlCharacter.fromString(formula[0]).getNumber();
        for (int i = 1; i + increment - 1 < formula.length; i += increment) {
            if (!indices.contains(formula[i])) {
                if (errortext == null) {
                    errortext = new StringBuilder("The following data could not be found: ");
                }
                errortext.append(formula[i]).append(", ");
            }
            if (!weights.contains(formula[i + 1]) && !formula[i + 1].matches("[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?")) {
                if (errortext == null) {
                    errortext = new StringBuilder("The following data could not be found: ");
                }
                errortext.append(formula[i + 1]).append(", ");
            }
        }
        if (errortext != null) {
            return errortext.delete(errortext.length() - 2, errortext.length()).toString();
        }
        return null;
    }
}
