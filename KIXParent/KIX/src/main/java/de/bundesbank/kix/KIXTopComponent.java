/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.ToolTipManager;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
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
    "HINT_KIXTopComponent=This is a KIX window"
})
public final class KIXTopComponent extends WorkspaceTopComponent<KIXDocument> {

    private static final String TOOL_TIP_TEXTAREA = "<html>"
            + "Enter formulas with the following syntax:"
            + "<br>"
            + "[Name=]KIX, i1, [w1,] +/-, i2, [w2,] …,refyr "
            + "<br>"
            + "[Name=]WBG, iContr, [wContr,] +/-, iTotal, [wTotal,] #ofLags"
            + "<br>"
            + "[Name=]KIXE, i1, [w1,] +/-, i2, [w2,] …,refyr "
            + "<br>"
            + "[Name=]WBGE, iContr, [wContr,] iTotal, [wTotal,] #ofLags"
            + "<br>"
            + "For more information, please consult the Help (F1)"
            + "</html>";

    private JSplitPane mainPane, listPane, dataPane, indexDataPane, weightDataPane, resultPane;
    private JPanel textPanel;
    private JTsKIXList indexDataList, weightsDataList;
    private JTsGrid results;
    private JTextArea inputText;
    private JToolBar toolBarRepresentation;
    private JButton runButton;
    private JLabel lblIndexData, lblWeightData, lblResults;
    private TsVariables indexData, weightsData;
    private IKIXModel _KIX;
    private JScrollPane scrollpane;

    private static KIXDocumentManager manager() {
        return WorkspaceFactory.getInstance().getManager(KIXDocumentManager.class);
    }

    public KIXTopComponent() {
        super(manager().create(WorkspaceFactory.getInstance().getActiveWorkspace()));
        initDocument();
    }

    public KIXTopComponent(WorkspaceItem<KIXDocument> doc) {
        super(doc);
        initDocument();
    }

    private void initDocument() {
        initComponents();
        setToolTipText(Bundle.HINT_KIXTopComponent());
        setName(getDocument().getDisplayName());
    }

    private void initComponents() {
        setLayout(new java.awt.BorderLayout());

        toolBarRepresentation = NbComponents.newInnerToolbar();
        runButton = new JButton(DemetraUiIcon.COMPILE_16);
        runButton.setToolTipText("Start calculation");

        toolBarRepresentation.add(runButton);
        toolBarRepresentation.setFloatable(false);

        indexData = this.getDocument().getElement().getIndices();
        indexDataList = new JTsKIXList(indexData);
        indexDataList.setToolTipText("Drop your index times series into this list");

        weightsData = this.getDocument().getElement().getWeights();
        weightsDataList = new JTsKIXList(weightsData);
        weightsDataList.setToolTipText("Drop your weight times series into this list");

        inputText = new JTextArea();
        inputText.setLineWrap(true);
        inputText.setWrapStyleWord(true);

        inputText.setToolTipText(TOOL_TIP_TEXTAREA);
        ToolTipManager.sharedInstance().setDismissDelay(20000);
        ToolTipManager.sharedInstance().setInitialDelay(500);
        ToolTipManager.sharedInstance().setReshowDelay(500);
        ToolTipManager.sharedInstance().registerComponent(inputText);

        inputText.setDocument(this.getDocument().getElement().getinput());

        scrollpane = new JScrollPane(inputText);
        textPanel = new JPanel(new BorderLayout());
        textPanel.add(scrollpane, BorderLayout.CENTER);

        results = new JTsGrid();
        results.setTsUpdateMode(ITsCollectionView.TsUpdateMode.None);

        lblIndexData = new JLabel("Index Data", JLabel.CENTER);
        indexDataPane = NbComponents.newJSplitPane(JSplitPane.VERTICAL_SPLIT, lblIndexData, indexDataList);
        indexDataPane.setDividerSize(0);

        lblWeightData = new JLabel("Weight Data", JLabel.CENTER);
        weightDataPane = NbComponents.newJSplitPane(JSplitPane.VERTICAL_SPLIT, lblWeightData, weightsDataList);
        weightDataPane.setDividerSize(0);

        lblResults = new JLabel("Results", JLabel.CENTER);
        resultPane = NbComponents.newJSplitPane(JSplitPane.VERTICAL_SPLIT, lblResults, results);
        resultPane.setDividerSize(0);

        dataPane = NbComponents.newJSplitPane(JSplitPane.HORIZONTAL_SPLIT, indexDataPane, weightDataPane);
        listPane = NbComponents.newJSplitPane(JSplitPane.HORIZONTAL_SPLIT, dataPane, resultPane);
        mainPane = NbComponents.newJSplitPane(JSplitPane.VERTICAL_SPLIT, textPanel, listPane);

        setLayout(new BorderLayout());
        add(toolBarRepresentation, BorderLayout.NORTH);
        add(mainPane, BorderLayout.CENTER);
        mainPane.setDividerLocation(.5);
        mainPane.setResizeWeight(.2);
        listPane.setDividerLocation(.5);
        listPane.setResizeWeight(.5);
        dataPane.setDividerLocation(.5);
        dataPane.setResizeWeight(.5);

        initbutton();
        _KIX = new KIXModel();
    }

    /**
     * Returns {@link KIXDocumentManager#CONTEXTPATH}
     */
    @Override
    protected String getContextPath() {
        return KIXDocumentManager.CONTEXTPATH;
    }

    private void initbutton() {
        runButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!inputText.getText().trim().isEmpty()) {
                    results.getTsCollection().clear();
                    inputText.setText(addMissingWeights(inputText.getText()));
                    TsCollection data = _KIX.parser(inputText.getText(), indexData, weightsData);
                    if (data != null) {
                        results.getTsCollection().append(data);
                    }
                }
            }
        });
    }

    private String addMissingWeights(String input) {
        input = input.replaceAll(" ", "");
//        input = input.replaceAll("i((?:\\d)+)(?=(?: )*,(?: )*(?:[\\+\\-]){1}(?: )*,)", "i$1,w$1");
//        input = input.replaceAll("i((?:\\d)+)(?=(?: )*,(?: )*i((?:\\d)+)(?: )*,)", "i$1,w$1");
//        input = input.replaceAll("i((?:\\d)+)(?=(?: )*,(?: )*((?:\\d)+))", "i$1,w$1");
        input = input.replaceAll("i((?:\\d)+)(?=,(((([\\+\\-]){1}|i(\\d)+),)|(\\d)+))", "i$1,w$1");

        return input;
    }

}
