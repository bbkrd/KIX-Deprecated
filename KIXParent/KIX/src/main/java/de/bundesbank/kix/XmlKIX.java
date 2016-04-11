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

import ec.tss.xml.IXmlConverter;
import ec.tss.xml.regression.XmlTsVariables;
import javax.swing.text.BadLocationException;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.openide.util.Exceptions;

/**
 *
 * @author Thomas Witthohn
 */
@XmlRootElement(name = XmlKIX.RNAME)
@XmlType(name = XmlKIX.NAME)
public class XmlKIX implements IXmlConverter<KIXDocument> {

    static final String NAME = "KIXType";
    static final String RNAME = "KIX";

    @XmlElement
    public String input;

   
    @XmlElement(name = "indices")
    public XmlTsVariables indices;
    
    @XmlElement(name = "weights")
    public XmlTsVariables weights;
  
    @Override
    public KIXDocument create() {
        KIXDocument doc = new KIXDocument();
        if(input!=null && input.length()!=0){
             doc.setInput(input);
        }
        doc.setIndices(indices.create());
        doc.setWeights(weights.create());
        return doc;
    }

    @Override
    public void copy(KIXDocument k) {
        try {
            input = k.getinput().getText(0, k.getinput().getLength());
            indices = new XmlTsVariables();
            indices.copy(k.getIndices());
            weights = new XmlTsVariables();
            weights.copy(k.getWeights());
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
