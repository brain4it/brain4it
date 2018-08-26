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

import android.content.Context;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import org.brain4it.manager.android.view.ButtonWidget;
import org.brain4it.manager.android.view.DisplayWidget;
import org.brain4it.manager.android.view.EditTextWidget;
import org.brain4it.manager.android.view.GaugeWidget;
import org.brain4it.manager.android.view.GraphWidget;
import org.brain4it.manager.android.view.ImageWidget;
import org.brain4it.manager.android.view.LedWidget;
import org.brain4it.manager.android.view.RangeWidget;
import org.brain4it.manager.android.view.SelectWidget;
import org.brain4it.manager.android.view.StickWidget;
import org.brain4it.manager.android.view.SwitchWidget;

/**
 *
 * @author realor
 */
public class DashboardWidgetFactory
{
  static DashboardWidgetFactory factory;
  final HashMap<String, Class> types = new HashMap<String, Class>();
 
  public DashboardWidgetFactory()
  {
    types.put("button", ButtonWidget.class);
    types.put("display", DisplayWidget.class);
    types.put("editText", EditTextWidget.class);
    types.put("gauge", GaugeWidget.class);
    types.put("graph", GraphWidget.class);
    types.put("image", ImageWidget.class);
    types.put("led", LedWidget.class);
    types.put("range", RangeWidget.class);
    types.put("select", SelectWidget.class);
    types.put("stick", StickWidget.class);
    types.put("switch", SwitchWidget.class);
  }
  
  public static DashboardWidgetFactory getInstance()
  {
    if (factory == null)
    {
      factory = new DashboardWidgetFactory();
    }
    return factory;
  }
  
  public DashboardWidget createWidget(String type, Context context) 
    throws Exception
  {
    DashboardWidget widget = null;
    Class cls = types.get(type);
    if (cls != null)
    {
      Constructor constructor = cls.getConstructor(Context.class);
      widget = (DashboardWidget)constructor.newInstance(context);
    }
    return widget;
  }

  public ArrayList<String> getTypes()
  {
    ArrayList<String> typeNames = new ArrayList(types.keySet());
    Collections.sort(typeNames);
    return typeNames;
  }
}
