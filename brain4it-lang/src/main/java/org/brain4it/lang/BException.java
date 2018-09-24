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
 * A BPL exception. 
 * 
 * Exceptions in BPL are represented as a {@link org.brain4it.lang.BList} 
 * where the first element is a string that indicates the exception type. 
 * More elements may be present to describe the exception message, the code
 * that thrown the exception or the stack of user function calls when the 
 * exception was thrown:
 * 
 * Example: 
 * (
 *   "InvalidHostException" 
 *   "message" => "www.delta245w.com"
 *   "code" => (http "GET" url)
 *   "stack" => (process_data send_data) 
 * )
 *
 * A BException is a convenient Java Exception class that contains such a list.
 * 
 * The BPL {@link org.brain4it.lang.Context} and the throw function generate 
 * BExceptions so they can be handled by the catch clauses of the try function.
 * 
 * @author realor
 */
public class BException extends RuntimeException
{
  /* optional exception properties */
  public static final String MESSAGE = "message";
  public static final String CODE = "code";
  public static final String STACK = "stack";
  
  private final BList list;

  /**
   * Creates a BException with the given type
   * @param type the exception type
   */  
  public BException(String type)
  {
    this(type, null);
  }
  
  /**
   * Creates a BException with the given type and message
   * @param type the exception type
   * @param message the exception message
   */
  public BException(String type, String message)
  {
    this.list = new BList(2);
    list.add(type);
    if (message != null)
    {
      list.put(MESSAGE, message);
    }
  }
  
  /**
   * Creates a BException from a BList, typically used when rethrowing an
   * exception with the throw function.
   * 
   * @param list the BList that contains exception information
   */
  public BException(BList list)
  {
    this.list = new BList();
    Object value = list.size() > 0 ? list.get(0) : null;
    if (value == null)
    {
      this.list.add("Exception");
    }
    else
    {
      this.list.add(String.valueOf(value));
    }
    value = list.get(MESSAGE);
    if (value != null)
    {
      this.list.put(MESSAGE, String.valueOf(value));
    }
    value = list.get(CODE);
    if (value != null)
    {
      this.list.put(CODE, value);
    }
    value = list.get(STACK);
    if (value != null)
    {
      this.list.put(STACK, value);
    }
  }
  
  /**
   * Creates a BException from a Java Throwable
   * @param t the Java Throwable
   */
  public BException(Throwable t)
  {
    super(t);
    list = toBList(t);
  }
  
  /**
   * Adds source information to this exception if it is not present
   * @param code the code that thrown this exception
   * @param callStack the call stack when this exception was thrown
   * @return this exception
   */
  public BException addSourceInfo(Object code, BList callStack)
  {
    if (code != null && !list.has(CODE))
    {
      list.put(CODE, code);
    }
    if (callStack != null && callStack.size() > 0 && !list.has(STACK))
    {
      list.put(STACK, callStack);
    }
    return this;
  }
  
  /**
   * Remove source information from this exception
   * @return this exception 
   */
  public BException removeSourceInfo()
  {
    list.remove(CODE);
    list.remove(STACK);
    return this;
  }
  
  /**
   * Gets the exception type
   * @return the first element of the exception list that represents the 
   * exception type.
   */
  public String getType()
  {
    return (String)list.get(0);
  }

  /**
   * Gets the message associated to this exception
   * @return the exception message if present or null otherwise.
   */
  @Override
  public String getMessage()
  {
    return (String)list.get(MESSAGE);
  }
  
  /**
   * Gets the code that thrown this exception
   * @return the code that thrown this exception if present or null otherwise.
   */
  public Object getCode()
  {
    return list.get(CODE);
  }

  /**
   * Gets the call stack of this exception
   * @return the call stack of this exception if present or null otherwise.
   */
  public Object getStack()
  {
    return list.get(STACK);
  }
  
  /**
   * Gets the BList associated with this exception
   * @return the BList associated with this exception
   */
  public BList getBList()
  {
    return list;
  }
  
  /**
   * Creates a BList from a Java Throwable
   * @param t the Throwable from which to obtain the list
   * @return a BList that contains information of the given Throwable
   */
  public static BList toBList(Throwable t)
  {
    BList list = new BList();
    list.add(t.getClass().getSimpleName());
    String message = t.getMessage();
    if (message != null)
    {
      list.put(MESSAGE, message);
    }
    return list;
  }
}
