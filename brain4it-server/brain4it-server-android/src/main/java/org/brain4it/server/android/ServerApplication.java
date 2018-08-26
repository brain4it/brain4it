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
package org.brain4it.server.android;

import android.app.Application;
import android.os.Environment;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Date;

/**
 *
 * @author realor
 */
public class ServerApplication extends Application
{  
  private static final String TAG = "brain4it";

  @Override
  public void onCreate()
  {
    super.onCreate();
    
    Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler()
    {
      @Override
      public void uncaughtException(Thread t, Throwable e)
      {
        handleUncaughtException(t, e);
      }
    });
  }

  private void handleUncaughtException(Thread t, Throwable e)
  {
    try
    {
      PrintWriter out = new PrintWriter(new FileWriter(getLogFile(), true));
      try
      {
        out.println(new Date() + ":");
        e.printStackTrace(out);
        out.flush();
      }
      finally
      {
        out.close();
      }
    }
    catch (Exception ex)
    {
    }
    finally
    {
      System.exit(1);
    }
  }
  
  private File getLogFile()
  {
    File baseDir = new File(Environment.getExternalStorageDirectory(), TAG);
    baseDir.mkdirs();
    return new File(baseDir + "/server_log.txt");
  }  
}
