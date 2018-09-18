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
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import org.brain4it.client.Monitor;
import org.brain4it.lang.BList;
import org.brain4it.lang.BSoftReference;
import org.brain4it.lang.Utils;
import org.brain4it.manager.swing.DashboardPanel;
import org.brain4it.manager.swing.DashboardWidget;
import org.brain4it.manager.swing.ManagerApp;
import org.brain4it.manager.widgets.FunctionInvoker;
import org.brain4it.manager.widgets.SwitchWidgetType;
import org.brain4it.manager.widgets.WidgetType;

/**
 *
 * @author realor
 */
public class SwitchWidget extends JPanel implements DashboardWidget
{
  protected DashboardPanel dashboard;
  protected String getValueFunction;
  protected String setValueFunction;
  protected JLabel label;
  protected SwitchButton switchButton;
  protected FunctionInvoker invoker;

  protected final Monitor.Listener monitorListener = new Monitor.Listener()
  {
    @Override
    public void onChange(String reference, final Object value, 
      final long serverTime)
    {
      SwingUtilities.invokeLater(new Runnable()
      {
        @Override
        public void run()
        {
          if (invoker == null || 
             (!invoker.isSending() && 
              invoker.updateInvokeTime(serverTime)))
          {
            switchButton.removeActionListener(actionListener);
            switchButton.setActive(Utils.toBoolean(value));
            switchButton.addActionListener(actionListener);
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
        invoker.invoke(switchButton.isActive());
      }
    }
  };

  public SwitchWidget()
  {
    initComponents();
  }

  @Override
  public void init(DashboardPanel dashboard, String name, BList properties)
    throws Exception
  {
    this.dashboard = dashboard;

    SwitchWidgetType type =
      (SwitchWidgetType)WidgetType.getType(WidgetType.SWITCH);

    type.validate(properties);

    String labelText = type.getLabel(properties);
    label.setText(labelText);

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
      switchButton.setEnabled(false);
    }
    else
    {
      switchButton.setEnabled(true);
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
    setBorder(new EmptyBorder(4, 4, 4, 4));
    setLayout(new BorderLayout());
    label = new JLabel();
    label.setHorizontalAlignment(JLabel.LEFT);
    label.setHorizontalTextPosition(JLabel.LEFT);
    add(label, BorderLayout.CENTER);
    switchButton = new SwitchButton();
    add(switchButton, BorderLayout.EAST);
    switchButton.addActionListener(actionListener);
  }

  public static class SwitchButton extends JComponent
  {
    private boolean active;
    private final List<ActionListener> listeners =
      new ArrayList<ActionListener>();

    public SwitchButton()
    {
      initComponents();
    }

    public boolean isActive()
    {
      return active;
    }

    public void setActive(boolean active)
    {
      if (active != this.active)
      {
        this.active = active;
        activationChanged();
      }
    }
    
    public void invert()
    {
      this.active = !active;
      activationChanged();
    }

    public void addActionListener(ActionListener listener)
    {
      this.listeners.add(listener);
    }

    public void removeActionListener(ActionListener listener)
    {
      this.listeners.remove(listener);
    }

    @Override
    public void paintComponent(Graphics g)
    {
      int height = getHeight();
      int width = getWidth();
      int size = 2 * height > width ? width / 2 : height;
      int centerX = width / 2;
      int centerY = height / 2;

      ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
         RenderingHints.VALUE_ANTIALIAS_ON);
      // paint background
      int startY = centerY - size / 2;
      if (isEnabled())
      {
        g.setColor(Color.GRAY);
      }
      else
      {
        g.setColor(Color.LIGHT_GRAY);
      }
      g.fillRoundRect(0, startY, 2 * size, size, size / 2, size / 2);

      if (active)
      {
        g.setColor(new Color(140, 140, 255));
      }
      else
      {
        g.setColor(Color.LIGHT_GRAY);
      }
      int x = active ? centerX : centerX - size;
      int scalingFactor = ManagerApp.getPreferences().getScalingFactor();
      int margin = 4 * scalingFactor;
      int innerSize = size - 2 * margin - 1;
      g.fillRoundRect(x + margin, startY + margin, innerSize, innerSize,
         innerSize / 2, innerSize / 2);
      g.setColor(Color.BLACK);
      g.drawRoundRect(x + margin, startY + margin, innerSize, innerSize,
         innerSize / 2, innerSize / 2);
    }

    @Override
    public Dimension getPreferredSize()
    {
      int scalingFactor = ManagerApp.getPreferences().getScalingFactor();
      return new Dimension(64 * scalingFactor, 32 * scalingFactor);
    }

    private void initComponents()
    {
      addMouseListener(new MouseAdapter()
      {
        @Override
        public void mouseReleased(MouseEvent e)
        {
          if (isEnabled())
          {
            invert();
          }
        }

        @Override
        public void mouseEntered(MouseEvent e)
        {
          if (isEnabled())
          {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
          }
        }

        @Override
        public void mouseExited(MouseEvent e)
        {
          if (isEnabled())
          {
            setCursor(Cursor.getDefaultCursor());
          }
        }
      });
    }

    private void activationChanged()
    {
      ActionEvent event = new ActionEvent(this, 0, String.valueOf(active));
      for (ActionListener listener : listeners)
      {
        listener.actionPerformed(event);
      }
      invalidate();
      revalidate();
      repaint();
    }
  }
}
