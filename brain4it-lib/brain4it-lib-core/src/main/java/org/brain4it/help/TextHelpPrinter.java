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

package org.brain4it.help;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.List;
import java.util.Locale;
import org.brain4it.lang.BList;
import org.brain4it.lang.Context;
import org.brain4it.lib.Library;
import org.brain4it.lib.LibraryFactory;
/**
 *
 * @author realor
 */
public class TextHelpPrinter extends HelpPrinter
{
  protected PrintWriter writer;

  public TextHelpPrinter(Writer writer, Context context, Locale locale)
  {
    super(context, locale);
    this.writer = new PrintWriter(writer);
  }
  
  @Override
  protected void beginBlock(int level, String type, String title, Object value)
  {
    if (type.equals(LIBRARY))
    {
      writer.println("===" + value + "===");
    }
    else if (type.equals(FUNCTION))
    {
      writer.println(value);
    }
    else if (type.equals(SYNOPSIS))
    {
    }
    else if (type.equals(ARGUMENT) || type.equals(EXCEPTION))
    {
      writer.print("- ");
    }
    else if (type.equals(USAGE) || type.equals(RETURNS))
    {
      writer.print(title + ": ");
    }
    else
    {
      writer.println(title + ":");
    }
  }

  @Override
  protected void endBlock(int level, String type)
  {
    if (!type.equals(WHERE) &&             
        !type.equals(FUNCTIONS) && 
        !type.equals(FUNCTION) &&
        !type.equals(EXCEPTIONS) &&
        !type.equals(EXAMPLES))
    {
      writer.println();
    }
  }  

  @Override
  protected void printLibraryIndex(List<BList> helpList)
  {
    writer.println(getLocalizedText(INDEX) + ":");
    int lineLength = 0;
    for (BList help : helpList)
    {
      String functionName = getFunctionName(help);
      if (functionName.length() + lineLength > 80)
      {
        writer.println();
        lineLength = 0;
      }
      writer.print(functionName + " ");
      lineLength += functionName.length() + 1;
    }
    writer.println();
    writer.println();
  }

  @Override
  protected void printExample(BList example) throws IOException
  {
    Object code = example.get(0);    
    printText("> ");
    printCode(code);
    printText("\n");

    if (example.size() > 1)
    {
      code = example.get(1);
      printCode(code);
      printText("\n");
    }
  }
  
  @Override
  protected void printText(String text)
  {
    writer.print(text);
  }
  
  @Override
  protected void printSeparator()
  {
    writer.println("----------------------------------------");
  }  
  
  public static void main(String[] args) throws Exception
  {
    if (args.length >= 2)
    {
      String libraries = args[0];
      String directory = args[1];

      String[] libraryNames = libraries.split(",");
      for (String libraryName : libraryNames)
      {
        PrintWriter writer = new PrintWriter(directory + "/library_" + 
          libraryName.toLowerCase() + ".txt", "UTF-8");
        TextHelpPrinter printer = new TextHelpPrinter(
          writer, null, Locale.getDefault());
        Class<? extends Library> libraryClass =
          LibraryFactory.getLibraryClass(libraryName);
        System.out.println("Generating doc for " + libraryName + "...");
        printer.printLibrary(libraryClass);
        writer.flush();
        System.out.println();
      }
      System.out.println("done.");
    }
    else
    {
      System.out.println("arguments: <libraries> <output_dir>");        
    }
  }
}
