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
package de.bundesbank.kix.options;

import static de.bundesbank.kix.options.KIXOptionsPanelController.KIX2_DEFAULT_METHOD;
import static de.bundesbank.kix.options.KIXOptionsPanelController.KIXE_DEFAULT_METHOD;
import org.openide.util.NbPreferences;

final class KIXPanel extends javax.swing.JPanel {

    KIXPanel() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.JPanel defaultModelMainPanel = new javax.swing.JPanel();
        javax.swing.JPanel defaultModelPanel = new javax.swing.JPanel();
        javax.swing.JLabel kix2DefaultMethodLabel = new javax.swing.JLabel();
        kix2DefaultMethod = new javax.swing.JComboBox();
        javax.swing.JLabel kixeDefaultMethodLabel = new javax.swing.JLabel();
        kixeDefaultMethod = new javax.swing.JComboBox();
        javax.swing.Box.Filler defaultModelFiller = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        javax.swing.JScrollPane descriptionPane = new javax.swing.JScrollPane();
        javax.swing.JTextArea descriptionText = new javax.swing.JTextArea();

        defaultModelMainPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createTitledBorder(""), org.openide.util.NbBundle.getMessage(KIXPanel.class, "KIXPanel.defaultModelMainPanel.border.title"), javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP)); // NOI18N
        defaultModelMainPanel.setLayout(new javax.swing.BoxLayout(defaultModelMainPanel, javax.swing.BoxLayout.LINE_AXIS));

        defaultModelPanel.setMaximumSize(new java.awt.Dimension(300, 100));
        defaultModelPanel.setPreferredSize(new java.awt.Dimension(200, 100));
        defaultModelPanel.setLayout(new java.awt.GridLayout(5, 0));

        org.openide.awt.Mnemonics.setLocalizedText(kix2DefaultMethodLabel, org.openide.util.NbBundle.getMessage(KIXPanel.class, "KIXPanel.kix2DefaultMethodLabel.text")); // NOI18N
        defaultModelPanel.add(kix2DefaultMethodLabel);

        kix2DefaultMethod.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));
        defaultModelPanel.add(kix2DefaultMethod);

        org.openide.awt.Mnemonics.setLocalizedText(kixeDefaultMethodLabel, org.openide.util.NbBundle.getMessage(KIXPanel.class, "KIXPanel.kixeDefaultMethodLabel.text")); // NOI18N
        defaultModelPanel.add(kixeDefaultMethodLabel);

        kixeDefaultMethod.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));
        defaultModelPanel.add(kixeDefaultMethod);

        defaultModelMainPanel.add(defaultModelPanel);
        defaultModelMainPanel.add(defaultModelFiller);

        descriptionPane.setBorder(null);

        descriptionText.setEditable(false);
        descriptionText.setBackground(new java.awt.Color(240, 240, 240));
        descriptionText.setColumns(20);
        descriptionText.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        descriptionText.setLineWrap(true);
        descriptionText.setRows(4);
        descriptionText.setText(org.openide.util.NbBundle.getMessage(KIXPanel.class, "KIXPanel.descriptionText.text")); // NOI18N
        descriptionText.setWrapStyleWord(true);
        descriptionPane.setViewportView(descriptionText);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(defaultModelMainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 756, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(descriptionPane)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(defaultModelMainPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(descriptionPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        defaultModelMainPanel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(KIXPanel.class, "KIXPanel.defaultModelMainPanel.AccessibleContext.accessibleName")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    void load() {
        boolean kix2_displayFirstYear = NbPreferences.forModule(KIXOptionsPanelController.class).getBoolean(KIX2_DEFAULT_METHOD, true);
        String kix2_text = kix2_displayFirstYear ? "Yes" : "No";
        kix2DefaultMethod.setSelectedItem(kix2_text);

        boolean kixe_displayFirstYear = NbPreferences.forModule(KIXOptionsPanelController.class).getBoolean(KIXE_DEFAULT_METHOD, false);
        String kixe_text = kixe_displayFirstYear ? "Yes" : "No";
        kixeDefaultMethod.setSelectedItem(kixe_text);
    }

    void store() {
        NbPreferences.forModule(KIXOptionsPanelController.class).putBoolean(KIX2_DEFAULT_METHOD, kix2DefaultMethod.getSelectedItem().toString().equals("Yes"));
        NbPreferences.forModule(KIXOptionsPanelController.class).putBoolean(KIXE_DEFAULT_METHOD, kixeDefaultMethod.getSelectedItem().toString().equals("Yes"));
    }

    boolean valid() {
        return true;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox kix2DefaultMethod;
    private javax.swing.JComboBox kixeDefaultMethod;
    // End of variables declaration//GEN-END:variables
}
