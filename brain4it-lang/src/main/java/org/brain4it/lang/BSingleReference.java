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
 * A single reference.
 * 
 * A reference that contains a single name that points to a value inside
 * the local or global scope lists.
 * 
 * @author realor
 */
public class BSingleReference extends BSoftReference
{ 
  public BSingleReference(String name)
  {
    super(name);
  }

  @Override
  public BList getPath()
  {
    BList path = new BList(1);
    path.add(name);
    return path;
  }
  
  @Override
  public Object evaluate(Context context)
  {
    return context.get(name);
  }

  @Override
  public Object getReferencedData(Context context)
  {
    return context.get(name);
  }

  @Override
  public void setReferencedData(Context context, Object data)
  {
    context.set(name, data);
  }

  @Override
  public boolean deleteReferencedData(Context context)
  {
    return context.delete(name);
  }

  @Override
  public boolean existsReferencedData(Context context)
  {
    return context.exists(name);
  }  
}
