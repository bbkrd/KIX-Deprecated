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
import ec.tstoolkit.timeseries.simplets.TsPeriod;

/**
 *
 * @author Thomas Witthohn
 */
public class WBGCalc extends AnnualOverlapCalc {

    private final TsData wholeIndex, wholeWeight;
    private TsData tsWBTDataLagDiff;
    private final int lag;

    /**
     *
     * @param index
     * @param weight
     * @param lag
     */
    public WBGCalc(TsData index, TsData weight, int lag) {

        this.wholeIndex = index.index(TsPeriod.year(index.getStart().getYear()), 100);
        this.wholeWeight = mid(weight, true);
        if (lag > 0) {
            this.lag = lag;
        } else {
            this.lag = wholeIndex.getFrequency().intValue() * lag * -1;
        }
    }

    @Override
    public void plus(TsData index, TsData weight) {
        weight = mid(weight, true);

        TsData TsRemainData = weightsum(unchain(wholeIndex), wholeWeight,
                                        unchain(index), weight, true);

        TsData TsRemainWeights = wholeWeight.minus(weight);

        tsWBTDataLagDiff = weightsum(unchain(index.lead(lag), index),
                                     weight, TsRemainData, TsRemainWeights, false);
    }

    @Override
    public void minus(TsData index, TsData weight) {
        weight = mid(weight, true);

        TsData TsRemainData = weightsum(unchain(wholeIndex), wholeWeight,
                                        unchain(index), weight, false);

        TsData TsRemainWeights = wholeWeight.plus(weight);

        tsWBTDataLagDiff = weightsum(unchain(index.lead(lag), index),
                                     weight, TsRemainData, TsRemainWeights, true);
    }

    @Override
    public TsData getResult() {
        TsData a = wholeIndex.lead(lag);

        TsData TsAllDataLagDiff = (wholeIndex.minus(a)).div(a).times(100);

        TsData chainedTsWBTDataLagDiff = chainSum(tsWBTDataLagDiff, unchain(wholeIndex));
        chainedTsWBTDataLagDiff = (chainedTsWBTDataLagDiff.minus(a)).div(a).times(100);

        TsData TsReturnData = TsAllDataLagDiff.minus(chainedTsWBTDataLagDiff);
        return TsReturnData;

    }

    private TsData weightsum(TsData weightedSumD, TsData weightedSumW, TsData addD, TsData addW, boolean minus) {
        if (minus) {
            return weightedSumD.times(weightedSumW)
                    .minus(addD.times(addW))
                    .div(weightedSumW.minus(addW));
        } else {
            return weightedSumD.times(weightedSumW)
                    .plus(addD.times(addW))
                    .div(weightedSumW.plus(addW));
        }

    }
}
