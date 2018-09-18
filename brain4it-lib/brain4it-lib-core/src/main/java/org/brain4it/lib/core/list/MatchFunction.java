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

package org.brain4it.lib.core.list;

import java.util.Stack;
import org.brain4it.lang.BSoftReference;
import org.brain4it.lang.Context;
import org.brain4it.lang.BList;
import org.brain4it.lang.Utils;
import org.brain4it.lang.Function;

/**
 *
 * @author realor
 *
 */
public class MatchFunction implements Function
{
  public static final String SINGLE_VARIABLE_SUFFIX = "?";
  public static final String LIST_VARIABLE_SUFFIX = "...";
  public static final String MATCH_VAR = "match$";
  public static final String MATCH_PARENT_VAR = "match_parent$";
  public static final String MATCH_POSITION_VAR = "match_position$";
  public static final String MATCH_MAP_VAR = "match_map$";
  
  private static final Object TOP = new Object();
  private static final Object BEGIN_OF_LIST = new Object();
  private static final Object END_OF_LIST = new Object();

  @Override
  public Object invoke(Context context, BList args) throws Exception
  {
    Utils.checkArguments(args, 2);    
    BList list = (BList)context.evaluate(args.get(1));
    BList pattern = (BList)context.evaluate(args.get(2));
    Object expression;
    if (args.size() >= 4)
    {
      expression = args.get(3);
    }
    else
    {
      expression = true;
    }    
    boolean stop = match(list, pattern, null, -1, context, expression);
    if (!stop)
    {
      stop = matchChildren(list, pattern, context, expression);
    }
    return stop;
  }
  
  boolean executeAction(Context context, Object action, Candidate candidate, 
    BList parent, int position) throws Exception
  {
    context.getLocalScope().addAll(candidate.map);
    context.setLocal(MATCH_VAR, candidate.list);
    context.setLocal(MATCH_PARENT_VAR, parent);
    context.setLocal(MATCH_POSITION_VAR, position);
    context.setLocal(MATCH_MAP_VAR, candidate.map);

    return Utils.toBoolean(context.evaluate(action));
  }
  
  boolean matchChildren(BList parent, BList pattern, 
    Context context, Object expression) throws Exception
  {
    int i = 0;
    boolean stop = false;
    while (i < parent.size() && !stop)
    {
      Object object = parent.get(i);
      if (object instanceof BList)
      {
        BList list = (BList)object;   
        stop = match(list, pattern, parent, i, context, expression);
        if (!stop)
        {
          stop = matchChildren(list, pattern, context, expression);
        }
      }
      i++;
    }
    return stop;
  }

  boolean match(BList list, BList pattern, BList parent, int position, 
     Context context, Object expression) throws Exception
  {
    boolean stop = false;
    Stack<Candidate> candidates = new Stack<Candidate>();
    Candidate candidate = new Candidate(list, pattern);
    candidates.push(candidate);
    while (!candidates.isEmpty() && !stop && !Thread.interrupted())
    {
      candidate = candidates.pop();
      if (match(candidate, candidates))
      {
        stop = executeAction(context, expression, candidate, parent, position);
      }
    }
    return stop;
  }

