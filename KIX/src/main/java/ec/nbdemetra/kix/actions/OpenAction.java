/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.nbdemetra.kix.actions;

import ec.nbdemetra.kix.KIXDocument;
import ec.nbdemetra.kix.KIXDocumentManager;
import ec.nbdemetra.ws.WorkspaceFactory;
import ec.nbdemetra.ws.WorkspaceItem;
import ec.nbdemetra.ws.nodes.WsNode;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

@ActionID(category = "KIX",
          id = "ec.nbdemetra.kix.actions.OpenAction")
@ActionRegistration(displayName = "#CTL_OpenAction")
@ActionReferences({
    @ActionReference(path = KIXDocumentManager.ITEMPATH, position = 1600, separatorBefore = 1590)
})
@NbBundle.Messages("CTL_OpenAction=Open")
public class OpenAction implements ActionListener {

    private final WsNode context;

    public OpenAction(WsNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        WorkspaceItem<KIXDocument> doc = context.getWorkspace().searchDocument(context.lookup(), KIXDocument.class);
        KIXDocumentManager mgr = WorkspaceFactory.getInstance().getManager(KIXDocumentManager.class);
        mgr.openDocument(doc);
    }
}
