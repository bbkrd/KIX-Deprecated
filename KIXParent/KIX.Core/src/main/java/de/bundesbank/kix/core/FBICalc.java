/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.kix.core;

import ec.tstoolkit.timeseries.simplets.TsData;

/**
 *
 * @author Thomas Witthohn
 */
public class FBICalc {

    private TsData totalIndex, totalWeight;

    public FBICalc(TsData totalIndex, double totalWeightDouble) {
        this.totalIndex = totalIndex;
        this.totalWeight = new TsData(totalIndex.getDomain(), totalWeightDouble);
    }

    public FBICalc(TsData totalIndex, TsData totalWeight) {
        this.totalIndex = totalIndex;
        this.totalWeight = totalWeight;
    }

    public void add(TsData indexSeriesTwo, TsData weightSeriesTwo) {
        totalIndex = totalIndex.times(totalWeight).plus(indexSeriesTwo.times(weightSeriesTwo)).div(totalWeight.plus(weightSeriesTwo));
        totalWeight = totalWeight.plus(weightSeriesTwo);
    }

    public void add(TsData indexSeriesTwo, double weightTwo) {
        totalIndex = totalIndex.times(totalWeight).plus(indexSeriesTwo.times(weightTwo)).div(totalWeight.plus(weightTwo));
        totalWeight = totalWeight.plus(weightTwo);
    }

    public void minus(TsData indexSeriesTwo, TsData weightSeriesTwo) {
        totalIndex = totalIndex.times(totalWeight).minus(indexSeriesTwo.times(weightSeriesTwo)).div(totalWeight.minus(weightSeriesTwo));
        totalWeight = totalWeight.minus(weightSeriesTwo);
    }

    public void minus(TsData indexSeriesTwo, double weightTwo) {
        totalIndex = totalIndex.times(totalWeight).minus(indexSeriesTwo.times(weightTwo)).div(totalWeight.minus(weightTwo));
        totalWeight = totalWeight.minus(weightTwo);
    }

    public TsData getResult() {
        return totalIndex;
    }
}
