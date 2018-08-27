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
package org.brain4it.server.standalone;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author realor
 */
public class ShutdownFileMonitor extends Thread
{
  private final String filename;
  private static final Logger LOGGER = Logger.getLogger("ShutdownFileMonitor");
  
  public ShutdownFileMonitor(String filename)
  {
    this.filename = filename;
  }
  
  @Override
  public void run()
  {
    File file = new File(filename);
    try
    {
      file.createNewFile();
    }
    catch (IOException ex)
    {
      LOGGER.log(Level.WARNING, "Shutdown file {0} could not be created: {1}", 
        new Object[]{filename, ex.toString()});
      return;
    }

    try
    {
      LOGGER.log(Level.INFO, "Remove file {0} to stop server.", 
        file.getCanonicalPath());
      while (file.exists())
      {
        Thread.sleep(1000);
      }
    }
    catch (Exception ex)
    {
      // ignore
    }
    finally
    {
      System.exit(0);
    }
  }
}
