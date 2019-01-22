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

package org.brain4it.server.store;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static org.brain4it.server.store.Store.WILDCARD;

/**
 *
 * @author realor
 */
public class FileSystemStore implements Store
{
  public static final String BASE_PATH = "basePath";
  public static final String DEFAULT_MODULES_DIR = "brain4it_modules";
  private static final Comparator<File> FILE_COMPARATOR = new Comparator<File>()
  {
    @Override
    public int compare(File f1, File f2)
    {
      return f1.getName().compareTo(f2.getName());
    }
  };
  private static final Logger LOGGER = Logger.getLogger("FileSystemStore");

  private String basePath;

  public FileSystemStore()
  {
  }

  public String getBasePath()
  {
    return basePath;
  }

  @Override
  public void init(Properties properties)
  {
    LOGGER.log(Level.INFO, "Initializing {0}...", 
      new Object[]{getClass().getSimpleName()});
    this.basePath = properties.getProperty(BASE_PATH);
    if (this.basePath == null)
    {
      this.basePath = System.getProperty("user.home") + "/" +
       DEFAULT_MODULES_DIR;
    }
  }

  @Override
  public void open() throws IOException
  {
    File directory = new File(basePath);
    if (!directory.exists())
    {
      directory.mkdirs();
    }
    LOGGER.log(Level.INFO, "Base path: {0}", 
      new Object[]{directory.getCanonicalPath()});
  }

  @Override
  public Entry getEntry(String path) throws IOException
  {
    File file = getFile(path);
    if (file.exists() && !file.isHidden())
    {
      return new Entry(path, file.isDirectory(), file.lastModified(), 
        file.length());
    }
    return null;
  }
  
  @Override
  public List<Entry> listEntries(String path, String pattern)
     throws IOException
  {
    LOGGER.log(Level.FINE, "list entries: [{0}], pattern: {1}", 
      new Object[]{path, pattern});    
    File file = getFile(path);

    if (!file.exists() ||
        !file.isDirectory() ||
        file.isHidden()) return Collections.EMPTY_LIST;

    ArrayList<Entry> entries = new ArrayList<Entry>();

    Filter filter = pattern == null ? null : new Filter(pattern);
    File[] children = file.listFiles(filter);
    if (children == null)
      throw new IOException("Can't read directory: " + file.getAbsolutePath());
    Arrays.sort(children, FILE_COMPARATOR);
    for (File child : children)
    {
      if (!child.isHidden())
      {
        String childPath = path.length() == 0 ? 
          child.getName() : path + PATH_SEPARATOR + child.getName();
        Entry entry = new Entry(childPath, child.isDirectory(), 
           child.lastModified(), child.length());
        entries.add(entry);
      }
    }
    return entries;
  }

  @Override
  public InputStream readEntry(String path) throws IOException
  {
    LOGGER.log(Level.FINE, "read entry: [{0}]", new Object[]{path});
    File file = getFile(path);
    return new FileInputStream(file);
  }

  @Override
  public OutputStream writeEntry(String path) throws IOException
  {
    LOGGER.log(Level.FINE, "write entry: [{0}]", new Object[]{path});
    File file = getFile(path);
    File parentFile = file.getParentFile();
    if (!parentFile.exists())
    {
      parentFile.mkdirs();
    }
    return new FileOutputStream(file);
  }

  @Override
  public boolean deleteEntry(String path) throws IOException
  {
    LOGGER.log(Level.FINE, "delete entry: [{0}]", new Object[]{path});    
    File file = getFile(path);

    if (file.isDirectory())
    {
      deleteDirectory(file);
    }
    return file.delete();
  }
  
  @Override
  public String renameEntry(String path, String oldName, String newName)
    throws IOException
  {
    LOGGER.log(Level.FINE, "rename entry: [{0}], '{1}' -> '{2}'", 
      new Object[]{path, oldName, newName});    
    
    String oldPath = path + PATH_SEPARATOR + oldName;
    String newPath = path + PATH_SEPARATOR + newName;
    File oldFile = getFile(oldPath);
    File newFile = getFile(newPath);
    if (!oldFile.renameTo(newFile))
      throw new IOException("Can't rename entry");
    return newPath;
  }

  @Override
  public void close() throws IOException
  {
  }

  private File getFile(String path) throws IOException
  {
    if (!isValidPath(path)) throw new IOException("Invalid path: " + path);
    return new File(basePath + "/" + path.replace(PATH_SEPARATOR, '/'));
  }

  private boolean isValidPath(String path)
  {
    boolean valid = true;
    int i = 0;
    boolean lastSeparator = false;
    while (valid && i < path.length())
    {
      char ch = path.charAt(i);
      boolean separator = ch == '/' || ch == '_' || ch == '.';
      valid = Character.isLetter(ch) || 
              Character.isDigit(ch) ||
              (separator && !lastSeparator);
      lastSeparator = separator;
      i++;
    }
    return valid;
  }
  
  private void deleteDirectory(File dir)
  {
    File[] children = dir.listFiles();
    for (File child : children)
    {
      if (child.isDirectory())
      {
        deleteDirectory(child);
      }
      else
      {
        child.delete();
      }
    }
  }
  
  class Filter implements FileFilter
  {
    private final Pattern pattern;

    Filter(String pattern)
    {
      StringBuilder buffer = new StringBuilder();
      for (int i = 0; i < pattern.length(); i++)
      {
        char ch = pattern.charAt(i);
        if (Character.isLetter(ch) || Character.isDigit(ch))
        {
          buffer.append(ch);
        }
        else if (ch == WILDCARD)
        {
          buffer.append(".*");
        }
        else if (ch == '.')
        {
          buffer.append("\\x2e");
        }
        else if (ch == '_')
        {
          buffer.append("\\x5f");
        }
      }
      this.pattern = Pattern.compile(buffer.toString());
    }

    @Override
    public boolean accept(File file)
    {
      Matcher matcher = pattern.matcher(file.getName());
      return matcher.matches();
    }
  }
}
