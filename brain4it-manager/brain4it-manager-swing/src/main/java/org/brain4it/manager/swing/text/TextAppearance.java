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

package org.brain4it.manager.swing.text;

import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import org.brain4it.io.Token;

/**
 *
 * @author realor
 */
public class TextAppearance
{
  // additional types
  public static String FUNCTION_TYPE = "FUNCTION";
  public static String COMMENT_TYPE = "COMMENT";
  
  private final HashMap<String, Color> colors = new HashMap<String, Color>();
  private final HashMap<String, Integer> styles = new HashMap<String, Integer>();

  public TextAppearance()
  {
    initDefaults();
  }
  
  public void setColor(String type, Color color)
  {
    colors.put(type, color);
  }
  
  public Color getColor(String type)
  {
    return colors.get(type);
  }

  public void setStyle(String type, Integer style)
  {
    styles.put(type, style);
  }
  
  public Integer getStyle(String type)
  {
    return styles.get(type);
  }
  
  public void setColorAndStyle(String type, Color color, Integer style)
  {
    colors.put(type, color);
    styles.put(type, style);    
  }
  
  private void initDefaults()
  {
    setColorAndStyle(Token.INVALID, Color.RED, null);
    setColorAndStyle(Token.STRING, Color.BLUE, null);
    setColorAndStyle(Token.REFERENCE, Color.BLACK, null);
    setColorAndStyle(Token.BOOLEAN, new Color(128, 128, 0), null);
    setColorAndStyle(Token.NUMBER, new Color(0, 128, 0), null);
    setColorAndStyle(Token.STRING, Color.BLUE, null);
    setColorAndStyle(Token.NULL, new Color(128, 0, 128), null);
    setColorAndStyle(Token.NAME_OPERATOR, new Color(64, 128, 255), Font.BOLD);
    setColorAndStyle(Token.TAG, new Color(255 , 128, 0), null);
    setColorAndStyle(FUNCTION_TYPE, Color.BLACK, Font.BOLD);
    setColorAndStyle(COMMENT_TYPE, Color.GRAY, null);
  }  
}
