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
package org.brain4it.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * A utility class to write text to a File or URL asynchronously.
 * 
 * @author realor
 */
public class Exporter
{
  public static final String FILE_SCHEME = "file://";  
  private final String url;
  private final String charset;
  
  public Exporter(String url, String charset)
  {
    this.url = url;
    this.charset = charset;
  }

  public Exporter(File file, String charset)
  {
    this.url = FILE_SCHEME + file.getAbsolutePath();
    this.charset = charset;
  }
  
  public void exportData(final String data)
  {
    Thread thread = new Thread()
    {
      @Override
      public void run()
      {
        try
        {
          if (url.startsWith(FILE_SCHEME))
          {
            File file = new File(url.substring(FILE_SCHEME.length()));
            IOUtils.writeString(data, charset, new FileOutputStream(file));
            onSuccess(data);
          }
          else throw new IOException("Unsupported scheme: " + url);
        }
        catch (IOException ex)
        {
          onError(ex);
        }
      }
    };
    thread.start();
  }
  
  protected void onSuccess(String data)
  {
  }

  protected void onError(Exception ex)
  {    
  }
}
