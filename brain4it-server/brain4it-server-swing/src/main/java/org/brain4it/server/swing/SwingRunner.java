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

package org.brain4it.server.swing;

import org.brain4it.server.standalone.Runner;
import org.brain4it.server.standalone.HttpServer;
import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Properties;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.brain4it.lib.SwingLibrary;
import org.brain4it.server.standalone.ServerLogManager;

/**
 *
 * @author realor
 */
public class SwingRunner extends JFrame
{
  public static final String FULLSCREEN = "fullscreen";
  
  public static SwingRunner instance;
  private final HttpServer server;
  private Screen screen;
          
  public SwingRunner(HttpServer server, boolean fullScreen)
  {
    this.server = server;
    initComponents(fullScreen);
  }

  public HttpServer getServer()
  {
    return server;
  }
  
  public Screen getScreen()
  {
    return screen;
  }
  
  public void exit()
  {
    server.stop();
    ServerLogManager.resetFinally();
    dispose();
  }
  
  private void initComponents(boolean fullScreen)
  {
    setTitle("brain4it");
    setAlwaysOnTop(true);
    if (fullScreen)
    {
      setUndecorated(true);
      setSize(Toolkit.getDefaultToolkit().getScreenSize());
    }
    else
    {
      setSize(500, 400);
    }
    screen = new Screen();
    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(screen, BorderLayout.CENTER);
    addMouseListener(new MouseAdapter()
    {
      @Override
      public void mousePressed(MouseEvent event)
      {
        if (event.getClickCount() > 1)
        {
          System.exit(0);
        }
      }
    });    
    addWindowListener(new WindowAdapter()
    {
      @Override
      public void windowClosing(WindowEvent e)
      {
        System.exit(0);
      }
    });
  }
  
  public static void main(String args[]) throws Exception
  {
    Properties properties = new Properties();
    Runner.loadProperties(args, properties);
    final HttpServer server = Runner.createServer(properties);
    server.getModuleManager().getLibraries().add(new SwingLibrary());
    Runner.runServer(server, properties);

    final boolean fs = "true".equals(properties.getProperty(FULLSCREEN));
    SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        SwingRunner.instance = new SwingRunner(server, fs);
        SwingRunner.instance.setVisible(true);
      }
    });
  }
}
