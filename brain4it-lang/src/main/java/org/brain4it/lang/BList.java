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
import java.util.Map;

/**
 * A BPL list.
 * 
 * Lists are the only mutable data type in BPL.
 * 
 * They contain a list of elements, where each of them can be of any of the
 * supported BPL types.
 * 
 * Elements in a list can be labeled with a name that identifies 
 * univocally that element within the list.
 * 
 * When a list has elements labeled with names, it holds a 
 * {@link org.brain4it.lang.Structure} that maps these names to 
 * indices and viceversa.
 * 
 * Elements in a BList can be acceded by index of by name.
 * 
 * All lists evaluate to themselves except if the first element references a 
 * function. In that case, the result of the evaluation is the value obtained 
 * by invoking that function passing the rest of elements of the list as 
 * arguments.
 * 
 * @author realor
 */
public class BList extends BObject implements Cloneable
{
  private static final int DEFAULT_CAPACITY = 4;

  private ArrayList elements;
  private Structure structure;
  private int hash;
  Function function = Context.DEFAULT_FUNCTION;

  public BList()
  {
    this(DEFAULT_CAPACITY);
  }

  public BList(int capacity)
  {
    elements = new ArrayList(capacity);
  }

  public BList(Structure structure)
  {
    structure.setShared(true);
    this.structure = structure;
    elements = new ArrayList(structure.size());
    for (int i = 0; i < structure.size(); i++)
    {
      elements.add(null);
    }
  }
  
  public Structure getStructure()
  {
    return structure;
  }

  public synchronized void setStructure(Structure structure)
  {
    structure.setShared(true); 
    this.structure = structure;   

    if (elements.size() > structure.size())
    {
      for (int i = elements.size() - 1; i >= structure.size(); i--)
      {
        elements.remove(i);
      }
    } 
    else if (elements.size() < structure.size())
    {
      for (int i = elements.size(); i < structure.size(); i++)
      {
        elements.add(null);
      }
    }
  }
  
  public synchronized void add(Object element)
  {
    if (structure != null)
    {
      modifyStructure();
      structure.add();
    }
    elements.add(element);
  }

  public synchronized void insert(int index, Object element)
  {
    if (structure != null)
    {
      modifyStructure();
      structure.insert(index);
    }
    elements.add(index, element);
    if (index == 0)
    {
      function = Context.DEFAULT_FUNCTION;
    }
  }

  public synchronized final Object get(int index)
  {
    return elements.get(index);
  }

  public synchronized final Object get(String name)
  {
    if (structure != null)
    {
      int index = structure.getIndex(name);
      if (index != -1) return elements.get(index);
    }
    return null;
  }

  public synchronized Object get(BList path)
  {
    return get(path, 0, path.size());
  }

  public synchronized Object get(BList path, int fromIndex, int toIndex)
  {
    Object element = this;

    int i = fromIndex;
    while (i < toIndex)
    {
      BList list = (BList)element;
      Object spec = path.get(i);
      if (spec instanceof Number)
      {
        element = list.get(((Number)spec).intValue());
      }
      else if (spec instanceof String)
      {
        element = list.get((String)spec);
      }
      else throw new RuntimeException("Invalid path item: " + spec);
      i++;
    }
    return element;
  }

  public synchronized Object get(Object spec)
  {
    if (spec instanceof Number)
    {
      Number number = (Number)spec;
      return get(number.intValue());
    }
    else if (spec instanceof String)
    {
      String name = (String)spec;
      return get(name);
    }
    else if (spec instanceof BList)
    {
      BList path = (BList)spec;
      return get(path);
    }
    throw new RuntimeException("Invalid path item: " + spec);
  }

  public synchronized Object put(int index, Object element)
  {
    if (index >= elements.size())
    {
      int newSize = index + 1;
      if (structure != null)
      {
        modifyStructure();
        structure.add(newSize - elements.size());
      }
      elements.ensureCapacity(newSize);
      for (int i = elements.size(); i < newSize; i++)
      {
        elements.add(null);
      }
    }
    Object old = elements.set(index, element);
    if (index == 0)
    {
      function = Context.DEFAULT_FUNCTION;
    }
    return old;
  }

  public synchronized Object put(String name, Object element)
  {
    Object old;
    int index;
    if (structure == null)
    {
      structure = new Structure(elements.size());
      index = -1;
    }
    else
    {
      index = structure.getIndex(name);
    }

    if (index == -1)
    {
      modifyStructure();
      structure.putName(elements.size(), name);
      elements.add(element);
      old = null;
    }
    else
    {
      old = elements.get(index);
      elements.set(index, element);
    }
    return old;
  }

  public synchronized Object put(BList path, Object element)
  {
    Object lastList = get(path, 0, path.size() - 1);
    if (lastList instanceof BList)
    {
      Object spec = path.get(path.size() - 1);
      return ((BList)lastList).put(spec, element);
    }
    throw new RuntimeException("Invalid path");
  }

  public synchronized Object put(Object spec, Object element)
  {
    if (spec instanceof Number)
    {
      Number number = (Number)spec;
      return put(number.intValue(), element);
    }
    else if (spec instanceof String)
    {
      String name = (String)spec;
      return put(name, element);
    }
    else if (spec instanceof BList)
    {
      BList path = (BList)spec;
      return put(path, element);
    }
    throw new RuntimeException("Invalid path item: " + spec);
  }

