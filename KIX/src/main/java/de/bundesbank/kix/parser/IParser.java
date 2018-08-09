/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.kix.parser;

import ec.tstoolkit.design.ServiceDefinition;
import ec.tstoolkit.timeseries.regression.TsVariables;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.util.Locale;

/**
 *
 * @author Thomas Witthohn
 */
@ServiceDefinition
public interface IParser {

    TsData compute(final String formula, final TsVariables indices, TsVariables weights);

    default boolean isValidControlCharacter(String controlCharacter) {
        String temp = controlCharacter.toLowerCase(Locale.ENGLISH);
        for (String string : getValidControlCharacter()) {
            if (string.equals(temp)) {
                return true;
            }
        }
        return false;
    }

    String[] getValidControlCharacter();

    String getErrorMessage();

    void clearErrorMessage();

}
