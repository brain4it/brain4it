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

package org.brain4it.manager.android.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import static android.view.View.MeasureSpec.EXACTLY;
import android.view.ViewGroup;

public class BoxGridLayout extends ViewGroup
{
  int gridWidth = 4;
  int gridHeight = 4;
  boolean stretch;

  public BoxGridLayout(Context context)
  {
    this(context, null, 0);
  }

  public BoxGridLayout(Context context, AttributeSet attrs)
  {
    this(context, attrs, 0);
  }

  public BoxGridLayout(Context context, AttributeSet attrs, int defStyleAttr)
  {
    super(context, attrs, defStyleAttr);
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
  protected void onLayout(boolean changed,
    int left, int top, int right, int bottom)
  {
    final int count = getChildCount();
    if (count > 0)
    {
      int panelWidth = right - left;
      int panelHeight = bottom - top;
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
        View child = getChildAt(i);

        if (child.getVisibility() != GONE)
        {
          LayoutParams params = (LayoutParams)child.getLayoutParams();
          int x = xOffset + params.x * cellWidth;
          int y = yOffset + params.y * cellHeight;
          int width = params.xSize * cellWidth;
          int height = params.ySize * cellHeight;
          
          child.layout(x, y, x + width, y + height);
        }
      }
    }
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
  {
    int panelWidth = MeasureSpec.getSize(widthMeasureSpec);
    int panelHeight = MeasureSpec.getSize(heightMeasureSpec);

    int count = getChildCount();
    if (count > 0)
    {
      int cellWidth = panelWidth / gridWidth;
      int cellHeight = panelHeight / gridHeight;
      if (!stretch)
      {
        int cellSize = Math.min(cellWidth, cellHeight);
        cellWidth = cellSize;
        cellHeight = cellSize;
      }
      for (int i = 0; i < count; i++)
      {
        View child = getChildAt(i);

        if (child.getVisibility() != GONE)
        {
          LayoutParams params = (LayoutParams)child.getLayoutParams();
          int width = params.xSize * cellWidth;
          int height = params.ySize * cellHeight;                    
          child.measure(
            MeasureSpec.makeMeasureSpec(width, EXACTLY),
            MeasureSpec.makeMeasureSpec(height, EXACTLY));
        }
      }
    }
    setMeasuredDimension(panelWidth, panelHeight);
  }

  @Override
  public LayoutParams generateLayoutParams(AttributeSet attrs)
  {
    return new LayoutParams(0, 0, 1, 1);
  }

  @Override
  protected LayoutParams generateDefaultLayoutParams()
  {
    return new LayoutParams(0, 0, 1, 1);
  }

  @Override
  protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p)
  {
    return new ViewGroup.LayoutParams(p);
  }

  @Override
  protected boolean checkLayoutParams(ViewGroup.LayoutParams p)
  {
    return p instanceof LayoutParams;
  }

  public static class LayoutParams extends ViewGroup.LayoutParams
  {
    public int x;
    public int y;
    public int xSize;
    public int ySize;

    public LayoutParams(int x, int y, int xSize, int ySize)
    {
      super(10, 10);
      this.x = x;
      this.y = y;
      this.xSize = xSize;
      this.ySize = ySize;
    }
  }
}
