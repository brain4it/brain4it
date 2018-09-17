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
 * The interface for the implementation of built-in functions.
 * 
 * Built-in functions are evaluated in some {@link org.brain4it.lang.Context}
 * and receive the arguments in a {@link org.brain4it.lang.BList}.
 * 
 * @author realor
 */


public interface Function
{
  /**
   * Invokes the built-in function.
   * 
   * @param context, the context where this function will be evaluated.
   * @param args, the arguments passed to the function starting at index 1.
   * The element at index 0 is the function reference to this built-in function.
   * @return the result of evaluating this function in the given 
   * {@code context} with the specified arguments, that is a value of a 
   * supported BPL object type.
   * @throws Exception 
   */  
  
  public Object invoke(Context context, BList args) throws Exception;    
}
