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

import static org.brain4it.io.IOConstants.DECLARATION_TAG_PREFIX;
import static org.brain4it.io.IOConstants.DECLARATION_TAG_SUFFIX;
import static org.brain4it.io.IOConstants.DECLARATION_TAG_SEPARATOR;
import static org.brain4it.io.IOConstants.LINK_TAG_PREFIX;
import static org.brain4it.io.IOConstants.LINK_TAG_SUFFIX;

/**
 *
 * @author realor
 */
public class Tag
{
  protected String dataListId;
  
  public Tag(Object dataListId)
  {
    if (dataListId != null)
    {
      this.dataListId = String.valueOf(dataListId);
    }
  }
  
  public static Tag parseTag(String text)
  {
    if (text.startsWith(LINK_TAG_PREFIX) && 
        text.endsWith(LINK_TAG_SUFFIX) && text.length() > 
        LINK_TAG_PREFIX.length() + LINK_TAG_SUFFIX.length())
    {
      return new LinkTag(text.substring(LINK_TAG_PREFIX.length(), 
        text.length() - LINK_TAG_SUFFIX.length()));
    }
    else if (text.startsWith(DECLARATION_TAG_PREFIX) &&
            text.endsWith(DECLARATION_TAG_SUFFIX) && text.length() > 
        DECLARATION_TAG_PREFIX.length() + 
        DECLARATION_TAG_SUFFIX.length())
    {
      int index = text.indexOf(DECLARATION_TAG_SEPARATOR, 
        DECLARATION_TAG_PREFIX.length());
      if (index == -1)
      {
        return new DeclarationTag(
          text.substring(DECLARATION_TAG_PREFIX.length(), 
          text.length() - DECLARATION_TAG_SUFFIX.length()));
      }
      else if (index == DECLARATION_TAG_PREFIX.length())
      {
        return new DeclarationTag(null, text.substring(index + 1, 
          text.length() - DECLARATION_TAG_SUFFIX.length()));        
      }
      else
      {
        return new DeclarationTag(
          text.substring(DECLARATION_TAG_PREFIX.length(), index), 
          text.substring(index + 1, 
            text.length() - DECLARATION_TAG_SUFFIX.length()));                
      }
    }
    return null;
  }
  
  public String getDataListId()
  {
    return dataListId;
  }
}
