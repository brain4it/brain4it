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

import java.lang.reflect.Method;

/**
 *
 * @author realor
 */
public class WrapperFunction implements Function
{
  private final String pathName;
  private final Object instance;
  private final Method method;
  private Class<?>[] parameterTypes;
  
  public WrapperFunction(String pathName, Method method)
  {
    this(pathName, null, method);
  }

  public WrapperFunction(String pathName, Object instance, Method method)
  {
    this.pathName = pathName;
    this.instance = instance;
    this.method = method;
    this.parameterTypes = method.getParameterTypes();
  }

  public WrapperFunction(String pathName, Class cls, String methodName, 
    Class... parameterTypes) throws NoSuchMethodException
  {
    this.pathName = pathName;
    this.instance = null;
    this.method = cls.getMethod(methodName, parameterTypes);
    this.parameterTypes = parameterTypes;
  }

  public WrapperFunction(String pathName, Object instance, String methodName, 
     Class... parameterTypes) throws NoSuchMethodException
  {
    this.pathName = pathName;
    this.instance = instance;
    Class cls = instance.getClass();
    this.method = cls.getMethod(methodName, parameterTypes);
    this.parameterTypes = parameterTypes;
  }
  
  public String getPathName()
  {
    return pathName;
  }
  
  public Object getInstance()
  {
    return instance;
  }
          
  public Method getMethod()
  {
    return method;
  }
  
  @Override
  public Object invoke(Context context, BList args) throws Exception
  {
    if (args.size() - 1 < parameterTypes.length)
    {
      throw new RuntimeException("Insufficient number of arguments");
    }
    Object[] parameters = new Object[parameterTypes.length];
    for (int i = 0; i < parameterTypes.length; i++)
    {
      Class parameterType = parameterTypes[i];
      Object value = context.evaluate(args.get(i + 1));
      parameters[i] = convert(value, parameterType);
    }
    return method.invoke(instance, parameters);
  }
  
  private Object convert(Object value, Class parameterType)
  {
    Object result;
    if (value == null)
    {      
      result = null;
    }
    else if (parameterType == String.class)
    {
      result = value.toString();
    }
    else if (parameterType == Integer.class || parameterType == int.class)
    {
      result = ((Number)value).intValue();
    }
    else if (parameterType == Long.class || parameterType == long.class)
    {
      result = ((Number)value).longValue();
    }
    else if (parameterType == Double.class || parameterType == double.class)
    {
      result = ((Number)value).doubleValue();
    }
    else if (parameterType == Boolean.class || parameterType == boolean.class)
    {
      result = Utils.toBoolean(value);
    }
    else
    {
      result = null;
    }
    return result;
  }
}
