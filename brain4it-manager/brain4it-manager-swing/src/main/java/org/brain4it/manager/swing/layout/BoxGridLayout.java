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

package org.brain4it.manager.swing.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager2;
import java.util.HashMap;

/**
 *
 * @author realor
 */
public class BoxGridLayout implements LayoutManager2
{
  private int gridWidth;
  private int gridHeight;
  private boolean stretch;
  private final HashMap<Component, Constraints> constraintsMap = 
    new HashMap<Component, Constraints>();

  public BoxGridLayout(int gridWidth, int gridHeight)
  {
    this(gridWidth, gridHeight, true);
  }
  
  public BoxGridLayout(int gridWidth, int gridHeight, boolean stretch)
  {
    this.gridWidth = gridWidth;
    this.gridHeight = gridHeight;
    this.stretch = stretch;
  }

  public void setGridSize(int gridWidth, int gridHeight)
  {
    this.gridWidth = gridWidth;
    this.gridHeight = gridHeight;    
  }
  
  public int getGridWidth()
  {
    return gridWidth;
  }

  public void setGridWidth(int gridWidth)
  {
    this.gridWidth = gridWidth;
  }

  public int getGridHeight()
  {
    return gridHeight;
  }

  public void setGridHeight(int gridHeight)
  {
    this.gridHeight = gridHeight;
  }

  public boolean isStretch()
  {
    return stretch;
  }

  public void setStretch(boolean stretch)
  {
    this.stretch = stretch;
  }

  @Override
  public void addLayoutComponent(String name, Component comp)
  {
  }

  @Override
  public void addLayoutComponent(Component component, Object constraints)
  {
    constraintsMap.put(component, (Constraints)constraints);
  }
  
  @Override
  public void removeLayoutComponent(Component component)
  {
    constraintsMap.remove(component);
  }

  @Override
  public Dimension preferredLayoutSize(Container container)
  {
    int panelWidth = container.getWidth();
    int panelHeight = container.getHeight();
    int cellWidth = panelWidth / gridWidth;
    int cellHeight = panelHeight / gridHeight;
    if (!stretch)
    {
      int cellSize = Math.min(cellWidth, cellHeight);
      cellWidth = cellSize;
      cellHeight = cellSize;
    }
    return new Dimension(gridWidth * cellWidth, gridHeight * cellHeight);
  }

  @Override
  public Dimension minimumLayoutSize(Container container)
  {
    return new Dimension(gridWidth * 10, gridHeight * 10);
  }

  @Override
  public Dimension maximumLayoutSize(Container container)
  {
    return preferredLayoutSize(container);
  }  
  
  @Override
  public void layoutContainer(Container container)
  {
    final int count = container.getComponentCount();
    if (count > 0)
    {
      int panelWidth = container.getWidth();
      int panelHeight = container.getHeight();
      int cellWidth = panelWidth / gridWidth;
      int cellHeight = panelHeight / gridHeight;
      int xOffset = 0;
      int yOffset = 0;
      if (!stretch)
      {
        int cellSize = Math.min(cellWidth, cellHeight);
        if (cellWidth > cellSize) 
        {
          xOffset += (panelWidth - cellSize * gridWidth) / 2;
          cellWidth = cellSize;
        }
        else if (cellHeight > cellSize)
        {
          yOffset += (panelHeight - cellSize * gridHeight) / 2;
          cellHeight = cellSize;
        }
      }
      for (int i = 0; i < count; i++)
      {
        Component component = container.getComponent(i);

        if (component.isVisible())
        {
          Constraints constraints = constraintsMap.get(component);
          if (constraints != null)
          {
            int x = xOffset + constraints.x * cellWidth;
            int y = yOffset + constraints.y * cellHeight;
            int width = constraints.xSize * cellWidth;
            int height = constraints.ySize * cellHeight;
          
            component.setBounds(x, y, width, height);
          }
        }
      }
    }    
  }  

  @Override
  public float getLayoutAlignmentX(Container container)
  {
    return 0;
  }

  @Override
  public float getLayoutAlignmentY(Container container)
  {
    return 0;
  }

  @Override
  public void invalidateLayout(Container container)
  {
  }
  
  public static class Constraints
  {
    int x;
    int y;
    int xSize;
    int ySize;
    
    public Constraints(int x, int y, int xSize, int ySize)
    {
      this.x = x;
      this.y = y;
      this.xSize = xSize;
      this.ySize = ySize;
    }
  }
}
