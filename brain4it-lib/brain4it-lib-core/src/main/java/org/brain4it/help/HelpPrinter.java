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
import java.text.ParseException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import org.brain4it.io.Printer;
import org.brain4it.lang.BSoftReference;
import org.brain4it.lang.BList;
import org.brain4it.lang.BSingleReference;
import org.brain4it.lang.Context;
import org.brain4it.lang.Utils;
import org.brain4it.lib.Library;

/**
 *
 * @author realor
 */
public abstract class HelpPrinter
{
  public static final String LIBRARY = "library";
  public static final String INDEX = "index";
  public static final String FUNCTIONS = "functions";
  public static final String FUNCTION = "function";
  public static final String SYNOPSIS = "synopsis";
  public static final String DESCRIPTION = "description";
  public static final String EXAMPLES = "examples";
  public static final String EXAMPLE = "example";
  public static final String EXAMPLE_INPUT = "example_input";
  public static final String EXAMPLE_OUTPUT = "example_output";
  public static final String RELATED = "related";
  public static final String ARGUMENT = "argument";
  public static final String USAGE = "usage";
  public static final String EXCEPTIONS = "exceptions";
  public static final String EXCEPTION = "exception";
  public static final String RETURNS = "returns";
  public static final String WHERE = "where";
  public static final String IS = "is";

  private Locale locale;
  private ResourceBundle helpBundle;
  private Context context;

  public HelpPrinter()
  {
    this(Locale.getDefault());
  }

  public HelpPrinter(Locale locale)
  {
    this.locale = locale;
    String pkg = HelpPrinter.class.getPackage().getName().replace('.', '/');
    helpBundle = ResourceBundle.getBundle(pkg + "/HelpMessages", locale);
  }

  public HelpPrinter(Context context, Locale locale)
  {
    this(locale);
    this.context = context;
  }

  public Context getContext()
  {
    return context;
  }

  public Locale getLocale()
  {
    return locale;
  }

  public void printFunction(String functionName)
    throws IOException, ParseException
  {
    if (context != null)
    {
      BList help = HelpBuilder.buildHelp(context, functionName, locale);
      if (help != null)
      {
        printFunction(functionName, help);
      }
    }
  }

  public void printLibrary(Class<? extends Library> libraryClass)
    throws IOException, ParseException
  {
    List<BList> helpList = LibraryHelpBuilder.buildHelp(libraryClass, locale);
    sortLibraryFunctions(helpList);

    beginBlock(0, LIBRARY, getLocalizedText(LIBRARY),
      libraryClass.getSimpleName());
    printLibraryIndex(helpList);
    printLibraryfunctions(helpList);
    endBlock(0, LIBRARY);
  }

  protected void sortLibraryFunctions(List<BList> helpList)
  {
    Collections.sort(helpList, new Comparator<BList>()
    {
      @Override
      public int compare(BList list1, BList list2)
      {
        return getFunctionName(list1).compareTo(getFunctionName(list2));
      }
    });
  }

  protected void printLibraryIndex(List<BList> helpList)
  {
    beginBlock(1, INDEX, getLocalizedText(INDEX), helpList);
    for (BList help : helpList)
    {
      String functionName = getFunctionName(help);
      printFunctionReference(functionName);
    }
    endBlock(1, INDEX);
  }

  protected void printFunctionReference(String functionName)
  {
  }

  protected void printLibraryfunctions(List<BList> helpList)
    throws IOException, ParseException
  {
    beginBlock(1, FUNCTIONS, getLocalizedText(FUNCTIONS), helpList);
    for (BList help : helpList)
    {
      String functionName = getFunctionName(help);
      printFunction(functionName, help);
      printSeparator();
    }
    endBlock(1, FUNCTIONS);
  }

  protected void printFunction(String functionName, BList help)
    throws IOException
  {
    beginBlock(2, FUNCTION, getLocalizedText(FUNCTION), functionName);

    String synopsis = (String)help.get(HelpBuilder.SYNOPSIS);
    if (synopsis != null)
    {
      printSynopsis(synopsis);
    }
    printUsage(functionName, help);

    printReturns(functionName, help);

    BList where = (BList)help.get(HelpBuilder.WHERE);
    if (where != null)
    {
      printWhere(functionName, where);
    }

    String description = (String)help.get(HelpBuilder.DESCRIPTION);
    if (description != null)
    {
      printDescription(description);
    }

    BList exceptions = (BList)help.get(HelpBuilder.EXCEPTIONS);
    if (exceptions != null)
    {
      printExceptions(exceptions);
    }

    BList examples = (BList)help.get(HelpBuilder.EXAMPLES);
    if (examples != null)
    {
      printExamples(examples);
    }

    BList related = (BList)help.get(HelpBuilder.RELATED);
    if (related != null)
    {
      printRelated(related);
    }
    endBlock(2, FUNCTION);
  }

  protected void printSynopsis(String synopsis) throws IOException
  {
    beginBlock(3, SYNOPSIS, getLocalizedText(SYNOPSIS), synopsis);
    printText(synopsis);
    endBlock(3, SYNOPSIS);
  }

  protected void printUsage(String functionName, BList help) throws IOException
  {
    beginBlock(3, USAGE, getLocalizedText(USAGE), help);
    Object usage = help.get(1);
    BList where = (BList)help.get(HelpBuilder.WHERE);
    usage = transformDefinitionCode(usage, where);
    printCode(usage, true);
    endBlock(3, USAGE);
  }

  protected void printReturns(String functionName, BList help)
    throws IOException
  {
    beginBlock(3, RETURNS, getLocalizedText(RETURNS), help);
    Object returns = help.get(2);
    BList where = (BList)help.get(HelpBuilder.WHERE);
    returns = transformDefinitionCode(returns, where);
    printCode(returns);
    endBlock(3, RETURNS);
  }

