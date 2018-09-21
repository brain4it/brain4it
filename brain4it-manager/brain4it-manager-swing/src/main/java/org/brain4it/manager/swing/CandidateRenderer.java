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
package org.brain4it.manager.swing;

import java.awt.Color;
import java.awt.Font;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;
import org.brain4it.lang.Utils;
import org.brain4it.manager.TextCompleter.Candidate;

/**
 *
 * @author realor
 */
public class CandidateRenderer extends JLabel implements ListCellRenderer
{
  private ImageIcon stringIcon;
  private ImageIcon numberIcon;
  private ImageIcon booleanIcon;
  private ImageIcon referenceIcon;
  private ImageIcon atomIcon;
  private ImageIcon listIcon;
  private ImageIcon functionListIcon;
  private static final Color BACKGROUND = new Color(255, 255, 200);
  
  public CandidateRenderer()
  {
    initComponents();
  }  
  
  @Override
  public JLabel getListCellRendererComponent(JList list, Object value,
    int index, boolean selected, boolean hasFocus)
  {
    Candidate candidate = (Candidate)value;
    if (selected)
    {
      setBackground(ManagerApp.BASE_COLOR);
      setForeground(Color.WHITE);
    }
    else
    {
      setBackground(BACKGROUND);
      setForeground(Color.BLACK);
    }
    String name = candidate.getName();
    setText(name);    
    String type = candidate.getType();
    if (type.equals(Utils.HARD_REFERENCE_SUBTYPE))
    {
      setIcon(functionListIcon);
      setFont(list.getFont().deriveFont(Font.BOLD));
    }
    else 
    {
      setFont(list.getFont().deriveFont(Font.PLAIN));
      if (type.equals(Utils.FUNCTION_LIST_SUBTYPE))
      {
        setIcon(functionListIcon);
      }
      else if (type.endsWith(Utils.LIST_TYPE))
      {
        setIcon(listIcon);
      }
      else if (type.endsWith(Utils.INTEGER_SUBTYPE) || 
        type.endsWith(Utils.LONG_SUBTYPE) ||
        type.endsWith(Utils.DOUBLE_SUBTYPE))
      {
        setIcon(numberIcon);
      }
      else if (type.equals(Utils.STRING_TYPE))
      {
        setIcon(stringIcon);
      }
      else if (type.equals(Utils.BOOLEAN_TYPE))
      {
        setIcon(booleanIcon);
      }
      else if (type.equals(Utils.SOFT_REFERENCE_SUBTYPE))
      {
        setIcon(referenceIcon);
      }
      else
      {
        setIcon(atomIcon);
      }
    }
    return this;
  }
  
  private void initComponents()
  {
    try
    {
      atomIcon = IconCache.getIcon("types/atom");
      booleanIcon = IconCache.getIcon("types/boolean");
      stringIcon = IconCache.getIcon("types/string");
      numberIcon = IconCache.getIcon("types/number");
      referenceIcon = IconCache.getIcon("types/reference");
      listIcon = IconCache.getIcon("types/list");
      functionListIcon = IconCache.getIcon("types/function_list");
      setBorder(new EmptyBorder(1, 0, 1, 1));
      setIconTextGap(5);
      setOpaque(true);
    }
    catch (Exception ex)
    {
      // ignore
    }
  }
}
