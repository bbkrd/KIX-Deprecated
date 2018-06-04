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

import ec.nbdemetra.ui.DemetraUiIcon;
import ec.nbdemetra.ui.NbComponents;
import ec.nbdemetra.ws.WorkspaceFactory;
import ec.nbdemetra.ws.WorkspaceItem;
import ec.nbdemetra.ws.ui.WorkspaceTopComponent;
import ec.tss.TsCollection;
import ec.tstoolkit.timeseries.regression.TsVariables;
import ec.ui.grid.JTsGrid;
import ec.ui.interfaces.ITsCollectionView;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.*;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

/**
 * Top component which displays the KIX UI.
 */
@TopComponent.Description(
        preferredID = "KIXTopComponent",
        persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
@ActionID(category = "Window", id = "ec.nbdemetra.kix.KIXTopComponent")
@ActionReference(path = "Menu/Tools", position = 336)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_KIXAction")
@Messages({
    "CTL_KIXAction=KIX",
    "CTL_KIXTopComponent=KIX Window",
    "HINT_KIXTopComponent=This is a KIX window",
    "KIX_TOOL_TIP_TEXTAREA=<html>For information about the available formulas, please consult the <b>Help (F1)</b></html>",
    "KIX_TOOL_TIP_RUNBUTTON=Start calculation",
    "KIX_LblIndexData=Index Data",
    "KIX_LblWeightData=Weight Data",
    "KIX_LblResults=Results"
})
public final class KIXTopComponent extends WorkspaceTopComponent<KIXDocument> {

    private JTsKIXList indexDataList, weightsDataList;
    private JTsGrid results;
    private JTextArea inputText;
    private JButton runButton;
    private TsVariables indexData, weightsData;
    private Repository repository;

    private static KIXDocumentManager manager() {
        return WorkspaceFactory.getInstance().getManager(KIXDocumentManager.class);
    }

    public KIXTopComponent() {
        this(manager().create(WorkspaceFactory.getInstance().getActiveWorkspace()));
    }

    public KIXTopComponent(WorkspaceItem<KIXDocument> doc) {
        super(doc);
        initComponents();
        setToolTipText(Bundle.HINT_KIXTopComponent());
        setName(getDocument().getDisplayName());
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        JToolBar toolBarRepresentation = NbComponents.newInnerToolbar();
        runButton = new JButton(DemetraUiIcon.COMPILE_16);
        runButton.setToolTipText(Bundle.KIX_TOOL_TIP_RUNBUTTON());

        toolBarRepresentation.add(runButton);
        toolBarRepresentation.setFloatable(false);

        indexData = this.getDocument().getElement().getIndices();
        indexDataList = new JTsKIXList(indexData);

        weightsData = this.getDocument().getElement().getWeights();
        weightsDataList = new JTsKIXList(weightsData);

        inputText = new JTextArea();
        inputText.setLineWrap(true);
        inputText.setWrapStyleWord(true);

        inputText.setToolTipText(Bundle.KIX_TOOL_TIP_TEXTAREA());
        ToolTipManager.sharedInstance().registerComponent(inputText);

        inputText.setDocument(this.getDocument().getElement().getinput());

        JScrollPane scrollpane = new JScrollPane(inputText);
        TextLineNumber tln = new TextLineNumber(inputText);
        scrollpane.setRowHeaderView(tln);

        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.add(scrollpane, BorderLayout.CENTER);

        results = new JTsGrid();
        results.setTsUpdateMode(ITsCollectionView.TsUpdateMode.None);

        JLabel lblIndexData = new JLabel(Bundle.KIX_LblIndexData(), JLabel.CENTER);
        JSplitPane indexDataPane = NbComponents.newJSplitPane(JSplitPane.VERTICAL_SPLIT, lblIndexData, indexDataList);
        indexDataPane.setDividerSize(0);

        JLabel lblWeightData = new JLabel(Bundle.KIX_LblWeightData(), JLabel.CENTER);
        JSplitPane weightDataPane = NbComponents.newJSplitPane(JSplitPane.VERTICAL_SPLIT, lblWeightData, weightsDataList);
        weightDataPane.setDividerSize(0);

        JLabel lblResults = new JLabel(Bundle.KIX_LblResults(), JLabel.CENTER);
        JSplitPane resultPane = NbComponents.newJSplitPane(JSplitPane.VERTICAL_SPLIT, lblResults, results);
        resultPane.setDividerSize(0);

        JSplitPane dataPane = NbComponents.newJSplitPane(JSplitPane.HORIZONTAL_SPLIT, indexDataPane, weightDataPane);
        JSplitPane listPane = NbComponents.newJSplitPane(JSplitPane.HORIZONTAL_SPLIT, dataPane, resultPane);
        JSplitPane mainPane = NbComponents.newJSplitPane(JSplitPane.VERTICAL_SPLIT, textPanel, listPane);

        setLayout(new BorderLayout());
        add(toolBarRepresentation, BorderLayout.NORTH);
        add(mainPane, BorderLayout.CENTER);
        mainPane.setDividerLocation(.5);
        mainPane.setResizeWeight(.2);
        listPane.setDividerLocation(.5);
        listPane.setResizeWeight(.5);
        dataPane.setDividerLocation(.5);
        dataPane.setResizeWeight(.5);

        initActionListener();
        repository = new Repository();
    }

    /**
     * Returns {@link KIXDocumentManager#CONTEXTPATH}
     */
    @Override
    protected String getContextPath() {
        return KIXDocumentManager.CONTEXTPATH;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("kix.possible_commands");
    }

    private void initActionListener() {
        RunAction runAction = new RunAction();
        runButton.addActionListener(runAction);
        String run = "run";
        this.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK), run);
        this.getActionMap().put(run, runAction);
    }

    private final class RunAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (!inputText.getText().trim().isEmpty()) {
                results.getTsCollection().clear();
                TsCollection data = repository.calculate(inputText.getText(), indexData, weightsData);
                if (data != null) {
                    results.getTsCollection().append(data);
                }
            }
        }
    }
}
