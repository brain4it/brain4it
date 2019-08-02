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
import java.io.StringReader;
import java.io.Writer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import org.brain4it.lang.BList;
import org.brain4it.lang.Context;
import org.brain4it.lib.Library;
import org.brain4it.lib.LibraryFactory;
import org.brain4it.io.HTMLFormatter;
import org.brain4it.io.Printer;
import static org.brain4it.help.HelpPrinter.ARGUMENT;

/**
 *
 * @author realor
 */
public class HTMLHelpPrinter extends TextHelpPrinter
{
  protected static final String HEADER =
    "<html>\n<head>\n<title>{title}</title>\n" +
    "<link rel=\"stylesheet\" type=\"text/css\" href=\"{css}\">\n" +
    "</head>\n<body>";
  protected static final String FOOTER = "</body\n></html>";
  protected final HTMLFormatter codeFormatter;
  protected String cssName = "brain4it.css";

  public HTMLHelpPrinter(Writer writer, Context context, Locale locale)
  {
    super(writer, context, locale);
    codeFormatter = new HTMLFormatter();
  }

  public String getCssName()
  {
    return cssName;
  }

  public void setCssName(String cssName)
  {
    this.cssName = cssName;
  }

  @Override
  public void printLibrary(Class<? extends Library> libraryClass)
    throws IOException, ParseException
  {
    printDocumentHeader(libraryClass.getSimpleName());
    super.printLibrary(libraryClass);
    printDocumentFooter();
  }

  public void printDocumentHeader(String title)
  {
    String header = HEADER.replace("{title}", title);
    header = header.replace("{css}", cssName);
    writer.write(header);
  }

  public void printDocumentFooter()
  {
    writer.write(FOOTER);
  }

  @Override
  protected void printSeparator()
  {
  }

  @Override
  protected void printLibraryIndex(List<BList> helpList)
  {
    writer.println("<h2>" + getLocalizedText(INDEX) + ":</h2>");

    // build function groups
    HashSet<String> groupSet = 
      new HashSet<String>();
    for (BList help : helpList)
    {
      String group = (String)help.get(HelpBuilder.GROUP);
      if (group != null)
      {
        groupSet.add(group);
      }
    }
    if (groupSet.size() > 0)
    {
      ArrayList<String> groups = new ArrayList<String>(groupSet);
      Collections.sort(groups);
      writer.write("<div class=\"groups\">");
      writer.write("<a href=\"#\" class=\"selected\" " + 
        "onclick=\"filterGroup(this);\">ALL</a> ");

      for (String group : groups)
      {
        writer.write("<a href=\"#\" onclick=\"filterGroup(this);\">" + 
         group + "</a> ");
      }
      writer.write("</div>");
    }

    // function index
    writer.println("<div class=\"index\">");
    for (BList help  : helpList)
    {
      String group = (String)help.get(HelpBuilder.GROUP);
      String className = "function";
      if (group != null) className += " " + group;
      String functionName = getFunctionName(help);
      String functionId = getFunctionId(functionName);
      writer.print("<a id=\"" + functionId + "\" href=\"#an_" + 
        functionId + "\" class=\"" + className + "\">" +
        functionName + "</a> ");
    }
    writer.println("</div>"); // end index
    writer.println("<script type=\"text/javascript\">");
    writer.print("function showFunc(funcId) { " +
     "var elem = document.getElementById('fn_'+funcId);" +
     "elem.style.display=null;return true;}\n;" + 
     "function filterGroup(elem) {" + 
     "var groupsElem = document.getElementsByClassName('groups')[0];" +
     "var elems = groupsElem.getElementsByTagName('a');" +
     "for (var i = 0; i < elems.length; i++) elems[i].className='';" + 
     "elem.className='selected';\n" +
     "var group = elem.innerHTML; if (group == 'ALL') group = 'function';" +
     "var indexElem = document.getElementsByClassName('index')[0];" +
     "var fnElems = indexElem.getElementsByClassName('function');" +
     "for (var i = 0; i < fnElems.length; i++) {" +
     "var fnElem = fnElems[i];" +
     "var display = (fnElem.className.indexOf(group) == -1) ? 'none' : null;" +
     "fnElem.style.display=display; " +
     "document.getElementById('fn_' + fnElem.id).style.display=display;}};");
    writer.println("</script>");
  }

