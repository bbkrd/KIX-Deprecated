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
package de.bundesbank.kix;

import ec.tstoolkit.timeseries.regression.TsVariables;
import ec.tstoolkit.utilities.DefaultNameValidator;
import ec.tstoolkit.utilities.IModifiable;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import org.openide.util.Exceptions;

/**
 *
 * @author Thomas Witthohn
 */
public class KIXDocument implements IModifiable {

    private final String VALIDATOR = ",= +-";
    private TsVariables indices, weights;
    private final Document inputString;
    private String initial;

    public KIXDocument() {
        indices = new TsVariables("i", new DefaultNameValidator(VALIDATOR));
        weights = new TsVariables("w", new DefaultNameValidator(VALIDATOR));
        initial = "";
        inputString = new PlainDocument();
    }

    public Document getinput() {
        return inputString;
    }

    public void setInput(String i) {
        try {
            this.inputString.insertString(0, i, null);
            this.initial = i;
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public TsVariables getIndices() {
        //TODO better way
        boolean dirty = indices.isDirty();
        TsVariables temp = indices;
        indices = new TsVariables("i", new DefaultNameValidator(VALIDATOR));
        for (String name : temp.getNames()) {
            indices.set(name, temp.get(name));
        }
        if (!dirty) {
            indices.resetDirty();
        }
        return indices;
    }

    public void setIndices(TsVariables i) {
        this.indices = i;
    }

    public TsVariables getWeights() {
        //TODO better way
        boolean dirty = weights.isDirty();
        TsVariables temp = weights;
        weights = new TsVariables("w", new DefaultNameValidator(VALIDATOR));
        for (String name : temp.getNames()) {
            weights.set(name, temp.get(name));
        }
        if (!dirty) {
            weights.resetDirty();
        }
        return weights;
    }

    public void setWeights(TsVariables i) {
        this.weights = i;
    }

    @Override
    public boolean isDirty() {
        try {
            return indices.isDirty() || weights.isDirty() || !inputString.getText(0, inputString.getLength()).equals(initial);
        } catch (BadLocationException ex) {
            return true;
        }
    }

    @Override
    public void resetDirty() {
        indices.resetDirty();
        weights.resetDirty();
        try {
            initial = inputString.getText(0, inputString.getLength());
        } catch (BadLocationException e) {
            initial = null;
        }
    }
}
