/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.nbdemetra.kix.actions;

import ec.nbdemetra.kix.KIXDocumentManager;
import ec.nbdemetra.ws.IWorkspaceItemManager;
import ec.nbdemetra.ws.Workspace;
import ec.nbdemetra.ws.WorkspaceFactory;
import ec.nbdemetra.ws.nodes.WsNode;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(category = "KIX",
          id = "ec.nbdemetra.kix.actions.NewAction")
@ActionRegistration(displayName = "#CTL_NewAction")
@ActionReferences({
    @ActionReference(path = KIXDocumentManager.PATH, position = 1000)
})
@Messages("CTL_NewAction=New")
public class NewAction implements ActionListener {

    private final WsNode context;

    public NewAction(WsNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        IWorkspaceItemManager mgr = WorkspaceFactory.getInstance().getManager(context.lookup());
        if (mgr != null) {
            Workspace ws = context.getWorkspace();
            mgr.create(ws);
        }
    }
}
