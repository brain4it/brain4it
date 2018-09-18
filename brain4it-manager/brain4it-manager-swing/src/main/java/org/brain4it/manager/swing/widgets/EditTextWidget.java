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
package org.brain4it.manager.swing.widgets;

import java.awt.BorderLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.brain4it.client.Monitor;
import org.brain4it.lang.BList;
import org.brain4it.lang.BSoftReference;
import org.brain4it.manager.swing.DashboardPanel;
import org.brain4it.manager.swing.DashboardWidget;
import org.brain4it.manager.swing.FontCache;
import org.brain4it.manager.widgets.EditTextWidgetType;
import org.brain4it.manager.widgets.FunctionInvoker;
import org.brain4it.manager.widgets.WidgetType;

/**
 *
 * @author realor
 */
public class EditTextWidget extends JPanel implements DashboardWidget,
  DocumentListener
{
  protected DashboardPanel dashboard;
  protected String getValueFunction;
  protected String setValueFunction;
  protected JLabel label;
  protected JTextPane textPane;
  protected JScrollPane scrollPane;
  protected int invokeInterval = 100;
  protected FunctionInvoker invoker;

  protected final Monitor.Listener monitorListener = new Monitor.Listener()
  {
    @Override
    public void onChange(String reference, final Object value, 
      final long serverTime)
    {
      if (value instanceof String)
      {
        SwingUtilities.invokeLater(new Runnable()
        {
          @Override
          public void run()
          {
            if (invoker == null ||
                (!invoker.isSending() && invoker.updateInvokeTime(serverTime)))
            {
              String text = (String)value;
              int selStart = textPane.getSelectionStart();
              int selEnd = textPane.getSelectionEnd();
              textPane.getDocument().
                removeDocumentListener(EditTextWidget.this);
              textPane.setText(text);
              textPane.getDocument().addDocumentListener(EditTextWidget.this);
              textPane.setSelectionStart(selStart);
              textPane.setSelectionEnd(selEnd);
            }
          }
        });
      }
    }
  };

  public EditTextWidget()
  {
    initComponents();
  }

  @Override
  public void init(DashboardPanel dashboard, String name, BList properties)
    throws Exception
  {
    this.dashboard = dashboard;

    EditTextWidgetType type =
      (EditTextWidgetType)WidgetType.getType(WidgetType.EDIT_TEXT);

    type.validate(properties);

    String labelText = type.getLabel(properties);
    label.setText(labelText);

    String fontFamily = type.getFontFamily(properties);
    textPane.setFont(FontCache.getFont(fontFamily));

    int fontSize = type.getFontSize(properties);
    textPane.setFont(textPane.getFont().deriveFont((float)fontSize));

    invokeInterval = type.getInvokeInterval(properties);
    
    BSoftReference func;
    func = type.getGetValueFunction(properties);
    if (func != null)
    {
      getValueFunction = func.getName();
      if (dashboard != null)
      {
        Monitor monitor = dashboard.getMonitor();
        monitor.watch(getValueFunction, monitorListener);
      }
    }

    func = type.getSetValueFunction(properties);
    if (func == null)
    {
      textPane.setEditable(false);
    }
    else
    {
      setValueFunction = func.getName();
      if (dashboard != null)
      {
        if (invokeInterval == 0)
        {
          invoker = new FunctionInvoker(dashboard.getInvoker(), 
            setValueFunction);          
        }
        else
        {
          invoker = new FunctionInvoker(dashboard.getInvoker(), 
            setValueFunction, dashboard.getTimer(), invokeInterval);
        }
      }
    }
  }

  private void initComponents()
  {
    setOpaque(false);
    setLayout(new BorderLayout());
    label = new JLabel();
    label.setHorizontalAlignment(JLabel.LEFT);
    label.setHorizontalTextPosition(JLabel.LEFT);
    add(label, BorderLayout.NORTH);
    scrollPane = new JScrollPane();
    scrollPane.setHorizontalScrollBarPolicy(
      JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    scrollPane.setVerticalScrollBarPolicy(
      JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    add(scrollPane, BorderLayout.CENTER);
    textPane = new JTextPane();
    textPane.getDocument().addDocumentListener(this);
    scrollPane.setViewportView(textPane);
  }

  @Override
  public void insertUpdate(DocumentEvent evt)
  {
    onChanged(evt);
  }

  @Override
  public void removeUpdate(DocumentEvent evt)
  {
    onChanged(evt);
  }

  @Override
  public void changedUpdate(DocumentEvent evt)
  {
    onChanged(evt);
  }

  private void onChanged(DocumentEvent evt)
  {
    if (invoker != null)
    {
      invoker.invoke(textPane.getText());
    }
  }
}
