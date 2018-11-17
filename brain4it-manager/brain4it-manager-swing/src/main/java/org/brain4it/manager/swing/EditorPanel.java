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

import org.brain4it.manager.swing.text.TextUtils;
import java.util.Set;
import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.InputMap;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import org.brain4it.client.RestClient;
import org.brain4it.client.RestClient.Callback;
import org.brain4it.io.Formatter;
import org.brain4it.io.IOConstants;
import org.brain4it.manager.Module;
import org.brain4it.manager.ModuleEvent;
import org.brain4it.manager.TextCompleter;
import org.brain4it.manager.swing.text.AutoCompleter;
import org.brain4it.manager.swing.text.AutoIndenter;
import org.brain4it.manager.swing.text.SymbolMatcher;
import org.brain4it.manager.swing.text.ColoredEditorKit;
import org.brain4it.manager.swing.text.LineTracker;
import static org.brain4it.server.ServerConstants.*;

/**
 *
 * @author realor
 */
public class EditorPanel extends ModulePanel
{
  private UndoManager undoManager;
  private SymbolMatcher matcher;
  private AutoIndenter indenter;
  private AutoCompleter completer;
  private TextCompleter textCompleter;
  private LineTracker lineTracker;
  private boolean updateFunctions = true;
  private ColoredEditorKit editorKit;
  private CompoundEdit compoundEdit;
  private Formatter formatter = new Formatter();

  /**
   * Creates new form Editor
   */
  public EditorPanel(ManagerApp managerApp, Module module)
  {
    super(managerApp, module);
    initComponents();
    initEditor();
  }

  @Override
  public String getPanelType()
  {
    return managerApp.getLocalizedMessage("Editor");
  }

  @Override
  public String getPanelName()
  {
    String panelName = super.getPanelName();
    String path = getCurrentPath();
    if (path != null)
    {
      if (path.startsWith("/")) panelName += path;
      else panelName += "/" + path;
    }
    return panelName;
  }

