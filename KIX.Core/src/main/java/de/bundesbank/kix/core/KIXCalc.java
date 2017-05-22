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

import static de.bundesbank.kix.core.AnnualOverlapCalc.mid;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsPeriod;

/**
 *
 * @author Thomas Witthohn
 */
public class KIXCalc extends AnnualOverlapCalc {

    private double factor, factorWeight;
    private final boolean puristic;
    private final int referenzYear;
    private TsData weightedSumData;
    private TsData weightedSumWeights;

    public KIXCalc(TsData indexData, TsData indexWeights, int referenzYear) {
        this(indexData, indexWeights, referenzYear, false);
    }

    public KIXCalc(TsData indexData, TsData indexWeights, int referenzYear, boolean puristic) {
        this.weightedSumData = unchain(indexData);
        this.weightedSumWeights = mid(indexWeights, true);

        this.puristic = puristic;
        this.referenzYear = referenzYear;

        TsPeriod referenzYearPeriod = new TsPeriod(indexData.getFrequency(), referenzYear, 1);
        factor = mid(indexData, false).get(referenzYearPeriod);
        factorWeight = calculateFactorWeight(indexData, indexWeights);
    }

    @Override
    public void add(TsData addData, TsData addWeights) {
        addFactor(addData, addWeights);
        addWeightsum(addData, addWeights);
    }

    @Override
    public TsData getResult() {
        TsPeriod referenzYearPeriod = new TsPeriod(weightedSumData.getFrequency(), referenzYear, 0);
        double meanInRefYear = mid(chainSum(weightedSumData), false).get(referenzYearPeriod);

        TsData indexData = chainSum(weightedSumData).times(factor).div(meanInRefYear);
        if (puristic) {
            return indexData.drop(indexData.getFrequency().intValue() - indexData.getStart().getPosition(), 0);
        }
        return indexData;
    }

    @Override
    public void minus(TsData subtractData, TsData subtractWeights) {
        subtractFactor(subtractData, subtractWeights);
        subtractWeightsum(subtractData, subtractWeights);
    }

    private void addFactor(TsData addData, TsData addWeights) {
        TsPeriod referenzYearPeriod = new TsPeriod(addData.getFrequency(), referenzYear, 1);
        double midIndexAtRefYear = mid(addData, false).get(referenzYearPeriod);
        double addFactorWeight = calculateFactorWeight(addData, addWeights);

        factor = (factor * factorWeight + midIndexAtRefYear * addFactorWeight) / (factorWeight + addFactorWeight);
        factorWeight += addFactorWeight;
    }

    /**
     *
     * @param addData
     * @param addWeights
     */
    private void addWeightsum(TsData addData, TsData addWeights) {

        addData = unchain(addData);
        addWeights = mid(addWeights, true);
        weightedSumData = (weightedSumData.times(weightedSumWeights)
                .plus(addData.times(addWeights)))
                .div(weightedSumWeights.plus(addWeights));
        weightedSumWeights = weightedSumWeights.plus(addWeights);

    }

    private double calculateFactorWeight(TsData data, TsData weights) {
        double sumDataRefYear = 0, sumDataPreYear = 0, sumWeightPreYear = 0;
        for (int i = 0; i < data.getFrequency().intValue(); i++) {
            TsPeriod referenzYearPeriod = new TsPeriod(data.getFrequency(), referenzYear, i);
            TsPeriod previousYearPeriod = new TsPeriod(data.getFrequency(), referenzYear - 1, i);

            sumDataRefYear += data.get(referenzYearPeriod);
            sumDataPreYear += data.get(previousYearPeriod);
            sumWeightPreYear += weights.get(previousYearPeriod);
        }
        return sumDataRefYear * sumWeightPreYear / sumDataPreYear;
    }

    private void subtractFactor(TsData addData, TsData addWeights) {
        TsPeriod referenzYearPeriod = new TsPeriod(addData.getFrequency(), referenzYear, 1);
        double midIndexAtRefYear = mid(addData, false).get(referenzYearPeriod);
        double subtractFactorWeight = calculateFactorWeight(addData, addWeights);

        factor = (factor * factorWeight - midIndexAtRefYear * subtractFactorWeight) / (factorWeight - subtractFactorWeight);
        factorWeight -= subtractFactorWeight;
    }

    /**
     *
     * @param subtractData
     * @param subtractWeights
     */
    private void subtractWeightsum(TsData subtractData, TsData subtractWeights) {
        subtractData = unchain(subtractData);
        subtractWeights = mid(subtractWeights, true);
        weightedSumData = (weightedSumData.times(weightedSumWeights)
                .minus(subtractData.times(subtractWeights)))
                .div(weightedSumWeights.minus(subtractWeights));
        weightedSumWeights = weightedSumWeights.minus(subtractWeights);
    }
}
