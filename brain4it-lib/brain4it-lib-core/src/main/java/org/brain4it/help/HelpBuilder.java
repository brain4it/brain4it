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

package org.brain4it.help;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.stream.Stream;
import org.brain4it.lang.BList;
import org.brain4it.io.Parser;
import org.brain4it.lang.BSingleReference;
import org.brain4it.lang.BSoftReference;
import org.brain4it.lang.Context;
import org.brain4it.lang.WrapperFunction;
import org.brain4it.lang.Function;
import org.brain4it.lang.Utils;
import org.brain4it.lib.Library;
import org.brain4it.lib.LibraryFactory;

/**
 *
 * @author realor
 */
public class HelpBuilder
{
  // help source contants
  public static final String HELP_SUFFIX = "_help";
  public static final String HELP_EXTENSION = ".txt";

  // help list sections
  public static final String WHERE = "where";
  public static final String EXCEPTIONS = "exceptions";
  public static final String EXAMPLES = "examples";
  public static final String RELATED = "related";
  public static final String SYNOPSIS = "synopsis";
  public static final String DESCRIPTION = "description";
  public static final String GROUP = "group";
  public static final String BUNDLE_NAME = "bundle";
  
  public static BList buildHelp(Context context, String functionName,
     Locale locale) throws IOException, ParseException
  {
    BList help = null;
    Function function = context.getFunctions().get(functionName);
    if (function != null)
    {
      help = HelpBuilder.readHelp(function);
      if (help != null)
      {
        BList parameters = (BList)help.get(1);
        parameters.put(0, BSoftReference.getInstance(functionName));
      }
    }
    else
    {
      Object value = context.get(functionName);
      if (value instanceof BList)
      {
        BList userFunction = (BList)value;
        if (context.isUserFunction(userFunction))
        {
          Object object = context.get(functionName + HELP_SUFFIX);
          if (object instanceof BList)
          {
            help = ((BList)object).clone(true);
          }
          else // create a help list from function code
          {
            help = new BList();
            help.add(new BSingleReference(functionName)); // functionTag
            BList usage = new BList();
            usage.add(new BSingleReference(functionName));
            Object args = userFunction.get(1);
            if (args instanceof BList)
            {
              usage.addAll((BList)args);
            }
            else
            {
              usage.add(args);
            }
            help.add(usage);
            help.add(new BSingleReference("result")); // return
          }
        }
      }
    }
    return mergeHelp(help, locale, context);
  }
  
  public static List<BList> buildHelp(Class<? extends Library> libraryClass, 
    Locale locale) throws IOException, ParseException
  {
    String libraryPkg = libraryClass.getName();
    libraryPkg = "/" + libraryPkg.substring(0, libraryPkg.length() - 
      LibraryFactory.LIBRARY_SUFFIX.length()).toLowerCase().replace('.', '/');

    URI uri;
    try
    {
      URL location = libraryClass.getResource(libraryPkg);
      uri = location.toURI();
    }
    catch (URISyntaxException ex)
    {
      throw new IOException(ex);
    }
    Path path;
    if (uri.getScheme().equals("jar")) 
    {
      FileSystem fileSystem;
      try
      {
        fileSystem = FileSystems.getFileSystem(uri);    
      }
      catch (FileSystemNotFoundException ex)
      {
        fileSystem = FileSystems.newFileSystem(uri, 
        Collections.<String, Object>emptyMap());
      }
      path = fileSystem.getPath(libraryPkg);
    }
    else
    {
      path = Paths.get(uri);
    }
    
    ArrayList<BList> helpList = new ArrayList<BList>();    

    Stream<Path> walk = Files.walk(path, 3);
    Iterator<Path> iter = walk.iterator();
    while (iter.hasNext())
    {
      path = iter.next();
      String pathName = path.toString().replace('\\', '/');
      if (pathName.endsWith(HELP_EXTENSION))
      {
        int index = pathName.lastIndexOf(libraryPkg);
        String url = pathName.substring(index);
        System.out.println(url);
        InputStream is = libraryClass.getResource(url).openStream();
        BList readHelp = readHelp(is);
        helpList.add(readHelp);
        String relativeUrl = url.substring(libraryPkg.length() + 1);
        index = relativeUrl.indexOf("/");
        if (index != -1)
        {
          String group = relativeUrl.substring(0, index);
          readHelp.put(GROUP, group);
        }
      }
    }
    walk.close();

    for (BList help : helpList)
    {
      mergeHelp(help, locale, null);
    }
    return helpList;
  }
  
