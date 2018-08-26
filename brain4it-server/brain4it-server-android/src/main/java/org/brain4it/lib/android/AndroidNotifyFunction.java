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
package org.brain4it.lib.android;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import static android.content.Context.NOTIFICATION_SERVICE;
import android.content.Intent;
import org.brain4it.lang.BList;
import org.brain4it.lang.Context;
import org.brain4it.lang.Utils;
import org.brain4it.server.android.AndroidService;
import org.brain4it.server.android.R;
import org.brain4it.server.android.ServerActivity;

/**
 *
 * @author realor
 */
public class AndroidNotifyFunction extends AndroidFunction
{
  private static int notificationId = 100;
  
  @Override
  public Object invoke(Context context, BList args) throws Exception
  {
    Utils.checkArguments(args, 1);
    String message = (String)context.evaluate(args.get(1));
    AndroidService service = AndroidService.getInstance();

    Notification notification = createNotification(service, message);
    NotificationManager notificationManager =
      (NotificationManager)service.getSystemService(NOTIFICATION_SERVICE);
    notificationManager.notify(notificationId++, notification);
    
    return message;
  }

  private Notification createNotification(AndroidService service, 
    String message)
  {
    Intent targetIntent = new Intent(service, ServerActivity.class);
    PendingIntent contentIntent = PendingIntent.getActivity(
      service, 0, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);

    String appName = service.getResources().getString(R.string.appName);
    Notification.Builder nb = new Notification.Builder(service);
    nb.setContentTitle(appName);
    nb.setSmallIcon(R.drawable.notification);
    nb.setContentText(message);
    nb.setOngoing(false);
    nb.setContentIntent(contentIntent);

    return nb.build();
  }
}
