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
package org.brain4it.manager.swing;

import java.net.URL;
import java.util.HashMap;
import javax.swing.ImageIcon;

/**
 *
 * @author realor
 */
public class IconCache
{
  private static final HashMap<String, ImageIcon> iconMap = 
    new HashMap<String, ImageIcon>();
  
  private static int scalingFactor = 1;

  public static int getScalingFactor()
  {
    return scalingFactor;
  }

  public static void setScalingFactor(int scalingFactor)
  {
    IconCache.scalingFactor = scalingFactor;
  }

  public static ImageIcon getIcon(String name)
  {
    return getIcon(name, null);
  }
  
  public static ImageIcon getIcon(String name, String defaultName)
  {
    ImageIcon imageIcon = iconMap.get(name);
    if (imageIcon == null && !iconMap.containsKey(name))
    {
      String basePath = "resources/icons/";
      URL resource = null;
      if (scalingFactor > 1) // look for high density icon
      {
        String path = basePath + name + "@" + scalingFactor + "x.png";
        resource = IconCache.class.getResource(path);
      }
      if (resource == null) // look for normal density icon
      {
        String path = basePath + name + ".png";
        resource = IconCache.class.getResource(path);
      }
      if (resource == null) // look for default icon
      {
        if (defaultName != null)
        {
          imageIcon = getIcon(defaultName, null);
        }
      }
      if (resource != null)
      {
        imageIcon = new javax.swing.ImageIcon(resource);
      }
      iconMap.put(name, imageIcon);
    }
    return imageIcon;
  }
}
