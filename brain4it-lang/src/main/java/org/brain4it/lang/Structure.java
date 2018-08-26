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

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author realor
 */
public class Structure implements Cloneable
{
  private final HashMap<String, Integer> nameToIndexMap;
  private final ArrayList<String> namesList;
  private boolean shared;

  public Structure()
  {
    this(0, 4);
  }

  public Structure(int size)
  {
    this(size, size + 2);
  }  
  
  public Structure(int size, int capacity)
  {
    nameToIndexMap = new HashMap<String, Integer>();
    namesList = new ArrayList<String>(capacity);
    for (int i = 0; i < size; i++)
    {
      namesList.add(null);
    }    
  }
  
  public Structure(String... names)
  {
    nameToIndexMap = new HashMap<String, Integer>(names.length);
    namesList = new ArrayList<String>(names.length);
    shared = true;
    for (int i = 0; i < names.length; i++)
    {
      putName(i, names[i]);
    }
  }
  
  public int size()
  {
    return namesList.size();
  }

  public int nameCount()
  {
    return nameToIndexMap.size();
  }
  
  public boolean isShared()
  {
    return shared;
  }

  public void setShared(boolean shared)
  {
    this.shared = shared;
  }
  
  public synchronized final void add()
  {
    namesList.add(null);
  }

  public synchronized final void add(int count)
  {
    namesList.ensureCapacity(namesList.size() + count);
    for (int i = 0; i < count; i++)
    {
      namesList.add(null);
    }
  }
  
  public synchronized final void insert(int index)
  {
    shiftIndices(index, 1);
    namesList.add(index, null);
  }

  public synchronized final void delete(int index)
  {
    shiftIndices(index, -1);
    namesList.remove(index);
  }
  
  public synchronized final void putName(int index, String name)
  {
    // index <= size
    if (index == namesList.size())
    {
      nameToIndexMap.put(name, index);
      namesList.add(name);
    }
    else if (index < namesList.size())
    {
      String oldName = namesList.get(index);
      if (oldName != null)
      {
        nameToIndexMap.remove(oldName);
      }
      if (name != null)
      {
        Integer oldIndex = nameToIndexMap.put(name, index);
        if (oldIndex != null)
        {
          namesList.set(oldIndex, null);
        }
      }
      namesList.set(index, name);
    }
    else throw new RuntimeException("invalid index");
  }

  public synchronized final String getName(int index)
  {
    return namesList.get(index);
  }

  public synchronized final void deleteName(int index)
  {
    putName(index, null);
  }
  
  public synchronized final int getIndex(String name)
  {
    Integer index = nameToIndexMap.get(name);
    return index == null ? -1 : index;
  }

  @Override
  public Structure clone() throws CloneNotSupportedException
  {
    super.clone();
    Structure structure = new Structure(0, namesList.size());
    structure.nameToIndexMap.putAll(nameToIndexMap);
    structure.namesList.addAll(namesList);
    return structure;
  }
  
  @Override
  public synchronized boolean equals(Object other)
  {
    if (this == other) return true;

    if (other instanceof Structure)
    {
      Structure structure = (Structure)other;
      ArrayList<String> otherNamesList = structure.namesList;
      if (namesList.size() == otherNamesList.size())
      {
        boolean equals = true;
        int i = 0;
        while (i < namesList.size() && equals)
        {
          String name1 = namesList.get(i);
          String name2 = otherNamesList.get(i);
          equals = (name1 == null && name2 == null) || 
                   (name1 != null && name1.equals(name2));
          i++;
        }
        return equals;
      }
    }
    return false;
  }
  
  private void shiftIndices(int index, int offset)
  {
    if (offset < 0)
    {
      String name = namesList.get(index);
      if (name != null)
      {
        nameToIndexMap.remove(name);
      }
      index++;
    }
    while (index < namesList.size())
    {
      String name = namesList.get(index);
      if (name != null)
      {
        nameToIndexMap.put(name, index + offset);  
      }
      index++;
    }
  }  
}