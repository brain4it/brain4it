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

import opennlp.tools.cmdline.parser.ParserTool;
import opennlp.tools.lemmatizer.Lemmatizer;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.Parser;
import opennlp.tools.tokenize.Tokenizer;
import org.brain4it.lang.Context;
import org.brain4it.lang.BList;
import org.brain4it.lang.Utils;
import org.brain4it.lib.NlpLibrary;

/**
 *
 * @author realor
 *
 */
public class NlpParseFunction extends NlpBaseFunction
{
  public NlpParseFunction(NlpLibrary library)
  {
    super(library);
  }

  @Override
  public Object invoke(Context context, BList args) throws Exception
  {
    Utils.checkArguments(args, 1);
    String text = (String)context.evaluate(args.get(1));

    String parserModelPath = (String)context.evaluate(args.get("parser"));
    Parser parser = library.getParser(parserModelPath);

    String tokenizerModelPath = (String)context.evaluate(args.get("tokenizer"));
    Tokenizer tokenizer = library.getTokenizer(tokenizerModelPath);

    String dictionaryPath = (String)context.evaluate(args.get("dictionary"));
    Lemmatizer lemmatizer = library.getDictionary(dictionaryPath);

    Number parses = (Number)context.evaluate(args.get("parses"));
    if (parses == null) parses = 1;
    
    Parse[] parse = ParserTool.parseLine(text, parser, tokenizer, 
      parses.intValue());
    return toObject(parse[0], lemmatizer);
  }

  private Object toObject(Parse parse, Lemmatizer lemmatizer)
  {
    BList list = new BList();
    String type = parse.getType();
    list.add(type);
    Parse[] children = parse.getChildren();
    for (Parse child : children)
    {
      if (child.getChildCount() == 0) // token
      {
        String word = parse.getCoveredText();
        if (lemmatizer != null)
        {
          String lemmas[] =
            lemmatizer.lemmatize(new String[]{word}, new String[]{type});
          if (!lemmas[0].equals("O"))
          {
            word = lemmas[0];
          }
        }
        list.add(word);
      }
      else
      {
        list.add(toObject(child, lemmatizer));
      }
    }
    return list;
  }
}
