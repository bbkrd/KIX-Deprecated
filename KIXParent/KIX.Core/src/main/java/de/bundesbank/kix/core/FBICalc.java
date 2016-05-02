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

    private static String errorMessage = "Can't calculate scalar weights and matrix weights together";

    private TsData totalIndex, totalWeight;
    private double totalWeightDouble;
    private final boolean variableWeight;

    public FBICalc(TsData totalIndex, double totalWeightDouble) {
        this.totalIndex = totalIndex;
        this.totalWeightDouble = totalWeightDouble;
        variableWeight = false;
    }

    public FBICalc(TsData totalIndex, TsData totalWeight) {
        this.totalIndex = totalIndex;
        this.totalWeight = totalWeight;
        variableWeight = true;
    }

    public void add(TsData indexSeriesTwo, TsData weightSeriesTwo) {
        if (variableWeight) {
            totalIndex = totalIndex.times(totalWeight).plus(indexSeriesTwo.times(weightSeriesTwo)).div(totalWeight.plus(weightSeriesTwo));
            totalWeight = totalWeight.plus(weightSeriesTwo);
        } else {
            throw new FBIException(errorMessage);
        }
    }

    public void add(TsData indexSeriesTwo, double weightTwo) {
        if (!variableWeight) {
            totalIndex = totalIndex.times(totalWeightDouble).plus(indexSeriesTwo.times(weightTwo)).div(totalWeightDouble + weightTwo);
            totalWeightDouble = totalWeightDouble + weightTwo;
        } else {
            throw new FBIException(errorMessage);
        }
    }

    public void minus(TsData indexSeriesTwo, TsData weightSeriesTwo) {
        if (variableWeight) {
            totalIndex = totalIndex.times(totalWeight).minus(indexSeriesTwo.times(weightSeriesTwo)).div(totalWeight.minus(weightSeriesTwo));
            totalWeight = totalWeight.minus(weightSeriesTwo);
        } else {
            throw new FBIException(errorMessage);
        }
    }

    public void minus(TsData indexSeriesTwo, double weightTwo) {
        if (!variableWeight) {
            totalIndex = totalIndex.times(totalWeightDouble).minus(indexSeriesTwo.times(weightTwo)).div(totalWeightDouble - weightTwo);
            totalWeightDouble = totalWeightDouble - weightTwo;
        } else {
            throw new FBIException(errorMessage);
        }
    }

    public TsData getResult() {
        return totalIndex;
    }

    public static class FBIException extends RuntimeException {

        public FBIException(String message) {
            super(message);
        }

    }

}
