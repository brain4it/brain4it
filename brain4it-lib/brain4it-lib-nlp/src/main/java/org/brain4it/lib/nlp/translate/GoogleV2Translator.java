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
package org.brain4it.lib.nlp.translate;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import org.brain4it.io.JSONParser;
import org.brain4it.lang.BList;

/**
 *
 * @author realor
 */
public class GoogleV2Translator implements Translator
{
  private static final String API_URL =
    "https://translation.googleapis.com/language/translate/v2";

  @Override
  public BList translate(BList texts, String sourceLanguage,
    String targetLanguage, BList options) throws Exception
  {
    if (options == null)
      throw new Exception("options argument is required");

    String key = (String)options.get("key");
    if (key == null)
      throw new Exception("API key is required");

    StringBuilder buffer = new StringBuilder();
    buffer.append(API_URL);
    for (int i = 0; i < texts.size(); i++)
    {
      if (i == 0)
      {
        buffer.append("?");
      }
      else
      {
        buffer.append("&");
      }
      buffer.append("q=");
      buffer.append(URLEncoder.encode((String)texts.get(i), "UTF-8"));
    }
    buffer.append("&target=");
    buffer.append(targetLanguage);
    buffer.append("&format=text");
    buffer.append("&key=");
    buffer.append(key);

    URL url = new URL(buffer.toString());
    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
    conn.setRequestMethod("GET");
    conn.setDoOutput(true);
    InputStreamReader reader =
      new InputStreamReader(conn.getInputStream(), "UTF-8");
    JSONParser parser = new JSONParser(reader);
    BList list = (BList)parser.parse();

    BList translations = new BList();
    list = (BList)list.get("data");
    list = (BList)list.get("translations");
    for (int i = 0; i < list.size(); i++)
    {
      BList subList = (BList)list.get(i);
      translations.add((String)subList.get("translatedText"));
    }
    return translations;
  }
}
