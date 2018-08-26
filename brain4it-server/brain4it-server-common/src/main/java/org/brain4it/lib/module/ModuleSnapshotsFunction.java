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

package org.brain4it.lib.module;

import java.util.List;
import org.brain4it.lang.BList;
import org.brain4it.lang.Context;
import org.brain4it.lang.Function;
import org.brain4it.lang.Structure;
import org.brain4it.server.module.Module;
import org.brain4it.server.store.Entry;

/**
 *
 * @author realor
 */
public class ModuleSnapshotsFunction implements Function
{
  private final Structure structure = 
    new Structure("name", "last-modified", "length");

  @Override
  public Object invoke(Context context, BList args) throws Exception
  {
    Module module = (Module)context.getGlobalScope();
    String pattern = null;
    if (args.size() >= 2)
    {
      pattern = (String)context.evaluate(args.get(1));
    }
    List<Entry> snapshots = module.getSnapshots(pattern);
    BList result = new BList(snapshots.size());
    for (Entry snapshot : snapshots)
    {
      BList snapshotList = new BList(structure);
      result.add(snapshotList);
      String snapshotName = snapshot.getName();
      snapshotName = snapshotName.substring(0, 
        snapshotName.length() - Module.SNAPSHOT_EXTENSION.length());
      snapshotList.put(0, snapshotName);
      snapshotList.put(1, (Object)snapshot.getLastModified());
      snapshotList.put(2, (Object)snapshot.getLength());
    }
    return result;
  }
}
