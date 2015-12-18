/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.kix;

import ec.nbdemetra.ws.AbstractFileItemRepository;
import ec.nbdemetra.ws.IWorkspaceItemRepository;
import ec.nbdemetra.ws.WorkspaceItem;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author Thomas Witthohn
 */
@ServiceProvider(service = IWorkspaceItemRepository.class)
public class KIXFileRepository extends AbstractFileItemRepository<KIXDocument> {

    public static final String REPOSITORY = "KIX";

    @Override
    public boolean load(WorkspaceItem<KIXDocument> item) {
        String sfile = this.fullName(item, REPOSITORY, false);
        if (sfile == null) {
            return false;
        }
        KIXDocument doc = loadLegacy(sfile, XmlKIX.class);
        item.setElement(doc);
        item.resetDirty();
        return doc != null;
    }

    @Override
    public boolean save(WorkspaceItem<KIXDocument> item) {
        String sfile = this.fullName(item, REPOSITORY, true);
        if (sfile == null) {
            return false;
        }
        if (saveLegacy(sfile, item, XmlKIX.class)) {
            item.resetDirty();
            item.getElement().resetDirty();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean delete(WorkspaceItem<KIXDocument> doc) {
        return delete(doc, REPOSITORY);
    }

    @Override
    public Class<KIXDocument> getSupportedType() {
        return KIXDocument.class;
    }
}
