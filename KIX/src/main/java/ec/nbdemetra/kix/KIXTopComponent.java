/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.nbdemetra.kix;

import ec.nbdemetra.ui.DemetraUiIcon;
import ec.nbdemetra.ui.NbComponents;
import ec.nbdemetra.ws.WorkspaceFactory;
import ec.nbdemetra.ws.WorkspaceItem;
import ec.nbdemetra.ws.ui.WorkspaceTopComponent;
import ec.tss.TsCollection;
import ec.tstoolkit.timeseries.regression.TsVariables;
import ec.ui.grid.JTsGrid;
import ec.ui.interfaces.ITsCollectionView;
import ec.ui.list.JTsVariableList;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.ToolTipManager;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.ImageUtilities;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//ec.nbdemetra.kix//KIX//EN",
        autostore = false)
@TopComponent.Description(
        preferredID = "KIXTopComponent",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS)
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
            + "KIX, i1, w1, +/-, i2, w2, …,refyr "
            + "<br>"
            + "WBG, iContr, wContr, +/-, iTotal, wTotal, #ofLags"
            + "<br>"
            + "For more information, please consult the Help (F1)"
            + "</html>";

    private JSplitPane mainPane,listPane,dataPane;
    private JPanel textPanel;
    private JTsVariableList indexDataList,weightsDataList;
    private JTsGrid kixGrid;
    private JTextArea inputText;
    private JToolBar toolBarRepresentation;
    private JButton runButton;
    private TsVariables indexData,weightsData;
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
        runButton.setDisabledIcon(ImageUtilities.createDisabledIcon(runButton.getIcon()));
        runButton.setToolTipText("Run KIX");

        toolBarRepresentation.add(runButton);
        toolBarRepresentation.setFloatable(false);
        toolBarRepresentation.addSeparator();
        toolBarRepresentation.add(Box.createRigidArea(new Dimension(5, 0)));
        toolBarRepresentation.add(Box.createHorizontalGlue());
        toolBarRepresentation.addSeparator();

        indexData = this.getDocument().getElement().getIndices();
        indexDataList = new JTsVariableList(indexData);

        weightsData = this.getDocument().getElement().getWeights();
        weightsDataList = new JTsVariableList(weightsData);
        
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

        kixGrid = new JTsGrid();
        kixGrid.setTsUpdateMode(ITsCollectionView.TsUpdateMode.None);
        dataPane = NbComponents.newJSplitPane(JSplitPane.HORIZONTAL_SPLIT, indexDataList, weightsDataList);
        listPane = NbComponents.newJSplitPane(JSplitPane.HORIZONTAL_SPLIT, dataPane, kixGrid);
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

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    @Override
    protected String getContextPath() {
        return KIXDocumentManager.CONTEXTPATH;
    }

    private void initbutton() {
        runButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                run();
                
            }

        }
        );
    }

    private void run() {
        kixGrid.getTsCollection().clear();
        addMissingWeights();
        TsCollection data = _KIX.parser(inputText.getText(), indexData,weightsData);
        if (data != null) {
            kixGrid.getTsCollection().append(data);
        }
        
    }
    
    private void addMissingWeights() {
        String input = inputText.getText();
        input = input.replaceAll(" ", "");
        input = input.replaceAll("i((?:\\d)+)(?=(?: )*,(?: )*([\\+\\-]){1}(?: )*,)", "i$1,w$1");
        input = input.replaceAll("i((?:\\d)+)(?=(?: )*,(?: )*((?:\\d)+))", "i$1,w$1");

        inputText.setText(input);
    }
    
}