  protected void printWhere(String functionName, BList where) throws IOException
  {
    beginBlock(3, WHERE, getLocalizedText(WHERE), where);
    for (int i = 0; i < where.size(); i++)
    {
      String name = where.getName(i);
      Object value = where.get(i);
      if (name != null && value instanceof BList)
      {
        BList argument = (BList)where.get(i);
        printArgument(name, argument, where);
      }
    }
    endBlock(3, WHERE);
  }

  protected void printArgument(String argumentName, BList argument, BList where)
    throws IOException
  {
    beginBlock(4, ARGUMENT, getLocalizedText(ARGUMENT), argument);
    printCodeText(argumentName);
    printText(" ");
    printText(getLocalizedText(IS));
    printText(" ");
    Object argumentType = argument.get(0);
    if (argumentType instanceof String)
    {
      printCodeText((String)argumentType);
    }
    else
    {
      argumentType = transformDefinitionCode(argumentType, where);
      printCode(argumentType);
    }
    String minOcurrs = Utils.toString(argument.get(1));
    String maxOcurrs = Utils.toString(argument.get(2));
    if (!"1".equals(minOcurrs) || !"1".equals(maxOcurrs))
    {
      printCodeText("[" + minOcurrs + ".." + maxOcurrs + "]");
    }
    if (argument.size() >= 4)
    {
      String argDescription = (String)argument.get(3);
      printText(": " + argDescription);
    }
    endBlock(4, ARGUMENT);
  }

  protected void printExceptions(BList exceptions)
  {
    beginBlock(3, EXCEPTIONS, getLocalizedText(EXCEPTIONS), exceptions);
    for (int i = 0; i < exceptions.size(); i++)
    {
      BList exception = (BList)exceptions.get(i);
      printException(exception);
    }
    endBlock(3, EXCEPTIONS);
  }

  protected void printException(BList exception)
  {
    beginBlock(4, EXCEPTION, getLocalizedText(EXCEPTION), exception);
    String type = (String)exception.get(0);
    printCode(type);
    if (exception.size() > 1)
    {
      String exDescription = (String)exception.get(1);
      printText(": " + exDescription);
    }
    endBlock(4, EXCEPTION);
  }

  protected void printDescription(String description)
    throws IOException
  {
    beginBlock(3, DESCRIPTION, getLocalizedText(DESCRIPTION), description);
    printText(description);
    endBlock(3, DESCRIPTION);
  }

  protected void printExamples(BList examples) throws IOException
  {
    beginBlock(3, EXAMPLES, getLocalizedText(EXAMPLES), examples);
    for (int i = 0; i < examples.size(); i++)
    {
      printExample((BList)examples.get(i));
    }
    endBlock(3, EXAMPLES);
  }

  protected void printExample(BList example) throws IOException
  {
    beginBlock(4, EXAMPLE, getLocalizedText(EXAMPLE), example);

    Object code = example.get(0);
    beginBlock(5, EXAMPLE_INPUT, getLocalizedText(EXAMPLE_INPUT), code);
    printCode(code, true);
    endBlock(5, EXAMPLE_INPUT);

    if (example.size() > 1)
    {
      code = example.get(1);
      beginBlock(5, EXAMPLE_OUTPUT, getLocalizedText(EXAMPLE_OUTPUT), code);
      printCode(code, true);
      endBlock(5, EXAMPLE_OUTPUT);
    }
    endBlock(4, EXAMPLE);
  }

  protected void printRelated(BList related) throws IOException
  {
    beginBlock(3, RELATED, getLocalizedText(RELATED), related);
    for (int i = 0; i < related.size(); i++)
    {
      String functionName = Utils.toString(related.get(i));
      printRelatedFunction(i, functionName);
    }
    endBlock(3, RELATED);
  }

  protected void printRelatedFunction(int index, String functionName)
    throws IOException
  {
    if (index > 0) printText(", ");
    printCodeText(functionName);
  }

  protected String getLocalizedText(String text)
  {
    return helpBundle.getString(text);
  }

  protected final String getFunctionName(BList help)
  {
    BList parameters = (BList)help.get(1);
    return Utils.toString(parameters.get(0));
  }

  protected Object transformDefinitionCode(Object code, BList where)
  {
    if (code instanceof BList)
    {
      BList list = (BList)code;
      BList transformed = new BList();
      for (int i = 0; i < list.size(); i++)
      {
        Object elem = list.get(i);
        if (elem instanceof BSoftReference)
        {
          transformed.add(elem);
          if (where != null)
          {
            String argument = ((BSoftReference)elem).getName();
            BList definition = (BList)where.get(argument);
            if (definition != null)
            {
              String maxOcurrs = Utils.toString(definition.get(2));
              if (!"1".equals(maxOcurrs))
              {
                if (!"2".equals(maxOcurrs))
                {
                  transformed.add(new BSingleReference("..."));
                }
                transformed.add(elem);
              }
            }
          }
        }
        else if (elem instanceof BList)
        {
          transformed.add(transformDefinitionCode(elem, where));
        }
        else
        {
          transformed.add(elem);
        }
        String name = list.getName(i);
        transformed.putName(i, name);
      }
      return transformed;
    }
    return code;
  }

  protected void printCode(Object code)
  {
    printCode(code, false);
  }

  protected void printCode(Object code, boolean hilighFunctions)
  {
    printCodeText(Printer.toString(code));
  }

  protected void printCodeText(String code)
  {
    printText(code);
  }

  protected abstract void beginBlock(int level, String type, String title,
    Object value);

  protected abstract void endBlock(int level, String type);

  protected abstract void printText(String text);

  protected abstract void printSeparator();
}
