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
package de.bundesbank.kix;

import java.util.Locale;

/**
 *
 * @author Thomas Witthohn
 */
public enum ControlCharacter {

    KIX(new String[]{"kix", "cli.ann"}, 3),
    WBG(new String[]{"wbg", "pcg.ann"}, 3),
    UNC(new String[]{"unc.ann"}, 0),
    CHA(new String[]{"cha.ann"}, 0),
    KIXE(new String[]{"kixe", "cli.per"}, 3),
    WBGE(new String[]{"wbge", "pcg.per"}, 2),
    UNCE(new String[]{"unc.per"}, 0),
    CHAE(new String[]{"cha.per"}, 0),
    FBI(new String[]{"fbi"}, 3),
    UNKNOWN(new String[]{}, 0);

    private ControlCharacter(String[] names, int number) {
        this.names = names;
        this.number = number;
    }

    private final String[] names;
    private final int number;

    public String[] getNames() {
        return names;
    }

    public int getNumber() {
        return number;
    }

    /**
     * @param text the text of the constant to return
     *
     * @return the enum constant with the specified text
     *
     * @throws IllegalArgumentException - if no constant with the specified name
     *                                  exists
     * @throws NullPointerException     - if text is null
     */
    public static ControlCharacter fromString(String text) {
        String inputLowerCase = text.toLowerCase(Locale.ENGLISH);
        for (ControlCharacter b : ControlCharacter.values()) {
            for (String name : b.getNames()) {
                if (inputLowerCase.equals(name)) {
                    return b;
                }
            }
        }
        return UNKNOWN;
    }
}
