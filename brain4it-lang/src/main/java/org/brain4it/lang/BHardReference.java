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
 * A hard reference.
 * 
 * A hard reference is a reference that points to a built-in function.
 * 
 * It contains a pointer to the {@link org.brain4it.lang.Function} 
 * implementation of the built-in function it references.
 * 
 * Hard references always evaluate themselves.
 * 
 * @author realor
 */
public class BHardReference extends BReference
{
  Function function;
  
  public BHardReference(String name, Function function)
  {
    super(name);
    this.function = function;
  }
  
  public Function getFunction()
  {
    return function;
  }

  @Override
  public boolean equals(Object other)
  {
    if (other instanceof BHardReference)
    {
      return name.equals(((BHardReference)other).getName());
    }
    return false;
  }

  @Override
  public Object evaluate(Context context)
  {
    return this;
  }
}
