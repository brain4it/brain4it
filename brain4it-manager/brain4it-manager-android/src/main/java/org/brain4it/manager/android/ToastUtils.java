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

import android.app.Activity;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 *
 * @author realor
 */
public class ToastUtils
{
  public static void showLong(final Activity activity, final String message)
  {
    show(activity, message, Toast.LENGTH_LONG);
  }

  public static void showShort(final Activity activity, final String message)
  {
    show(activity, message, Toast.LENGTH_SHORT);
  }

  public static void show(final Activity activity, final String message,
     final int length)
  {
    activity.runOnUiThread(new Runnable()
    {
      @Override
      public void run()
      {
        Toast toast = Toast.makeText(activity, message,
          Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        View view = toast.getView();
        TextView textView = (TextView)view.findViewById(android.R.id.message);
        view.setBackgroundColor(Color.BLACK);
        textView.setTextColor(Color.WHITE);
        toast.show();
      }
    });
  }
}
