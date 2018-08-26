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

import java.awt.Cursor;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.brain4it.client.RestClient;
import org.brain4it.client.RestClient.Callback;
import org.brain4it.io.Parser;
import org.brain4it.io.Printer;
import org.brain4it.lang.BList;
import org.brain4it.lang.BSoftReference;
import org.brain4it.lang.Utils;
import org.brain4it.manager.Module;
import static org.brain4it.io.IOConstants.PATH_REFERENCE_SEPARATOR;

/**
 *
 * @author realor
 */
public class DataNode extends DefaultMutableTreeNode
{
  private final JTree tree;
  private String type;
  private boolean explored = false;
  private Exception exploreError;
  private static final String EXPLORABLE_TYPE = "data-list";
  private static final String EXPLORE_COMMAND = 
  "(do" +
  "  (local l)" +
  "  (set l (get (global-scope) *))" +
  "  (apply (names l) x (list x (subtype-of (get l x))))" +
  ")";
          
  public DataNode(JTree tree, Object userObject)
  {
    this(tree, userObject, EXPLORABLE_TYPE);
  }

  public DataNode(JTree tree, Object userObject, String type) 
  {
    super(userObject);
    this.tree = tree;
    this.type = type;
  }
  
  public String getType()
  {
    return type;
  }
  
  @Override
  public boolean isLeaf()
  {
    if (!type.equals(EXPLORABLE_TYPE)) return true;
    if (!explored) return false;
    else return super.isLeaf();
  }
  
  @Override
  public int getChildCount()
  {
    if (!explored)
    {
      explore();
    }
    return super.getChildCount();
  }
  
  public Module getModule()
  {
    DefaultMutableTreeNode node = this;
    Module module = null;
    while (node != null && module == null)
    {
      Object data = node.getUserObject();
      if (data instanceof Module) module = (Module)data;
      else node = (DefaultMutableTreeNode)node.getParent();
    }
    return module;
  }

  public BList getModulePathList()
  {
    ArrayList<String> path = new ArrayList<String>();
    DefaultMutableTreeNode node = this;
    Module module = null;
    while (node != null && module == null)
    {
      Object data = node.getUserObject();
      if (data instanceof Module) 
      {
        module = (Module)data;
      }
      else 
      {
        String name = node.getUserObject().toString();
        path.add(name);
        node = (DefaultMutableTreeNode)node.getParent();
      }
    }
    Collections.reverse(path);    
    return Utils.toBList(path);
  }
  
  public String getModulePath()
  {
    BList path = getModulePathList();
    if (path.size() == 0) return "";
    String referenceValue = BSoftReference.pathToString(path);
    if (referenceValue.charAt(0) == PATH_REFERENCE_SEPARATOR)
    {
      // remove initial separator because is never necessary in this context
      referenceValue = referenceValue.substring(1);
    }
    return referenceValue;
  }
  
  public Exception getExploreError()
  {
    return exploreError;
  }
  
  public void explore()
  {
    explored = true;
    exploreError = null;
    
    if (!EXPLORABLE_TYPE.equals(type)) return;
    
    tree.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

    Module module = getModule();
    RestClient client = module.getRestClient();
    BList pathList = getModulePathList();
    String command = EXPLORE_COMMAND.replace("*", Printer.toString(pathList));
    client.execute(module.getName(), command, new Callback()
    {
      @Override
      public void onSuccess(RestClient client, String resultString)
      {
        try
        {
          Object data = Parser.fromString(resultString);
          if (data instanceof BList)
          {
            BList list = (BList)data;
            populate(list);
          }
        }
        catch (ParseException ex)
        {          
          setExploreError(ex);
        }
      }

      @Override
      public void onError(RestClient client, Exception ex)
      {
        setExploreError(ex);
      }
    });
  }
  
  public void update()
  {
    SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        getModel().nodeChanged(DataNode.this);
      }
    });
  }
  
  private DefaultTreeModel getModel()
  {
    return (DefaultTreeModel)tree.getModel();
  }
  
  private void populate(final BList list)
  {
    SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        tree.setCursor(Cursor.getDefaultCursor());
        removeAllChildren();
        for (int i = 0; i < list.size(); i++)
        {
          Object data = list.get(i);
          if (data instanceof BList)
          {
            BList elem = (BList)data;
            if (elem.size() == 2)
            {
              String name = String.valueOf(elem.get(0));
              String type = String.valueOf(elem.get(1));          
              DefaultMutableTreeNode dataNode = new DataNode(tree, name, type);
              add(dataNode);
            }
          }
        }
        getModel().nodeStructureChanged(DataNode.this);
        TreePath path = new TreePath(getPath());
        tree.expandPath(path);
      }
    });
  } 

  private void setExploreError(final Exception ex)
  {
    SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        tree.setCursor(Cursor.getDefaultCursor());
        exploreError = ex;
        getModel().nodeChanged(DataNode.this);
      }
    });
  }
}
