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

import java.io.File;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Locale;
import org.brain4it.lang.Context;
import org.brain4it.lib.Library;
import org.brain4it.lib.LibraryFactory;

/**
 *
 * @author realor
 */
public class WebHelpPrinter extends HTMLHelpPrinter
{
  public WebHelpPrinter(Writer writer, Context context, Locale locale)
  {
    super(writer, context, locale);
    codeFormatter.getConfiguration().setMaxColumns(50);
  }

  @Override
  public void printDocumentHeader(String title)
  {
    writer.write(
      "<!DOCTYPE html>\n" +
      "<html>\n" +
      "<head><title></title></head>\n" +
      "<body>\n" +
      "{--title--}" + title + " - Brain4it{----}\n" +
      "{--body--}\n");
  }

  @Override
  public void printDocumentFooter()
  {
    writer.write(
      "{----}\n" +
      "</body>\n" +
      "</html>");
  }

  public static void main(String[] args) throws Exception
  {
    if (args.length >= 2)
    {
      String libraries = args[0];
      String directory = args[1];
      File outputDir  = new File(directory);
      outputDir.mkdirs();

      String[] libraryNames = libraries.split(",");
      for (String libraryName : libraryNames)
      {
        File docFile = new File(outputDir, "library_" +
          libraryName.toLowerCase() + ".html");

        try (PrintWriter writer = new PrintWriter(docFile, "UTF-8"))
        {
          WebHelpPrinter printer = new WebHelpPrinter(
            writer, null, Locale.getDefault());

          Class<? extends Library> libraryClass =
            LibraryFactory.getLibraryClass(libraryName);
          System.out.println("Generating doc for " + libraryName + "...");
          printer.printLibrary(libraryClass);
          writer.flush();
          System.out.println();
        }
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
