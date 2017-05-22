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

import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;

/**
 *
 * @author Thomas Witthohn
 */
public abstract class AnnualOverlapCalc implements ICalc {

    /**
     *
     * @param s
     * @param previousYearAverage
     * @return
     */
    public static TsData mid(TsData s, boolean previousYearAverage) {
        int startYear = s.getStart().getYear();
        int numberOfYears = s.getLastPeriod().getYear() - startYear + 1;

        int lag = previousYearAverage ? 1 : 0;
        double[] averages = new double[numberOfYears + lag];
        s.changeFrequency(TsFrequency.Yearly, TsAggregationType.Average, false).copyTo(averages, lag);
        if (lag == 1) {
            averages[0] = averages[1];
        }

        TsFrequency frequence = s.getFrequency();
        TsData result = new TsData(s.getDomain());
        for (int i = 0; i < numberOfYears; i++) {
            double average = averages[i];

            if (i == numberOfYears - 1) {
                int lastPeriodPosition = s.getLastPeriod().getPosition();
                for (int k = 0; k <= lastPeriodPosition; k++) {
                    result.set(new TsPeriod(frequence, startYear + i, k), average);
                }
            } else {
                for (int k = 0; k < frequence.intValue(); k++) {
                    result.set(new TsPeriod(frequence, startYear + i, k), average);
                }
            }
        }

        return result;
    }

    /**
     *
     * @param s
     * @return
     */
    public static TsData chainSum(TsData s) {
        return chainSum(s, s);
    }

    /**
     *
     * @param s
     * @param helper
     * @return
     */
    public static TsData chainSum(TsData s, TsData helper) {
        helper = mid(helper, true);
        int startYear = helper.getStart().getYear();
        int endYear = helper.getLastPeriod().getYear();
        int lastPeriodPosition = s.getLastPeriod().getPosition();
        TsFrequency frequence = helper.getFrequency();

        for (int i = 1; i <= endYear - startYear; i++) {
            TsPeriod period = new TsPeriod(frequence, startYear + i - 1, 0);
            if (i == endYear - startYear) {
                for (int j = 0; j <= lastPeriodPosition; j++) {
                    if (Double.isFinite(helper.get(period.plus(j)))) {
                        double prevyearval = helper.get(period.plus(j));
                        double thisyearval = helper.get(period.plus(j + frequence.intValue()));
                        double result = (thisyearval * prevyearval / 100);
                        helper.set(period.plus(j + frequence.intValue()), result);
                    }
                }
            } else {
                for (int j = 0; j < frequence.intValue(); j++) {
                    if (Double.isFinite(helper.get(period.plus(j)))) {
                        double prevyearval = helper.get(period.plus(j));
                        double thisyearval = helper.get(period.plus(j + frequence.intValue()));
                        double result = (thisyearval * prevyearval / 100);
                        helper.set(period.plus(j + frequence.intValue()), result);
                    }
                }
            }
        }
        return helper.times(s).div(100);
    }

    /**
     * Returns an unchained time series. Each value of <code>index</code> is
     * therefor divided by the prior-year average and multiplyed by 100. Calls
     * unchain(inputTsData, inputTsData).
     *
     * @param index the time series
     * @return A new unchained time series is returned.
     * @see #unchain(TsData, TsData)
     */
    public static TsData unchain(TsData index) {
        return unchain(index, index);
    }

    /**
     * Returns an unchained time series. Each value of <code>index</code> is
     * therefor divided by the prior-year average of <code>helper</code> and
     * multiplyed by 100.
     *
     * @param index the time series to be unchained
     * @param helper the time series used for the prior-year averages
     * @return A new unchained time series is returned.
     */
    public static TsData unchain(TsData index, TsData helper) {
        return index.times(100).div(mid(helper, true));
    }

}
