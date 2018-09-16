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
import android.app.Application;
import static android.content.Context.MODE_PRIVATE;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Date;
import java.util.Locale;
import org.brain4it.manager.Workspace;

/**
 *
 * @author realor
 */
public class ManagerApplication extends Application
{
  public static final String PREFERENCES = "Brain4itManagerPreferences";
  private Workspace workspace;
  private static final String TAG = "brain4it"; 

  @Override
  public void onCreate()
  {
    super.onCreate();
    
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler()
    {
      @Override
      public void uncaughtException(Thread t, Throwable e)
      {
        handleUncaughtException(t, e);
      }
    });
  }
  
  public Workspace getWorkspace()
  {
    if (workspace == null)
    {
      loadWorkspace();      
    }
    return workspace;
  }
  
  public void loadWorkspace()
  {
    try
    {
      workspace = Workspace.loadWorkspace(getWorkspaceFile());
    }
    catch (Exception ex)
    {
      workspace = new Workspace();
      Log.e(TAG, ex.toString(), ex);
    }
  }
  
  public void saveWorkspace()
  {
    try
    {
      Workspace.saveWorkspace(workspace, getWorkspaceFile());
    }
    catch (Exception ex)
    {
      Log.e(TAG, ex.toString(), ex);      
    }
  }
  
  public String getLanguage()
  {
    Resources res = getResources(); 
    Configuration conf = res.getConfiguration(); 
    return conf.locale.getLanguage();
  }

  public void setLanguage(String language) 
  {
    Locale locale = new Locale(language); 
    Resources res = getResources(); 
    DisplayMetrics dm = res.getDisplayMetrics(); 
    Configuration conf = res.getConfiguration(); 
    conf.locale = locale; 
    res.updateConfiguration(conf, dm); 
  }
  
  public SharedPreferences getPreferences()
  {
    SharedPreferences preferences = 
      getSharedPreferences(ManagerApplication.PREFERENCES, MODE_PRIVATE);
    return preferences;
  }
    
  public void setupActivity(Activity activity, boolean upEnabled)
  {
    SharedPreferences preferences = getPreferences();
    String currentLanguage = getLanguage();
    String language = preferences.getString("language", currentLanguage);
    
    if (!currentLanguage.equals(language))
    {
      setLanguage(language);
    }

    if (upEnabled)
    {
      activity.getActionBar().setDisplayHomeAsUpEnabled(true);
    }
    else
    {
      // hide up button in action bar
      int upId = Resources.getSystem().getIdentifier("up", "id", "android");
      if (upId > 0)
      {
        ImageView up = (ImageView)activity.findViewById(upId);
        up.setImageResource(R.drawable.no_home);
      }
    }
  }
  
  private File getWorkspaceFile()
  {
    File baseDir = new File(Environment.getExternalStorageDirectory(), TAG);
    baseDir.mkdirs();
    return new File(baseDir + "/manager.cfg");
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
    return new File(baseDir + "/manager_log.txt");
  }
}
