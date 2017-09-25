/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.kix.parser;

import ec.tstoolkit.design.ServiceDefinition;
import ec.tstoolkit.timeseries.regression.TsVariables;
import ec.tstoolkit.timeseries.simplets.TsData;

/**
 *
 * @author Thomas Witthohn
 */
@ServiceDefinition
public interface IParser {

    public TsData compute(final String[] formula, final TsVariables indices, TsVariables weights);

}
