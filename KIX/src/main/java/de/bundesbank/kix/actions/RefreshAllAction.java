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
package de.bundesbank.kix.actions;

import de.bundesbank.kix.KIXDocument;
import de.bundesbank.kix.KIXDocumentManager;
import ec.nbdemetra.ws.WorkspaceFactory;
import ec.nbdemetra.ws.WorkspaceItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "KIX",
        id = "ec.nbdemetra.kix.actions.RefreshAllAction")
@ActionRegistration(displayName = "#CTL_RefreshAllAction")
@ActionReferences({
    @ActionReference(path = KIXDocumentManager.PATH, position = 1700, separatorBefore = 1699)
})
@Messages("CTL_RefreshAllAction=Refresh all")
public final class RefreshAllAction implements ActionListener {

    public RefreshAllAction() {
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        List<WorkspaceItem<KIXDocument>> documents = WorkspaceFactory.getInstance().getActiveWorkspace().searchDocuments(KIXDocument.class);
        documents.stream()
                .filter(document -> document != null && !document.isReadOnly())
                .forEach(document -> document.getElement().refresh());
    }
}
