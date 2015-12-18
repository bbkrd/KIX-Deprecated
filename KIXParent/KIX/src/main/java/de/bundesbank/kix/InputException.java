/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.kix;

/**
 * Exceptions related to user input in KIX
 *
 * @author Thomas Witthohn
 */
public class InputException extends RuntimeException {

    public InputException() {
        super();
    }

    public InputException(String s) {
        super(s);
    }
}
