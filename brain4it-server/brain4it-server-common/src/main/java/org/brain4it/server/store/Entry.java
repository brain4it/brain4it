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

package org.brain4it.server.store;

import static org.brain4it.server.store.Store.PATH_SEPARATOR;

/**
 *
 * @author realor
 */
public class Entry
{
  private final String path;
  private final boolean directory;
  private final long lastModified;
  private final long length;
  
  public Entry(String path, boolean directory, long lastModified, long length)
  {
    this.path = path;
    this.directory = directory;
    this.lastModified = lastModified;
    this.length = length;
  }

  public String getPath()
  {
    return path;
  }

  public long getLastModified()
  {
    return lastModified;
  }
  
  public long getLength()
  {
    return length;
  }
  
  public boolean isDirectory()
  {
    return directory;
  }
  
  public String getName()
  {
    int index = path.lastIndexOf(PATH_SEPARATOR);
    if (index == -1) return path;
    return path.substring(index + 1);
  }
  
  public String getParentPath()
  {
    int index = path.lastIndexOf(PATH_SEPARATOR);
    if (index == -1) return null;
    return path.substring(0, index);
  }
}
