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

import com.google.common.base.Strings;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.information.InformationSetSerializable;
import ec.tstoolkit.timeseries.regression.TsVariables;
import ec.tstoolkit.utilities.DefaultNameValidator;
import ec.tstoolkit.utilities.IDynamicObject;
import ec.tstoolkit.utilities.IModifiable;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import org.openide.util.Exceptions;

/**
 *
 * @author Thomas Witthohn
 */
public class KIXDocument implements IModifiable, InformationSetSerializable, IDynamicObject {

    private static final String INDICES = "indices", WEIGHTS = "weights", INPUT = "input";

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
            i = Strings.emptyToNull(i);
            this.inputString.insertString(0, i, null);
            this.initial = i;
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public TsVariables getIndices() {
        return indices;
    }

    public void setIndices(TsVariables i) {
        this.indices = i;
    }

    public TsVariables getWeights() {
        return weights;
    }

    public void setWeights(TsVariables i) {
        this.weights = i;
    }

    @Override
    public boolean isDirty() {
        try {
            return indices.isDirty()
                    || weights.isDirty()
                    || !inputString.getText(0, inputString.getLength()).equals(initial);
        } catch (BadLocationException ex) {
            return true;
        }
    }

    @Override
    public boolean read(InformationSet info) {
        setInput(info.get(INPUT, String.class));
        InformationSet indicesInfo = info.getSubSet(INDICES);
        if (indicesInfo != null) {
            boolean tok = indices.read(indicesInfo);
            if (!tok) {
                return false;
            }
        }
        InformationSet weightsInfo = info.getSubSet(WEIGHTS);
        if (weightsInfo != null) {
            boolean tok = weights.read(weightsInfo);
            if (!tok) {
                return false;
            }
        }
        return true;
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

    @Override
    public InformationSet write(boolean verbose) {
        InformationSet info = new InformationSet();
        try {
            info.add(INPUT, inputString.getText(0, inputString.getLength()));
        } catch (BadLocationException ex) {
            //Can't be thrown
        }
        InformationSet indicesInfo = indices.write(verbose);
        if (indicesInfo != null) {
            info.add(INDICES, indicesInfo);
        }
        InformationSet weightsInfo = weights.write(verbose);
        if (weightsInfo != null) {
            info.add(WEIGHTS, weightsInfo);
        }
        return info;
    }

    @Override
    public boolean refresh() {
        indices.variables().stream()
                .filter((variable) -> (variable instanceof IDynamicObject))
                .forEach(dynamicVariable -> ((IDynamicObject) dynamicVariable).refresh());
        weights.variables().stream()
                .filter((variable) -> (variable instanceof IDynamicObject))
                .forEach(dynamicVariable -> ((IDynamicObject) dynamicVariable).refresh());
        return true;
    }
}
