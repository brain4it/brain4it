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
import java.lang.reflect.Modifier;
import java.text.ParseException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.brain4it.io.Printer;
import org.brain4it.lang.annotation.MakeFunction;
import static org.brain4it.io.IOConstants.CALL_FUNCTION_NAME;
import static org.brain4it.io.IOConstants.FUNCTION_FUNCTION_NAME;

/**
 * A utility class with data conversion methods and other basic operations.
 * 
 * @author realor
 */
public class Utils
{
  public static final String NULL_TYPE = "null";
  public static final String BOOLEAN_TYPE = "boolean";
  public static final String NUMBER_TYPE = "number";
  public static final String STRING_TYPE = "string";
  public static final String REFERENCE_TYPE = "reference";
  public static final String LIST_TYPE = "list";
  
  public static final String INTEGER_SUBTYPE = "integer";
  public static final String LONG_SUBTYPE = "long";
  public static final String DOUBLE_SUBTYPE = "double";
  public static final String HARD_REFERENCE_SUBTYPE = "hard-reference";
  public static final String SOFT_REFERENCE_SUBTYPE = "soft-reference";
  public static final String FUNCTION_LIST_SUBTYPE = "function-list";
  public static final String CODE_LIST_SUBTYPE = "code-list";
  public static final String DATA_LIST_SUBTYPE = "data-list";
  
  public static String toString(Number number)
  {
    return toString(number, 10, false);
  }

  public static String toString(Number number, int radix, boolean unsigned)
  {
    if (number == null)
    {
      return "null";
    }
    else if (number instanceof Integer)
    {
      int integer = number.intValue();
      return Integer.toString(integer, radix);
    }
    else if (number instanceof Long)
    {
      long longInteger = number.longValue();
      return Long.toString(longInteger, radix);
    }
    else if (number.intValue() == number.doubleValue())
    {
      int integer = number.intValue();
      return Integer.toString(integer, radix);
    }
    else
    {
      return String.valueOf(number.doubleValue());
    }
  }

  public static String toString(Object value)
  {
    if (value == null)
    {
      return "null";
    }
    else if (value instanceof String)
    {
      return (String)value;
    }
    else if (value instanceof Number)
    {
      return toString((Number)value);
    }
    else if (value instanceof BReference)
    {
      return ((BReference)value).getName();
    }
    else if (value instanceof BList)
    {
      return Printer.toString(value);
    }
    else
    {
      return String.valueOf(value);
    }
  }

  public static Number toNumber(String value)
  {
    Number number;
    if (value.startsWith("0x"))
    {
      number = Integer.parseInt(value.substring(2), 16);
    }
    else if (value.startsWith("0b"))
    {
      number = Integer.parseInt(value.substring(2), 2);
    }
    else if (value.indexOf('.') != -1 ||
             value.indexOf('E') != -1 ||
             value.indexOf('e') != -1)
    {
      number = Double.parseDouble(value);
    }
    else
    {
      try
      {
        number = Integer.parseInt(value);
      }
      catch (NumberFormatException ex)
      {
        try
        {
          number = Long.parseLong(value);
        }
        catch (NumberFormatException ex2)
        {
          number = Double.parseDouble(value);
        }
      }
    }
    return number;
  }

  public static Number toNumber(String value, int radix)
  {
    if (value == null) return null;
    return Long.parseLong(value, radix);
  }

  public static Number toNumber(Object value)
  {
    if (value == null) return null;
    if (value instanceof Number) return (Number)value;
    if (value instanceof String) return toNumber((String)value);
    throw new RuntimeException("Invalid number: " + value.toString());
  }

  public static Boolean toBoolean(Object value)
  {
    if (value == null) return false;
    if (value instanceof Boolean) return (Boolean)value;
    if (value instanceof String) return Boolean.valueOf(value.toString());
    if (value instanceof Integer) return ((Number)value).intValue() != 0;
    if (value instanceof Long) return ((Number)value).longValue() != 0;
    if (value instanceof Number) return ((Number)value).doubleValue() != 0;
    if (value instanceof BList) return ((BList)value).size() > 0;
    return true;
  }

  public static boolean equals(Object value1, Object value2)
  {
    return equals(value1, value2, null, false);
  }

  public static boolean equals(Object value1, Object value2, 
    boolean compareNames)
  {
    return equals(value1, value2, null, compareNames);
  }
  
  public static boolean exactEquals(Object value1, Object value2)
  {
    if (value1 != null && value2 != null)
    {
      return value1.equals(value2);
    }
    return value1 == value2;
  }
  
