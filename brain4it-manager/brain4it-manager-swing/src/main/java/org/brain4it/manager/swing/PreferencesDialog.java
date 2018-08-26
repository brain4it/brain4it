/*
 * Brain4it
 * 
 * Copyright (C) 2018, Ajuntament de Sant Feliu de Llobregat
 * 
 * This program is licensed and may be used, modified and redistributed under 
 * the terms of the European Public License (EUPL), either version 1.1 or (at 
 * your option) any later version as soon as they are approved by the European 
 * Commission.
 * 
 * Alternatively, you may redistribute and/or modify this program under the 
 * terms of the GNU Lesser General Public License as published by the Free 
 * Software Foundation; either  version 3 of the License, or (at your option) 
 * any later version. 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *   
 * See the licenses for the specific language governing permissions, limitations 
 * and more details.
 *   
 * You should have received a copy of the EUPL1.1 and the LGPLv3 licenses along 
 * with this program; if not, you may find them at: 
 *   
 *   https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 *   http://www.gnu.org/licenses/ 
 *   and 
 *   https://www.gnu.org/licenses/lgpl.txt
 */

package org.brain4it.manager.swing;

import java.util.Arrays;

/**
 *
 * @author realor
 */
public class PreferencesDialog extends javax.swing.JDialog
{
  private static final String[] LANGUAGES = new String[]{"en", "es", "ca"}; 
  ManagerApp managerApp;
  
  /**
   * Creates new form PreferencesDialog
   */
  public PreferencesDialog(ManagerApp managerApp, boolean modal)
  {
    super(managerApp, modal);
    this.managerApp = managerApp;
    initComponents();
    loadPreferences();
  }

  private void loadPreferences()
  {
    Preferences preferences = ManagerApp.getPreferences();
    
    int scalingFactor = preferences.getScalingFactor();
    if (scalingFactor == 1 || scalingFactor == 2)
    {
      scalingFactorComboBox.setSelectedIndex(scalingFactor - 1);
    }
    
    String language = preferences.getLanguage();
    int index = Arrays.asList(LANGUAGES).indexOf(language);
    languageComboBox.setSelectedIndex(index);
    
    fontSizeSpinner.setValue(preferences.getFontSize());
    
    formatColumnsSpinner.setValue(preferences.getFormatColumns());
    
    indentSizeSpinner.setValue(preferences.getIndentSize());
  }
  
