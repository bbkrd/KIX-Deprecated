/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.kix;

import ec.nbdemetra.ui.NbComponents;
import ec.nbdemetra.ui.awt.PopupListener;
import ec.tss.DynamicTsVariable;
import ec.tss.Ts;
import ec.tss.TsCollection;
import ec.tss.TsInformationType;
import ec.tss.datatransfer.TssTransferSupport;
import ec.tstoolkit.timeseries.regression.ITsVariable;
import ec.tstoolkit.timeseries.regression.TsVariable;
import ec.tstoolkit.timeseries.regression.TsVariables;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import ec.ui.chart.TsSparklineCellRenderer;
import ec.ui.list.TsFrequencyTableCellRenderer;
import ec.ui.list.TsPeriodTableCellRenderer;
import ec.util.grid.swing.XTable;
import java.awt.BorderLayout;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.TransferHandler;
import static javax.swing.TransferHandler.COPY;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 *
 * @author Thomas Witthohn
 */
public class JTsKIXList extends JComponent {

    public static final String DELETE_ACTION = "delete";
    public static final String CLEAR_ACTION = "clear";
    public static final String SELECT_ALL_ACTION = "selectAll";
    public static final String RENAME_ACTION = "rename";
    private transient final ListTableSelectionListener listTableListener;
    private final XTable table;
    private transient final TsVariables variables;
    private final ClearAction clear;
    private final DeleteAction remove;

    public JTsKIXList(TsVariables vars) {
        this.variables = vars;
        this.table = buildTable();
        remove = new DeleteAction();
        clear = new ClearAction();
        this.listTableListener = new ListTableSelectionListener();
        table.getSelectionModel().addListSelectionListener(listTableListener);
        table.addMouseListener(new PopupListener.PopupAdapter(buildPopupMenu()));

        setLayout(new BorderLayout());
        add(NbComponents.newJScrollPane(table), BorderLayout.CENTER);
    }

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
            TsCollection col = TssTransferSupport.getDefault().toTsCollection(support.getTransferable());
            if (col != null) {
                col.query(TsInformationType.All);
                if (!col.isEmpty()) {
                    appendTsVariables(col);
                }
                return true;
            }
            return false;
        }
    }

    public void appendTsVariables(TsCollection coll) {
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
            updateMenus();
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
            switch (columnIndex) {
                case 0:
                    return names[rowIndex];
                case 1:
                    return var.getDescription();
                case 2:
                    return var.getDefinitionFrequency();
                case 3: {
                    TsDomain d = var.getDefinitionDomain();
                    if (d != null) {
                        return d.getStart();
                    }
                }
                case 4: {
                    TsDomain d = var.getDefinitionDomain();
                    if (d != null) {
                        return d.getLast();
                    }
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

    protected JPopupMenu buildPopupMenu() {
        JMenu result = new JMenu();
        JMenuItem item;

        item = new JMenuItem(remove);
        item.setText("Remove");
        result.add(item);

        item = new JMenuItem(clear);
        item.setText("Clear");
        result.add(item);

        return result.getPopupMenu();
    }

    private String[] names(int[] pos) {
        String[] n = new String[pos.length];
        KIXTableModel model = (KIXTableModel) table.getModel();
        for (int i = 0; i < pos.length; ++i) {
            n[i] = model.names[pos[i]];
        }
        return n;
    }

    private class DeleteAction extends AbstractAction {

        public static final String DELETE_MESSAGE = "Are you sure you want to delete the selected items?";

        DeleteAction() {
            super(DELETE_ACTION);
            enabled = false;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int[] sel = table.getSelectedRows();
            if (sel.length == 0) {
                return;
            }
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation(DELETE_MESSAGE, NotifyDescriptor.OK_CANCEL_OPTION);
            if (DialogDisplayer.getDefault().notify(nd) != NotifyDescriptor.OK_OPTION) {
                return;
            }

            String[] n = names(sel);
            for (int i = 0; i < n.length; ++i) {
                variables.remove(n[i]);
            }
            ((AbstractTableModel) table.getModel()).fireTableStructureChanged();
            this.firePropertyChange(DELETE_ACTION, null, null);
        }

    }

    private class ClearAction extends AbstractAction {

        public static final String DELETE_MESSAGE = "Are you sure you want to delete the selected items?";

        ClearAction() {
            super(CLEAR_ACTION);
            enabled = false;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation(DELETE_MESSAGE, NotifyDescriptor.OK_CANCEL_OPTION);
            if (DialogDisplayer.getDefault().notify(nd) != NotifyDescriptor.OK_OPTION) {
                return;
            }
            variables.clear();
            ((AbstractTableModel) table.getModel()).fireTableStructureChanged();
            this.firePropertyChange(CLEAR_ACTION, null, null);
        }
    }

    private void updateMenus() {
        clear.setEnabled(!variables.isEmpty());
        int selectedRows = table.getSelectedRowCount();
        remove.setEnabled(selectedRows > 0);
    }

    private class ListTableSelectionListener implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()) {
//                ListSelectionModel model = (ListSelectionModel) e.getSource();
                updateMenus();
            }
        }
    }
}
