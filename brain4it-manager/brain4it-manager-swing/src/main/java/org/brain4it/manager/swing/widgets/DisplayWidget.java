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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import org.brain4it.client.Monitor;
import org.brain4it.lang.BSoftReference;
import org.brain4it.lang.BList;
import org.brain4it.lang.Utils;
import org.brain4it.manager.swing.DashboardPanel;
import org.brain4it.manager.swing.DashboardWidget;
import org.brain4it.manager.swing.FontCache;
import org.brain4it.manager.widgets.DisplayWidgetType;
import org.brain4it.manager.widgets.WidgetType;

/**
 *
 * @author realor
 */
public class DisplayWidget extends JTextArea implements DashboardWidget
{
  protected DashboardPanel dashboard;
  protected String getValueFunction;
  protected int lines = 1;  
  protected final Monitor.Listener monitorListener = new Monitor.Listener()
  {
    @Override
    public void onChange(String reference, final Object data, 
      long serverTime)
    {
      try
      {
        updateText(Utils.toString(data));
      }
      catch (Exception ex)
      {
        updateText("");        
      }
    }
  };
  
  public DisplayWidget()
  {
    initComponents();
  }
  
  @Override
  public void init(DashboardPanel dashboard, String name, BList properties)
    throws Exception
  {
    this.dashboard = dashboard;

    DisplayWidgetType type = 
      (DisplayWidgetType)WidgetType.getType(WidgetType.DISPLAY);
    
    type.validate(properties);
    
    this.lines = type.getLines(properties);

    String fontFamily = type.getFontFamily(properties);
    setFont(FontCache.getFont(fontFamily));

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
  }
  
  @Override
  public void paintComponent(Graphics g)
  {
    super.paintComponent(g);
    if (dashboard == null) // design mode
    {
      int height = getHeight();
      if (height > 0)
      {
        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
          RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        Insets insets = getInsets();
        float actualHeight =  height - insets.top - insets.bottom;
        float lineHeight = actualHeight / lines;
        g.setFont(getFont().deriveFont(lineHeight));
        g.setColor(Color.WHITE);
        for (int i = 1; i <= lines; i++)
        {
          g.drawString("ABC123", insets.left, 
            (int)(lineHeight * i) - insets.top);
        }
      }
    }
  }
  
  private void updateText(final String text)
  {
    SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        setText(text);
      }
    });
  }
  
  private void updateFontSize()
  {
    int height = getHeight();
    if (height > 0)
    {
      Insets insets = getInsets();
      float actualHeight =  height - insets.top - insets.bottom;
      float lineHeight = 0.75f * actualHeight / lines;
      setFont(getFont().deriveFont(lineHeight));
    }      
  }

  private void initComponents()
  {
    setOpaque(true);
    setBackground(Color.BLUE);
    setForeground(Color.WHITE);
    setBorder(new LineBorder(Color.BLACK, 2));
    setEditable(false);
    setLineWrap(true);
    setWrapStyleWord(true);
    
    addComponentListener(new ComponentAdapter()
    {
      @Override
      public void componentResized(ComponentEvent e)
      {
        updateFontSize();
      }
    });
  }  
}
