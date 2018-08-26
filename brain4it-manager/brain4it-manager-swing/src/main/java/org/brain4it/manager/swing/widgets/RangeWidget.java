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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.brain4it.client.Monitor;
import org.brain4it.client.Invoker;
import org.brain4it.lang.BSoftReference;
import org.brain4it.lang.BList;
import org.brain4it.manager.swing.DashboardPanel;
import org.brain4it.manager.swing.DashboardWidget;
import org.brain4it.manager.widgets.FunctionInvoker;
import org.brain4it.manager.widgets.RangeWidgetType;
import org.brain4it.manager.widgets.WidgetType;

/**
 *
 * @author realor
 */
public class RangeWidget extends JComponent implements DashboardWidget
{
  protected DashboardPanel dashboard;
  protected String getValueFunction;
  protected String setValueFunction;
  protected String labelText;
  protected JLabel label;
  protected JSlider slider;
  protected int min;
  protected int max;
  protected boolean tracking = false;
  protected FunctionInvoker invoker;

  protected final Monitor.Listener monitorListener = new Monitor.Listener()
  {
    @Override
    public void onChange(String reference, final Object value, 
      final long serverTime)
    {
      if (value instanceof Number)
      {
        SwingUtilities.invokeLater(new Runnable()
        {
          @Override
          public void run()
          {
            if (!tracking)
            {
              if (invoker == null || 
                 (!invoker.isSending() && 
                  invoker.updateInvokeTime(serverTime)))
              {
                int remoteValue = ((Number)value).intValue();
                label.setText(labelText + " " + remoteValue);
                slider.removeChangeListener(changeListener);
                slider.removeMouseListener(mouseListener);
                slider.setValue(remoteValue);
                slider.addChangeListener(changeListener);
                slider.addMouseListener(mouseListener);
              }
            }
          }
        });
      }
    }
  };

  protected MouseAdapter mouseListener = new MouseAdapter()
  {
    @Override
    public void mousePressed(MouseEvent e)
    {
      tracking = true;
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
      tracking = false;
      if (invoker != null)
      {
        invoker.invoke(slider.getValue());
      }
    }
  };

  protected ChangeListener changeListener = new ChangeListener()
  {
    @Override
    public void stateChanged(ChangeEvent e)
    {
      label.setText(labelText + " " + slider.getValue());
      if (!tracking)
      {
        if (invoker != null)
        {
          invoker.invoke(slider.getValue());
        }
      }
    }
  };

  public RangeWidget()
  {
    initComponents();
  }

  @Override
  public void init(DashboardPanel dashboard, String name, BList properties)
    throws Exception
  {
    this.dashboard = dashboard;

    RangeWidgetType type =
      (RangeWidgetType)WidgetType.getType(WidgetType.RANGE);

    type.validate(properties);

    labelText = type.getLabel(properties);
    label.setText(labelText);

    BSoftReference func;
    func = type.getGetValueFunction(properties);
    if (func != null)
    {
      getValueFunction = func.getValue();
      if (dashboard != null)
      {
        Monitor monitor = dashboard.getMonitor();
        monitor.watch(getValueFunction, monitorListener);
      }
    }

    func = type.getSetValueFunction(properties);
    if (func == null)
    {
      slider.setEnabled(false);
    }
    else
    {
      slider.setEnabled(true);
      setValueFunction = func.getValue();
      if (dashboard != null)
      {
        invoker = new FunctionInvoker(dashboard.getInvoker(), setValueFunction);
      }
    }

    min = type.getMin(properties);
    slider.setMinimum(min);

    max = type.getMax(properties);
    slider.setMaximum(max);

    slider.setValue(min);

    if (dashboard != null)
    {
      slider.addChangeListener(changeListener);
      slider.addMouseListener(mouseListener);
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
    slider = new JSlider();
    add(slider, new GridBagConstraints(0, 1, 1, 1, 1, 0.5,
      GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
      new Insets(2, 2, 2, 2), 0, 0));
  }
}
