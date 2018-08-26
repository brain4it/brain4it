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

package org.brain4it.lib;

import java.util.HashMap;
import java.util.Map;
import org.brain4it.lang.WrapperFunction;
import org.brain4it.lang.Function;

/**
 *
 * @author realor
 */
public abstract class Library
{
  protected Map<String, Function> functions = new HashMap<String, Function>();
  
  public abstract String getName();
   
  public abstract void load();
  
  public void unload()
  {    
  }
  
  public final Map<String, Function> getFunctions()
  {
    return functions;
  }

  protected void createFunction(String pkg, String functionName, Class cls,
    String methodName, Class... parameterTypes)
  {
    try
    {
      String functionNameCap = functionName.substring(0, 1).toUpperCase() +
        functionName.substring(1);
      String pathName = pkg + "." + functionNameCap + "Function";
      Function function = 
        new WrapperFunction(pathName, cls, methodName, parameterTypes);
      functions.put(functionName, function);
    }
    catch (NoSuchMethodException ex)
    {
    }
  }  
}
