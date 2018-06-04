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

import ec.nbdemetra.ui.DemetraUI;
import ec.nbdemetra.ui.NbComponents;
import ec.nbdemetra.ui.awt.ActionMaps;
import ec.nbdemetra.ui.awt.InputMaps;
import ec.nbdemetra.ui.awt.KeyStrokes;
import ec.nbdemetra.ui.tsaction.ITsAction;
import ec.tss.*;
import ec.tss.datatransfer.TssTransferSupport;
import ec.tstoolkit.timeseries.regression.ITsVariable;
import ec.tstoolkit.timeseries.regression.TsVariable;
import ec.tstoolkit.timeseries.regression.TsVariables;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import ec.ui.chart.TsSparklineCellRenderer;
import ec.ui.interfaces.ITsActionAble;
import ec.ui.list.TsFrequencyTableCellRenderer;
import ec.ui.list.TsPeriodTableCellRenderer;
import ec.util.chart.swing.Charts;
import ec.util.grid.swing.XTable;
import ec.util.various.swing.JCommand;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.datatransfer.Transferable;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.annotation.Nonnull;
import javax.swing.*;
import static javax.swing.TransferHandler.COPY;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 *
 * @author Thomas Witthohn
 */
public class JTsKIXList extends JComponent implements ITsActionAble {

    public static final String DELETE_ACTION = "delete";
    public static final String CLEAR_ACTION = "clear";
    public static final String OPEN_ACTION = "open";

    private final XTable table;
    private transient final TsVariables variables;
    private transient ITsAction tsAction;

    public JTsKIXList(TsVariables vars) {
        this.variables = vars;
        this.table = buildTable();

        registerActions();
        registerInputs();
        enableOpenOnDoubleClick();
        enablePopupMenu();

        setLayout(new BorderLayout());
        add(NbComponents.newJScrollPane(table), BorderLayout.CENTER);
    }

    private void registerActions() {
        ActionMap am = getActionMap();
        am.put(OPEN_ACTION, OpenCommand.INSTANCE.toAction(this));
        am.put(DELETE_ACTION, DeleteCommand.INSTANCE.toAction(this));
        am.put(CLEAR_ACTION, ClearCommand.INSTANCE.toAction(this));
        ActionMaps.copyEntries(am, false, table.getActionMap());
    }

