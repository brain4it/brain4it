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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;
import org.brain4it.lang.BList;
import org.brain4it.manager.swing.text.TextUtils;

/**
 *
 * @author realor
 */
public class ModuleDialog extends javax.swing.JDialog
{
  private ManagerApp managerApp;
  private boolean accepted;

  /**
   * Creates new form ServerDialog
   * @param parent
   * @param modal
   */
  public ModuleDialog(ManagerApp managerApp, boolean modal)
  {
    super(managerApp, modal);
    this.managerApp = managerApp;
    initComponents();
    initIcons();
    TextUtils.updateInputMap(nameTextField);
    TextUtils.updateInputMap(keyTextField);
    TextUtils.updateInputMap(descriptionTextPane);
    int size = descriptionTextPane.getFont().getSize();
    scrollPane.setPreferredSize(new Dimension(20 * size, 5 * size));
    pack();
  }

  public void setModuleName(String name)
  {
    nameTextField.setText(name);
  }
  
  public String getModuleName()
  {
    return nameTextField.getText();
  }
    
  public void setIndentityKey(String key)
  {
    keyTextField.setText(key);
  }
  
  public String getAccessKey()
  {
    return keyTextField.getText();
  }
  
  public void setMetadata(BList metadata)
  {
    if (metadata != null)
    {
      Object value;
      value = metadata.get("icon");
      if (value instanceof String)
      {
        iconComboBox.setSelectedItem((String)value);
      }
      value = metadata.get("description");
      if (value instanceof String)
      {
        descriptionTextPane.setText((String)value);
      }
    }
  }
  
  public BList getMetadata()
  {
    BList metadata = new BList();
    String iconName = (String)iconComboBox.getSelectedItem();
    if (iconName != null && iconName.length() > 0)
    {
      metadata.put("icon", iconName);
    }
    String description = (String)descriptionTextPane.getText();
    if (description != null && description.length() > 0)
    {
      metadata.put("description", description);
    }
    return metadata;
  }
    
  public void setNameEditable(boolean enabled)
  {
    nameTextField.setEnabled(enabled);
  }

  public boolean isNameEditable()
  {
    return nameTextField.isEnabled();
  }
  
  public boolean isAccepted()
  {
    return accepted;
  }
  
  private void initIcons()
  {
    URI uri;
    try
    {
      String iconsFolder = "/org/brain4it/manager/swing/resources/icons/modules";
      URL location = getClass().getResource(iconsFolder);
      uri = location.toURI();
      Path path;
      if (uri.getScheme().equals("jar"))
      {
        FileSystem fileSystem;
        try
        {
          fileSystem = FileSystems.getFileSystem(uri);
        }
        catch (FileSystemNotFoundException ex)
        {
          fileSystem = FileSystems.newFileSystem(uri, 
          Collections.<String, Object>emptyMap());
        }
        path = fileSystem.getPath(iconsFolder);
      }
      else
      {
        path = Paths.get(uri);
      }
      List<String> iconNames = new ArrayList<String>();
      iconNames.add("");
      Stream<Path> walk = Files.walk(path, 1);
      Iterator<Path> iter = walk.iterator();
      while (iter.hasNext())
      {
        path = iter.next();
        String filename = path.getFileName().toString();
        if (!filename.contains("@") && filename.endsWith(".png"))
        {
          iconNames.add(filename.substring(0, filename.length() - 4));
        }
      }
      walk.close();
      Collections.sort(iconNames);
      DefaultComboBoxModel model = new DefaultComboBoxModel(
        iconNames.toArray(new String[iconNames.size()]));
      iconComboBox.setModel(model);
      iconComboBox.setRenderer(new IconRenderer());
    }
    catch (Exception ex)
    {
      // ignore
    }
  }
  
  public class IconRenderer extends JLabel implements ListCellRenderer
  {
    private static final int BORDER = 2;
    public IconRenderer()
    {
      setBorder(new EmptyBorder(BORDER, BORDER, BORDER, BORDER));
    }
    
    @Override
    public Component getListCellRendererComponent(JList list, 
       Object value, int index, boolean selected, boolean hasFocus)
    {
      String iconName = (String)value;
      if (selected)
      {
        setBackground(ManagerApp.BASE_COLOR);
        setForeground(Color.WHITE);
      }
      else
      {
        setBackground(Color.WHITE);
        setForeground(Color.BLACK);
      }
      this.setIcon(IconCache.getIcon("modules/" + iconName, "module"));
      this.setText(iconName.length() == 0 ? "module" : iconName);
      return this;
    }
    
    @Override
    public Dimension getPreferredSize()
    {
      ImageIcon icon = IconCache.getIcon("modules/weather");
      Dimension size = super.getPreferredSize();
      return new Dimension(size.width + 2* BORDER, 
        icon.getIconHeight() + 2 * BORDER);
    }
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
    keyLabel = new javax.swing.JLabel();
    okButton = new javax.swing.JButton();
    cancelButton = new javax.swing.JButton();
    keyTextField = new javax.swing.JTextField();
    descriptionLabel = new javax.swing.JLabel();
    iconLabel = new javax.swing.JLabel();
    iconComboBox = new javax.swing.JComboBox<>();
    scrollPane = new javax.swing.JScrollPane();
    descriptionTextPane = new javax.swing.JTextPane();

    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    setTitle(managerApp.getLocalizedMessage("Module.title"));

    java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/brain4it/manager/swing/resources/Manager"); // NOI18N
    nameLabel.setText(bundle.getString("Module.name")); // NOI18N

    keyLabel.setText(bundle.getString("Module.accessKey")); // NOI18N

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

    descriptionLabel.setText(bundle.getString("Module.description")); // NOI18N

    iconLabel.setText(bundle.getString("Module.icon")); // NOI18N

    scrollPane.setViewportView(descriptionTextPane);

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(layout.createSequentialGroup()
            .addGap(0, 286, Short.MAX_VALUE)
            .addComponent(okButton)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(cancelButton))
          .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                .addComponent(descriptionLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(keyLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(nameLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
              .addComponent(iconLabel))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(keyTextField)
              .addComponent(nameTextField)
              .addGroup(layout.createSequentialGroup()
                .addComponent(iconComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
              .addComponent(scrollPane, javax.swing.GroupLayout.Alignment.TRAILING))))
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
          .addComponent(keyLabel)
          .addComponent(keyTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(iconLabel)
          .addComponent(iconComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(layout.createSequentialGroup()
            .addComponent(descriptionLabel)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
          .addGroup(layout.createSequentialGroup()
            .addComponent(scrollPane)
            .addGap(7, 7, 7)))
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(okButton)
          .addComponent(cancelButton))
        .addContainerGap())
    );

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void okButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_okButtonActionPerformed
  {//GEN-HEADEREND:event_okButtonActionPerformed
    String moduleName = getModuleName();
    if (moduleName == null || moduleName.trim().length() == 0)
    {
      JOptionPane.showMessageDialog(null, "Module name is mandatoty", 
        "Warning", JOptionPane.WARNING_MESSAGE);
      return;
    }
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
  private javax.swing.JLabel descriptionLabel;
  private javax.swing.JTextPane descriptionTextPane;
  private javax.swing.JComboBox<String> iconComboBox;
  private javax.swing.JLabel iconLabel;
  private javax.swing.JLabel keyLabel;
  private javax.swing.JTextField keyTextField;
  private javax.swing.JLabel nameLabel;
  private javax.swing.JTextField nameTextField;
  private javax.swing.JButton okButton;
  private javax.swing.JScrollPane scrollPane;
  // End of variables declaration//GEN-END:variables
}