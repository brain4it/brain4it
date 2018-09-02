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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;
import org.brain4it.lang.BList;
import org.brain4it.lang.BSoftReference;
import static org.brain4it.manager.widgets.WidgetProperty.*;

/**
 *
 * @author realor
 */
public abstract class WidgetType
{
  /* widget types */
  public static final String BUTTON = "button";
  public static final String DISPLAY = "display";
  public static final String EDIT_TEXT = "editText";
  public static final String GAUGE = "gauge";
  public static final String GRAPH = "graph";
  public static final String IMAGE = "image";
  public static final String INDICATOR = "indicator";
  public static final String LED = "led";
  public static final String RANGE = "range";
  public static final String SELECT = "select";
  public static final String STICK = "stick";
  public static final String SWITCH = "switch";

  /* special widget type property in property BList */
  public static final String TYPE = "type"; 
  
  /* common properties */  
  public static final String LABEL = "label";
  public static final String FONT_FAMILY = "font-family";
  public static final String FONT_SIZE = "font-size";
  public static final String GET_VALUE = "get-value";
  public static final String SET_VALUE = "set-value";
  public static final String MIN = "min";
  public static final String MAX = "max";
  public static final String COLOR = "color";
  public static final String INVOKE_INTERVAL = "invoke-interval";

  public static final WidgetProperty LABEL_PROPERTY = 
    new WidgetProperty(LABEL, STRING, false, null);  
  public static final WidgetProperty FONT_FAMILY_PROPERTY = 
    new WidgetProperty(FONT_FAMILY, STRING, false, null);
  public static final WidgetProperty FONT_SIZE_PROPERTY = 
    new WidgetProperty(FONT_SIZE, NUMBER, false, 14);
  public static final WidgetProperty MIN_PROPERTY = 
    new WidgetProperty(MIN, NUMBER, false, 0);
  public static final WidgetProperty MAX_PROPERTY = 
    new WidgetProperty(MAX, NUMBER, false, 100);
  public static final WidgetProperty COLOR_PROPERTY = 
    new WidgetProperty(COLOR, STRING, false, null);
  public static final WidgetProperty INVOKE_INTERVAL_PROPERTY = 
    new WidgetProperty(INVOKE_INTERVAL, NUMBER, false, 100);
  
  public static final Map<String, WidgetType> TYPES = 
    new HashMap<String, WidgetType>();
  
  protected Map<String, WidgetProperty> propertyMap = 
    new TreeMap<String, WidgetProperty>();
  
  public abstract String getWidgetType();

  public Collection<WidgetProperty> getProperties()
  {
    return Collections.unmodifiableCollection(propertyMap.values());
  }
  
  public WidgetProperty getProperty(String name)
  {
    return propertyMap.get(name);
  }

  /* common properties */
  
  public String getLabel(BList properties)
  {
    return this.propertyMap.get(LABEL).getString(properties);
  }
  
  public String getFontFamily(BList properties)
  {
    return this.propertyMap.get(FONT_FAMILY).getString(properties);
  }

  public int getFontSize(BList properties) throws Exception
  {
    return this.propertyMap.get(FONT_SIZE).getInteger(properties);
  }
  
  public int getMin(BList properties) throws Exception
  {
    return this.propertyMap.get(MIN).getInteger(properties);
  }

  public int getMax(BList properties) throws Exception
  {
    return this.propertyMap.get(MAX).getInteger(properties);
  }
  
  public BSoftReference getGetValueFunction(BList properties) throws Exception
  {
    return this.propertyMap.get(GET_VALUE).getFunction(properties);
  }  

  public BSoftReference getSetValueFunction(BList properties) throws Exception
  {
    return this.propertyMap.get(SET_VALUE).getFunction(properties);
  }    
  
  public int getColor(BList properties)
  {
    return this.propertyMap.get(COLOR).getColor(properties);    
  }

  public int getInvokeInterval(BList properties) throws Exception
  {
    return getProperty(INVOKE_INTERVAL).getInteger(properties);
  }
  
  /* gets widget description */
  public String getDescription(Locale locale)
  {
    return getLocalizedText(getWidgetType(), locale);
  }

  /* gets property description */  
  public String getDescription(String property, Locale locale)
  {
    return getLocalizedText(getWidgetType() + "." + property, locale);
  }
    
  public void init(BList properties)
  {
    for (WidgetProperty property : this.propertyMap.values())
    {
      property.setDefaultValue(properties);
    }
  }

  public void validate(BList properties) throws Exception
  {    
  }

  public static List<String> getWidgetTypes()
  {
    ArrayList<String> typeNames = new ArrayList(TYPES.keySet());
    Collections.sort(typeNames);
    return typeNames;    
  }
  
  public static WidgetType getType(String widgetType)
  {
    return TYPES.get(widgetType);
  }
  
  protected String getLocalizedText(String key, Locale locale)
  {
    ResourceBundle bundle = ResourceBundle.getBundle(
      "org/brain4it/manager/widgets/resources/Widgets", locale);
    if (bundle.containsKey(key))
    {
      return bundle.getString(key);
    }
    return null;
  }
  
  protected final void addProperty(WidgetProperty property)
  {
    propertyMap.put(property.getName(), property);
  }
  
  protected final void addProperty(String name, String type, boolean function, 
    Object defaultValue)
  {
    propertyMap.put(name, new WidgetProperty(name, type, function, defaultValue));
  }
  
  static
  {
    TYPES.put(BUTTON, new ButtonWidgetType());
    TYPES.put(DISPLAY, new DisplayWidgetType());
    TYPES.put(EDIT_TEXT, new EditTextWidgetType());
    TYPES.put(GAUGE, new GaugeWidgetType());
    TYPES.put(GRAPH, new GraphWidgetType());
    TYPES.put(IMAGE, new ImageWidgetType());
    TYPES.put(INDICATOR, new IndicatorWidgetType());
    TYPES.put(LED, new LedWidgetType());
    TYPES.put(RANGE, new RangeWidgetType());
    TYPES.put(SELECT, new SelectWidgetType());
    TYPES.put(STICK, new StickWidgetType());
    TYPES.put(SWITCH, new SwitchWidgetType());
  }
}
