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
import java.util.stream.Stream;
import static org.brain4it.help.HelpBuilder.HELP_EXTENSION;
import static org.brain4it.help.HelpBuilder.readHelp;
import org.brain4it.lang.BList;
import org.brain4it.lib.Library;
import org.brain4it.lib.LibraryFactory;

/**
 *
 * @author realor
 */
public class LibraryHelpBuilder
{
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
          readHelp.put(HelpBuilder.GROUP, group);
        }
      }
    }
    walk.close();

    for (BList help : helpList)
    {
      HelpBuilder.mergeHelp(help, locale, null);
    }
    return helpList;
  }
}
