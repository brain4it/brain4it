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
package org.brain4it.lang;

/**
 * A path reference.
 * 
 * A path reference contains a list of names that represent a path to access 
 * a value inside a structure of nested lists.
 * 
 * @author realor
 */
public class BPathReference extends BSoftReference
{
  protected BList path;
  
  public BPathReference(BList path)
  {
    super(pathToString(path));
    this.path = path;
  }

  @Override
  public BList getPath()
  {
    BList copy = new BList();
    copy.addAll(path);
    return copy;
  }

  public BList getInternalPath()
  {
    return path;
  }
  
  @Override
  public Object evaluate(Context context)
  {
    if (path.size() == 1)
    {
      return context.get((String)path.get(0));
    }
    else
    {
      String name = (String)path.get(0);
      BList list = (BList)context.get(name);
      return list.get(path, 1, path.size());
    }
  }
  
  @Override
  public Object getReferencedData(Context context)
  {
    return evaluate(context);
  }
  
  @Override
  public void setReferencedData(Context context, Object data)
  {
    if (path.size() == 1)
    {
      context.set((String)path.get(0), data);
    }
    else
    {
      String name = (String)path.get(0);
      BList list = (BList)context.get(name);
      BList lastList = (BList)list.get(path, 1, path.size() - 1);
      Object spec = path.get(path.size() - 1);
      lastList.put(spec, data);
    }
  }
  
  @Override
  public boolean deleteReferencedData(Context context)
  {
    if (path.size() == 1)
    {
      return context.delete((String)path.get(0));
    }
    else
    {
      String name = (String)path.get(0);
      BList list = (BList)context.get(name);
      BList lastList = (BList)list.get(path, 1, path.size() - 1);
      Object spec = path.get(path.size() - 1);
      synchronized (lastList)
      {
        if (lastList.has(spec))
        {
          lastList.remove(spec);
          return true;
        }
      }
    }
    return false;
  }
  
  @Override
  public boolean existsReferencedData(Context context)
  {
    if (path.size() == 1)
    {
      return context.exists((String)path.get(0));
    }
    else
    {
      String name = (String)path.get(0);
      Object object = context.get(name);
      if (!(object instanceof BList)) return false;
      
      BList list = (BList)object;
      try
      {
        BList lastList = (BList)list.get(path, 1, path.size() - 1);
        Object spec = path.get(path.size() - 1);
        return lastList.has(spec);
      }
      catch (RuntimeException ex)
      {
        return false;
      }
    }
  }  
}