  public synchronized Object remove(int index)
  {
    if (structure != null)
    {
      modifyStructure();
      structure.delete(index);
    }
    if (index == 0)
    {
      function = Context.DEFAULT_FUNCTION;
    }
    return elements.remove(index);
  }

  public synchronized Object remove(String name)
  {
    if (structure == null) return null;
    int index = structure.getIndex(name);
    if (index == -1) return null;
    return BList.this.remove(index);
  }

  public synchronized Object remove(BList path)
  {
    Object lastList = get(path, 0, path.size() - 1);
    if (lastList instanceof BList)
    {
      Object spec = path.get(path.size() - 1);
      return ((BList)lastList).remove(spec);
    }
    throw new RuntimeException("Invalid path");
  }

  public synchronized Object remove(Object spec)
  {
    if (spec instanceof Number)
    {
      Number number = (Number)spec;
      return BList.this.remove(number.intValue());
    }
    else if (spec instanceof String)
    {
      String name = (String)spec;
      return BList.this.remove(name);
    }
    else if (spec instanceof BList)
    {
      BList path = (BList)spec;
      return BList.this.remove(path);
    }
    throw new RuntimeException("Invalid path item: " + spec);
  }

  public synchronized boolean has(int index)
  {
    return index >= 0 && index < elements.size();
  }
  
  public synchronized boolean has(String name)
  {
    if (structure == null) return false;
    return structure.getIndex(name) != -1;
  }

  public synchronized boolean has(BList path)
  {
    try
    {
      BList lastList = (BList)get(path, 0, path.size() - 1);
      Object spec = path.get(path.size() - 1);
      return ((BList)lastList).has(spec);
    }
    catch (RuntimeException ex)
    {
      return false;
    }
  }

  public synchronized boolean has(Object spec)
  {
    if (spec instanceof Number)
    {
      return has(((Number)spec).intValue());
    }
    else if (spec instanceof String)
    {
      return has((String)spec);
    }
    else if (spec instanceof BList)
    {
      return has((BList)spec);      
    }
    return false;
  }

  public synchronized String getName(int index)
  {
    if (structure == null) return null;
    return structure.getName(index);
  }

  public synchronized void putName(int index, String name)
  {
    if (structure == null)
    {
      if (name == null) return;
      structure = new Structure(elements.size());
    }
    else
    {
      modifyStructure();
    }
    structure.putName(index, name);
  }

  public synchronized int getIndexOfName(String name)
  {
    if (structure == null) return -1;
    return structure.getIndex(name);
  }

  public synchronized int size()
  {
    return elements.size();
  }

  public synchronized void addAll(BList source)
  {
    addAll(source, 0, source.size());
  }

  public synchronized void addAll(BList source, int fromIndex, int toIndex)
  {
    for (int index = fromIndex; index < toIndex; index++)
    {
      Object element = source.get(index);
      String name = source.getName(index);
      if (name != null)
      {
        put(name, element);
      }
      else
      {
        add(element);
      }
    }
  }

  public synchronized void removeAll()
  {
    elements.clear();
    structure = null;
    function = Context.DEFAULT_FUNCTION;
  }

  @Override
  public boolean equals(Object object)
  {
    if (object instanceof BList)
    {
      return this == (BList)object;
    }
    return false;
  }

  @Override
  public int hashCode()
  {
    if (hash == 0)
    {
      hash = (int)(Math.random() * 100000.0) + 1;
    }
    return hash;
  }

  @Override
  public synchronized BList clone()
  {
    BList clone = new BList(elements.size());
    clone.elements.addAll(elements);
    if (structure != null)
    {
      clone.structure = structure;
      structure.setShared(true);
    }
    return clone;
  }

  public synchronized BList clone(boolean recursive)
  {
    return recursive ? cloneRecursive(new HashMap<BList, BList>()) : clone();
  }

  public synchronized BList sublist(int fromIndex)
  {
    return sublist(fromIndex, elements.size());
  }  
  
  public synchronized BList sublist(int fromIndex, int toIndex)
  {
    BList sublist = new BList();
    for (int i = fromIndex; i < toIndex; i++)
    {
      String name = getName(i);
      Object value = get(i);
      if (name == null)
      {
        sublist.add(value);
      }
      else
      {
        sublist.put(name, value);
      }
    }
    return sublist;
  }

  public Object[] toArray()
  {
    return elements.toArray();
  }
  
  @Override
  public String toString()
  {
    return "BList";
  }

  private synchronized BList cloneRecursive(Map<BList, BList> cloned)
  {
    BList clone = cloned.get(this);
    if (clone != null) return clone;

    clone = new BList(elements.size());
    cloned.put(this, clone);

    if (structure != null)
    {
      clone.structure = structure;
      structure.setShared(true);
    }
    for (Object element : elements)
    {
      if (element instanceof BList)
      {
        BList child = (BList)element;
        element = child.cloneRecursive(cloned);
      }
      clone.elements.add(element);
    }
    return clone;
  }

  private void modifyStructure()
  {
    if (structure.isShared())
    {
      try
      {
        structure = structure.clone();
      }
      catch (CloneNotSupportedException ex)
      {
      }
    }
  }
}