  public static int compare(Object value1, Object value2)
  {
    if (value1 instanceof Integer && value2 instanceof Integer)
    {
      return ((Integer)value1).compareTo((Integer)value2);
    }
    else if (value1 instanceof Long && value2 instanceof Long)
    {
      return ((Long)value1).compareTo((Long)value2);
    }
    else if (value1 instanceof Double && value2 instanceof Double)
    {
      return ((Double)value1).compareTo((Double)value2);
    }
    else if (value1 instanceof Number && value2 instanceof Number)
    {
      double numValue1 = ((Number)value1).doubleValue();
      double numValue2 = ((Number)value2).doubleValue();
      return Double.compare(numValue1, numValue2);
    }
    else if (value1 instanceof String && value2 instanceof String)
    {
      return ((String)value1).compareTo((String)value2);
    }
    else if (value1 instanceof BReference && value2 instanceof BReference)
    {
      String refName1 = ((BReference)value1).getName();
      String refName2 = ((BReference)value2).getName();
      return refName1.compareTo(refName2);
    }
    return 0;
  }
  
  /* get the index of name in list case insensitive */
  public static int getIndexOfNameCI(BList list, String name)
  {
    synchronized (list)
    {
      int i = 0;
      int index = -1;
      while (i < list.size() && index == -1)
      {
        String iname = list.getName(i);
        if ((iname == null && name == null) ||
           (iname != null && iname.equalsIgnoreCase(name)))
        {
          index = i;
        }
        else i++;
      }
      return index;
    }
  }

  public static BList toBList(Object[] array)
  {
    BList list = new BList(array.length);
    for (Object elem : array)
    {
      if (elem == null ||
          elem instanceof BObject ||
          elem instanceof Number ||
          elem instanceof String ||
          elem instanceof Boolean)
      {
        list.add(elem);
      }
    }
    return list;
  }

  public static BList toBList(Collection collection)
  {
    BList list = new BList(collection.size());
    for (Object elem : collection)
    {
      if (elem == null ||
          elem instanceof BObject ||
          elem instanceof Number ||
          elem instanceof String ||
          elem instanceof Boolean)
      {
        list.add(elem);
      }
    }
    return list;
  }
  
  public static BList toBList(Exception ex)
  {
    BList list;
    if (ex instanceof BException)
    {
      list = ((BException)ex).toList();
    }
    else
    {
      list = new BList(2);
      list.add(ex.getClass().getSimpleName());
      String message = ex.getMessage();
      if (message != null)
      {
        list.add(message);
      }
    }
    return list;
  }

  public static String typeOf(Object value)
  {
    if (value == null) return NULL_TYPE;
    else if (value instanceof Number) return NUMBER_TYPE;
    else if (value instanceof String) return STRING_TYPE;
    else if (value instanceof Boolean) return BOOLEAN_TYPE;
    else if (value instanceof BReference) return REFERENCE_TYPE;
    else if (value instanceof BList) return LIST_TYPE;
    return null;
  }

  public static String subtypeOf(Object value)
  {
    if (value == null) return NULL_TYPE;
    else if (value instanceof Integer) return INTEGER_SUBTYPE;
    else if (value instanceof Long) return LONG_SUBTYPE;
    else if (value instanceof Double) return DOUBLE_SUBTYPE;
    else if (value instanceof Number) return NUMBER_TYPE;
    else if (value instanceof String) return STRING_TYPE;
    else if (value instanceof Boolean) return BOOLEAN_TYPE;
    else if (value instanceof BSoftReference) return SOFT_REFERENCE_SUBTYPE;
    else if (value instanceof BHardReference) return HARD_REFERENCE_SUBTYPE;
    else if (value instanceof BList)
    {
      BList list = (BList)value;
      if (list.size() > 0)
      {
        Object first = list.get(0);
        if (first instanceof BHardReference)
        {
          BHardReference reference = (BHardReference)first;
          if (reference.getName().equals(FUNCTION_FUNCTION_NAME))
          {
            return FUNCTION_LIST_SUBTYPE;
          }
          else
          {
            return CODE_LIST_SUBTYPE;
          }
        }
      }
      return DATA_LIST_SUBTYPE;
    }
    return null;
  }
  
  public static String unescapeString(String text) throws ParseException
  {
    boolean escape = false;
    StringBuilder buffer = new StringBuilder();
    for (int i = 0; i < text.length(); i++)
    {
      char ch = text.charAt(i);
      if (escape)
      {
        switch (ch)
        {
          case 'n': buffer.append('\n'); break;
          case 'r': buffer.append('\r'); break;
          case 't': buffer.append('\t'); break;
          case '\\': buffer.append('\\'); break;
          case 'b': buffer.append('\b'); break;
          case 'f': buffer.append('\f'); break;
          case '\'': buffer.append('\''); break;
          case '"': buffer.append('"'); break;
          case 'u':
            try 
            {
              StringBuilder unicodeBuffer = new StringBuilder();
              unicodeBuffer.append(text.substring(i + 1, i + 5));
              buffer.append((char)Integer.parseInt(unicodeBuffer.toString(), 16));
              i += 4;
            }
            catch (NumberFormatException | StringIndexOutOfBoundsException ex)
            {
              throw new ParseException("Invalid unicode character", i);              
            }
            break;
          default:
            throw new ParseException("Invalid character: \\" + ch, i);
        }
        escape = false;
      }
      else if (ch == '"')
      {        
      }
      else if (ch == '\\')
      {        
        escape = true;
      }
      else
      {
        buffer.append(ch);
      }
    }
    return buffer.toString();
  }
  
  
  public static String escapeString(String text)
  {
    if (text == null) return null;
    StringBuilder buffer = new StringBuilder();
    for (int i = 0; i < text.length(); i++)
    {
      char ch = text.charAt(i);
      switch (ch)
      {
        case '\n':
          buffer.append("\\n");
          break;
        case '\r':
          buffer.append("\\r");
          break;
        case '\t':
          buffer.append("\\t");
          break;
        case '\\':
          buffer.append("\\\\");
          break;
        case '"':
          buffer.append("\\\"");
          break;
        default:
          buffer.append(ch);
          break;
      }
    }
    return buffer.toString();
  }

