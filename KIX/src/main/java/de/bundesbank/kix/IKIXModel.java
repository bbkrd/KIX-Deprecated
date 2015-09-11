/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.kix;

import ec.tss.TsCollection;
import ec.tstoolkit.timeseries.regression.TsVariables;

/**
 *
 * @author Thomas Witthohn
 */
public interface IKIXModel {

    public TsCollection parser(String inputString, TsVariables indices, TsVariables weights);

}
