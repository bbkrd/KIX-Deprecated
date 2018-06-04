/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.kix;

/**
 *
 * @author Deutsche Bundesbank
 */
@lombok.Data
public class FormulaInput {

    private final String name;
    private final String controlCharacter;
    private final String formula;

}
