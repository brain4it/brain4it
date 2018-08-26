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

package org.brain4it.lib.nlp;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import opennlp.tools.cmdline.parser.ParserTool;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.Parser;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;
import org.brain4it.lang.Context;
import org.brain4it.lang.BList;
import org.brain4it.lang.Utils;
import org.brain4it.lang.Function;

/**
 *
 * @author realor
 */
public class NlpFunction implements Function
{
  private final BList models = new BList();

  @Override
  public Object invoke(Context context, BList args) throws Exception
  {
    Utils.checkArguments(args, 1);    
    String text = Utils.toString(context.evaluate(args.get(1)));
    String modelPath = (String)context.evaluate(args.get("model"));
    if (modelPath == null)
    {
      modelPath = getDefaultModelPath();
    }
    ParserModel model = getModel(modelPath);
    return parse(text, model);
  }

  private String getDefaultModelPath()
  {
    return System.getProperty("user.home") + "/opennlp/en-parser-chunking.bin";    
  }
  
  private Object parse(String text, ParserModel model) throws Exception
  {
    Parser parser = ParserFactory.create(model);
    Parse[] parse = ParserTool.parseLine(text, parser, 1);
    return toObject(parse[0]);
  }

  private ParserModel getModel(String modelPath) throws IOException
  {
    ParserModel model = (ParserModel)models.get(modelPath);    
    if (model == null)
    {
      InputStream is = new FileInputStream(modelPath);
      try
      {
        model = new ParserModel(is);
        models.put(modelPath, model);
      }
      finally
      {
        is.close();
      }
    }
    return model;
  }
  
  private Object toObject(Parse parse)
  {
    if (parse.getChildCount() == 0)
    {
      return parse.getCoveredText();
    }
    else
    {
      BList list = new BList();
      String type = parse.getType();
      list.add(type);
      Parse[] children = parse.getChildren();
      for (Parse child : children)
      {
        list.add(toObject(child));
      }
      return list;
    }
  }
}
