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
package org.brain4it.server;

import org.brain4it.lang.Structure;

/**
 *
 * @author realor
 */
public class ServerConstants
{
  // BPL mimetype & encoding
  public static final String BPL_MIMETYPE = "text/plain";
  public static final String BPL_CHARSET = "UTF-8";
  public static final String URL_CHARSET = "UTF-8";

  // HTTP request header properties
  public static final String ACCESS_KEY_HEADER = "access-key";
  public static final String MONITOR_HEADER = "monitor";
  public static final String SESSION_ID_HEADER = "session-id";

  // HTTP response header properties
  public static final String SERVER_TIME_HEADER = "server-time";

  // special module variables
  public static final String MODULE_START_VAR = "start";
  public static final String MODULE_STOP_VAR = "stop";
  public static final String MODULE_METADATA_VAR = "module-metadata";
  public static final String MODULE_ACCESS_KEY_VAR = "access-key";
  public static final String MODULE_SETUP_VAR = "setup";
  public static final String EXTERIOR_FUNCTION_PREFIX = "@";
  public static final String DASHBOARDS_FUNCTION_NAME =
    EXTERIOR_FUNCTION_PREFIX + "dashboards";

  // request context Structure for exterior functions
  public static final Structure REQUEST_CONTEXT_STRUCTURE = new Structure();
  public static final String REQUEST_HEADERS = "request-headers";
  public static final String RESPONSE_HEADERS = "response-headers";
  public static final String REMOTE_ADDRESS = "remote-address";
  public static final String REMOTE_PORT = "remote-port";

  /* Exterior functions request context structure definition */
  static
  {
    REQUEST_CONTEXT_STRUCTURE.putName(0, REMOTE_ADDRESS);
    REQUEST_CONTEXT_STRUCTURE.putName(1, REMOTE_PORT);
    REQUEST_CONTEXT_STRUCTURE.putName(2, REQUEST_HEADERS);
    REQUEST_CONTEXT_STRUCTURE.putName(3, RESPONSE_HEADERS);
  }
}
