/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.nbdemetra.kix.actions;

import ec.nbdemetra.kix.KIXDocument;
import ec.nbdemetra.kix.KIXDocumentManager;
import ec.nbdemetra.ws.WorkspaceFactory;
import ec.nbdemetra.ws.WorkspaceItem;
import ec.tstoolkit.timeseries.regression.ITsVariable;
import ec.tstoolkit.utilities.IDynamicObject;
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
    @ActionReference(path=KIXDocumentManager.PATH, position = 1700, separatorBefore = 1699)
})
@Messages("CTL_RefreshAllAction=Refresh all")
public final class RefreshAllAction implements ActionListener {

    public RefreshAllAction() {
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        List<WorkspaceItem<KIXDocument>> documents = WorkspaceFactory.getInstance().getActiveWorkspace().searchDocuments(KIXDocument.class);
        for (WorkspaceItem<KIXDocument> document : documents) {
            for (ITsVariable var : document.getElement().getIndices().variables()) {
                if (var instanceof IDynamicObject) {
                    IDynamicObject dvar = (IDynamicObject) var;
                    dvar.refresh();
                }
            }
            for (ITsVariable var : document.getElement().getWeights().variables()) {
                if (var instanceof IDynamicObject) {
                    IDynamicObject dvar = (IDynamicObject) var;
                    dvar.refresh();
                }
            }
        }
    }
}
