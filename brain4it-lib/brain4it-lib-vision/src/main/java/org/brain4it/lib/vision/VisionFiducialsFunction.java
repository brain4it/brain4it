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

import boofcv.abst.fiducial.FiducialDetector;
import boofcv.alg.distort.LensDistortionNarrowFOV;
import boofcv.alg.distort.radtan.LensDistortionRadialTangential;
import boofcv.alg.geo.PerspectiveOps;
import boofcv.alg.geo.WorldToCameraToPixel;
import boofcv.factory.fiducial.ConfigFiducialBinary;
import boofcv.factory.fiducial.FactoryFiducial;
import boofcv.factory.filter.binary.ConfigThreshold;
import boofcv.factory.filter.binary.ThresholdType;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.calib.CameraPinholeRadial;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.ImageType;
import georegression.struct.point.Point2D_F64;
import georegression.struct.point.Point3D_F64;
import georegression.struct.se.Se3_F64;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import javax.imageio.ImageIO;
import org.ejml.data.DenseMatrix64F;
import org.brain4it.lang.Context;
import org.brain4it.lang.BList;
import org.brain4it.lang.Structure;
import org.brain4it.lang.Utils;
import org.brain4it.lib.VisionLibrary;

/**
 *
 * @author realor
 */
public class VisionFiducialsFunction extends VisionFunction
{
  public VisionFiducialsFunction(VisionLibrary library)
  {
    super(library);
  }
  
  @Override
  public Object invoke(Context context, BList args) throws Exception
  {
    Utils.checkArguments(args, 1);    
    String path = (String)context.evaluate(args.get(1));
    BufferedImage image;
    if (path.contains("://"))
    {
      URL url = new URL(path);
      image = ImageIO.read(url);
    }
    else
    {
      File file = new File(path);
      image = ImageIO.read(file);
    }
    
    int width = image.getWidth();
    int height = image.getHeight();
        
    GrayF32 original = ConvertBufferedImage.convertFrom(image, true, 
      ImageType.single(GrayF32.class));

    FiducialDetector<GrayF32> detector = FactoryFiducial.squareBinary(
      new ConfigFiducialBinary(0.1),
      ConfigThreshold.local(ThresholdType.LOCAL_SQUARE, 10), GrayF32.class);

    CameraPinholeRadial parameters = new CameraPinholeRadial();
    parameters.skew = 0;
    parameters.width = width;
    parameters.height = height;

    BList pinhole = (BList)args.get("camera-parameters");
    if (pinhole == null)
    {
      parameters.cx = 316.67722865086887;
      parameters.cy = 226.93146144997266;
      parameters.fx = 529.0340750592192;
      parameters.fy = 528.7980189489572;
    }
    else
    {
      parameters.cx = (double)pinhole.get(0);
      parameters.cy = (double)pinhole.get(1);
      parameters.fx = (double)pinhole.get(2);
      parameters.fy = (double)pinhole.get(3);     
    }
    // detect fiducials
    LensDistortionNarrowFOV lensDistortion = 
      new LensDistortionRadialTangential(parameters);    
    
    detector.setLensDistortion(lensDistortion);
    detector.detect(original);
   
    BList result = new BList();
        
    BList list = new BList(2);
    list.add(width);
    list.add(height);
    result.put("size", list);

    BList detections = new BList();
    result.put("detections", detections);
    Structure structure = new Structure(
      "id", "origin", "x-axis", "y-axis", "z-axis", "normal");

    Point3D_F64 originPoint = new Point3D_F64(0, 0, 0);
		Point3D_F64 xAxisPoint = new Point3D_F64(1, 0, 0);
		Point3D_F64 yAxisPoint = new Point3D_F64(0, 1, 0);
		Point3D_F64 zAxisPoint = new Point3D_F64(0, 0, 1);
		Point2D_F64 screenPoint = new Point2D_F64();

    Se3_F64 transformation = new Se3_F64();
    for (int i = 0; i < detector.totalFound(); i++)
    {
      BList target = new BList(structure);
      detector.getFiducialToCamera(i, transformation);
		  
      WorldToCameraToPixel worldToPixel = 
        PerspectiveOps.createWorldToPixel(parameters, transformation);
      
      // fiducial id
      target.put("id", detector.getId(i));
            
      if (detector.is3D())
      {
        double fiducialWidth = detector.getWidth(i);
        
        // origin
        worldToPixel.transform(originPoint, screenPoint);
        list = new BList(2);
        list.add(screenPoint.x);
        list.add(screenPoint.y);
        target.put("origin", list);

        // xAxis
        xAxisPoint.x = fiducialWidth;
        worldToPixel.transform(xAxisPoint, screenPoint);
        list = new BList(2);
        list.add(screenPoint.x);
        list.add(screenPoint.y);
        target.put("x-axis", list);

        // yAxis      
        yAxisPoint.y = fiducialWidth;
        worldToPixel.transform(yAxisPoint, screenPoint);
        list = new BList(2);
        list.add(screenPoint.x);
        list.add(screenPoint.y);
        target.put("y-axis", list);

        // zAxis 
        zAxisPoint.z = fiducialWidth;
        worldToPixel.transform(zAxisPoint, screenPoint);
        list = new BList(2);
        list.add(screenPoint.x);
        list.add(screenPoint.y);
        target.put("z-axis", list);

        DenseMatrix64F normal = transformation.getRotation();
        double vx = normal.get(0, 2);
        double vy = normal.get(1, 2);
        double vz = normal.get(2, 2);
        list = new BList(3);
        list.add(vx);
        list.add(vy);
        list.add(vz);
        target.put("normal", list);
      }
      detections.add(target);
    }
    return result;
  }
}
