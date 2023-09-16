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
package org.brain4it.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import org.brain4it.io.HTMLFormatter;

/**
 *
 * @author realor
 */
public class Merger
{
  public static final String START_MARK = "{--";
  public static final String END_MARK = "--}";
  public static final String START_CODE = "<code>";
  public static final String END_CODE = "</code>";

  public Merger()
  {
  }

  public void merge(String templateFilename,
    String inputDirname, String outputDirname)
    throws IOException
  {
    String template = loadTemplate(templateFilename);
    File inputDir = new File(inputDirname);
    FileFilter filter = new FileFilter()
    {
      @Override
      public boolean accept(File pathname)
      {
        return pathname.getName().endsWith(".html");
      }
    };
    File outputDir = new File(outputDirname);
    outputDir.mkdirs();

    File[] files = inputDir.listFiles(filter);
    for (File file : files)
    {
      Map<String, String> variables = readVariables(file.getAbsolutePath());
      File output = new File(outputDir, file.getName());
      mergeVariables(template, variables, output);
    }
  }

  public void mergeVariables(String template, Map<String, String> variables,
    File output) throws IOException
  {
    try (PrintWriter writer = new PrintWriter(output, "UTF-8"))
    {
      int index0 = 0;
      int index1 = template.indexOf(START_MARK);
      int index2;

      while (index1 != -1)
      {
        writer.write(template.substring(index0, index1));

        index2 = template.indexOf(END_MARK, index1 + START_MARK.length());
        if (index2 == -1) throw new IOException("Unterminated mark");

        String variable = template.substring(index1 + START_MARK.length(),
          index2);
        String content = variables.get(variable);
        if (content != null)
        {
          writer.write(content);
        }
        index0 = index2 + END_MARK.length();
        index1 = template.indexOf(START_MARK, index0);
      }
      writer.write(template.substring(index0));
    }
  }

  protected String loadTemplate(String filename) throws IOException
  {
    Path templatePath = Paths.get(filename);
    return new String(Files.readAllBytes(templatePath), "UTF-8");
  }

  protected String transformContent(String content)
    throws IOException
  {
    StringWriter writer = new StringWriter();
    HTMLFormatter codeFormatter = new HTMLFormatter();
    HTMLFormatter inlineCodeFormatter = new HTMLFormatter();
    codeFormatter.getConfiguration().setMaxColumns(50);
    inlineCodeFormatter.getConfiguration().getNotInlineFunctions().clear();
    inlineCodeFormatter.getConfiguration().setMaxColumns(Integer.MAX_VALUE);
    int index0 = 0;
    int index1 = content.indexOf(START_CODE);
    while (index1 != -1)
    {
      writer.write(content.substring(index0, index1));
      int index2 = content.indexOf(END_CODE, index1 + START_CODE.length());
      if (index2 == -1) throw new IOException("Unmatched code tag");
      String code = content.substring(index1 + START_CODE.length(), index2);
      writer.write(START_CODE);
      try
      {
        if (code.contains("\n"))
        {
          codeFormatter.format(new StringReader(code), writer);
        }
        else
        {
          inlineCodeFormatter.format(new StringReader(code), writer);
        }
      }
      catch (ParseException ex)
      {
        writer.write(code);
      }
      writer.write(END_CODE);
      index0 = index2 + END_CODE.length();
      index1 = content.indexOf(START_CODE, index0);
    }
    writer.write(content.substring(index0));
    return writer.toString();
  }

  protected Map<String, String> readVariables(String filename)
    throws IOException
  {
    Map<String, String> variables = new HashMap<>();
    Path templatePath = Paths.get(filename);
    String content = new String(Files.readAllBytes(templatePath), "UTF-8");
    content = transformContent(content);
    int index = content.indexOf(START_MARK);
    int index2;
    int blockIndex = -1;
    String variable = null;

    while (index != -1)
    {
      index2 = content.indexOf(END_MARK, index + START_MARK.length());
      if (index2 == -1) throw new IOException("Unterminated mark");
      if (blockIndex != -1)
      {
        variables.put(variable, content.substring(blockIndex, index));
      }
      variable = content.substring(index + START_MARK.length(), index2);

      blockIndex = index2 + END_MARK.length();
      index = content.indexOf(START_MARK, blockIndex);
    }
    return variables;
  }

  public static void main(String[] args) throws Exception
  {
    Merger merger = new Merger();
    merger.merge(args[0], args[1], args[2]);
  }
}

