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
package de.bundesbank.kix.parser;

import ec.tstoolkit.timeseries.simplets.TsData;

/**
 *
 * @author Thomas Witthohn
 */
public interface ICalc {

    void plus(TsData index, TsData weight);

    default void plus(TsData index, double weight) {
        plus(index, new TsData(index.getDomain(), weight));
    }

    void minus(TsData index, TsData weight);

    default void minus(TsData index, double weight) {
        minus(index, new TsData(index.getDomain(), weight));
    }

    TsData getResult();
}