  boolean match(Candidate candidate, Stack<Candidate> candidates)
  {    
    ListPath listPath = candidate.listPath;
    ListPath patternPath = candidate.patternPath;
    BList map = candidate.map;

    boolean match = true;
    boolean end = false;
    do
    {
      Object listObject = listPath.getCurrentObject();
      Object patternObject = patternPath.getCurrentObject();
      if (listObject == TOP && patternObject == TOP)
      {
        end = true;
      }
      else if (listObject == END_OF_LIST && patternObject == END_OF_LIST)
      {
        //System.out.println("UP");
        match = listPath.moveUp() == patternPath.moveUp();
        if (match)
        {
          listPath.moveNext();
          patternPath.moveNext();          
        }
      }
      else if (listObject instanceof BList && patternObject instanceof BList)
      {
        //System.out.println("DOWN");
        listPath.moveDown();
        patternPath.moveDown();
        listPath.moveNext();
        patternPath.moveNext();
      }
      else if (listObject != BEGIN_OF_LIST &&
               listObject != END_OF_LIST &&
               isSingleVariable(patternObject))
      {
        BSoftReference variable = (BSoftReference)patternObject;
        String variableName = variable.getName();
        if (map.has(variableName))
        {
          match = Utils.equals(listObject, map.get(variableName));
          //System.out.println(variableName + " => " + objectA);
        }
        else
        {
          map.put(variableName, listObject);
          //System.out.println("set " + variableName + " => " + objectA);
        }
        listPath.moveNext();
        patternPath.moveNext();
      }
      else if (isListVariable(patternObject))
      {
        BSoftReference variable = (BSoftReference)patternObject;
        String variableName = variable.getName();
        if (map.has(variableName))
        {
          match = false; // Compare values
        }
        else
        {
          int pos = listPath.getCurrentPosition();
          int listSize = listPath.getCurrentList().size();
          for (int i = pos; i <= listSize; i++)
          {
            Candidate newCandidate = new Candidate(candidate);
            newCandidate.listPath.move(i - pos);
            newCandidate.patternPath.moveNext();
            BList sublist = listPath.getCurrentList().sublist(pos, i);
            newCandidate.map.put(variableName, sublist);
            candidates.push(newCandidate);
          }
          match = false;
        }
      }
      else if (Utils.equals(listObject, patternObject))
      {
        listPath.moveNext();
        patternPath.moveNext();        
      }
      else
      {
        match = false;
      }
    } while (match && !end);

    return match;
  }

  boolean isSingleVariable(Object value)
  {
    if (value instanceof BSoftReference)
    {
      BSoftReference reference = (BSoftReference)value;
      return reference.getName().endsWith(SINGLE_VARIABLE_SUFFIX);
    }
    return false;
  }

  boolean isListVariable(Object value)
  {
    if (value instanceof BSoftReference)
    {
      BSoftReference reference = (BSoftReference)value;
      return reference.getName().endsWith(LIST_VARIABLE_SUFFIX);
    }
    return false;
  }

  class Candidate
  {
    BList list;
    ListPath listPath;
    ListPath patternPath;
    BList map;

    Candidate(Candidate candidate)
    {
      this.list = candidate.list;
      this.listPath = candidate.listPath.clone();
      this.patternPath = candidate.patternPath.clone();
      this.map = candidate.map.clone();
    }

    Candidate(BList list, BList pattern)
    {
      this(list, pattern, new BList());
    }

    Candidate(BList list, BList pattern, BList map)
    {
      this.list = list;
      this.listPath = new ListPath(list);
      this.patternPath = new ListPath(pattern);
      this.map = map;
    }
  }

  class ListPath implements Cloneable
  {
    Stack<Pair> path = new Stack<Pair>();

    ListPath()
    {
    }

    ListPath(BList list)
    {
      path.push(new Pair(list, -1));
    }

    public BList getCurrentList()
    {
      if (path.isEmpty()) return null;
      return path.peek().list;
    }

    public int getCurrentPosition()
    {
      if (path.isEmpty()) return -1;
      return path.peek().position;
    }

    Object getCurrentObject()
    {
      if (path.isEmpty()) return TOP;
      return path.peek().getObject();
    }

    boolean move(int count)
    {
      if (path.isEmpty()) return false;
      Pair pair = path.peek();
      pair.position += count;
      int size = pair.list.size();
      if (pair.position > size)
      {
        pair.position = size;
      }
      return pair.position < size;
    }

    boolean moveNext()
    {
      return move(1);
    }

    boolean moveDown()
    {
      if (path.isEmpty()) return false;
      Pair pair = path.peek();
      Object object = pair.getObject();
      if (object instanceof BList)
      {
        BList list = (BList)object;
        path.push(new Pair(list, -1));
        return true;
      }
      return false;
    }

    boolean moveUp()
    {
      if (path.isEmpty()) return false;
      path.pop();
      return !path.isEmpty();
    }

    @Override
    public ListPath clone()
    {
      ListPath clone = new ListPath();
      for (int i = 0; i < path.size(); i++)
      {
        Pair pair = path.get(i);
        clone.path.push(new Pair(pair.list, pair.position));
      }
      return clone;
    }

    class Pair
    {
      Pair(BList list, int position)
      {
        this.list = list;
        this.position = position;
      }
      BList list;
      int position;

      Object getObject()
      {
        if (position < 0) return BEGIN_OF_LIST;
        if (position >= list.size()) return END_OF_LIST;
        return list.get(position);
      }
    }
  }
}




