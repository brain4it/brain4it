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
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.Action;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import org.brain4it.lang.BList;
import org.brain4it.manager.Module;
import org.brain4it.manager.Server;

/**
 *
 * @author realor
 */
public class Explorer extends JTree
  implements MouseListener, MouseMotionListener
{
  private DataNode draggedNode;
  private DataNode targetNode;
  private boolean drag;
  private int yDrag;
  private Action moveDataAction;

  public Explorer()
  {
    init();
  }

  private void init()
  {
    addMouseListener(this);
    addMouseMotionListener(this);
    ToolTipManager.sharedInstance().registerComponent(this);
  }

  public Action getMoveDataAction()
  {
    return moveDataAction;
  }

  public void setMoveDataAction(Action moveDataAction)
  {
    this.moveDataAction = moveDataAction;
  }

  public DataNode getDraggedNode()
  {
    return draggedNode;
  }

  public DataNode getTargetNode()
  {
    return targetNode;
  }

  @Override
  public void mouseClicked(MouseEvent e)
  {
  }

  @Override
  public void mousePressed(MouseEvent e)
  {
    int row = getRowForLocation(e.getX(), e.getY());
    TreePath treePath = getPathForRow(row);
    if (treePath != null)
    {
      DefaultMutableTreeNode node =
        (DefaultMutableTreeNode)treePath.getLastPathComponent();
      if (node instanceof DataNode && !(node.getUserObject() instanceof Module))
      {
        draggedNode = (DataNode)node;
      }
    }
  }

  @Override
  public void mouseReleased(MouseEvent e)
  {
    if (draggedNode != null && targetNode != null && moveDataAction != null)
    {
      moveDataAction.actionPerformed(new ActionEvent(this, 0, "move"));
    }
    draggedNode = null;
    targetNode = null;
    drag = false;
    repaint();
    setCursor(Cursor.getDefaultCursor());
  }

  @Override
  public void mouseEntered(MouseEvent e)
  {
  }

  @Override
  public void mouseExited(MouseEvent e)
  {
  }

  @Override
  public void mouseDragged(MouseEvent e)
  {
    if (draggedNode != null)
    {
      drag = true;
      yDrag = -1;
      targetNode = null;
      setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
      int row = getRowForLocation(e.getX(), e.getY() + getRowHeight() / 2);
      TreePath treePath = getPathForRow(row);
      if (treePath != null)
      {
        DefaultMutableTreeNode node =
          (DefaultMutableTreeNode)treePath.getLastPathComponent();
        if (node != draggedNode && node.getParent() == draggedNode.getParent() 
          && node != draggedNode.getNextNode())
        {
          targetNode = (DataNode)node;
          yDrag = (int)getRowBounds(row).getY();
        }
      }
      repaint();
    }
  }

  @Override
  public void mouseMoved(MouseEvent e)
  {
  }

  @Override
  public void paintComponent(Graphics g)
  {
    super.paintComponent(g);
    if (drag && yDrag != -1)
    {
      g.setColor(Color.LIGHT_GRAY);
      g.drawLine(0, yDrag - 1, getWidth(), yDrag - 1);
      g.drawLine(0, yDrag + 1, getWidth(), yDrag + 1);
      g.setColor(Color.GRAY);
      g.drawLine(0, yDrag, getWidth(), yDrag);
    }
  }

  @Override
  public String getToolTipText(MouseEvent event)
  {
    int row = getRowForLocation(event.getX(), event.getY());
    TreePath treePath = getPathForRow(row);
    if (treePath != null)
    {
      DefaultMutableTreeNode node =
        (DefaultMutableTreeNode)treePath.getLastPathComponent();
      if (node.getUserObject() instanceof Module)
      {
        Module module = (Module)node.getUserObject();
        BList metadata = module.getMetadata();
        if (metadata != null)
        {
          Object value = metadata.get("description");
          if (value instanceof String)
          {
            return (String)value;
          }
        }
      }
      else if (node.getUserObject() instanceof Server)
      {
        Server server = (Server)node.getUserObject();
        return server.getUrl();
      }
    }
    return null;
  }
}
