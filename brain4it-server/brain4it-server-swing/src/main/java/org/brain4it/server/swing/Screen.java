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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import org.brain4it.lang.BList;

/**
 *
 * @author realor
 */
public class Screen extends JComponent
{
  private BList scene;

  public BList getScene()
  {
    return scene;
  }

  public void setScene(BList scene)
  {
    this.scene = scene;
  }
  
  @Override
  public void paintComponent(Graphics g)
  {  
    Graphics2D g2 = (Graphics2D)g;

    int width = getWidth();
    int height = getHeight();

    g.setColor(Color.BLACK);
    g.fillRect(0, 0, width, height);
    
    if (scene != null)
    {
      g.setColor(Color.WHITE);

      g.setFont(new Font("Arial" , Font.PLAIN, 14));

      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
        RenderingHints.VALUE_ANTIALIAS_ON);        

      g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);    

      g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, 
        RenderingHints.VALUE_FRACTIONALMETRICS_ON);    
      
      Styles styles = getStyles(g2);
      paintNode(g2, scene, styles);
    }
  }
  
  protected void paintNode(Graphics2D g, BList node, Styles styles)
  {
    try
    {
      String type = String.valueOf(node.get(0));
      switch (type)
      {
        case "text":
          paintText(g, node, styles);
          break;
        case "line":
          paintLine(g, node, styles);
          break;
        case "rectangle":
          paintRectangle(g, node, styles);
          break;
        case "circle":
          paintCircle(g, node, styles);
          break;
        case "image":
          paintImage(g, node, styles);
          break;
        case "group":
          paintGroup(g, node, styles);
          break;
        default:
          break;
      }
    }
    catch (Exception ex)
    {
      // igonore exception
    }
  }
  
  protected void paintText(Graphics2D g, BList textNode, Styles parentStyles)
  {
    String text = (String)textNode.get(1);
    Number number;
        
    number = (Number)textNode.get(2);
    int x = number.intValue();

    number = (Number)textNode.get(3);
    int y = number.intValue();

    applyStyles(g, getStyles(textNode, parentStyles));
    // deprecated attributes
    if (textNode.has("size") || textNode.has("font"))
    {
      number = (Number)textNode.get("size");
      int size = number == null ? 20 : number.intValue();
      String fontValue = (String)textNode.get("font");
      String fontName = fontValue == null ? "Arial" : fontValue; 
      g.setFont(new Font(fontName, Font.PLAIN, size));
    }
    g.drawString(text, x, y);
  }  
  
  protected void paintLine(Graphics2D g, BList lineNode, Styles parentStyles)
  {
    Number number = (Number)lineNode.get(1);
    int x1 = number.intValue();

    number = (Number)lineNode.get(2);
    int y1 = number.intValue();
    
    number = (Number)lineNode.get(3);
    int x2 = number.intValue();

    number = (Number)lineNode.get(4);
    int y2 = number.intValue();

    applyStyles(g, getStyles(lineNode, parentStyles));
    g.drawLine(x1, y1, x2, y2);
  }

  protected void paintRectangle(Graphics2D g, BList lineNode, 
    Styles parentStyles)
  {
    Number number = (Number)lineNode.get(1);
    int x = number.intValue();

    number = (Number)lineNode.get(2);
    int y = number.intValue();
    
    number = (Number)lineNode.get(3);
    int width = number.intValue();

    number = (Number)lineNode.get(4);
    int height = number.intValue();

    Styles styles = getStyles(lineNode, parentStyles);    
    applyStyles(g, styles);
    if (styles.fill)
    {
      g.fillRect(x, y, width, height);
    }
    else
    {
      g.drawRect(x, y, width, height);
    }
  }
  
  protected void paintCircle(Graphics2D g, BList circleNode, 
    Styles parentStyles)
  {
    Number number;
        
    number = (Number)circleNode.get(1);
    int x = number.intValue();

    number = (Number)circleNode.get(2);
    int y = number.intValue();

    number = (Number)circleNode.get(3);
    int radius = number.intValue();

    Styles styles = getStyles(circleNode, parentStyles);
    applyStyles(g, styles);
    if (styles.fill)
    {
      g.fillOval(x - radius / 2, y - radius / 2, radius, radius);
    }
    else
    {
      g.drawOval(x - radius / 2, y - radius / 2, radius, radius);
    }
  }
  
  protected void paintImage(Graphics2D g, BList imageNode, Styles parentStyles)
    throws Exception
  {
    String url = (String)imageNode.get(1);
    Number number = (Number)imageNode.get(2);
    int x = number.intValue();

    number = (Number)imageNode.get(3);
    int y = number.intValue();

    BufferedImage image = ImageIO.read(new URL(url));

    if (imageNode.size() > 4)
    {
      number = (Number)imageNode.get(4);
      int width = number.intValue();

      number = (Number)imageNode.get(5);
      int height = number.intValue();
      
      Image scaledImage = 
       image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
      g.drawImage(scaledImage, x, y, this);
    }
    else
    {
      g.drawImage(image, x, y, this);      
    }
  }
  
  protected void paintGroup(Graphics2D g, BList groupNode, Styles parentStyles)
  {
    Number number;
    
    int offsetX = 0;
    int offsetY = 0;    
    number = (Number)groupNode.get("offset-x");
    if (number != null)
    {
      offsetX = number.intValue();
    }
    number = (Number)groupNode.get("offset-y");
    if (number != null)
    {
      offsetY = number.intValue();
    }
    g.translate(offsetX, offsetY);
    
    double rotation = 0;
    number = (Number)groupNode.get("rotation");
    if (number != null)
    {
      rotation = number.doubleValue();
    }
    g.rotate(-rotation * Math.PI / 180);
    
    Styles styles = getStyles(groupNode, parentStyles);
    applyStyles(g, styles);
    for (int i = 0; i < groupNode.size(); i++)
    {
      Object node = groupNode.get(i);
      if (node instanceof BList)
      {
        paintNode(g, (BList)node, styles);
      }
    }
    g.rotate(rotation * Math.PI / 180);    

    g.translate(-offsetX, -offsetY);
  }
  
  protected Styles getStyles(Graphics2D g)
  {
    Styles styles = new Styles();
    styles.color = g.getColor();
    styles.font = g.getFont();
    styles.fill = false;
    styles.stroke = g.getStroke();
    if (styles.stroke == null) styles.stroke = new BasicStroke(1);
    return styles;
  }
  
  protected Styles getStyles(BList node, Styles parentStyles)
  {
    Styles styles;
    
    Boolean fill = (Boolean)node.get("fill");
    String colorValue = (String)node.get("color");
    Number lineWidth = (Number)node.get("line-width");
    String fontFamily = (String)node.get("font-family");
    Number fontSize = (Number)node.get("font-size");
    
    if (fill != null || colorValue != null || lineWidth != null || 
        fontFamily != null || fontSize != null)
    {
      styles = new Styles();
      
      styles.fill = fill == null ? parentStyles.fill : fill;

      styles.color = colorValue == null ? 
        parentStyles.color : Color.decode(colorValue);

      styles.stroke = lineWidth == null ? 
        parentStyles.stroke : new BasicStroke(lineWidth.intValue());
      if (fontFamily != null || fontSize != null)
      {
        if (fontFamily == null) fontFamily = parentStyles.font.getFontName();
        if (fontSize == null) fontSize = parentStyles.font.getSize();
        styles.font = new Font(fontFamily, Font.PLAIN, fontSize.intValue());    
      }
      else
      {
        styles.font = parentStyles.font;
      }
    }
    else
    {
      styles = parentStyles;
    }
    return styles;
  }
  
  protected void applyStyles(Graphics2D g, Styles styles)
  {
    g.setColor(styles.color);
    g.setStroke(styles.stroke);
    g.setFont(styles.font);
  }
  
  protected class Styles
  {
    Font font;
    Color color;
    Stroke stroke;
    boolean fill;
  }
}
