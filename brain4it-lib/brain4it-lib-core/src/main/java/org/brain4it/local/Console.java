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

package org.brain4it.local;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import org.brain4it.lang.Context;
import org.brain4it.lang.BList;
import org.brain4it.io.Parser;
import org.brain4it.io.Printer;
import org.brain4it.lang.Executor;
import org.brain4it.lang.Function;
import org.brain4it.lang.Utils;
import org.brain4it.lib.CoreLibrary;
import org.brain4it.lib.Library;
import org.brain4it.lib.LibraryFactory;

/**
 *
 * @author realor
 */
public class Console
{
  protected Context context;
  protected String prompt = "> ";
  protected String charset = "ISO-8859-1";
  protected ArrayList<Library> libraries = new ArrayList<Library>();
  protected HashMap<String, Function> functions = 
    new HashMap<String, Function>();

  public Console()
  {
    BList scope = new BList();
    scope.put("global-scope", scope);
    Library library = new CoreLibrary();
    libraries.add(library);
  }

  public void addLibrary(String libraryClassName) throws Exception
  {
    libraries.add(LibraryFactory.createLibrary(libraryClassName));
  }
  
  protected void loadLibraries()
  {
    for (Library library : libraries)
    {
      library.load();
      functions.putAll(library.getFunctions());
    }
  }

  protected void unloadLibraries()
  {
    for (Library library : libraries)
    {
      library.unload();
    }
  }
    
  public void run() throws Exception
  {
    loadLibraries();

    Executor.init();
    
    BList globalScope = new BList();
    
    BufferedReader reader = new BufferedReader(
      new InputStreamReader(System.in, charset));
    try
    {
      System.out.print(prompt);
      String line = reader.readLine();
      while (line != null && line.length() > 0)
      {
        try
        {
          Object code = Parser.fromString(line, functions);
          Object result = Executor.execute(code, globalScope, functions, 30);
          if (result instanceof String)
          {
            System.out.println(result);
          }
          else
          {
            System.out.println(Printer.toString(result));
          }
        }
        catch (Exception ex)
        {
          BList list = Utils.toBList(ex);
          System.out.println(Printer.toString(list));
        }
        System.out.print(prompt);
        line = reader.readLine();
      }
    }
    finally
    {
      reader.close();
      unloadLibraries();
      Executor.shutdown();
    }
  }

  public static void main(String[] args) throws Exception
  {
    Console console = new Console();
    for (String libraryClassName : args)
    {
      console.addLibrary(libraryClassName);
    }
    console.run();
  }
}
