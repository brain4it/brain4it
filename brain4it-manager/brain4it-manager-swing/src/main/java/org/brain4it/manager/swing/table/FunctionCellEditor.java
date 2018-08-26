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
package org.brain4it.manager.swing.table;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.TableCellEditor;
import org.brain4it.lang.BSoftReference;
import org.brain4it.manager.Module;
import org.brain4it.manager.swing.DashboardWidgetEditor;
import org.brain4it.manager.swing.DesignerPanel;
import org.brain4it.manager.swing.ManagerApp;

/**
 *
 * @author realor
 */

public class FunctionCellEditor extends AbstractCellEditor 
  implements TableCellEditor
{
  private final JPanel panel;
  private final JTextField textField;
  private final JButton button;
  private final DashboardWidgetEditor widgetEditor;
  private static final String FUNCTION_BODY = "(function (ctx data) null)";

  public FunctionCellEditor(DashboardWidgetEditor widgetEditor)
  {
    this.widgetEditor = widgetEditor;
    panel = new JPanel();
    panel.setLayout(new BorderLayout());
    textField = new JTextField();
    textField.setMargin(new Insets(0, 0, 0, 0));
    textField.setBorder(new LineBorder(Color.BLACK, 1));
    button = new JButton();
    button.setBorder(new LineBorder(Color.GRAY, 1));
    button.setMargin(new Insets(0, 0, 0, 0));
    button.setUI(new BasicButtonUI());
    button.setText("...");
    panel.add(textField, BorderLayout.CENTER);
    panel.add(button, BorderLayout.EAST);
    button.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        openFunction();
      }
    });
  }

  @Override
  public boolean isCellEditable(EventObject event)
  {
    return (event == null || event instanceof MouseEvent);
  }
  
  @Override
  public Component getTableCellEditorComponent(JTable table, Object value, 
    boolean isSelected, int row, int column)
  {
    if (value instanceof BSoftReference)
    {
      BSoftReference reference = (BSoftReference)value;
      textField.setText(reference.getValue());
    }
    else
    {
      textField.setText(null);
    }
    SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        textField.requestFocus();
      }
    });
    return panel;
  }

  @Override
  public Object getCellEditorValue()
  {
    String functionName = textField.getText();
    if (functionName == null) return null;
    functionName = functionName.trim();
    if (functionName.length() == 0) return null;

    return BSoftReference.getInstance(functionName);
  }
  
  @Override
  public boolean stopCellEditing()
  {
    try
    {
      getCellEditorValue();
      return super.stopCellEditing();
    }
    catch (Exception ex)
    {
      return false;
    }
  }
  
  private void openFunction()
  {
    final String functionName = textField.getText();
    if (functionName != null && functionName.length() > 0)
    {
      if (stopCellEditing())
      {
        DesignerPanel.WidgetView widgetView = widgetEditor.getWidgetView();
        DesignerPanel designerPanel = widgetView.getDesignerPanel();
        final ManagerApp managerApp = designerPanel.getManagerApp();
        final Module module = designerPanel.getModule();
        module.saveData(functionName, FUNCTION_BODY, new Module.Callback()
        {
          @Override
          public void actionCompleted(Module module, String action)
          {
            managerApp.openEditor(module, functionName);
          }

          @Override
          public void actionFailed(Module module, String action, 
            Exception error)
          {
            managerApp.showError("Error", error);
          }          
        });
      }
    }
  }
}