  public JTextPane getInputTextPane()
  {
    return inputTextPane;
  }

  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents()
  {

    northPanel = new javax.swing.JPanel();
    pathLabel = new javax.swing.JLabel();
    pathComboBox = new javax.swing.JComboBox<>();
    pathToolBar = new javax.swing.JToolBar();
    loadButton = new javax.swing.JButton();
    saveButton = new javax.swing.JButton();
    editToolBar = new javax.swing.JToolBar();
    clearButton = new javax.swing.JButton();
    undoButton = new javax.swing.JButton();
    redoButton = new javax.swing.JButton();
    formatButton = new javax.swing.JButton();
    findButton = new javax.swing.JButton();
    inputScrollPane = new javax.swing.JScrollPane();
    inputTextPane = new javax.swing.JTextPane();

    setLayout(new java.awt.BorderLayout());

    northPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2));
    northPanel.setLayout(new java.awt.BorderLayout());

    java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/brain4it/manager/swing/resources/Manager"); // NOI18N
    pathLabel.setText(bundle.getString("Editor.path")); // NOI18N
    northPanel.add(pathLabel, java.awt.BorderLayout.WEST);

    pathComboBox.setEditable(true);
    pathComboBox.setModel(new javax.swing.DefaultComboBoxModel<String>()
    );
    northPanel.add(pathComboBox, java.awt.BorderLayout.CENTER);

    pathToolBar.setFloatable(false);
    pathToolBar.setRollover(true);
    pathToolBar.setBorderPainted(false);

    loadButton.setIcon(IconCache.getIcon("load"));
    loadButton.setText(bundle.getString("Editor.load")); // NOI18N
    loadButton.setFocusable(false);
    loadButton.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
    loadButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        loadButtonActionPerformed(evt);
      }
    });
    pathToolBar.add(loadButton);

    saveButton.setIcon(IconCache.getIcon("save"));
    saveButton.setText(bundle.getString("Editor.save")); // NOI18N
    saveButton.setFocusable(false);
    saveButton.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
    saveButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        saveButtonActionPerformed(evt);
      }
    });
    pathToolBar.add(saveButton);

    northPanel.add(pathToolBar, java.awt.BorderLayout.LINE_END);

    editToolBar.setFloatable(false);
    editToolBar.setRollover(true);

    clearButton.setIcon(IconCache.getIcon("clear"));
    clearButton.setText(bundle.getString("Editor.clear")); // NOI18N
    clearButton.setFocusable(false);
    clearButton.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
    clearButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        clearButtonActionPerformed(evt);
      }
    });
    editToolBar.add(clearButton);

    undoButton.setIcon(IconCache.getIcon("undo"));
    undoButton.setText(bundle.getString("Editor.undo")); // NOI18N
    undoButton.setFocusable(false);
    undoButton.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
    undoButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        undoButtonActionPerformed(evt);
      }
    });
    editToolBar.add(undoButton);

    redoButton.setIcon(IconCache.getIcon("redo"));
    redoButton.setText(bundle.getString("Editor.redo")); // NOI18N
    redoButton.setFocusable(false);
    redoButton.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
    redoButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        redoButtonActionPerformed(evt);
      }
    });
    editToolBar.add(redoButton);

    formatButton.setIcon(IconCache.getIcon("format"));
    formatButton.setText(bundle.getString("Editor.format")); // NOI18N
    formatButton.setFocusable(false);
    formatButton.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
    formatButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        formatButtonActionPerformed(evt);
      }
    });
    editToolBar.add(formatButton);

    findButton.setIcon(IconCache.getIcon("find"));
    findButton.setText(bundle.getString("Editor.find")); // NOI18N
    findButton.setFocusable(false);
    findButton.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
    findButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        findButtonActionPerformed(evt);
      }
    });
    editToolBar.add(findButton);

    northPanel.add(editToolBar, java.awt.BorderLayout.SOUTH);

    add(northPanel, java.awt.BorderLayout.PAGE_START);

    inputTextPane.setFont(new java.awt.Font("Monospaced", 0, 14)); // NOI18N
    inputTextPane.setSelectionColor(new java.awt.Color(204, 204, 204));
    inputScrollPane.setViewportView(inputTextPane);

    add(inputScrollPane, java.awt.BorderLayout.CENTER);
  }// </editor-fold>//GEN-END:initComponents

  private void saveButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_saveButtonActionPerformed
  {//GEN-HEADEREND:event_saveButtonActionPerformed
    saveData();
  }//GEN-LAST:event_saveButtonActionPerformed

  private void loadButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_loadButtonActionPerformed
  {//GEN-HEADEREND:event_loadButtonActionPerformed
    loadData();
  }//GEN-LAST:event_loadButtonActionPerformed

  public void loadPath(String path)
  {
    pathComboBox.getEditor().setItem(path);
    loadData();
  }

  public String getCurrentPath()
  {
    String path = (String)pathComboBox.getEditor().getItem();
    if (path == null) return null;
    path = path.trim();
    if (path.length() == 0) return null;
    return path;
  }

  @Override
  public void setModified(boolean modified)
  {
    saveButton.setEnabled(modified);
    super.setModified(modified);
  }

  protected void loadData()
  {
    final String path = getCurrentPath();
    if (path == null) return;

    if (isModified())
    {
      int result = JOptionPane.showConfirmDialog(managerApp,
        managerApp.getLocalizedMessage("Editor.discardChanges"), null,
        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
      if (result == JOptionPane.NO_OPTION) return;
    }

    RestClient restClient = getRestClient();
    restClient.get(module.getName(), path, new Callback()
    {
      @Override
      public void onSuccess(RestClient client, final String resultString)
      {
        onReadSuccess(path, resultString);
        if (updateFunctions)
        {
          findFunctions();
        }
      }

      @Override
      public void onError(RestClient client, Exception ex)
      {
        managerApp.showError("Editor", ex);
      }
    });
  }

  protected void onReadSuccess(final String path, final String data)
  {
    SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        try
        {
          replaceText(formatter.format(data));
          addPathToComboBox(path);
          undoManager.discardAllEdits();
          updateUndoRedoButtons();
          setModified(false);
          inputTextPane.setCaretPosition(0);
          managerApp.updateTab(EditorPanel.this);
        }
        catch (Exception ex)
        {
          managerApp.showError("Editor", ex);
        }
      }
    });
  }

  protected void saveData()
  {
    final String path = getCurrentPath();
    if (path == null)
    {
      getManagerApp().showError("Editor", "Enter path before saving.");
      return;
    }

    String data = inputTextPane.getText();

    RestClient restClient = getRestClient();
    restClient.put(module.getName(), path, data, new Callback()
    {
      @Override
      public void onSuccess(RestClient client, String resultString)
      {
        onWriteSuccess(path);
      }

      @Override
      public void onError(RestClient client, Exception ex)
      {
        managerApp.showError("Editor.save", ex);
      }
    });
  }

  protected void onWriteSuccess(final String path)
  {
    SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        addPathToComboBox(path);
        managerApp.updateTab(EditorPanel.this);
        setModified(false);
      }
    });
  }

  private void clearButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_clearButtonActionPerformed
  {//GEN-HEADEREND:event_clearButtonActionPerformed
    inputTextPane.setText("");
  }//GEN-LAST:event_clearButtonActionPerformed

  private void undoButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_undoButtonActionPerformed
  {//GEN-HEADEREND:event_undoButtonActionPerformed
    if (undoManager.canUndo())
    {
      undoManager.undo();
      updateUndoRedoButtons();
    }
  }//GEN-LAST:event_undoButtonActionPerformed

  private void redoButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_redoButtonActionPerformed
  {//GEN-HEADEREND:event_redoButtonActionPerformed
    if (undoManager.canRedo())
    {
      undoManager.redo();
      updateUndoRedoButtons();
    }
  }//GEN-LAST:event_redoButtonActionPerformed

  private void formatButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_formatButtonActionPerformed
  {//GEN-HEADEREND:event_formatButtonActionPerformed
    String code = inputTextPane.getText();
    try
    {
      replaceText(formatter.format(code));
    }
    catch (Exception ex)
    {
    }
  }//GEN-LAST:event_formatButtonActionPerformed

  private void findButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_findButtonActionPerformed
  {//GEN-HEADEREND:event_findButtonActionPerformed
    findText();
  }//GEN-LAST:event_findButtonActionPerformed

  protected void findText()
  {
    FindDialog findDialog = managerApp.getFindDialog();
    findDialog.pack();
    findDialog.setLocationRelativeTo(this);
    findDialog.setVisible(true);
  }

  protected void findFunctions()
  {
    module.findFunctions(null);
  }

  @Override
  public void functionsUpdated(ModuleEvent event)
  {
    Set<String> functionNames = module.getFunctionNames();
    editorKit.setFunctionNames(functionNames);
    inputTextPane.repaint();
  }

  protected void addPathToComboBox(String path)
  {
    DefaultComboBoxModel model =
     (DefaultComboBoxModel<String>)pathComboBox.getModel();
    if (model.getIndexOf(path) == -1)
    {
      model.addElement(path);
    }
    pathComboBox.getEditor().setItem(path);
    model.setSelectedItem(path);
  }

  private void updateUndoRedoButtons()
  {
    undoButton.setEnabled(undoManager.canUndo());
    redoButton.setEnabled(undoManager.canRedo());
  }

  private void replaceText(String text)
  {
    compoundEdit = new CompoundEdit();
    inputTextPane.setText(text);
    compoundEdit.end();
    undoManager.addEdit(compoundEdit);
    compoundEdit = null;
    setModified(true);
  }

  private void initEditor()
  {
    int scalingFactor = ManagerApp.getPreferences().getScalingFactor();
    int fontSize = ManagerApp.getPreferences().getFontSize();
    Font font = new Font("Monospaced", Font.PLAIN, fontSize * scalingFactor);
    inputTextPane.setFont(font);
    inputTextPane.putClientProperty("caretWidth", 2);
    inputTextPane.setCaretColor(Color.BLACK);
    TextUtils.updateInputMap(inputTextPane);
    TextUtils.updateInputMap(
      (JTextComponent)pathComboBox.getEditor().getEditorComponent());
    InputMap inputMap = inputTextPane.getInputMap();
    int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    inputMap.put(
      KeyStroke.getKeyStroke(KeyEvent.VK_F, mask), "find");
    inputTextPane.getActionMap().put("find", new AbstractAction()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        findText();
      }
    });
    editorKit = new ColoredEditorKit();
    inputTextPane.setEditorKit(editorKit);
    inputTextPane.getDocument().putProperty(
      DefaultEditorKit.EndOfLineStringProperty, "\n");
    lineTracker = new LineTracker(inputTextPane);
    lineTracker.setEnabled(true);
    matcher = new SymbolMatcher(inputTextPane,
      IOConstants.OPEN_LIST_TOKEN, IOConstants.CLOSE_LIST_TOKEN);
    matcher.setEnabled(true);
    indenter = new AutoIndenter(inputTextPane);
    int indentSize = ManagerApp.getPreferences().getIndentSize();
    indenter.setIndentSize(indentSize);
    indenter.setEnabled(true);

    formatter.getConfiguration().setIndentSize(indentSize);
    int columns = ManagerApp.getPreferences().getFormatColumns();
    formatter.getConfiguration().setMaxColumns(columns);

    textCompleter = new TextCompleter(module);
    completer = new AutoCompleter(inputTextPane);
    completer.setTextCompleter(textCompleter);
    completer.setEnabled(true);

    JTextField textField =
      (JTextField)pathComboBox.getEditor().getEditorComponent();
    textField.getDocument().addDocumentListener(new DocumentListener()
    {
      @Override
      public void insertUpdate(DocumentEvent e)
      {
        saveButton.setEnabled(true);
      }

      @Override
      public void removeUpdate(DocumentEvent e)
      {
        saveButton.setEnabled(true);
      }

      @Override
      public void changedUpdate(DocumentEvent e)
      {
        saveButton.setEnabled(true);
      }
    });

    DefaultComboBoxModel model =
     (DefaultComboBoxModel<String>)pathComboBox.getModel();
    model.addElement("");
    model.addElement(MODULE_START_VAR);
    model.addElement(MODULE_STOP_VAR);
    model.addElement(MODULE_ACCESS_KEY_VAR);
    model.addElement(MODULE_METADATA_VAR);
    model.addElement(DASHBOARDS_FUNCTION_NAME);

    addComponentListener(new ComponentAdapter()
    {
      @Override
      public void componentShown(ComponentEvent e)
      {
        managerApp.setAuxiliaryPanel(null);
        inputTextPane.requestFocus();
      }
    });

    undoManager = new UndoManager();
    Document document = inputTextPane.getDocument();
    document.addUndoableEditListener(new UndoableEditListener()
    {
      @Override
      public void undoableEditHappened(UndoableEditEvent e)
      {
        UndoableEdit edit = e.getEdit();
        setModified(true);
        if (compoundEdit == null)
        {
          undoManager.addEdit(edit);
        }
        else
        {
          compoundEdit.addEdit(edit);
        }
        SwingUtilities.invokeLater(new Runnable()
        {
          @Override
          public void run()
          {
            updateUndoRedoButtons();
          }
        });
      }
    });
    updateUndoRedoButtons();
  }

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton clearButton;
  private javax.swing.JToolBar editToolBar;
  private javax.swing.JButton findButton;
  private javax.swing.JButton formatButton;
  private javax.swing.JScrollPane inputScrollPane;
  private javax.swing.JTextPane inputTextPane;
  private javax.swing.JButton loadButton;
  private javax.swing.JPanel northPanel;
  private javax.swing.JComboBox<String> pathComboBox;
  private javax.swing.JLabel pathLabel;
  private javax.swing.JToolBar pathToolBar;
  private javax.swing.JButton redoButton;
  private javax.swing.JButton saveButton;
  private javax.swing.JButton undoButton;
  // End of variables declaration//GEN-END:variables
}
