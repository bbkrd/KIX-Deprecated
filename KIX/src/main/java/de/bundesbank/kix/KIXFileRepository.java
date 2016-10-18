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
