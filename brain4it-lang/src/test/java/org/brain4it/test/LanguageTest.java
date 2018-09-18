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

package org.brain4it.test;

import org.brain4it.io.Parser;
import org.brain4it.io.Printer;
import org.brain4it.lang.*;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author realor
 */
public class LanguageTest
{
  @Test
  public void testReferences() throws Exception
  {
    BSoftReference reference;
    reference = BSoftReference.getInstance("alfa");
    assertTrue(reference instanceof BSingleReference);
    assertEquals("alfa", reference.getName(), "alfa");

    reference = BSoftReference.getInstance("alfa9_34");
    assertTrue(reference instanceof BSingleReference);
    assertEquals("alfa9_34", reference.getName());

    reference = BSoftReference.getInstance("/");
    assertTrue(reference instanceof BSingleReference);
    assertEquals("/", reference.getPath().get(0));

    reference = BSoftReference.getInstance("//");
    assertTrue(reference instanceof BSingleReference);
    assertEquals("//", reference.getPath().get(0));
    
    reference = BSoftReference.getInstance("alfa/9");
    assertTrue(reference instanceof BPathReference);
    assertEquals(2, reference.getPath().size());
    assertEquals("alfa", reference.getPath().get(0));
    assertEquals(9, reference.getPath().get(1));    

    reference = BSoftReference.getInstance("alfa/beta");
    assertTrue(reference instanceof BPathReference);
    assertEquals(2, reference.getPath().size());
    assertEquals("alfa", reference.getPath().get(0));
    assertEquals("beta", reference.getPath().get(1));    

    reference = BSoftReference.getInstance("alfa/beta/gamma");
    assertTrue(reference instanceof BPathReference);
    assertEquals(3, reference.getPath().size());
    assertEquals("alfa", reference.getPath().get(0));
    assertEquals("beta", reference.getPath().get(1));    
    assertEquals("gamma", reference.getPath().get(2));    

    reference = BSoftReference.getInstance("/alfa");
    assertEquals(1, reference.getPath().size());
    assertEquals("alfa", reference.getPath().get(0));

    reference = BSoftReference.getInstance("alfa/");
    assertEquals(1, reference.getPath().size());
    assertEquals("alfa", reference.getPath().get(0));

    reference = BSoftReference.getInstance("/alfa/");
    assertEquals(1, reference.getPath().size());
    assertEquals("alfa", reference.getPath().get(0));    

    reference = BSoftReference.getInstance("///alfa///");
    assertEquals(1, reference.getPath().size());
    assertEquals("alfa", reference.getPath().get(0));    

    reference = BSoftReference.getInstance("/\"alfa(5)/\"");
    assertEquals(1, reference.getPath().size());
    assertEquals("alfa(5)/", reference.getPath().get(0));    
    assertEquals("/\"alfa(5)/\"", reference.getName());    

    BList path = new BList();
    path.add(" Hello world!");
    path.add(9);
    reference = new BPathReference(path);
    assertEquals(reference.getPath().size(), 2);
    assertEquals(reference.getPath().get(0), " Hello world!");
    assertEquals(reference.getPath().get(1), 9);
    assertEquals(reference.getName(), "/\" Hello world!\"/9");
    
    try
    {
      BSoftReference.getInstance("aaa\"hjh\"");
      fail("Accepted invalid reference: " + "aaa\"hjh\"");
    }
    catch (BException ex)
    {      
    }

    try
    {
      BSoftReference.getInstance("/aaa bbb");
      fail("Accepted invalid reference: " + "/aaa bbb");
    }
    catch (BException ex)
    {      
    }

  }
  
  @Test
  public void testLists() throws Exception
  {
    BList list = new BList();
    assertEquals(0, list.size());
    
    list.add("hello");
    assertEquals(1, list.size());
    
    list.add("world");
    assertEquals(2, list.size());
    
    list.remove(0);
    assertEquals(1, list.size());
    
    list.insert(0, "hello");
    assertEquals(2, list.size());
    
    list.put("a", 89);    
    assertEquals(89, list.get("a"));
    assertEquals(null, list.get("b"));
    
    list.insert(1, true);
    assertEquals(true, list.get(1));
    assertEquals("(\"hello\" true \"world\" \"a\" => 89)", 
      Printer.toString(list));
    
    assertEquals("a", list.getName(3));
    
    list.putName(3, "alfa");
    assertEquals("alfa", list.getName(3));
    
    list.insert(2, 15);
    list.putName(2, "color");
    assertEquals("(\"hello\" true \"color\" => 15 \"world\" \"alfa\" => 89)", 
      Printer.toString(list));
    
    list.remove(1);
    list.remove(1);
    assertEquals(3, list.size());
    assertEquals("(\"hello\" \"world\" \"alfa\" => 89)", 
      Printer.toString(list));

    list.putName(0, "alfa");
    assertEquals("(\"alfa\" => \"hello\" \"world\" 89)", 
      Printer.toString(list));
    
    list.remove(2);
    list.putName(0, null);
    assertEquals("(\"hello\" \"world\")", 
      Printer.toString(list));
    
    Structure structure = new Structure("name", "sex", "age");
    BList slist = new BList(structure);
    assertEquals(3, structure.size());

    assertEquals(slist.getName(0), "name");
    assertEquals(slist.getName(1), "sex");
    assertEquals(slist.getName(2), "age");
    slist.put("name", "Ricard");
    slist.put("age", 27);
    slist.add(88);
    assertEquals("(\"name\" => \"Ricard\" \"sex\" => null \"age\" => 27 88)", 
      Printer.toString(slist));    

    list = new BList();

    BList listA = new BList();
    listA.add(8);
    listA.add(9);
    listA.put("name" ,"Ricard");
    BList listB = listA.clone();
    BList listC = listB.clone();
    listC.put("name", 0);
    
    list.add(listA);
    list.add(listB);
    list.add(listB);
    list.add(listC);

    String text = Printer.toString(list);
    BList result = (BList)Parser.fromString(text);
    assertEquals(Printer.toString(result),
      "((<#1> 8 9 \"name\" => \"Ricard\") (<#2:1> 8 9 \"name\" => \"Ricard\") <@2> (<#:1> 8 9 \"name\" => 0))");
    
    structure = new Structure("name", "age");
    listA.setStructure(structure);
    assertEquals(Printer.toString(listA), "(\"name\" => 8 \"age\" => 9)");
  }
}
