/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.nbdemetra.kix.actions;

import ec.nbdemetra.kix.KIXDocument;
import ec.nbdemetra.kix.KIXDocumentManager;
import ec.nbdemetra.ui.nodes.SingleNodeAction;
import ec.nbdemetra.ws.WorkspaceFactory;
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
          id = "ec.nbdemetra.kix.actions.RenameAction")
@ActionRegistration(
        displayName = "#CTL_RenameAction", lazy = false)
@ActionReferences({
    @ActionReference(path = KIXDocumentManager.ITEMPATH, position = 1100)
})
@Messages("CTL_RenameAction=Rename...")
public final class RenameAction extends SingleNodeAction<ItemWsNode> {

    public static final String RENAME_TITLE = "Please enter the new name",
            NAME_MESSAGE = "New name:";

    public RenameAction() {
        super(ItemWsNode.class);
    }

    @Override
    protected void performAction(ItemWsNode context) {
        WorkspaceItem<KIXDocument> cur = (WorkspaceItem<KIXDocument>) context.getItem();
        if (cur != null && !cur.isReadOnly()) {
            // create the input dialog
            String oldName = cur.getDisplayName(), newName;
            KIXName nd = new KIXName(NAME_MESSAGE, RENAME_TITLE, oldName);

            if (DialogDisplayer.getDefault().notify(nd) != NotifyDescriptor.OK_OPTION) {
                return;
            }
            newName = nd.getInputText();
            if (newName.equals(oldName)) {

            } else if (null != WorkspaceFactory.getInstance().getActiveWorkspace().searchDocumentByName(cur.getFamily(), newName)) {
                NotifyDescriptor descriptor = new NotifyDescriptor.Message(newName + " is in use. You should choose another name!");
                DialogDisplayer.getDefault().notify(descriptor);
            } else {
                cur.setDisplayName(newName);
                WorkspaceFactory.Event ev = new WorkspaceFactory.Event(cur.getOwner(), cur.getId(), WorkspaceFactory.Event.ITEMRENAMED);
                WorkspaceFactory.getInstance().notifyEvent(ev);
            }
        }
    }

    @Override
    protected boolean enable(ItemWsNode context) {
        WorkspaceItem<?> cur = context.getItem();
        return cur != null && !cur.isReadOnly();
    }

    @Override
    public String getName() {
        return Bundle.CTL_RenameAction();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }
}

class KIXName extends NotifyDescriptor.InputLine {

    KIXName(String title, String text, String oldName) {
        super(title, text);
        setInputText(oldName);
    }
}
