/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.kix.actions;

import de.bundesbank.kix.KIXDocument;
import de.bundesbank.kix.KIXDocumentManager;
import de.bundesbank.kix.KIXTopComponent;
import ec.nbdemetra.ws.WorkspaceItem;
import ec.nbdemetra.ws.actions.AbstractViewAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "KIX",
        id = "de.bundesbank.kix.actions.RefreshContext"
)
@ActionRegistration(
        displayName = "#CTL_RefreshContext", lazy = true
)
@ActionReferences({
    @ActionReference(path = KIXDocumentManager.CONTEXTPATH, position = 1600)})

@Messages("CTL_RefreshContext=Refresh")
public final class RefreshContext extends AbstractViewAction<KIXTopComponent> {

    public RefreshContext() {
        super(KIXTopComponent.class);
        putValue(NAME, Bundle.CTL_RefreshContext());
        refreshAction();
    }

    @Override
    protected void refreshAction() {
        KIXTopComponent ui = context();
        if (ui != null) {
            WorkspaceItem<?> cur = ui.getDocument();
            enabled = cur != null && !cur.isReadOnly();
        }
    }

    @Override
    protected void process(KIXTopComponent cur) {
        KIXTopComponent ui = context();
        if (ui == null) {
            return;
        }
        WorkspaceItem<KIXDocument> doc = ui.getDocument();
        if (doc != null && !doc.isReadOnly()) {
            doc.getElement().refresh();
        }
    }

}
