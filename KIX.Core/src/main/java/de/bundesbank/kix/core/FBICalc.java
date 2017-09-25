/* 
 * Copyright 2017 Deutsche Bundesbank
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
package de.bundesbank.kix.core;

import ec.tstoolkit.timeseries.simplets.TsData;

/**
 *
 * @author Thomas Witthohn
 */
public class FBICalc implements ICalc{

    private TsData totalIndex, totalWeight;

    public FBICalc(TsData totalIndex, double totalWeightDouble) {
        this.totalIndex = totalIndex;
        this.totalWeight = new TsData(totalIndex.getDomain(), totalWeightDouble);
    }

    public FBICalc(TsData totalIndex, TsData totalWeight) {
        this.totalIndex = totalIndex;
        this.totalWeight = totalWeight;
    }

    @Override
    public void plus(TsData indexSeriesTwo, TsData weightSeriesTwo) {
        totalIndex = totalIndex.times(totalWeight).plus(indexSeriesTwo.times(weightSeriesTwo)).div(totalWeight.plus(weightSeriesTwo));
        totalWeight = totalWeight.plus(weightSeriesTwo);
    }

    @Override
    public void plus(TsData indexSeriesTwo, double weightTwo) {
        totalIndex = totalIndex.times(totalWeight).plus(indexSeriesTwo.times(weightTwo)).div(totalWeight.plus(weightTwo));
        totalWeight = totalWeight.plus(weightTwo);
    }

    @Override
    public void minus(TsData indexSeriesTwo, TsData weightSeriesTwo) {
        totalIndex = totalIndex.times(totalWeight).minus(indexSeriesTwo.times(weightSeriesTwo)).div(totalWeight.minus(weightSeriesTwo));
        totalWeight = totalWeight.minus(weightSeriesTwo);
    }

    @Override
    public void minus(TsData indexSeriesTwo, double weightTwo) {
        totalIndex = totalIndex.times(totalWeight).minus(indexSeriesTwo.times(weightTwo)).div(totalWeight.minus(weightTwo));
        totalWeight = totalWeight.minus(weightTwo);
    }

    @Override
    public TsData getResult() {
        return totalIndex;
    }
}
