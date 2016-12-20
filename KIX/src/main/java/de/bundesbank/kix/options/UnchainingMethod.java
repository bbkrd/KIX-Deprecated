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
package de.bundesbank.kix.options;

import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Thomas Witthohn
 */
@Messages({"PRAGMATIC=Pragmatic", "PURISTIC=Puristic"})
public enum UnchainingMethod {

    PRAGMATIC(Bundle.PRAGMATIC()),
    PURISTIC(Bundle.PURISTIC());

    private String name;

    private UnchainingMethod(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * @param text the text of the constant to return
     * @return the enum constant with the specified text
     * @throws IllegalArgumentException - if no constant with the specified name
     * exists
     * @throws NullPointerException - if text is null
     */
    public static UnchainingMethod fromString(String text) {
        for (UnchainingMethod b : UnchainingMethod.values()) {
            if (text.equalsIgnoreCase(b.name)) {
                return b;
            }
        }
        throw new IllegalArgumentException("No constant with text " + text + " found");
    }
}
