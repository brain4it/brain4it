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

import java.util.HashMap;
import org.brain4it.lang.BList;
import org.brain4it.lang.Context;
import org.brain4it.lang.Utils;
import org.brain4it.lib.NlpLibrary;
import org.brain4it.lib.nlp.translate.GoogleV2Translator;
import org.brain4it.lib.nlp.translate.Translator;

/**
 *
 * @author realor
 */
public class NlpTranslateFunction extends NlpBaseFunction
{
  private final HashMap<String, Translator> translators =
    new HashMap<String, Translator>();

  public NlpTranslateFunction(NlpLibrary library)
  {
    super(library);
    registerTranslators();
  }

  @Override
  public Object invoke(Context context, BList args) throws Exception
  {
    Utils.checkArguments(args, 4);
    BList texts;
    Object input = context.evaluate(args.get(1));
    if (input instanceof String)
    {
      texts = new BList();
      texts.add((String)input);
    }
    else if (input instanceof BList)
    {
      texts = (BList)input;
    }
    else throw new Exception("Invalid text input");

    String sourceLanguage = (String)context.evaluate(args.get(2));
    String targetLanguage = (String)context.evaluate(args.get(3));
    String service = (String)context.evaluate(args.get(4));

    BList options = null;
    if (args.size() > 5)
    {
      options = (BList)context.evaluate(args.get(5));
    }
    Translator translator = translators.get(service);
    if (translator == null) throw new Exception("Unknow translation service");

    return translator.translate(texts, sourceLanguage, targetLanguage, options);
  }

  private void registerTranslators()
  {
    translators.put("google-v2", new GoogleV2Translator());
    translators.put("google", translators.get("google-v2"));
  }
}
