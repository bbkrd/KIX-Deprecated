/*
 * Copyright 2016 Deutsche Bundesbank
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
public class KIXECalc extends LastPeriodOverlapCalc {

    private double factor, factorWeight;
    private final boolean displayFirstYear;
    private final int referenzYear;
    private TsData weightedSumData;
    private TsData weightedSumWeights;

    public KIXECalc(TsData indexData, TsData indexWeights, int referenzYear, boolean displayFirstYear) {
        this.displayFirstYear = displayFirstYear;
        this.referenzYear = referenzYear;

        this.weightedSumData = unchain(indexData);
        this.weightedSumWeights = indexWeights;

        factorWeight = weightInRefYear(indexData, indexWeights, referenzYear);
        factor = meanInRefYear(indexData, referenzYear);

    }

    @Override
    public void plus(TsData index, TsData weight) {
        weightedSumData = addToWeightSum(weightedSumData, weightedSumWeights, unchain(index), weight);
        weightedSumWeights = weightedSumWeights.plus(weight);
        factor = addToFactor(factor, factorWeight, index, weightInRefYear(index, weight, referenzYear), referenzYear);
        factorWeight += weightInRefYear(index, weight, referenzYear);

    }

    @Override
    public void minus(TsData index, TsData weight) {
        weightedSumData = subtractFromWeightSum(weightedSumData, weightedSumWeights, unchain(index), weight);
        weightedSumWeights = weightedSumWeights.minus(weight);
        factor = subtractFromFactor(factor, factorWeight, index, weightInRefYear(index, weight, referenzYear), referenzYear);
        factorWeight -= weightInRefYear(index, weight, referenzYear);
    }

    @Override
    public TsData getResult() {

        TsData indexData = scaleToRefYear(chain(weightedSumData), factor, referenzYear);
        if (displayFirstYear) {
            return indexData;
        }
        return indexData.drop(indexData.getFrequency().intValue() - indexData.getStart().getPosition(), 0);

    }
}
