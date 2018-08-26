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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JSplitPane;

/**
 *
 * @author realor
 */
public class Splitter extends JComponent
{
  private TabContainer activeTabContainer;
  private final List<Component> componentList = new ArrayList<Component>();
  private final List<SplitterListener> splitterListeners =
    new ArrayList<SplitterListener>();
  private Color activeColor = Color.RED;

  public Splitter()
  {
    initComponents();
  }

  private void initComponents()
  {
    setLayout(new BorderLayout());
    activeTabContainer = new TabContainer(this);
    add(activeTabContainer, BorderLayout.CENTER);
  }

  public TabContainer getActiveTabContainer()
  {
    return activeTabContainer;
  }

  public void setActiveTabContainer(TabContainer tabContainer)
  {
    if (tabContainer.getSplitter() == this)
    {
      this.activeTabContainer = tabContainer;
      repaint();
    }
  }
  
  public Color getActiveColor()
  {
    return activeColor;
  }

  public void setActiveColor(Color activeColor)
  {
    this.activeColor = activeColor;
  }
  
  public void showComponent(Component component)
  {
    Container parent = component.getParent();    
    if (parent instanceof TabContainer)
    {
      TabContainer tabContainer = (TabContainer)parent;
      if (tabContainer.getSplitter() == this)
      {
        activeTabContainer = tabContainer;
        activeTabContainer.setSelectedComponent(component);
        repaint();
      }
    }
  }

  public TabComponent getTabComponent(Component component)
  {
    Container parent = component.getParent();
    if (parent instanceof TabContainer)
    {
      TabContainer tabContainer = (TabContainer)parent;
      return tabContainer.getTabComponent(component);
    }
    return null;
  }
  
  public void addComponent(Component component, String title, Icon icon)
  {
    if (!componentList.contains(component))
    {
      activeTabContainer.add(component);
      int index = activeTabContainer.getTabCount() - 1;
      TabComponent tabComponent = new TabComponent(this, component, title, icon);
      activeTabContainer.setTabComponentAt(index, tabComponent);
      activeTabContainer.setSelectedIndex(index);

      componentList.add(component);
      SplitterEvent event = new SplitterEvent(this, activeTabContainer, component);
      for (SplitterListener listener : splitterListeners)
      {
        listener.componentAdded(event);
      }
    }
  }

  public boolean removeComponent(Component component)
  {
    if (!componentList.contains(component)) return false;

    TabContainer tabContainer = (TabContainer)component.getParent();
    SplitterEvent event = new SplitterEvent(this, tabContainer, component);

    boolean close = true;
    Iterator<SplitterListener> iter = splitterListeners.iterator();
    while (iter.hasNext() && close)
    {
      close = iter.next().componentClosing(event);
    }
    
    if (close)
    {
      componentList.remove(component);
      tabContainer.remove(component);
      for (SplitterListener listener : splitterListeners)
      {
        listener.componentRemoved(event);
      }
    }
    return close;
  }

  public void closeActiveTabContainer()
  {
    Container parent = activeTabContainer.getParent();
    if (parent == Splitter.this) return;
    
    JSplitPane splitPane = (JSplitPane)parent;
    Component other = null;
    if (activeTabContainer == splitPane.getTopComponent())
    {
      other = splitPane.getBottomComponent();
    }
    else
    {
      other = splitPane.getTopComponent();
    }
    parent = splitPane.getParent();
    int dividerLocation = -1;
    if (parent instanceof JSplitPane)
    {
      dividerLocation = ((JSplitPane)parent).getDividerLocation();
    }
    parent.remove(splitPane);
    parent.add(other);
    if (parent instanceof JSplitPane)
    {
      ((JSplitPane)parent).setDividerLocation(dividerLocation);
    }
    for (Component component : activeTabContainer.getComponents())
    {
      removeComponent(component);
    }
    activeTabContainer = findTabContainer(other);
    parent.invalidate();
    parent.revalidate();
  }

  public void splitHorizontal()
  {
    Container parent = activeTabContainer.getParent();
    int dividerLocation = -1;
    if (parent instanceof JSplitPane)
    {
      dividerLocation = ((JSplitPane)parent).getDividerLocation();
    }
    parent.remove(activeTabContainer);
    TabContainer tabContainer = new TabContainer(this);
    JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
      activeTabContainer, tabContainer);
    splitPane.setDividerLocation(activeTabContainer.getHeight() / 2);
    activeTabContainer = tabContainer;
    parent.add(splitPane);
    if (parent instanceof JSplitPane)
    {
      ((JSplitPane)parent).setDividerLocation(dividerLocation);
    }
    parent.invalidate();
    parent.revalidate();
  }

  public void splitVertical()
  {
    Container parent = activeTabContainer.getParent();
    int dividerLocation = -1;
    if (parent instanceof JSplitPane)
    {
      dividerLocation = ((JSplitPane)parent).getDividerLocation();
    }
    parent.remove(activeTabContainer);
    TabContainer tabContainer = new TabContainer(this);
    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
      activeTabContainer, tabContainer);
    splitPane.setDividerLocation(activeTabContainer.getWidth() / 2);
    activeTabContainer = tabContainer;
    parent.add(splitPane);
    if (parent instanceof JSplitPane)
    {
      ((JSplitPane)parent).setDividerLocation(dividerLocation);
    }
    parent.invalidate();
    parent.revalidate();
  }
  
  public boolean isSplitted()
  {
    return getComponentCount() > 0 && getComponent(0) instanceof JSplitPane;
  }

  public List<Component> getComponentList()
  {
    return new ArrayList(componentList);
  }

  public void addSplitterListener(SplitterListener listener)
  {
    splitterListeners.add(listener);
  }

  public void removeSplitterListener(SplitterListener listener)
  {
    splitterListeners.remove(listener);
  }

  public SplitterListener[] getSplitterListeners()
  {
    int count = splitterListeners.size();
    return splitterListeners.toArray(new SplitterListener[count]);
  }

  private TabContainer findTabContainer(Component component)
  {
    if (component instanceof TabContainer)
      return (TabContainer)component;

    JSplitPane splitPane = (JSplitPane)component;
    if (splitPane.getTopComponent() instanceof TabContainer)
    {
      return (TabContainer)splitPane.getTopComponent();
    }
    else if (splitPane.getBottomComponent() instanceof TabContainer)
    {
      return (TabContainer)splitPane.getBottomComponent();
    }
    else
    {
      return findTabContainer(splitPane.getTopComponent());
    }
  }
}