  static BList readHelp(Function function) throws IOException, ParseException
  {
    BList help = null;
    String pathName;
    if (function instanceof WrapperFunction)
    {
      WrapperFunction wrapper = (WrapperFunction)function;
      pathName = wrapper.getPathName();
    }
    else
    {
      pathName = function.getClass().getName();
    }
    InputStream is = Library.class.getResourceAsStream(
      "/" + pathName.replace('.', '/') + HELP_EXTENSION);

    if (is != null)
    {
      help = readHelp(is);
    }
    return help;
  }
  
  static BList readHelp(InputStream is) throws IOException, ParseException
  {
    try
    {
      Parser parser = new Parser(new InputStreamReader(is));
      return (BList)parser.parse();
    }
    finally
    {
      is.close();
    }    
  }

  static BList mergeHelp(BList help, Locale locale, Context context)
  {
    if (help == null) return null;
    
    String functionTag = Utils.toString(help.get(0));
    String bundleName = (String)help.get(BUNDLE_NAME);
    if (bundleName == null) return help;
    
    ResourceBundle bundle = ResourceBundle.getBundle(bundleName, locale);
    if (bundle == null) return help;
    
    BList where = (BList)help.get(WHERE);
    if (where != null)
    {
      for (int i = 0; i < where.size(); i++)
      {
        String name = where.getName(i);
        Object value = where.get(i);
        if (name != null && value instanceof BList)
        {
          BList argument = (BList)value; // (type required multiple description)
          String argDescription = getLocalizedString(bundle, functionTag, name);

          if (argDescription == null)
          {
            // TODO: take argDescription from context
          }
          if (argDescription != null)
          {
            if (argument.size() >= 4)
            {
              argument.put(3, argDescription);
            }
            else
            {
              argument.add(argDescription);
            }
          }
        }
      }
    }
    BList exceptions = (BList)help.get(EXCEPTIONS);
    if (exceptions != null)
    {
      for (int i = 0; i < exceptions.size(); i++)
      {
        Object item = exceptions.get(i);
        if (item instanceof BList)
        {
          BList exception = (BList)item;
          if (exception.size() > 0)
          {
            item = exception.get(0);
            if (item instanceof String)
            {
              String type = (String)item;
              String exDescription = 
                getLocalizedString(bundle, functionTag, type);
              if (exDescription != null)
              {
                if (exception.size() >= 2)
                {
                  exception.put(1, exDescription);
                }
                else
                {
                  exception.add(exDescription);
                }
              }
            }
          }
        }
      } 
    }
    String synopsis = getLocalizedString(bundle, functionTag, SYNOPSIS);
    if (synopsis != null)
    {
      help.put(SYNOPSIS, synopsis);
    }
    String description = getLocalizedString(bundle, functionTag, DESCRIPTION);
    if (description != null)
    {
      help.put(DESCRIPTION, description);
    }
    return help;
  }

  static String getLocalizedString(ResourceBundle bundle,
    String prefix, String name)
  {
    try
    {
      return bundle.getString(prefix + "." + name);
    }
    catch (MissingResourceException ex)
    {
      try
      {
        return bundle.getString(name);
      }
      catch (MissingResourceException ex2)
      {
        return null;
      }
    }
  }  
}
