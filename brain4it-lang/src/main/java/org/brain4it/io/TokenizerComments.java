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
package org.brain4it.io;

import java.io.IOException;
import java.io.Reader;
import static org.brain4it.io.IOConstants.COMMENT_FUNCTION_NAME;

/**
 *
 * @author realor
 */
public class TokenizerComments extends Tokenizer
{
  public static final int COMMENT_FLAG = 0x1;

  private int level = 0; // < 0 outside comment, >= 1 inside comment
  
  public TokenizerComments(Reader reader)
  {
    super(reader);
  }
    
  @Override
  public Token readToken(Token token) throws IOException
  {
    token = super.readToken(token);
    if (level <= 0) // outside comment
    {
      token.flags &= ~COMMENT_FLAG;
      if (token.isType(Token.OPEN_LIST))
      {
        Token nextToken = super.readToken(null);
        super.unreadToken(nextToken);
        if (nextToken.isType(Token.REFERENCE) && 
            nextToken.getText().equals(COMMENT_FUNCTION_NAME))
        {
          level = 1;
          token.flags |= COMMENT_FLAG;
        }
      }
    }
    else // inside comment
    {
      token.flags |= COMMENT_FLAG;
      if (token.isType(Token.OPEN_LIST))
      {
        level++;
      }
      else if (token.isType(Token.CLOSE_LIST))
      {
        level--;
      }
    }
    return token;
  }
  
  @Override
  public void unreadToken(Token token)
  {
    super.unreadToken(token);
    if ((token.getFlags() & COMMENT_FLAG) == COMMENT_FLAG)
    {
      if (token.isType(Token.CLOSE_LIST))
      {
        level++;
      }
      else if (token.isType(Token.OPEN_LIST))
      {
        level--;
      }
    }
  }
}