  private void savePreferences()
  {
    Preferences preferences = ManagerApp.getPreferences();

    int index = languageComboBox.getSelectedIndex();
    int scalingFactor = scalingFactorComboBox.getSelectedIndex() + 1;
    preferences.setScalingFactor(scalingFactor);

    preferences.setLanguage(LANGUAGES[index]);
    
    preferences.setFontSize(((Integer)fontSizeSpinner.getValue()));

    preferences.setFormatColumns(((Integer)formatColumnsSpinner.getValue()));

    preferences.setIndentSize(((Integer)indentSizeSpinner.getValue()));
    
    preferences.save();
  }
  
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents()
  {

    southPanel = new javax.swing.JPanel();
    okButton = new javax.swing.JButton();
    cancelButton = new javax.swing.JButton();
    centerPanel = new javax.swing.JPanel();
    userInterfacePanel = new javax.swing.JPanel();
    scalingFactorComboBox = new javax.swing.JComboBox<>();
    scalingFactorLabel = new javax.swing.JLabel();
    languageComboBox = new javax.swing.JComboBox<>();
    languageLabel = new javax.swing.JLabel();
    editorPanel = new javax.swing.JPanel();
    indentSizeSpinner = new javax.swing.JSpinner();
    indentSizeLabel = new javax.swing.JLabel();
    fontSizeSpinner = new javax.swing.JSpinner();
    fontSizeLabel = new javax.swing.JLabel();
    formatColumnsLabel = new javax.swing.JLabel();
    formatColumnsSpinner = new javax.swing.JSpinner();
    restartLabel = new javax.swing.JLabel();

    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/brain4it/manager/swing/resources/Manager"); // NOI18N
    setTitle(bundle.getString("Preferences")); // NOI18N

    okButton.setText(bundle.getString("Accept")); // NOI18N
    okButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        okButtonActionPerformed(evt);
      }
    });
    southPanel.add(okButton);

    cancelButton.setText(bundle.getString("Cancel")); // NOI18N
    cancelButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        cancelButtonActionPerformed(evt);
      }
    });
    southPanel.add(cancelButton);

    getContentPane().add(southPanel, java.awt.BorderLayout.PAGE_END);

    centerPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));

    userInterfacePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("Preferences.userInterface"))); // NOI18N

    scalingFactorComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "x1", "x2" }));

    scalingFactorLabel.setText(bundle.getString("Preferences.scalingFactor")); // NOI18N

    languageComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "English", "Español", "Català" }));

    languageLabel.setText(bundle.getString("Preferences.language")); // NOI18N

    javax.swing.GroupLayout userInterfacePanelLayout = new javax.swing.GroupLayout(userInterfacePanel);
    userInterfacePanel.setLayout(userInterfacePanelLayout);
    userInterfacePanelLayout.setHorizontalGroup(
      userInterfacePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(userInterfacePanelLayout.createSequentialGroup()
        .addContainerGap()
        .addGroup(userInterfacePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
          .addComponent(languageLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(scalingFactorLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 87, Short.MAX_VALUE)
        .addGroup(userInterfacePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
          .addComponent(scalingFactorComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(languageComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
    );
    userInterfacePanelLayout.setVerticalGroup(
      userInterfacePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(userInterfacePanelLayout.createSequentialGroup()
        .addContainerGap()
        .addGroup(userInterfacePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(scalingFactorLabel)
          .addComponent(scalingFactorComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addGroup(userInterfacePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(languageLabel)
          .addComponent(languageComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addContainerGap())
    );

    editorPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("Preferences.editor"))); // NOI18N

    indentSizeSpinner.setModel(new javax.swing.SpinnerNumberModel(2, 0, 20, 1));

    indentSizeLabel.setText(bundle.getString("Preferences.indentSize")); // NOI18N

    fontSizeSpinner.setModel(new javax.swing.SpinnerNumberModel(14, 6, 72, 1));

    fontSizeLabel.setText(bundle.getString("Preferences.fontSize")); // NOI18N

    formatColumnsLabel.setText(bundle.getString("Preferences.formatColumns")); // NOI18N

    formatColumnsSpinner.setModel(new javax.swing.SpinnerNumberModel(80, 10, 200, 1));

    javax.swing.GroupLayout editorPanelLayout = new javax.swing.GroupLayout(editorPanel);
    editorPanel.setLayout(editorPanelLayout);
    editorPanelLayout.setHorizontalGroup(
      editorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(editorPanelLayout.createSequentialGroup()
        .addContainerGap()
        .addGroup(editorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
          .addComponent(fontSizeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(formatColumnsLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(indentSizeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addGroup(editorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(fontSizeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(indentSizeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(formatColumnsSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addContainerGap())
    );
    editorPanelLayout.setVerticalGroup(
      editorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(editorPanelLayout.createSequentialGroup()
        .addContainerGap()
        .addGroup(editorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(fontSizeLabel)
          .addComponent(fontSizeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addGroup(editorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(formatColumnsLabel)
          .addComponent(formatColumnsSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addGroup(editorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(indentSizeLabel)
          .addComponent(indentSizeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addContainerGap())
    );

    restartLabel.setText(bundle.getString("mustRestart")); // NOI18N

    javax.swing.GroupLayout centerPanelLayout = new javax.swing.GroupLayout(centerPanel);
    centerPanel.setLayout(centerPanelLayout);
    centerPanelLayout.setHorizontalGroup(
      centerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(centerPanelLayout.createSequentialGroup()
        .addContainerGap()
        .addGroup(centerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(centerPanelLayout.createSequentialGroup()
            .addGap(6, 6, 6)
            .addComponent(restartLabel)
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
          .addGroup(centerPanelLayout.createSequentialGroup()
            .addGroup(centerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(userInterfacePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
              .addComponent(editorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addContainerGap())))
    );
    centerPanelLayout.setVerticalGroup(
      centerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(centerPanelLayout.createSequentialGroup()
        .addContainerGap()
        .addComponent(userInterfacePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addGap(24, 24, 24)
        .addComponent(editorPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 11, Short.MAX_VALUE)
        .addComponent(restartLabel))
    );

    getContentPane().add(centerPanel, java.awt.BorderLayout.CENTER);

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cancelButtonActionPerformed
  {//GEN-HEADEREND:event_cancelButtonActionPerformed
    setVisible(false);
    dispose();
  }//GEN-LAST:event_cancelButtonActionPerformed

  private void okButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_okButtonActionPerformed
  {//GEN-HEADEREND:event_okButtonActionPerformed
    savePreferences();
    setVisible(false);
    dispose();
  }//GEN-LAST:event_okButtonActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton cancelButton;
  private javax.swing.JPanel centerPanel;
  private javax.swing.JPanel editorPanel;
  private javax.swing.JLabel fontSizeLabel;
  private javax.swing.JSpinner fontSizeSpinner;
  private javax.swing.JLabel formatColumnsLabel;
  private javax.swing.JSpinner formatColumnsSpinner;
  private javax.swing.JLabel indentSizeLabel;
  private javax.swing.JSpinner indentSizeSpinner;
  private javax.swing.JComboBox<String> languageComboBox;
  private javax.swing.JLabel languageLabel;
  private javax.swing.JButton okButton;
  private javax.swing.JLabel restartLabel;
  private javax.swing.JComboBox<String> scalingFactorComboBox;
  private javax.swing.JLabel scalingFactorLabel;
  private javax.swing.JPanel southPanel;
  private javax.swing.JPanel userInterfacePanel;
  // End of variables declaration//GEN-END:variables
}
