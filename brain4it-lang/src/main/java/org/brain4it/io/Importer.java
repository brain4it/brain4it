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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

/**
 *
 * @author realor
 */
public class Importer
{
  public static final String FILE_SCHEME = "file://";  
  private final String url;
  private final String charset;
  
  public Importer(String url, String charset)
  {
    this.url = url;
    this.charset = charset;
  }

  public Importer(File file, String charset)
  {
    this.url = FILE_SCHEME + file.getAbsolutePath();
    this.charset = charset;
  }
  
  public void importData()
  {
    Thread thread = new Thread()
    {
      @Override
      public void run()
      {
        try
        {
          URL u = new URL(url);
          String data = read(u.openStream(), charset);
          onSuccess(data);
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
  
  private String read(InputStream is, String charset) throws IOException
  {
    try
    {
      final char[] buffer = new char[1024];
      final StringBuilder out = new StringBuilder();
      Reader in = new InputStreamReader(is, charset);
      int numRead = in.read(buffer);
      while (numRead != -1)
      {
        out.append(buffer, 0, numRead);
        numRead = in.read(buffer);
      }
      return out.toString();
    }
    finally
    {
      is.close();
    }
  }  
}
