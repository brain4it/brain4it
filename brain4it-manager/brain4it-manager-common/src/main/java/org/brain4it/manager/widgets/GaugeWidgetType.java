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
import static org.brain4it.manager.widgets.WidgetProperty.*;

/**
 *
 * @author realor
 */
public class GaugeWidgetType extends WidgetType
{
  public static final String DIVISIONS = "divisions";
  public static final String DECIMALS = "decimals";
  
  public GaugeWidgetType()
  {
    addProperty(GET_VALUE, NUMBER, true, null);
    addProperty(LABEL_PROPERTY);
    addProperty(MIN_PROPERTY);
    addProperty(MAX_PROPERTY);
    addProperty(DIVISIONS, NUMBER, false, 10);
    addProperty(DECIMALS, NUMBER, false, 0);
  }
  
  @Override
  public String getWidgetType()
  {
    return GAUGE;
  }

  public int getDivisions(BList properties) throws Exception
  {
    return getProperty(DIVISIONS).getInteger(properties);
  }

  public int getDecimals(BList properties) throws Exception
  {
    return getProperty(DECIMALS).getInteger(properties);
  }
  
  @Override
  public void validate(BList properties) throws Exception
  {
    int min = getMin(properties);
    int max = getMax(properties);
    if (min >= max)
      throw new Exception("max must be greater than min!");

    int divisions = getDivisions(properties);
    if (divisions < 5)
      throw new Exception("divisions must be greater than 4");    

    int decimals = getDecimals(properties);
    if (decimals < 0)
      throw new Exception("decimals must be positive or zero");
  }
}
