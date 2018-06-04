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

import ec.nbdemetra.ws.*;
import ec.tstoolkit.utilities.Id;
import ec.tstoolkit.utilities.LinearId;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Thomas Witthohn
 */
@ServiceProvider(service = IWorkspaceItemManager.class, position = 8910)
public class KIXDocumentManager extends AbstractWorkspaceItemManager<KIXDocument> {

    public static final LinearId ID = new LinearId("KIX");
    public static final String PATH = "KIX";
    public static final String ITEMPATH = "KIX.item";
    public static final String CONTEXTPATH = "KIX.context";

    @Override
    public WorkspaceItem<KIXDocument> create(Workspace ws) {
        WorkspaceItem<KIXDocument> nvars = super.create(ws);
        return nvars;
    }

    @Override
    protected String getItemPrefix() {
        return "KIX";
    }

    @Override
    public Id getId() {
        return ID;
    }

    @Override
    protected KIXDocument createNewObject() {
        return new KIXDocument();
    }

    @Override
    public IWorkspaceItemManager.ItemType getItemType() {
        return IWorkspaceItemManager.ItemType.Doc;
    }

    @Override
    public String getActionsPath() {
        return PATH;
    }

    @Override
    public IWorkspaceItemManager.Status getStatus() {
        return IWorkspaceItemManager.Status.Experimental;
    }

    @Override
    public Action getPreferredItemAction(final Id child) {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                WorkspaceItem<KIXDocument> doc = (WorkspaceItem<KIXDocument>) WorkspaceFactory.getInstance().getActiveWorkspace().searchDocument(child);
                if (doc != null) {
                    openDocument(doc);
                }
            }
        };
    }

    public void openDocument(WorkspaceItem<KIXDocument> doc) {
        if (doc.isOpen()) {
            doc.getView().requestActive();
        } else {
            KIXTopComponent view = new KIXTopComponent(doc);
            view.open();
            view.requestActive();
        }
    }

    @Override
    public List<WorkspaceItem<KIXDocument>> getDefaultItems() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public Class<KIXDocument> getItemClass() {
        return KIXDocument.class;
    }

    @Override
    public Icon getManagerIcon() {
        return null;
    }

    @Override
    public Icon getItemIcon(WorkspaceItem<KIXDocument> doc) {
        return null;
    }

    public static WorkspaceItem<KIXDocument> systemItem(String name, KIXDocument p) {
        return WorkspaceItem.system(ID, name, p);
    }

    @Override
    public boolean isAutoLoad() {
        return true;
    }
}
