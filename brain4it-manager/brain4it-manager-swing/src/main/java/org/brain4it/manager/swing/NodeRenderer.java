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
import java.awt.Component;
import java.awt.Graphics;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import org.brain4it.lang.BList;
import org.brain4it.lang.Utils;
import org.brain4it.manager.Module;
import org.brain4it.manager.Server;
import org.brain4it.manager.Workspace;

/**
 *
 * @author realor
 */
public class NodeRenderer extends JLabel implements TreeCellRenderer
{
  private ImageIcon workspaceIcon;
  private ImageIcon serverIcon;
  private ImageIcon atomIcon;
  private ImageIcon listIcon;
  private ImageIcon functionListIcon;
  private boolean selected;
  private boolean hasFocus;
  
  public NodeRenderer()
  {
    initComponents();
  }  
  
  @Override
  public Component getTreeCellRendererComponent(JTree tree, Object value,
     boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus)
  {
    this.selected = selected;
    this.hasFocus = hasFocus;
    DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
    if (selected)
    {
      setForeground(Color.WHITE);
    }
    else
    {
      setForeground(Color.BLACK);
    }
    if (node instanceof DataNode && 
        ((DataNode)node).getExploreError() != null)
    {
      setForeground(Color.RED);
    }
    Object userObject = node.getUserObject();
    if (userObject instanceof Workspace)
    {
      Workspace workspace = (Workspace)userObject;
      setIcon(workspaceIcon);
      setText(workspace.getName());
    }
    else if (userObject instanceof Server)
    {
      Server server = (Server)userObject;
      setIcon(serverIcon);
      setText(server.getName());
    }
    else if (userObject instanceof Module)
    {
      Module module = (Module)userObject;
      setIcon(getModuleIcon(module));
      setText(module.getName());
    }
    else
    {
      DataNode dataNode = (DataNode)node;
      String type = dataNode.getType();
      if (type.equals(Utils.FUNCTION_LIST_SUBTYPE))
      {
        setIcon(functionListIcon);
      }
      else if (type.endsWith(Utils.LIST_TYPE))
      {
        setIcon(listIcon);
      }
      else
      {
        setIcon(atomIcon);        
      }
      setText(String.valueOf(userObject) + " (" + type + ")");
    }
    return this;
  }
  
  @Override
  public void paintComponent(Graphics g)
  {
    if (selected)
    {
      int iconWidth = this.getIcon().getIconWidth();
      g.setColor(ManagerApp.BASE_COLOR);
      int margin = iconWidth + 3;
      g.fillRect(margin, 1, 
        getWidth() - margin, 
        getHeight() - 1);
      if (hasFocus)
      {
        g.setColor(ManagerApp.BASE_COLOR.darker());        
        g.drawRect(margin, 1, 
          getWidth() - margin - 1, 
          getHeight() - 2);
      }
    }
    super.paintComponent(g);
  }
  
  private ImageIcon getModuleIcon(Module module)
  {
    BList metadata = module.getMetadata();
    if (metadata != null)
    {
      Object value = metadata.get("icon");
      if (value instanceof String)
      {
        return IconCache.getIcon("modules/" + value, "module");
      }
    }
    return IconCache.getIcon("module", null);    
  }
  
  private void initComponents()
  {
    try
    {
      workspaceIcon = IconCache.getIcon(("workspace"));
      serverIcon = IconCache.getIcon(("server"));
      atomIcon = IconCache.getIcon(("atom"));
      listIcon = IconCache.getIcon(("list"));
      functionListIcon = IconCache.getIcon(("function_list"));
      setBorder(new EmptyBorder(1, 0, 1, 1));
      setIconTextGap(5);
      setOpaque(false);
    }
    catch (Exception ex)
    {      
    }    
  }
}
