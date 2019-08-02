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

package org.brain4it.lib;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import opennlp.tools.lemmatizer.DictionaryLemmatizer;
import opennlp.tools.parser.Parser;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTagger;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.NewlineSentenceDetector;
import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.tokenize.WhitespaceTokenizer;
import org.brain4it.lib.nlp.*;

/**
 *
 * @author realor
 */
public class NlpLibrary extends Library
{
  private final HashMap<String, Object> cache = new HashMap<>();

  @Override
  public String getName()
  {
    return "Nlp";
  }

  @Override
  public void load()
  {
    functions.put("nlp-sentences", new NlpSentencesFunction(this));
    functions.put("nlp-parse", new NlpParseFunction(this));
    functions.put("nlp-postag", new NlpPosTagFunction(this));
    functions.put("nlp-tokenize", new NlpTokenizeFunction(this));
    functions.put("nlp-lemmatize", new NlpLemmatizeFunction(this));
    functions.put("nlp-translate", new NlpTranslateFunction(this));
  }

  @Override
  public void unload()
  {
    cache.clear();
  }

  public Parser getParser(String parserModelPath) throws IOException
  {
    if (parserModelPath == null)
    {
      parserModelPath = getDefaultPath("en-parser-chunking.bin");
    }
    else
    {
      parserModelPath = getAbsolutePath(parserModelPath);
    }

    ParserModel model = (ParserModel)cache.get(parserModelPath);
    if (model == null)
    {
      InputStream is = new FileInputStream(parserModelPath);
      try
      {
        model = new ParserModel(is);
        cache.put(parserModelPath, model);
      }
      finally
      {
        is.close();
      }
    }
    return ParserFactory.create(model);
  }

  public Tokenizer getTokenizer(String tokenizerModelPath) throws IOException
  {
    if (tokenizerModelPath == null)
    {
      tokenizerModelPath = getDefaultPath("en-token.bin");
    }
    else if (tokenizerModelPath.length() == 0)
    {
      return WhitespaceTokenizer.INSTANCE;
    }
    else
    {
      tokenizerModelPath = getAbsolutePath(tokenizerModelPath);
    }

    TokenizerModel model = (TokenizerModel)cache.get(tokenizerModelPath);
    if (model == null)
    {
      InputStream is = new FileInputStream(tokenizerModelPath);
      try
      {
        model = new TokenizerModel(is);
        cache.put(tokenizerModelPath, model);
      }
      finally
      {
        is.close();
      }
    }
    return new TokenizerME(model);
  }

  public POSTagger getPOSTagger(String posModelPath)
    throws IOException
  {
    if (posModelPath == null)
    {
      posModelPath = getDefaultPath("en-pos-maxent.bin");
    }
    else
    {
      posModelPath = getAbsolutePath(posModelPath);
    }

    POSModel model = (POSModel)cache.get(posModelPath);
    if (model == null)
    {
      InputStream is = new FileInputStream(posModelPath);
      try
      {
        model = new POSModel(is);
        cache.put(posModelPath, model);
      }
      finally
      {
        is.close();
      }
    }
    return new POSTaggerME(model);
  }

  public SentenceDetector getSentenceDetector(String sentenceModelPath)
    throws IOException
  {
    if (sentenceModelPath == null)
    {
      sentenceModelPath = getDefaultPath("en-sent.bin");
    }
    else if (sentenceModelPath.length() == 0)
    {
      return new NewlineSentenceDetector();
    }
    else
    {
      sentenceModelPath = getAbsolutePath(sentenceModelPath);
    }

    SentenceModel model = (SentenceModel)cache.get(sentenceModelPath);
    if (model == null)
    {
      InputStream is = new FileInputStream(sentenceModelPath);
      try
      {
        model = new SentenceModel(is);
        cache.put(sentenceModelPath, model);
      }
      finally
      {
        is.close();
      }
    }
    return new SentenceDetectorME(model);
  }

  public DictionaryLemmatizer getDictionary(String dictPath) throws IOException
  {
    if (dictPath == null)
    {
      return null;
    }
    else
    {
      dictPath = getAbsolutePath(dictPath);
    }

    DictionaryLemmatizer dictionary =
      (DictionaryLemmatizer)cache.get(dictPath);
    if (dictionary == null)
    {
      InputStream is = new FileInputStream(dictPath);
      try
      {
        dictionary = new DictionaryLemmatizer(is);
        cache.put(dictPath, dictionary);
      }
      finally
      {
        is.close();
      }
    }
    return dictionary;
  }

  private String getDefaultPath(String filename)
  {
    return getBasePath() + filename;
  }

  private String getAbsolutePath(String filename)
  {
    if (filename.startsWith(File.separator)) return filename;
    return getBasePath() + filename;
  }

  private String getBasePath()
  {
    return System.getProperty("user.home") + "/opennlp/";
  }
}