  @Override
  protected void beginBlock(int level, String type, String title, Object value)
  {
    // block header (h1, h2, h3 or div)
    if (type.equals(LIBRARY))
    {
      String libraryName = String.valueOf(value);
      int index = libraryName.indexOf("Library");
      libraryName = libraryName.substring(0, index);
      writer.println("<h1>" + libraryName + " library</h1>");
    }
    else if (type.equals(FUNCTIONS))
    {
      writer.print("<h2>");
      writer.print(title);
      writer.println(":</h2>");
    }
    else if (level == 3)
    {
      writer.print("<div class=\"section\">");
      writer.print(title);
      writer.println("</div>");
    }

    // block start
    if (type.equals(WHERE) || type.equals(EXCEPTIONS))
    {
      writer.println("<ul class=\"" + type + "\">");
    }
    else if (type.equals(FUNCTION))
    {
      String functionName = (String)value;
      String functionId = getFunctionId(functionName);
      writer.println("<div id=\"fn_" + functionId + 
        "\" class=\"" + type + "\">");
      writer.println("<a name=\"an_" + functionId + "\">" + 
        functionName + "</a>");
      writer.println("<h3 class=\"code\">" + functionName + "</h3>");
    }
    else if (type.equals(ARGUMENT) || type.equals(EXCEPTION))
    {
      writer.println("<li class=\"" + type + "\">");
    }
    else
    {
      writer.println("<div class=\"" + type + "\">");
    }
  }

   @Override
  protected void endBlock(int level, String type)
  {
    if (type.equals(WHERE) || type.equals(EXCEPTIONS))
    {
      writer.println("</ul>");
    }
    else if (type.equals(ARGUMENT) || type.equals(EXCEPTION))
    {
      writer.println("</li>");
    }
    else
    {
      writer.println("</div>");
    }
  }
  
  protected String getFunctionId(String functionName)
  {
    StringBuilder buffer = new StringBuilder();
    for (int i = 0; i < functionName.length(); i++)
    {
      char ch = functionName.charAt(i);
      if (Character.isLetterOrDigit(ch) || ch == '-') buffer.append(ch);
      else buffer.append("u").append(Integer.toHexString((int)ch));
    }
    return buffer.toString();
  }

  @Override
  protected void printExample(BList example) throws IOException
  {
    Object code = example.get(0);
    writer.print("<div class=\"example\">");
    
    writer.print("<div class=\"input\">");
    printCode(code, true);
    writer.print("</div>");

    if (example.size() > 1)
    {
      code = example.get(1);
      writer.print("<div class=\"output\">");
      printCode(code, true);
      writer.print("</div>");
    }
    writer.print("</div>");
  }

  @Override
  protected void printRelatedFunction(int index, String functionName) 
    throws IOException
  {
    String functionId = getFunctionId(functionName);
    writer.println("<a href=\"#an_" + functionId + "\" class=\"code\"" + 
    " onclick=\"return showFunc('" + functionId + "');\">" + 
      functionName + "</a>");
  }
  
  @Override
  protected void printCode(Object code, boolean highlightFunctions)
  {
    String codeString = Printer.toString(code);
    try
    {
      writer.write("<span class=\"code\">");
      codeFormatter.setHighlightFunctions(highlightFunctions);
      codeFormatter.format(new StringReader(codeString), writer);
      writer.write("</span>");
    }
    catch (Exception ex)
    {
    }
  }

  @Override
  protected void printCodeText(String text)
  {
    writer.print("<span class=\"code\">");
    writer.print(text);
    writer.print("</span>");
  }
  
  @Override
  protected void printText(String text)
  {
    text = text.replace("\n", "<br>");
    int index = 0;
    int index2 = text.indexOf("<");
    while (index2 != -1)
    {
      int index3 = text.indexOf(">", index2);
      if (index3 != -1)
      {
        String fragment = text.substring(index2 + 1, index3);
        if (!fragment.startsWith(" ") && !fragment.equals("br"))
        {
          writer.print(text.substring(index, index2));
          printStyledText(fragment);
          index = index3 + 1;
        }
        else
        {
          writer.print(text.substring(index, index2 + 1));
          index = index2 + 1;
        }
      }
      index2 = text.indexOf("<", index);
    }
    writer.print(text.substring(index));
  }

  protected void printStyledText(String text)
  {
    if (text.startsWith("http"))
    {
      writer.write("<a href=\"" + text + "\" target=\"_blank\">");
      writer.write(text);
      writer.write("</a>");
    }
    else
    {
      writer.write("<span class=\"code\">");
      writer.write(text);
      writer.write("</span>");
    }
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
          libraryName.toLowerCase() + ".html", "UTF-8");
        HTMLHelpPrinter printer = new HTMLHelpPrinter(
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
      System.out.println(
        "arguments: <libraries> <output_dir>");
    }
  }
}
