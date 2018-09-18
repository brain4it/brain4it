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

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import org.brain4it.client.Invoker;
import org.brain4it.lang.BSoftReference;
import org.brain4it.lang.BList;
import org.brain4it.manager.swing.DashboardPanel;
import org.brain4it.manager.swing.DashboardWidget;
import org.brain4it.manager.swing.FontCache;
import org.brain4it.manager.widgets.ButtonWidgetType;
import org.brain4it.manager.widgets.WidgetType;

/**
 *
 * @author realor
 */
public class ButtonWidget extends JButton implements DashboardWidget
{
  protected DashboardPanel dashboard;
  protected String onPressedFunction;
  protected String onReleasedFunction;
  protected String buttonId;
  protected Invoker invoker;
  
  public ButtonWidget()
  {
    initComponents();
  }

  @Override
  public void init(DashboardPanel dashboard, String name, BList properties)
    throws Exception
  {
    this.dashboard = dashboard;
    
    ButtonWidgetType type = 
      (ButtonWidgetType)WidgetType.getType(WidgetType.BUTTON);
    
    type.validate(properties);
    
    setText(type.getLabel(properties));

    String fontFamily = type.getFontFamily(properties);
    setFont(FontCache.getFont(fontFamily));

    int fontSize = type.getFontSize(properties);
    setFont(getFont().deriveFont((float)fontSize));
    
    BSoftReference func;
    func = type.getOnPressedFunction(properties);
    if (func != null)
    {
      onPressedFunction = func.getName();
    }
    func = type.getOnReleasedFunction(properties);
    if (func != null)
    {
      onReleasedFunction = func.getName();
    }
    if (onPressedFunction != null || onReleasedFunction != null)
    {
      if (dashboard != null)
      {
        invoker = dashboard.getInvoker();
      }
    }
    buttonId = type.getButtonId(properties);
  }
  
  private void initComponents()
  {
    addKeyListener(new KeyAdapter()
    {
      @Override
      public void keyPressed(KeyEvent e)
      {
        if (e.getKeyCode() == KeyEvent.VK_ENTER)
        {
          if (invoker != null && onPressedFunction != null)
          {
            invoker.invoke(onPressedFunction, buttonId);
          }
        }
      }

      @Override
      public void keyReleased(KeyEvent e)
      {
        if (e.getKeyCode() == KeyEvent.VK_ENTER)
        {
          if (invoker != null && onReleasedFunction != null)
          {
            invoker.invoke(onReleasedFunction, buttonId);
          }
        }
      }
    });

    addMouseListener(new MouseAdapter()
    {
      @Override
      public void mousePressed(MouseEvent e)
      {
        if (e.getButton() == MouseEvent.BUTTON1)
        {
          if (invoker != null && onPressedFunction != null)
          {
            invoker.invoke(onPressedFunction, buttonId);
          }
        }
      }
      
      @Override
      public void mouseReleased(MouseEvent e)
      {
        if (e.getButton() == MouseEvent.BUTTON1)
        {
          if (invoker != null && onReleasedFunction != null)
          {
            invoker.invoke(onReleasedFunction, buttonId);
          }
        }
      }
    });
  }
}
