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

package org.brain4it.test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.brain4it.io.Parser;
import org.brain4it.lang.BList;
import org.brain4it.lang.Context;
import org.brain4it.lang.Executor;
import org.brain4it.lang.Function;
import org.brain4it.lib.Library;
import org.junit.AfterClass;

/**
 *
 * @author realor
 */
public abstract class LibraryTest
{
  private static final ArrayList<Library> LIBRARIES = 
    new ArrayList<Library>();
  private static final Map<String, Function> FUNCTIONS = 
    new HashMap<String, Function>();
  
  public static final void loadLibrary(Library library)
  {
    try
    {
      LIBRARIES.add(library);
      library.load();
      FUNCTIONS.putAll(library.getFunctions());
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }            
  }
  
  @AfterClass
  public final static void unloadLibraries()
  {
    for (Library library : LIBRARIES)
    {
      library.unload();
    }
    LIBRARIES.clear();
    FUNCTIONS.clear();
  }  
  
  public static void testFile(String filename) throws Exception
  {
    Executor.init();
    BList globalScope = new BList();    
    Context context = new Context(globalScope, FUNCTIONS);
    InputStream is = LibraryTest.class.getResourceAsStream(filename);
    try
    {
      Parser parser = new Parser(new InputStreamReader(is), 
        context.getFunctions());
      Object code = parser.parse();
      context.evaluate(code);
    }
    finally
    {
      Executor.shutdown();
      is.close();
    }
  }
}
