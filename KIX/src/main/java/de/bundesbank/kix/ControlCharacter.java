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

/**
 *
 * @author Thomas Witthohn
 */
public enum ControlCharacter {

    KIX("kix", 3),
    WBG("wbg", 3),
    UNC("unc", 0),
    CHA("cha", 0),
    KIXE("kixe", 3),
    WBGE("wbge", 2),
    UNCE("unce", 0),
    CHAE("chae", 0),
    FBI("fbi", 3);

    private ControlCharacter(String name, int number) {
        this.name = name;
        this.number = number;
    }

    private final String name;
    private final int number;

    public String getName() {
        return name;
    }

    public int getNumber() {
        return number;
    }

    /**
     * @param text the text of the constant to return
     * @return the enum constant with the specified text
     * @throws IllegalArgumentException - if no constant with the specified name
     * exists
     * @throws NullPointerException - if text is null
     */
    public static ControlCharacter fromString(String text) {
        for (ControlCharacter b : ControlCharacter.values()) {
            if (text.equalsIgnoreCase(b.name)) {
                return b;
            }
        }
        throw new IllegalArgumentException("No constant with text " + text + " found");
    }
}