    private void registerInputs() {
        InputMap im = getInputMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), OPEN_ACTION);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), DELETE_ACTION);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK), CLEAR_ACTION);
        InputMaps.copyEntries(im, false, table.getInputMap());
    }

    private void enableOpenOnDoubleClick() {
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!Charts.isPopup(e) && Charts.isDoubleClick(e)) {
                    ActionMaps.performAction(getActionMap(), OPEN_ACTION, e);
                }
            }
        });
    }

    private void enablePopupMenu() {
        table.setComponentPopupMenu(buildPopupMenu());
    }

    private JMenu buildOpenWithMenu() {
        JMenu result = new JMenu(OpenWithCommand.INSTANCE.toAction(this));

        for (ITsAction o : DemetraUI.getDefault().getTsActions()) {
            JMenuItem item = new JMenuItem(new OpenWithItemCommand(o).toAction(this));
            item.setName(o.getName());
            item.setText(o.getDisplayName());
            result.add(item);
        }

        return result;
    }

    protected JPopupMenu buildPopupMenu() {
        ActionMap actionMap = getActionMap();

        JMenu result = new JMenu();

        JMenuItem item;

        item = new JMenuItem(actionMap.get(OPEN_ACTION));
        item.setText("Open");
        item.setAccelerator(KeyStrokes.OPEN.get(0));
        item.setFont(item.getFont().deriveFont(Font.BOLD));
        result.add(item);

        item = buildOpenWithMenu();
        item.setText("Open with");
        result.add(item);

        result.addSeparator();

        item = new JMenuItem(actionMap.get(DELETE_ACTION));
        item.setText("Remove");
        item.setAccelerator(KeyStrokes.DELETE.get(0));
        result.add(item);

        item = new JMenuItem(actionMap.get(CLEAR_ACTION));
        item.setText("Clear");
        item.setAccelerator(KeyStrokes.CLEAR.get(0));
        result.add(item);

        return result.getPopupMenu();
    }

    //<editor-fold defaultstate="collapsed" desc="Getters/Setters">
    @Override
    public ITsAction getTsAction() {
        return tsAction;
    }

    @Override
    public void setTsAction(ITsAction tsAction) {
        ITsAction old = this.tsAction;
        this.tsAction = tsAction;
        firePropertyChange(TS_ACTION_PROPERTY, old, this.tsAction);
    }
    //</editor-fold>

    private String[] names(int[] pos) {
        String[] n = new String[pos.length];
        KIXTableModel model = (KIXTableModel) table.getModel();
        for (int i = 0; i < pos.length; ++i) {
            n[i] = model.names[pos[i]];
        }
        return n;
    }

    //<editor-fold defaultstate="collapsed" desc="Commands">
    private static TsVariable getSelectedVariable(JTsKIXList c) {
        if (c.table.getSelectedRowCount() == 1) {
            int idx = c.table.convertRowIndexToModel(c.table.getSelectedRow());
            ITsVariable result = c.variables.get(c.variables.getNames()[idx]);
            if (result instanceof TsVariable) {
                return (TsVariable) result;
            }
        }
        return null;
    }

    private static Ts toTs(TsVariable variable) {
        return TsFactory.instance.createTs(variable.getDescription(TsFrequency.Undefined), null, variable.getTsData());
    }

    private static final class OpenCommand extends JCommand<JTsKIXList> {

        public static final OpenCommand INSTANCE = new OpenCommand();

        @Override
        public void execute(JTsKIXList c) throws Exception {
            TsVariable variable = getSelectedVariable(c);
            ITsAction tsAction = c.tsAction != null ? c.tsAction : DemetraUI.getDefault().getTsAction();
            tsAction.open(toTs(variable));
        }

        @Override
        public boolean isEnabled(JTsKIXList c) {
            return getSelectedVariable(c) != null;
        }

        @Override
        public JCommand.ActionAdapter toAction(JTsKIXList c) {
            return super.toAction(c).withWeakListSelectionListener(c.table.getSelectionModel());
        }
    }

    private static final class OpenWithCommand extends JCommand<JTsKIXList> {

        public static final OpenWithCommand INSTANCE = new OpenWithCommand();

        @Override
        public void execute(JTsKIXList c) throws Exception {
            // do nothing
        }

        @Override
        public boolean isEnabled(JTsKIXList c) {
            return c.table.getSelectedRowCount() == 1;
        }

        @Override
        public JCommand.ActionAdapter toAction(JTsKIXList c) {
            return super.toAction(c).withWeakListSelectionListener(c.table.getSelectionModel());
        }
    }

    private static final class OpenWithItemCommand extends JCommand<JTsKIXList> {

        private final ITsAction tsAction;

        OpenWithItemCommand(@Nonnull ITsAction tsAction) {
            this.tsAction = tsAction;
        }

        @Override
        public void execute(JTsKIXList c) throws Exception {
            tsAction.open(toTs(getSelectedVariable(c)));
        }
    }

    private static final class DeleteCommand extends JCommand<JTsKIXList> {

        public static final DeleteCommand INSTANCE = new DeleteCommand();

        @Override
        public void execute(JTsKIXList c) throws java.lang.Exception {
            int[] sel = c.table.getSelectedRows();
            if (sel.length == 0) {
                return;
            }
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation("Are you sure you want to delete the selected items?", NotifyDescriptor.OK_CANCEL_OPTION);
            if (DialogDisplayer.getDefault().notify(nd) != NotifyDescriptor.OK_OPTION) {
                return;
            }

            String[] n = c.names(sel);
            for (int i = 0; i < n.length; ++i) {
                c.variables.remove(n[i]);
            }
            ((AbstractTableModel) c.table.getModel()).fireTableStructureChanged();
        }

        @Override
        public boolean isEnabled(JTsKIXList c) {
            return c.table.getSelectedRowCount() > 0;
        }

        @Override
        public JCommand.ActionAdapter toAction(JTsKIXList c) {
            return super.toAction(c).withWeakListSelectionListener(c.table.getSelectionModel());
        }
    }

    private static final class ClearCommand extends JCommand<JTsKIXList> {

        public static final ClearCommand INSTANCE = new ClearCommand();

        @Override
        public void execute(JTsKIXList c) throws java.lang.Exception {
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation("Are you sure you want to clear the list?", NotifyDescriptor.OK_CANCEL_OPTION);
            if (DialogDisplayer.getDefault().notify(nd) != NotifyDescriptor.OK_OPTION) {
                return;
            }

            c.variables.clear();
            ((AbstractTableModel) c.table.getModel()).fireTableStructureChanged();
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Table">
    private static final String[] information = new String[]{"Name", "Description", "Frequency", "Start", "End"};

    private XTable buildTable() {
        final XTable result = new XTable();
        result.setNoDataRenderer(new XTable.DefaultNoDataRenderer("Drop data here", "Drop data here"));

        result.setDefaultRenderer(TsData.class, new TsSparklineCellRenderer());
        result.setDefaultRenderer(TsPeriod.class, new TsPeriodTableCellRenderer());
        result.setDefaultRenderer(TsFrequency.class, new TsFrequencyTableCellRenderer());

        result.setModel(new KIXTableModel());
        XTable.setWidthAsPercentages(result, .2, .2, .2, .2, .2);

        result.setAutoCreateRowSorter(true);
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(result.getModel());
        result.setRowSorter(sorter);
        result.setDragEnabled(true);
        result.setTransferHandler(new TsVariableTransferHandler());
        result.setFillsViewportHeight(true);

        return result;
    }

    private class TsVariableTransferHandler extends TransferHandler {

        @Override
        public int getSourceActions(JComponent c) {
            return COPY;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            return null;
        }

        @Override
        public boolean canImport(TransferHandler.TransferSupport support) {
            boolean result = TssTransferSupport.getDefault().canImport(support.getDataFlavors());
            if (result && support.isDrop()) {
                support.setDropAction(COPY);
            }
            return result;
        }

        @Override
        public boolean importData(TransferHandler.TransferSupport support) {
            return TssTransferSupport.getDefault()
                    .toTsCollectionStream(support.getTransferable())
                    .peek(o -> o.load(TsInformationType.All))
                    .filter(o -> !o.isEmpty())
                    .peek(JTsKIXList.this::appendTsVariables)
                    .count() > 0;
        }
    }

    private void appendTsVariables(TsCollection coll) {
        for (Ts s : coll) {
            if (s.getMoniker().isAnonymous()) {
                variables.set(variables.nextName(), new TsVariable(s.getName(), s.getTsData()));
            } else {
                variables.set(variables.nextName(), new DynamicTsVariable(s.getName(), s.getMoniker(), s.getTsData()));
            }
        }
        ((AbstractTableModel) table.getModel()).fireTableStructureChanged();
    }

    private class KIXTableModel extends AbstractTableModel {

        private String[] names;

        @Override
        public void fireTableStructureChanged() {
            names = variables.getNames();
            super.fireTableStructureChanged();
        }

        KIXTableModel() {
            names = variables.getNames();
        }

        @Override
        public int getRowCount() {
            return names.length;
        }

        @Override
        public int getColumnCount() {
            return 5;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                return names[rowIndex];
            }

            ITsVariable var = variables.get(names[rowIndex]);
            TsDomain domain;
            switch (columnIndex) {
                case 0:
                    return names[rowIndex];
                case 1:
                    return var.getDescription(TsFrequency.Undefined);
                case 2:
                    return var.getDefinitionFrequency();
                case 3:
                    domain = var.getDefinitionDomain();
                    if (domain != null) {
                        return domain.getStart();
                    }
                case 4:
                    domain = var.getDefinitionDomain();
                    if (domain != null) {
                        return domain.getLast();
                    }
                default:
                    return null;
            }
        }

        @Override
        public String getColumnName(int column) {
            return information[column];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 3:
                case 4:
                    return TsPeriod.class;
            }
            return super.getColumnClass(columnIndex);
        }
    }

    //</editor-fold>
}
