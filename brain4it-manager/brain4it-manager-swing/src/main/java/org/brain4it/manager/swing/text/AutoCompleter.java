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
package org.brain4it.manager.swing.text;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.InputMap;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JWindow;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.brain4it.io.IOConstants;
import org.brain4it.lang.BException;
import org.brain4it.lang.BSoftReference;
import org.brain4it.manager.TextCompleter;
import org.brain4it.manager.TextCompleter.Candidate;
import org.brain4it.manager.TextCompleter.OnCompleteListener;
import org.brain4it.manager.swing.CandidateRenderer;

/**
 *
 * @author realor
 */
public class AutoCompleter implements OnCompleteListener
{
  private final JTextComponent textComponent;
  private TextCompleter textCompleter;
  private boolean enabled;
  private JWindow window;
  private JList optionsList;
  private final KeyListener keyListener;
  private final FocusListener focusListener;
  private Action preTabAction;
  private Action preUpAction;
  private Action preDownAction;
  private Action preEnterAction;

  public AutoCompleter(JTextComponent textComponent)
  {
    this.textComponent = textComponent;
    keyListener = new KeyAdapter()
    {
      @Override
      public void keyReleased(KeyEvent e)
      {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_TAB) return;
        if (keyCode == KeyEvent.VK_ENTER) return;
        if (keyCode == KeyEvent.VK_UP) return;
        if (keyCode == KeyEvent.VK_DOWN) return;

        if (window != null)
        {
          if (keyCode == KeyEvent.VK_ESCAPE || 
              keyCode == KeyEvent.VK_LEFT || 
              keyCode == KeyEvent.VK_RIGHT)
          {
            window.dispose();
            window = null;
          }
          else
          {
            completeCursor();
          }
        }
      }
    };
    focusListener = new FocusAdapter()
    {
      @Override
      public void focusLost(FocusEvent e)
      {
        if (window != null)
        {
          window.dispose();
          window = null;
        }        
      }      
    };
  }

  public boolean isEnabled()
  {
    return enabled;
  }

  public void setEnabled(boolean enabled)
  {
    if (this.enabled != enabled)
    {
      InputMap inputMap = textComponent.getInputMap();
      KeyStroke tab = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
      KeyStroke up = KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0);
      KeyStroke down = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0);
      KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);

      if (enabled)
      {
        preTabAction = textComponent.getActionMap().get(inputMap.get(tab)); 
        preUpAction = textComponent.getActionMap().get(inputMap.get(up));
        preDownAction = textComponent.getActionMap().get(inputMap.get(down));
        preEnterAction = textComponent.getActionMap().get(inputMap.get(enter));

        textComponent.getActionMap().put(inputMap.get(tab), new TabAction());
        textComponent.getActionMap().put(inputMap.get(up), new UpAction());
        textComponent.getActionMap().put(inputMap.get(down), new DownAction());
        textComponent.getActionMap().put(inputMap.get(enter), new EnterAction());
        textComponent.addKeyListener(keyListener);
        textComponent.addFocusListener(focusListener);
      }
      else
      {
        textComponent.getActionMap().put(inputMap.get(tab), preTabAction);
        textComponent.getActionMap().put(inputMap.get(up), preUpAction);
        textComponent.getActionMap().put(inputMap.get(down), preDownAction);
        textComponent.getActionMap().put(inputMap.get(enter), preEnterAction);
        textComponent.removeKeyListener(keyListener);
        textComponent.removeFocusListener(focusListener);
      }
      this.enabled = enabled;
    }
  }

  public TextCompleter getTextCompleter()
  {
    return textCompleter;
  }

  public void setTextCompleter(TextCompleter textCompleter)
  {
    this.textCompleter = textCompleter;
  }
  
  private void completeCursor()
  {
    if (textCompleter == null) return;
    try
    {
      String head = findHead();
      if (head != null)
      {
        if (head.length() > 0)
        {
          BSoftReference.getInstance(head);
        }
        textCompleter.complete(head, this);
      }
    }
    catch (BException ex)
    {
      // ignore, head is not reference
    }
  }

  private String findHead()
  {
    try
    {
      int pos = textComponent.getCaretPosition();
      String text = textComponent.getText(0, pos);
      return textCompleter.findHead(text);
    }
    catch (BadLocationException ex)
    {      
    }
    return null;
  }
  
  @Override
  public void textCompleted(final String head, final List<Candidate> candidates)
  {
    SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        if (candidates.size() > 0)
        {
          SwingUtilities.invokeLater(new Runnable()
          {
            @Override
            public void run()
            {
              if (candidates.size() == 1)
              {
                if (window == null)
                {
                  String name = candidates.get(0).getName();
                  String currentHead = findHead();
                  int index = currentHead.lastIndexOf(
                    IOConstants.PATH_REFERENCE_SEPARATOR);
                  if (index != -1)
                  {
                    currentHead = currentHead.substring(index + 1);
                  }
                  insertText(name.substring(currentHead.length()));
                }
                else
                {
                  showOptions(candidates);
                }
              }
              else
              {
                showOptions(candidates);
              }
            }
          });
        }
        else
        {
          if (window != null)
          {
            window.dispose();
            window = null;
          }
        }
      }
    });
  }
  
  private void insertText(String text)
  {
    try
    {
      int pos = textComponent.getCaretPosition();
      textComponent.getDocument().insertString(pos, text, null);
    }
    catch (BadLocationException ex)
    {
    }
  }

  private void optionSelected()
  {
    window.dispose();
    window = null;
    Candidate candidate = (Candidate)optionsList.getSelectedValue();
    String name = candidate.getName();
    String head = findHead();
    int index = head.lastIndexOf(IOConstants.PATH_REFERENCE_SEPARATOR);
    if (index == -1)
    {
      insertText(name.substring(head.length()));
    }
    else
    {
      String lastHead = head.substring(index + 1);
      insertText(name.substring(lastHead.length()));
    }
  }

  public class TabAction extends AbstractAction
  {
    @Override
    public void actionPerformed(ActionEvent e)
    {
      if (window == null)
      {
        completeCursor();
      }
    }
  }

  public class UpAction extends AbstractAction
  {
    @Override
    public void actionPerformed(ActionEvent e)
    {
      if (window != null)
      {
        int size = optionsList.getModel().getSize();
        int index = optionsList.getSelectedIndex();
        index--;
        if (index < 0) index = size - 1;
        optionsList.setSelectedIndex(index);
        Rectangle bounds = optionsList.getCellBounds(index, index);
        optionsList.scrollRectToVisible(bounds);
      }
      else
      {
        preUpAction.actionPerformed(e);
      }
    }
  }
  
  public class DownAction extends AbstractAction
  {
    @Override
    public void actionPerformed(ActionEvent e)
    {
      if (window != null)
      {
        int size = optionsList.getModel().getSize();
        int index = optionsList.getSelectedIndex();
        index++;
        if (index >= size) index = 0;
        optionsList.setSelectedIndex(index);
        Rectangle bounds = optionsList.getCellBounds(index, index);
        optionsList.scrollRectToVisible(bounds);
      }
      else
      {
        preDownAction.actionPerformed(e);
      }
    }
  }

  public class EnterAction extends AbstractAction
  {
    @Override
    public void actionPerformed(ActionEvent e)
    {
      if (window != null)
      {
        optionSelected();
      }
      else
      {
        preEnterAction.actionPerformed(e);
      }
    }
  }
  
  private void showOptions(List<Candidate> candidates) 
  {
    if (window != null)
    {
      window.dispose();
    }    
    JFrame frame = (JFrame)textComponent.getRootPane().getParent();
    window = new JWindow(frame);
    window.setAlwaysOnTop(true);
    window.setFocusableWindowState(false);
    optionsList = new JList();
    optionsList.setFont(textComponent.getFont());
    optionsList.setCellRenderer(new CandidateRenderer());
    optionsList.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mouseClicked(MouseEvent e)
      {
        if (e.getClickCount() > 1)
        {
          optionSelected();
        }
      }
    });

    JScrollPane scrollPane = new JScrollPane(optionsList,
      JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
      JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    window.setContentPane(scrollPane);
    DefaultListModel<Candidate> model = new DefaultListModel<Candidate>();
    for (Candidate candidate : candidates)
    {
      model.addElement(candidate);
    }
    optionsList.setModel(model);
    optionsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    try
    {
      Point point = textComponent.getLocationOnScreen();
      Rectangle rect = textComponent.getUI().modelToView(textComponent,
        textComponent.getCaretPosition());
      point.translate((int)rect.getX(), (int)(rect.getY() + rect.getHeight()));
      optionsList.setSelectedIndex(0);
      optionsList.setVisibleRowCount(Math.min(candidates.size(), 6));
      window.pack();
      Dimension size = window.getSize();
      size.width += 40;
      int bottomY = point.y + size.height;
      Rectangle bounds = GraphicsEnvironment.getLocalGraphicsEnvironment().
        getMaximumWindowBounds();
      if (bottomY > bounds.y + bounds.getHeight())
      {
        int fontSize = textComponent.getFont().getSize();
        point.y = point.y - size.height - fontSize;
      }
      window.setSize(size);
      window.setLocation(point);
      window.setVisible(true);
    }
    catch (BadLocationException ex)
    {
    }
  }
}
