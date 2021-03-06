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

import opennlp.tools.lemmatizer.DictionaryLemmatizer;
import opennlp.tools.postag.POSTagger;
import org.brain4it.lang.BList;
import org.brain4it.lang.Context;
import org.brain4it.lang.Utils;
import org.brain4it.lib.NlpLibrary;

/**
 *
 * @author realor
 */
public class NlpPosTagFunction extends NlpBaseFunction
{
  public NlpPosTagFunction(NlpLibrary library)
  {
    super(library);
  }

  @Override
  public Object invoke(Context context, BList args) throws Exception
  {
    Utils.checkArguments(args, 1);

    BList list = (BList)context.evaluate(args.get(1));

    String posModelPath =
      (String)context.evaluate(args.get("pos"));
    POSTagger posTagger = library.getPOSTagger(posModelPath);

    String dictPath = (String)context.evaluate(args.get("dictionary"));
    DictionaryLemmatizer dictionary = library.getDictionary(dictPath);

    String[] words = new String[list.size()];
    for (int i = 0; i < words.length; i++)
    {
      words[i] = String.valueOf(list.get(i));
    }
    String[] tags = posTagger.tag(words);
    BList result = new BList();
    for (int i = 0; i < words.length; i++)
    {
      String word = words[i];
      String tag = tags[i];
      if (dictionary != null)
      {
        String[] lemmas =
          dictionary.lemmatize(new String[]{word}, new String[]{tag});
        String lemma = lemmas[0];
        if (!lemma.equals("O"))
        {
          word = lemma;
        }
      }
      BList item = new BList(2);
      item.add(tag);
      item.add(word);
      result.add(item);
    }
    return result;
  }
}
