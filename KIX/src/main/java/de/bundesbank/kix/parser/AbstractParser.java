/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.kix.parser;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.timeseries.regression.ITsVariable;
import ec.tstoolkit.timeseries.regression.TsVariable;
import ec.tstoolkit.timeseries.regression.TsVariables;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author Thomas Witthohn
 */
public abstract class AbstractParser implements IParser {

    private String errorMessage = "";

    @Override
    public final String getErrorMessage() {
        return errorMessage;
    }

    protected final void setErrorMessage(final String input) {
        if (input == null) {
            throw new IllegalArgumentException("Input shouldn't be null");
        }
        this.errorMessage = input;
    }

    @Override
    public void clearErrorMessage() {
        this.errorMessage = "";
    }

    protected final TsData extractData(final ITsVariable input) {
        if (input instanceof TsVariable) {
            return ((TsVariable) input).getTsData();
        }
        ArrayList<DataBlock> data = new ArrayList<>();
        TsDomain domain = input.getDefinitionDomain();
        data.add(new DataBlock(domain.getLength()));
        input.data(domain, data);
        return new TsData(domain.getStart(), data.get(0));
    }

    protected final boolean isDataAvailable(final String[] input, final TsVariables indices, final TsVariables weights) {
        Set<String> missingData = new TreeSet<>();
        for (int i = 1; i < input.length - 1; i++) {
            String string = input[i];
            if ((string.startsWith("i") && !indices.contains(string))
                    || (string.startsWith("w") && !weights.contains(string))) {
                missingData.add(string);
            }
        }
        if (!missingData.isEmpty()) {
            setErrorMessage("The following data is not available: " + String.join(" ", missingData));
            return false;
        }
        return true;
    }

}
