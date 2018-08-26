package org.brain4it.manager.swing.layout;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Container;
import java.awt.Insets;
import java.awt.LayoutManager;

public class VerticalFlowLayout implements LayoutManager
{
  protected int vgap = 4;

  public VerticalFlowLayout()
  {
  }

  public void setVGap(int vgap)
  {
    this.vgap = vgap;
  }

  public int getVGap()
  {
    return vgap;
  }

  @Override
  public void addLayoutComponent(String name, Component comp)
  {
  }

  @Override
  public void removeLayoutComponent(Component comp)
  {
  }

  @Override
  public Dimension preferredLayoutSize(Container target)
  {
    return minimumLayoutSize(target);
  }

  @Override
  public Dimension minimumLayoutSize(Container target)
  {
    synchronized (target.getTreeLock())
    {
      Insets insets = target.getInsets();
      int nmembers = target.getComponentCount();
      int x = insets.left;
      int y = insets.top;
      Dimension dim = new Dimension(0, 0);

      for (int i = 0; i < nmembers; i++)
      {
        Component m = target.getComponent(i);
        if (m.isVisible())
        {
          Dimension d = m.getPreferredSize();
          y += d.height + vgap;
          dim.width = Math.max(dim.width, x + d.width);
          dim.height = y;
        }
      }
      return dim;
    }
  }

  @Override
  public void layoutContainer(Container target)
  {
    synchronized (target.getTreeLock())
    {
      Insets insets = target.getInsets();
      int nmembers = target.getComponentCount();
      int x = insets.left;
      int y = insets.top;

      for (int i = 0; i < nmembers; i++)
      {
        Component m = target.getComponent(i);
        if (m.isVisible())
        {
          m
          .setBounds(0, 0, target.getWidth() - insets.left - insets.right, 1);
          Dimension d = m.getPreferredSize();
          m.setLocation(x, y);
          m.setSize(target.getWidth(), d.height);
          y += d.height + vgap;
        }
      }
    }
  }
}