  public static final void checkArguments(BList args, int expected)
  {
    if (expected >= args.size()) 
      throw new BException("RuntimeException", 
        "Insufficient number of arguments");
  }
  
  public static final BReference getBReference(Context context, BList args, 
    int pos) throws Exception
  {
    if (pos >= args.size()) 
      throw new BException("MissingArgument", "At position " + pos);
    Object arg = args.get(pos);
    if (arg instanceof BList)
    {
      arg = context.evaluate(arg);
    }
    if (arg instanceof BReference) return (BReference)arg;
    throw new BException("InvalidReference", "At position " + pos);
  }
  
  public static BReference createBReference(Map<String, Function> functions, 
    String value)
  {
    Function function = functions.get(value);
    if (function == null)
    {
      return BSoftReference.getInstance(value);
    }
    else
    {
      return new BHardReference(value, function);
    }
  }
  
  public static BList createFunctionCall(Map<String, Function> functions, 
    Object function, Object... args)
  {
    BList call = new BList();
    Function callFunction = functions.get(CALL_FUNCTION_NAME);
    if (callFunction == null)
      throw new RuntimeException("call function is not available");

    call.add(new BHardReference(CALL_FUNCTION_NAME, callFunction));
    if (function instanceof String)
    {
      call.add(createBReference(functions, (String)function));
    }
    else
    {
      call.add(function);
    }
    for (Object arg : args)
    {
      call.add(arg);
    }
    return call;
  }
  
  /* if object is a Class only static methods are considered */
  public static void createFunctions(Object object,
    Map<String, Function> functions)
  {
    Class cls;
    if (object instanceof Class)
    {
      cls = (Class)object;
      object = null;
    }
    else
    {
      cls = object.getClass();
    }
    Method[] methods = cls.getMethods();
    for (Method method : methods)
    {
      MakeFunction annotation = method.getAnnotation(MakeFunction.class);
      if (annotation != null)
      {
        String pathName = annotation.pathName();
        WrapperFunction wrapper = null;
        if (Modifier.isStatic(method.getModifiers()))
        {
          wrapper = new WrapperFunction(pathName, null, method);
        }
        else if (object != null)
        {
          wrapper = new WrapperFunction(pathName, object, method);
        }
        if (wrapper != null)
        {
          String functionName = annotation.functionName();
          if (functionName.length() == 0)
          {
            functionName = method.getName();
          }
          functions.put(functionName, wrapper);
        }
      }
    }
  }

  private static boolean equals(Object value1, Object value2, 
     Map<BList, BList> listMatch, boolean compareNames)
  {
    if (value1 == value2)
    {
      return true;
    }
    else if (value1 == null || value2 == null)
    {
      return false;
    }
    else if (value1 instanceof Number && value2 instanceof Number)
    {
      if (value1.getClass() == value2.getClass())
      {
        return value1.equals(value2);
      }
      else
      {
        return ((Number)value1).doubleValue() == ((Number)value2).doubleValue();
      }
    }
    else if (value1 instanceof BList && value2 instanceof BList)
    {
      BList list1 = (BList)value1;
      BList list2 = (BList)value2;
      if (list1.size() != list2.size()) return false;
      if (compareNames)
      {
        Structure structure1 = list1.getStructure();
        Structure structure2 = list2.getStructure();
        if ((structure1 == null && structure2 != null) ||
            (structure1 != null && !structure1.equals(structure2)))
          return false;
      }
      
      if (listMatch == null)
      {
        listMatch = new HashMap<BList, BList>();
      }
      else
      {
        BList list = listMatch.get(list1);
        if (list != null) return list == list2;
      }
      listMatch.put(list1, list2);

      int i = 0;
      boolean equals = true;
      while (equals && i < list1.size())
      {
        Object elem1 = list1.get(i);
        Object elem2 = list2.get(i);
        equals = Utils.equals(elem1, elem2, listMatch, compareNames);
        i++;
      }
      return equals;      
    }
    else
    {
      return value1.equals(value2);
    }
  }  
}

