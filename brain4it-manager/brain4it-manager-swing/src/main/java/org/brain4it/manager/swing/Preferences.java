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

import java.awt.Frame;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

/**
 *
 * @author realor
 */
public class Preferences extends Properties
{
  public static final String LAST_WORKSPACE_PATH = "lastWorkspacePath";
  public static final String SCALING_FACTOR = "scalingFactor";
  public static final String LANGUAGE = "language";
  public static final String FONT_SIZE = "fontSize";
  public static final String FORMAT_COLUMNS = "formatColumns";
  public static final String INDENT_SIZE = "indentSize";
  public static final String WINDOW_WIDTH = "windowWidth";
  public static final String WINDOW_HEIGHT = "windowHeight";
  public static final String WINDOW_STATE = "windowState";
  public static final String EXPLORER_WIDTH = "explorerWidth";
  public static final String AUXILIARY_PANEL_WIDTH = "auxiliaryPanelWidth";

  public File getLastWorkspaceFile()
  {
    String path = getProperty(LAST_WORKSPACE_PATH);
    return path == null ? null : new File(path);
  }
  
  public void setLastWorkspaceFile(File file)
  {
    if (file != null)
    {
      setProperty(LAST_WORKSPACE_PATH, file.getAbsolutePath());
    }
    else
    {
      super.remove(LAST_WORKSPACE_PATH);
    }
  }
  
  public int getScalingFactor()
  {
    return getIntegerProperty(SCALING_FACTOR, 1);
  }
  
  public void setScalingFactor(int scalingFactor)
  {
    setProperty(SCALING_FACTOR, String.valueOf(scalingFactor));
  }
  
  public String getLanguage()
  {
    return getProperty(LANGUAGE, Locale.getDefault().getLanguage());
  }
  
  public void setLanguage(String language)
  {
    setProperty(LANGUAGE, language);
  }
  
  public int getWindowState()
  {
    return getIntegerProperty(WINDOW_STATE, Frame.NORMAL);
  }
  
  public void setWindowState(int state)
  {
    setProperty(WINDOW_STATE, String.valueOf(state));
  }
  
  public int getFontSize()
  {
    return getIntegerProperty(FONT_SIZE, 14);    
  }
  
  public void setFontSize(int size)
  {
    setProperty(FONT_SIZE, String.valueOf(size));
  }
  
  public int getIndentSize()
  {
    return getIntegerProperty(INDENT_SIZE, 2);    
  }
  
  public void setIndentSize(int size)
  {
    setProperty(INDENT_SIZE, String.valueOf(size));
  }
  
  public int getWindowWidth()
  {
    return getIntegerProperty(WINDOW_WIDTH, 800);
  }

  public void setWindowWidth(int width)
  {
    setProperty(WINDOW_WIDTH, String.valueOf(width));
  }
  
  public int getWindowHeight()
  {
    return getIntegerProperty(WINDOW_HEIGHT, 500);
  }

  public void setWindowHeight(int height)
  {
    setProperty(WINDOW_HEIGHT, String.valueOf(height));
  }
  
  public int getExplorerWidth()
  {
    return getIntegerProperty(EXPLORER_WIDTH, 200);    
  }

  public void setExplorerWidth(int width)
  {
    setProperty(EXPLORER_WIDTH, String.valueOf(width));
  }

  public int getAuxiliaryPanelWidth()
  {
    return getIntegerProperty(AUXILIARY_PANEL_WIDTH, 200);    
  }

  public void setAuxiliaryPanelWidth(int width)
  {
    setProperty(AUXILIARY_PANEL_WIDTH, String.valueOf(width));
  }
  
  public int getFormatColumns()
  {
    return getIntegerProperty(FORMAT_COLUMNS, 80);
  }
  
  public void setFormatColumns(int formatColumns)
  {
    setProperty(FORMAT_COLUMNS, String.valueOf(formatColumns));
  }

  public void load()
  {
    File file = getPreferencesFile();
    if (file.exists())
    {
      try
      {
        FileInputStream is = new FileInputStream(file);
        try
        {
          load(is);        
        }
        finally
        {
          is.close();
        }
      }
      catch (IOException ex)
      {
        // ignore
      }
    }
  }
  
  public void save()
  {
    try
    {
      File file = getPreferencesFile();
      FileOutputStream os = new FileOutputStream(file);
      try
      {
        this.store(os, "Brain4it preferences");
      }
      finally
      {
        os.close();
      }
    }
    catch (IOException ex)
    {
      // ignore
    }
  }
  
  private File getPreferencesFile()
  {
    String home = System.getProperty("user.home");
    return new File(home, ".brain4it.properties");
  }
  
  private int getIntegerProperty(String name, int defaultValue)
  {
    int value = defaultValue;
    String stringValue = getProperty(name);
    if (stringValue != null)
    {
      try
      {
        value = Integer.parseInt(stringValue);
      }
      catch (NumberFormatException ex)
      {
      }
    }
    return value;
  }
}
