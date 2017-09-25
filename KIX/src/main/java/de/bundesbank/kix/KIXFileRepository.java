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

import ec.demetra.workspace.WorkspaceFamily;
import static ec.demetra.workspace.WorkspaceFamily.parse;
import ec.demetra.workspace.file.FileFormat;
import ec.demetra.workspace.file.spi.FamilyHandler;
import ec.demetra.workspace.file.util.InformationSetSupport;
import ec.nbdemetra.ws.AbstractFileItemRepository;
import ec.nbdemetra.ws.IWorkspaceItemRepository;
import ec.nbdemetra.ws.WorkspaceItem;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author Thomas Witthohn
 */
@ServiceProvider(service = IWorkspaceItemRepository.class)
public class KIXFileRepository extends AbstractFileItemRepository<KIXDocument> {

    public static final WorkspaceFamily KIX = parse("KIX");

    @Override
    public boolean load(WorkspaceItem<KIXDocument> item) {
        return loadFile(item, (KIXDocument o) -> {
                    item.setElement(o);
                    item.resetDirty();
                });
    }

    @Override
    public boolean save(WorkspaceItem<KIXDocument> item) {
        return storeFile(item, item.getElement(), () -> {
                     item.resetDirty();
                     item.getElement().resetDirty();
                 });
    }

    @Override
    public boolean delete(WorkspaceItem<KIXDocument> doc) {
        return deleteFile(doc);
    }

    @Override
    public Class<KIXDocument> getSupportedType() {
        return KIXDocument.class;
    }

    @ServiceProvider(service = FamilyHandler.class)
    public static final class KIXDoc implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = InformationSetSupport.of(KIXDocument::new, "KIX").asHandler(KIX, FileFormat.GENERIC);

    }
}
