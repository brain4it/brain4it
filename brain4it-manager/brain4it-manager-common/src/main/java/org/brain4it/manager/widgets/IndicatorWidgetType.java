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
package org.brain4it.manager.widgets;

import org.brain4it.lang.BList;
import static org.brain4it.manager.widgets.WidgetProperty.BOOLEAN;
import static org.brain4it.manager.widgets.WidgetProperty.NUMBER;
import static org.brain4it.manager.widgets.WidgetProperty.STRING;
import static org.brain4it.manager.widgets.WidgetType.GET_VALUE;
import static org.brain4it.manager.widgets.WidgetType.LABEL;

/**
 *
 * @author realor
 */
public class IndicatorWidgetType extends WidgetType
{
  public static final String MAX_VALUE_LENGTH = "max-value-length";  
  public static final String UNITS = "units";  
  
  public IndicatorWidgetType()
  {
    addProperty(LABEL, STRING, false, null);
    addProperty(GET_VALUE, BOOLEAN, true, null);
    addProperty(FONT_FAMILY, STRING, false, "Arial");
    addProperty(MAX_VALUE_LENGTH, NUMBER, false, 0);
    addProperty(UNITS, STRING, false, null);
  }

  @Override
  public String getWidgetType()
  {
    return INDICATOR;
  }
  
  public int getMaxValueLength(BList properties) throws Exception
  {
    return getProperty(MAX_VALUE_LENGTH).getInteger(properties);
  }
  
  public String getUnits(BList properties)
  {
    return getProperty(UNITS).getString(properties);
  }

  public float getFontSize(int width, int height, int valueLength)
  {
    float fontRatio = 1.0f;
    return Math.min(height, width * fontRatio / valueLength);
  }
}
