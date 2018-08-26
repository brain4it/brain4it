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
package org.brain4it.lib.vision;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import org.brain4it.lang.BList;
import org.brain4it.lang.Context;
import org.brain4it.lang.Utils;
import org.brain4it.lib.VisionLibrary;

/**
 *
 * @author realor
 */
public class StartVideoServerFunction extends VisionFunction
{
  public StartVideoServerFunction(VisionLibrary library)
  {
    super(library);
  }
  
  @Override
  public Object invoke(Context context, BList args) throws Exception
  {
    int port = 8080;
    WebcamResolution resolution = WebcamResolution.VGA;    
    Webcam webcam;
    Object value;

    value = context.evaluate(args.get("camera"));
    if (value instanceof String)
    {
      String name = String.valueOf(value);
      webcam = Webcam.getWebcamByName(name);
    }
    else
    {
      webcam = Webcam.getDefault();
    }
    if (webcam == null) return null;
    
    value = context.evaluate(args.get("port"));
    if (value instanceof Number)
    {
      port = Utils.toNumber(value).intValue();
    }
    
    value = context.evaluate(args.get("resolution"));
    if (value instanceof String)
    {
      String name = String.valueOf(value).toUpperCase();
      resolution = WebcamResolution.valueOf(name);
    }
    
    VideoServer videoServer = library.getVideoServer(port);
    if (videoServer != null)
    {
      videoServer.finish();
    }

    webcam.setViewSize(resolution.getSize());
    videoServer = new VideoServer(webcam, port);
    library.registerVideoServer(videoServer);
    videoServer.start();
    
    return "video server listening on port " + port + 
      ", resolution: " + resolution;
  }
}
