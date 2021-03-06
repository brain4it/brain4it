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

import javax.swing.JOptionPane;
import org.brain4it.io.IOUtils;
import org.brain4it.manager.Server;
import org.brain4it.manager.swing.text.TextUtils;

/**
 *
 * @author realor
 */
public class ServerDialog extends javax.swing.JDialog
{
  private final ManagerApp managerApp;
  private boolean accepted;
  private Server server;
  
  /**
   * Creates new form ServerDialog
   */
  public ServerDialog(ManagerApp managerApp, boolean modal)
  {
    super(managerApp, modal);
    this.managerApp = managerApp;
    initComponents();
    TextUtils.updateInputMap(nameTextField);
    TextUtils.updateInputMap(urlTextField);
    TextUtils.updateInputMap(keyTextField);
  }

  public Server getServer()
  {
    return server;
  }

  public void setServer(Server server)
  {
    this.server = server;
    nameTextField.setText(server.getName());
    urlTextField.setText(server.getUrl());
    urlTextField.setCaretPosition(0);
    keyTextField.setText(server.getAccessKey());
  }
  
  public boolean isAccepted()
  {
    return accepted;
  }
  
  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents()
  {

    nameLabel = new javax.swing.JLabel();
    nameTextField = new javax.swing.JTextField();
    urlLabel = new javax.swing.JLabel();
    urlTextField = new javax.swing.JTextField();
    keyLabel = new javax.swing.JLabel();
    okButton = new javax.swing.JButton();
    cancelButton = new javax.swing.JButton();
    keyTextField = new javax.swing.JTextField();

    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    setTitle(managerApp.getLocalizedMessage("Server.title"));

    java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/brain4it/manager/swing/resources/Manager"); // NOI18N
    nameLabel.setText(bundle.getString("Server.name")); // NOI18N

    urlLabel.setText(bundle.getString("Server.url")); // NOI18N

    keyLabel.setText(bundle.getString("Server.accessKey")); // NOI18N

    okButton.setText(bundle.getString("Accept")); // NOI18N
    okButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        okButtonActionPerformed(evt);
      }
    });

    cancelButton.setText(bundle.getString("Cancel")); // NOI18N
    cancelButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        cancelButtonActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
              .addComponent(keyLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
              .addComponent(nameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
              .addComponent(urlLabel, javax.swing.GroupLayout.Alignment.LEADING))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(nameTextField)
              .addComponent(urlTextField)
              .addComponent(keyTextField, javax.swing.GroupLayout.Alignment.TRAILING)))
          .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
            .addGap(0, 286, Short.MAX_VALUE)
            .addComponent(okButton)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(cancelButton)))
        .addContainerGap())
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addGap(17, 17, 17)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(nameLabel)
          .addComponent(nameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(urlLabel)
          .addComponent(urlTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(keyLabel)
          .addComponent(keyTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 38, Short.MAX_VALUE)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(okButton)
          .addComponent(cancelButton))
        .addContainerGap())
    );

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void okButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_okButtonActionPerformed
  {//GEN-HEADEREND:event_okButtonActionPerformed
    String serverName = nameTextField.getText().trim();
    if (serverName.length() == 0)
    {
      JOptionPane.showMessageDialog(null, 
        managerApp.getLocalizedMessage("ServerNameMandatory"), 
        "Warning", JOptionPane.WARNING_MESSAGE);
      return;
    }
    String serverUrl = urlTextField.getText().trim();
    if (serverUrl.length() == 0)
    {
      JOptionPane.showMessageDialog(null, 
        managerApp.getLocalizedMessage("ServerURLMandatory"), 
        "Warning", JOptionPane.WARNING_MESSAGE);
      return;
    }
    if (!IOUtils.isValidURL(serverUrl, true))
    {
      JOptionPane.showMessageDialog(null, 
        managerApp.getLocalizedMessage("InvalidURL"), 
        "Warning", JOptionPane.WARNING_MESSAGE);
      return;      
    }
    server.setName(serverName);
    server.setUrl(serverUrl);
    server.setAccessKey(keyTextField.getText());    
    accepted = true;
    this.setVisible(false);
    this.dispose();
  }//GEN-LAST:event_okButtonActionPerformed

  private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cancelButtonActionPerformed
  {//GEN-HEADEREND:event_cancelButtonActionPerformed
    this.setVisible(false);
    this.dispose();
  }//GEN-LAST:event_cancelButtonActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton cancelButton;
  private javax.swing.JLabel keyLabel;
  private javax.swing.JTextField keyTextField;
  private javax.swing.JLabel nameLabel;
  private javax.swing.JTextField nameTextField;
  private javax.swing.JButton okButton;
  private javax.swing.JLabel urlLabel;
  private javax.swing.JTextField urlTextField;
  // End of variables declaration//GEN-END:variables
}
