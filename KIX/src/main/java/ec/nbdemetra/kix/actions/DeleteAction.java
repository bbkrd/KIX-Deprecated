/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.nbdemetra.kix.actions;

import ec.nbdemetra.kix.KIXDocument;
import ec.nbdemetra.kix.KIXDocumentManager;
import ec.nbdemetra.ui.nodes.SingleNodeAction;
import ec.nbdemetra.ws.WorkspaceItem;
import ec.nbdemetra.ws.nodes.ItemWsNode;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle.Messages;

@ActionID(category = "KIX",
          id = "ec.nbdemetra.kix.actions.DeleteAction")
@ActionRegistration(
        displayName = "#CTL_DeleteAction", lazy = false)
@ActionReferences({
    @ActionReference(path = KIXDocumentManager.ITEMPATH, position = 1100)
})
@Messages("CTL_DeleteAction=Delete")
public final class DeleteAction extends SingleNodeAction<ItemWsNode> {

    public static final String DELETE_MESSAGE = "Are you sure you want to delete this item?";

    public DeleteAction() {
        super(ItemWsNode.class);
    }

    @Override
    protected void performAction(ItemWsNode context) {
        WorkspaceItem<?> cur = context.getItem();
        if (cur != null && !cur.isReadOnly()) {
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation(DELETE_MESSAGE, NotifyDescriptor.OK_CANCEL_OPTION);
            if (DialogDisplayer.getDefault().notify(nd) != NotifyDescriptor.OK_OPTION) {
                return;
            }
            context.getWorkspace().remove(cur);
        }
    }

    @Override
    protected boolean enable(ItemWsNode context) {
        WorkspaceItem<?> cur = context.getItem();
        return cur != null && !cur.isReadOnly() && cur.getElement() instanceof KIXDocument;
    }

    @Override
    public String getName() {
        return Bundle.CTL_DeleteAction();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }
}
