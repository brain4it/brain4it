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

package org.brain4it.manager.android;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.brain4it.io.Token;
import org.brain4it.io.Tokenizer;
import org.brain4it.io.TokenizerComments;

/**
 *
 * @author realor
 */
public class CodeHighlighter
{
  private static final String COMMENT_TYPE = "COMMENT";
  private final List spans = new ArrayList();
  private final HashMap<String, Integer> colors = new HashMap<String, Integer>();
  
  public CodeHighlighter()
  {
    colors.put(Token.NULL, Color.argb(255, 128, 0, 128));
    colors.put(Token.BOOLEAN, Color.argb(255, 128, 128, 0));
    colors.put(Token.NUMBER, Color.argb(255, 0, 128, 0));
    colors.put(Token.STRING, Color.BLUE);
    colors.put(Token.NAME_OPERATOR, Color.argb(255, 64, 128, 255));
    colors.put(Token.OPEN_LIST, Color.BLACK);
    colors.put(Token.CLOSE_LIST, Color.BLACK);
    colors.put(Token.TAG, Color.argb(255, 255, 128, 0));
    colors.put(Token.INVALID, Color.RED);
    colors.put(COMMENT_TYPE, Color.GRAY);
  }

  public void updateHighlight(Spannable spannable)
  {
    updateHighlight(spannable, null);
  }

  public void updateHighlight(Spannable spannable, 
     Set<String> functionNames)
  {
    updateHighlight(spannable, 0, spannable.length(), functionNames);
  }  
  
  public void updateHighlight(Spannable spannable, 
     int vstart, int vend, Set<String> functionNames)
  {
    try
    {
      for (Object span : spans)
      {
        spannable.removeSpan(span);
      }
      String text = spannable.toString();
      Tokenizer tokenizer = new TokenizerComments(new StringReader(text));
      Token token = new Token();
      tokenizer.readToken(token);
      int start = 0;
      int end = 0;
      while (!token.isType(Token.EOF) && start <= vend)
      {
        start = token.getStartPosition();
        end = token.getEndPosition();
        
        if (end >= vstart)
        {
          if ((token.getFlags() & TokenizerComments.COMMENT_FLAG) == 
            TokenizerComments.COMMENT_FLAG && !token.isType(Token.INVALID))
          {
            Integer color = colors.get(COMMENT_TYPE);            
            ForegroundColorSpan span = new ForegroundColorSpan(color); 
            spannable.setSpan(span, start, end, 
              Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            spans.add(span);            
          }
          else if (token.isType(Token.REFERENCE))
          {
            String name = token.getText();
            if (functionNames != null)
            {
              if (functionNames.contains(name))
              {
                StyleSpan span = new StyleSpan(Typeface.BOLD);
                spannable.setSpan(span, start, end, 
                  Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                spans.add(span);
              }
            }
          }
          else
          {
            String type = token.getType();
            Integer color = colors.get(type);
            if (color != null)
            {
              ForegroundColorSpan span = new ForegroundColorSpan(color); 
              spannable.setSpan(span, start, end, 
                Spannable.SPAN_INCLUSIVE_INCLUSIVE);
              spans.add(span);
            }
          }
        }
        tokenizer.readToken(token);
      }
    }
    catch (Exception ex)
    {      
    }
  }
}
