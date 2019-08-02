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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.brain4it.client.Monitor;
import org.brain4it.lang.BList;
import org.brain4it.lang.BSoftReference;
import org.brain4it.lang.Utils;
import org.brain4it.manager.swing.DashboardPanel;
import org.brain4it.manager.swing.DashboardWidget;
import org.brain4it.manager.widgets.FunctionInvoker;
import org.brain4it.manager.widgets.SelectWidgetType;
import org.brain4it.manager.widgets.WidgetType;

/**
 *
 * @author realor
 */
public class SelectWidget extends JPanel implements DashboardWidget
{
  protected DashboardPanel dashboard;
  protected String getOptionsFunction;
  protected String getValueFunction;
  protected String setValueFunction;
  protected String labelText;
  protected JLabel label;
  protected JComboBox<Option> comboBox;
  protected String currentValue;
  protected Object currentOptions;
  protected FunctionInvoker invoker;

  protected final Monitor.Listener monitorListener =
    new Monitor.Listener()
  {
    @Override
    public void onChange(final String reference, final Object data,
      final long serverTime)
    {
      SwingUtilities.invokeLater(new Runnable()
      {
        @Override
        public void run()
        {
          if (reference.equals(getValueFunction))
          {
            if (invoker == null ||
               (!invoker.isSending() &&
                invoker.updateInvokeTime(serverTime)))
            {
              String newValue = String.valueOf(data);
              if (!newValue.equals(currentValue))
              {
                setSelectedValue(newValue);
              }
            }
          }
          else if (reference.equals(getOptionsFunction))
          {
            if (!Utils.equals(data, currentOptions))
            {
              loadOptions(data);
            }
          }
        }
      });
    }
  };

  protected final ActionListener actionListener = new ActionListener()
  {
    @Override
    public void actionPerformed(ActionEvent e)
    {
      if (invoker != null)
      {
        Option option = (Option)comboBox.getSelectedItem();
        if (option != null)
        {
          invoker.invoke(option.value);
        }
      }
    }
  };

  public SelectWidget()
  {
    initComponents();
  }

  @Override
  public void init(DashboardPanel dashboard, String name, BList properties)
    throws Exception
  {
    this.dashboard = dashboard;

    SelectWidgetType type =
      (SelectWidgetType)WidgetType.getType(WidgetType.SELECT);

    type.validate(properties);

    labelText = type.getLabel(properties);
    label.setText(labelText);

    BSoftReference func;
    func = type.getOptionsFunction(properties);
    if (func != null)
    {
      getOptionsFunction = func.getName();
      if (dashboard != null)
      {
        Monitor monitor = dashboard.getMonitor();
        monitor.watch(getOptionsFunction, monitorListener);
      }
    }

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
      comboBox.setEnabled(false);
    }
    else
    {
      comboBox.setEnabled(true);
      setValueFunction = func.getName();
      if (dashboard != null)
      {
        invoker = new FunctionInvoker(dashboard.getInvoker(), setValueFunction);
      }
    }
  }

  private void initComponents()
  {
    setOpaque(false);
    setLayout(new GridBagLayout());
    label = new JLabel();
    label.setHorizontalAlignment(JLabel.CENTER);
    label.setHorizontalTextPosition(JLabel.CENTER);
    add(label, new GridBagConstraints(0, 0, 1, 1, 1, 0.5,
      GridBagConstraints.SOUTH, GridBagConstraints.HORIZONTAL,
      new Insets(2, 2, 2, 2), 0, 0));
    comboBox = new JComboBox<Option>();
    comboBox.setModel(new DefaultComboBoxModel<Option>());
    comboBox.addActionListener(actionListener);
    add(comboBox, new GridBagConstraints(0, 1, 1, 1, 1, 0.5,
      GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
      new Insets(2, 2, 2, 2), 0, 0));
  }

  protected void setSelectedValue(String value)
  {
    currentValue = value;

    if (value == null) return;

    comboBox.removeActionListener(actionListener);
    ComboBoxModel<Option> model = comboBox.getModel();
    int size = model.getSize();
    boolean found = false;
    int index = 0;
    while (index < size && !found)
    {
      if (model.getElementAt(index).value.equals(value))
      {
        comboBox.setSelectedIndex(index);
        found = true;
      }
      else index++;
    }
    if (!found && size > 0)
    {
      comboBox.setSelectedIndex(0);
    }
    comboBox.addActionListener(actionListener);
  }

  protected void loadOptions(Object data)
  {
    comboBox.removeActionListener(actionListener);
    DefaultComboBoxModel<Option> model = new DefaultComboBoxModel<Option>();
    try
    {
      BList options = (BList)data;
      for (int i = 0; i < options.size(); i++)
      {
        BList option = (BList)options.get(i);
        model.addElement(new Option(String.valueOf(option.get(0)),
          String.valueOf(option.get(1))));
      }
      currentOptions = options;
    }
    catch (Exception ex)
    {
      // bad data
    }
    comboBox.setModel(model);

    setSelectedValue(currentValue);
  }

  protected class Option
  {
    String value;
    String label;

    protected Option(String value, String label)
    {
      this.value = value;
      this.label = label;
    }

    @Override
    public boolean equals(Object o)
    {
      if (!(o instanceof Option)) return false;
      Option item = (Option)o;
      return value.equals(item.value);
    }

    @Override
    public String toString()
    {
      return label;
    }
  }
}
