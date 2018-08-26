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

package org.brain4it.lib;

import com.github.sarxos.webcam.Webcam;
import static com.github.sarxos.webcam.Webcam.getDiscoveryServiceRef;
import com.github.sarxos.webcam.WebcamDiscoveryService;
import com.github.sarxos.webcam.WebcamProcessor;
import com.github.sarxos.webcam.ds.v4l4j.V4l4jDriver;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import org.brain4it.lib.vision.StartVideoServerFunction;
import org.brain4it.lib.vision.StopVideoServerFunction;
import org.brain4it.lib.vision.VideoCamerasFunction;
import org.brain4it.lib.vision.VideoServer;
import org.brain4it.lib.vision.VisionFiducialsFunction;

/**
 *
 * @author realor
 */
public class VisionLibrary extends Library
{
  Map<Integer, VideoServer> videoServers = 
    Collections.synchronizedMap(new HashMap<Integer, VideoServer>());
  
  @Override
  public String getName()
  {
    return "Vision";
  }

  @Override
  public void load()
  {
    if ("true".equals(System.getProperty("v4l4j.enabled")))
    {
      Webcam.setDriver(new V4l4jDriver());
    }
    functions.put("video-cameras", new VideoCamerasFunction(this));
    functions.put("start-video-server", new StartVideoServerFunction(this));
    functions.put("stop-video-server", new StopVideoServerFunction(this));
    functions.put("vision-fiducials", new VisionFiducialsFunction(this));
  }
    
  @Override
  public void unload()
  {
    for (VideoServer videoServer : videoServers.values())
    {
      videoServer.finish();
    }
    videoServers.clear();

		WebcamDiscoveryService discovery = getDiscoveryServiceRef();
		if (discovery != null) 
    {
			discovery.stop();
		}

    WebcamProcessor processor = WebcamProcessor.getInstance();
    // Threre is a BUG in processor.shutdown() method (version 0.3.12):
    // processor.shutdown();
    // call workaround method instead:
    shutdownProcessor(processor);
  }

  public void registerVideoServer(VideoServer videoServer)
  {
    videoServers.put(videoServer.getPort(), videoServer);
  }
  
  public void unregisterVideoServer(VideoServer videoServer)
  {
    videoServers.remove(videoServer.getPort());    
  }
  
  public VideoServer getVideoServer(int port)
  {
    return videoServers.get(port);
  }
  
  private void shutdownProcessor(WebcamProcessor processor)
  {
    try
    {
      Field runnerField = processor.getClass().getDeclaredField("runner");
      runnerField.setAccessible(true);
      ExecutorService runner = (ExecutorService)runnerField.get(null);
      runner.shutdown();
			while (!runner.isTerminated())
      {
				try 
        {
					runner.awaitTermination(100, TimeUnit.MILLISECONDS);
				} 
        catch (InterruptedException e) 
        {
					return;
				}
				runner.shutdownNow();
			}
    }
    catch (Exception ex)
    {
    }    
  }
}
