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
package org.brain4it.manager.swing.splitter;

import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JTabbedPane;

/**
 *
 * @author realor
 */
public class TabContainer extends JTabbedPane
{
  private final Splitter splitter;

  public TabContainer(Splitter splitter)
  {
    this.splitter = splitter;
    initComponents();
  }

  public Splitter getSplitter()
  {
    return splitter;
  }

  public TabComponent getTabComponent(Component component)
  {
    int tabCount = getTabCount();
    int index = 0;
    TabComponent tabComponent = null;
    while (index < tabCount && tabComponent == null)
    {
      TabComponent c = (TabComponent)getTabComponentAt(index);
      if (c.getComponent() == component)
      {
        tabComponent = c;
      }
      index++;
    }
    return tabComponent;
  }

  @Override
  public void paintComponent(Graphics g)
  {
    if (splitter.isSplitted())
    {
      if (splitter.getActiveTabContainer() == this)
      {
        Graphics2D g2 = (Graphics2D)g;
        g2.setPaint(new GradientPaint(0, 0, splitter.getActiveColor(), 
          0, 4, getBackground()));
        g2.fillRect(0, 0, getWidth(), 4);
      }
    }
    super.paintComponent(g);
  }

  private final void initComponents()
  {
    addMouseListener(mouseListener);
  }

  private final MouseListener mouseListener = new MouseAdapter()
  {
    @Override
    public void mouseClicked(MouseEvent e)
    {
      splitter.setActiveTabContainer(TabContainer.this);
      splitter.repaint();
    }
  };
}
