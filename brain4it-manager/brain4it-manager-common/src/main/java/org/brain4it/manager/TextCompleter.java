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
package org.brain4it.manager;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.brain4it.client.RestClient;
import org.brain4it.client.RestClient.Callback;
import org.brain4it.io.IOConstants;
import org.brain4it.io.Parser;
import org.brain4it.lang.BList;
import org.brain4it.lang.BSoftReference;
import org.brain4it.lang.Utils;

/**
 *
 * @author realor
 */
public class TextCompleter
{
  private final Module module;
  private static final String FIND_COMMAND = 
  "(call " + 
  "  (function (lst head)" +
  "    (if (= (type-of lst) \"list\") " +
  "      (apply " +
  "        (find (names lst) x (= (locate-string x head) 0)) " +
  "        n " +
  "        (list n (subtype-of (get lst n))) " +
  "      ) " +
  "      ()" +
  "    )" +
  "  ) ";

  public TextCompleter(Module module)
  {
    this.module = module;
  }

  public Module getModule()
  {
    return module;
  }

  public void complete(final String head, final OnCompleteListener listener)
  {
    final List<Candidate> candidates = new ArrayList<Candidate>();
    final String lastHead; 
    String command;

    int index = head.lastIndexOf(IOConstants.PATH_REFERENCE_SEPARATOR);
    if (index == -1)
    {
      Set<String> functionNames = module.getFunctionNames();    
      if (functionNames != null)
      {
        for (String functionName : functionNames)
        {
          if (functionName.startsWith(head) && !functionName.equals(head))
          {
            candidates.add(
              new Candidate(functionName, Utils.HARD_REFERENCE_SUBTYPE));
          }
        }
      }
      lastHead = head;
      command = FIND_COMMAND + "(global-scope)";
    }
    else // containts path separator
    {
      String path = head.substring(0, index);
      lastHead = head.substring(index + 1);
      if (path.length() == 0)
      {
        command = FIND_COMMAND + "(global-scope)";
      }
      else
      {
        command = FIND_COMMAND + path;
      }
    }

    command += " \"" + Utils.escapeString(lastHead) + "\")";

    RestClient restClient = module.getRestClient();
    restClient.execute(module.getName(), command, new Callback()
    {
      @Override
      public void onSuccess(RestClient client, String resultString)
      {
        try
        {
          Object result = Parser.fromString(resultString);
          BList options = (BList)result;
          for (int i = 0; i < options.size(); i++)
          {
            BList option = (BList)options.get(i);
            String name = (String)option.get(0);
            String type = (String)option.get(1);
            if (BSoftReference.needEscape(name))
            {
              if (head.endsWith(
                String.valueOf(IOConstants.PATH_REFERENCE_SEPARATOR)))
              {
                name = "\"" + Utils.escapeString(name) + "\"";
                candidates.add(new Candidate(name, type));              
              }
            }
            else
            {
              if (!name.equals(head))
              {
                candidates.add(new Candidate(name, type));
              }
            }
          }
          Collections.sort(candidates);
          listener.textCompleted(head, candidates);
        }
        catch (ParseException ex)
        {
        }
      }

      @Override
      public void onError(RestClient client, Exception ex)
      {
      }
    });
  }
  
  public String findCommonHead(List<Candidate> candidates)
  {
    // candidates.size() > 1
    int min = Integer.MAX_VALUE;
    for (Candidate candidate : candidates)
    {
      if (candidate.getName().length() < min)
      {
        min = candidate.getName().length();
      }
    }
    Candidate firstCandidate = candidates.get(0);
    boolean common = true;
    int i = 0;
    while (i < min && common)
    {
      char ch = firstCandidate.getName().charAt(i);
      int j = 1;
      while (common && j < candidates.size())
      {
        common = (ch == candidates.get(j).getName().charAt(i));
        j++;
      }
      if (common) i++;
    }
    return firstCandidate.getName().substring(0, i);
  }
  
  public class Candidate implements Comparable
  {
    private final String name;
    private final String type;

    Candidate(String name, String type)
    {
      this.name = name;
      this.type = type;
    }
    
    public String getName()
    {
      return name;
    }

    public String getType()
    {
      return type;
    }

    @Override
    public int compareTo(Object o)
    {
      Candidate other = (Candidate)o;
      return name.compareTo(other.name);
    }
    
    @Override
    public String toString()
    {
      return "[" + name + ", " + type + "]";
    }
  }
  
  public interface OnCompleteListener
  {
    void textCompleted(String head, List<Candidate> candidates);
  }
}
