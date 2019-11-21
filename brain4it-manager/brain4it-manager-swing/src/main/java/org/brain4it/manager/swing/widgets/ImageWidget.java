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
package org.brain4it.manager.swing.widgets;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import org.brain4it.client.Monitor;
import org.brain4it.lang.BList;
import org.brain4it.lang.BSoftReference;
import org.brain4it.lang.Utils;
import org.brain4it.manager.swing.DashboardPanel;
import org.brain4it.manager.swing.DashboardWidget;
import org.brain4it.manager.video.MjpegStream;
import org.brain4it.manager.widgets.ImageWidgetType;
import org.brain4it.manager.widgets.WidgetType;
import org.brain4it.net.SSLUtils;

/**
 *
 * @author realor
 */
public class ImageWidget extends JComponent implements DashboardWidget
{
  protected DashboardPanel dashboard;
  protected String currentUrl;
  protected String urlFunction;
  protected BufferedImage image;
  protected VideoThread videoThread;

  protected Monitor.Listener monitorListener = new Monitor.Listener()
  {
    @Override
    public void onChange(String functionName, final Object value, 
      long serverTime)
    {
      if (value instanceof String)
      {
        String urlString = (String)value;
        if (!Utils.equals(urlString, currentUrl) || image == null)
        {
          currentUrl = urlString;
          if (isShowing())
          {
            showImage();
          }
        }
      }
    }
  };
  
  public ImageWidget()
  {
  }
  
  @Override
  public void init(DashboardPanel dashboard, String name, BList properties)
    throws Exception
  {
    this.dashboard = dashboard;
    
    ImageWidgetType type = 
      (ImageWidgetType)WidgetType.getType(WidgetType.IMAGE);

    type.validate(properties);
    
    BSoftReference func = type.getUrlFunction(properties);
    if (func != null)
    {
      urlFunction = func.getName();
    }

    if (dashboard != null)
    {
      if (urlFunction != null)
      {
        dashboard.getMonitor().watch(urlFunction, monitorListener);
      }
      
      addHierarchyListener(new HierarchyListener()
      {
        @Override
        public void hierarchyChanged(HierarchyEvent e)
        {
          if (isShowing())
          {
            showImage();
          }
          else
          {
            hideImage();
          }
        }
      });
    }
  }

  public void setImage(BufferedImage image)
  {
    this.image = image;
    repaint();
  }
  
  public BufferedImage getImage()
  {
    return image;
  }
  
  private void showImage()
  {
    if (videoThread != null)
    {
      videoThread.interrupt();
      videoThread.end = true;
      videoThread = null;
    }
    if (currentUrl == null)
    {
      setImage(null);
    }
    else
    {
      HttpURLConnection conn = null;
      try
      {
        URL url = new URL(currentUrl);
        conn = (HttpURLConnection)url.openConnection();
        SSLUtils.skipCertificateValidation(conn);
        String contentType = conn.getContentType();
        if (contentType == null || contentType.startsWith("image/"))
        {
          InputStream is = conn.getInputStream();
          try
          {
            setImage(ImageIO.read(is));
          }
          finally
          {
            is.close();
          }
        }
        else if (contentType.startsWith("multipart/x-mixed-replace"))
        {
          videoThread = new VideoThread(currentUrl);
          videoThread.start();       
        }
        else
        {
          setImage(null);
        }
      }
      catch (IOException ex)
      {
        setImage(null);
      }
      finally
      {
        if (conn != null) conn.disconnect();
      }
    }
  }

  private void hideImage()
  {
    if (videoThread != null)
    {
      videoThread.interrupt();
      videoThread.end = true;
      videoThread = null;
    }
    setImage(null);
  }
    
  @Override
  public void paintComponent(Graphics g)
  {
    int widgetWidth = getWidth();
    int widgetHeight = getHeight();
    g.setColor(Color.BLACK);
    g.fillRect(0, 0, widgetWidth, widgetHeight);
    if (image != null)
    {
      int imageWidth = image.getWidth();
      int imageHeight = image.getHeight();
      double widgetAspectRatio = (double)widgetWidth / widgetHeight;
      double imageAspectRatio = (double)imageWidth / imageHeight;
      int offsetX, offsetY;
      int width, height;
      if (widgetAspectRatio > imageAspectRatio)
      {
        height = widgetHeight;
        width = (int)(imageAspectRatio * height);
        offsetY = 0;
        offsetX = (widgetWidth - width) / 2;
      }
      else
      {
        width = widgetWidth;
        height = (int)(width / imageAspectRatio);
        offsetX = 0;
        offsetY = (widgetHeight - height) / 2;
      }
      g.drawImage(image, offsetX, offsetY, width, height, this);
    }
  }

  protected class VideoThread extends Thread
  {
    String url;
    boolean end;
    
    VideoThread(String url)
    {
      this.url = url;
    }
    
    @Override
    public void run()
    {
      try
      {
        MjpegStream stream = new MjpegStream(url);
        try
        {
          byte[] bytes = stream.readFrame();
          while (!end && bytes != null)
          {
            setImage(ImageIO.read(new ByteArrayInputStream(bytes)));
            bytes = stream.readFrame();
          }
        }
        finally
        {
          setImage(null);
          stream.close();
        }
      }
      catch (Exception ex)
      {
        // ignore
      }
    }
  }
}
