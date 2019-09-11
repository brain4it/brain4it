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
package org.brain4it.version;

import java.io.InputStream;
import java.util.Properties;

/**
 *
 * @author realor
 */
public class VersionInfo
{
  public static final String CREDITS =
    "Copyright (C) 2018, Ajuntament de Sant Feliu de Llobregat";
  public static final String VERSION_INFO_PATH = "/git.properties";
  private static final Properties properties = new Properties();

  static
  {
    try
    {
      InputStream is = VersionInfo.class.getResourceAsStream(VERSION_INFO_PATH);
      if (is != null)
      {
        try
        {
          properties.load(is);
        }
        finally
        {
          is.close();
        }
      }
    }
    catch (Exception ex)
    {
      // ignore: git.properties not present
    }
  }

  public static String getVersionString()
  {
    String buildVersion = getBuildVersion();
    String commitCount = getCommitCount();
    String lastCommitDate = getLastCommitDate();

    if (buildVersion == null || commitCount == null || lastCommitDate == null)
      return null;

    return "Version " + buildVersion + " commit " + commitCount + " (" +
      lastCommitDate + ")";
  }

  public static String getBuildVersion()
  {
    return properties.getProperty("git.build.version");
  }

  public static String getCommitCount()
  {
    return properties.getProperty("git.total.commit.count");
  }

  public static String getLastCommitDate()
  {
    return properties.getProperty("git.commit.time");
  }

  public static String getProperty(String propertyName)
  {
    return properties.getProperty(propertyName);
  }
}
