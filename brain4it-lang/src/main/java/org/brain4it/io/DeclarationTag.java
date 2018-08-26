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

/**
 *
 * @author realor
 */
public class DeclarationTag extends Tag
{
  /*
     Declaration reference format:  
     <#I:S> List identified by I that takes structure from list identified by S
     <#I>   List identified by I. Its structure is not shared with other lists
     <#:S>  Unidentified list that takes structure from list identified by S
  */

  protected String structureListId;
  
  public DeclarationTag(Object dataListId)
  {
    super(dataListId);
  }
  
  public DeclarationTag(Object dataListId, Object structureListId)
  {
    super(dataListId);
    if (structureListId != null)
    {
      this.structureListId = String.valueOf(structureListId);
    }
  }  
  
  public String getStructureListId()
  {
    return structureListId;
  } 
  
  @Override
  public String toString()
  {
    StringBuilder buffer = new StringBuilder();
    buffer.append(DECLARATION_TAG_PREFIX);
    if (dataListId != null)
    {
      buffer.append(dataListId);
    }
    if (structureListId != null && !structureListId.equals(dataListId))
    {
      buffer.append(DECLARATION_TAG_SEPARATOR);
      buffer.append(structureListId);
    }
    buffer.append(DECLARATION_TAG_SUFFIX);
    return buffer.toString();
  }  
}
