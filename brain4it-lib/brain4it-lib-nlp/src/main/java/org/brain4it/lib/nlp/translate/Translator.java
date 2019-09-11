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

import org.brain4it.lang.BList;

/**
 * 
 * @author realor
 */

public interface Translator
{
  public static final String TRANSLATED_TEXT = "translated-text";
  public static final String DETECTED_LANGUAGE = "detected-language";
  
  /**
   * Translates a list of texts to a target language.
   * 
   * @param text the list of texts to translate
   * @param sourceLanguage the source language. May be null.
   * @param targetLanguage the target language (ISO code).
   * @param options for the specified translation service (credentials, etc.)
   * @return a list that contains for each input text a list like this:
   * (
   *   "translated-text" => tranlated_text 
   *   "detected-language" => detected_language
   * )
   * @throws Exception 
   */
  public BList translate(BList text,
    String sourceLanguage, String targetLanguage, BList options)
    throws Exception;
}
