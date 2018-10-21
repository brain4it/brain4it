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
package org.brain4it.lib;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.brain4it.lang.BList;
import org.brain4it.lib.neural.NeuralCalculateFunction;
import org.brain4it.lib.neural.NeuralCreateFunction;
import org.brain4it.lib.neural.NeuralDestroyFunction;
import org.brain4it.lib.neural.NeuralLearnFunction;
import org.brain4it.lib.neural.NeuralWeightsFunction;
import org.neuroph.core.NeuralNetwork;

/**
 *
 * @author realor
 */
public class NeuralLibrary extends Library
{
  private final Map<String, NeuralNetwork> neuralNetworks =
    Collections.synchronizedMap(new HashMap<String, NeuralNetwork>());


  @Override
  public String getName()
  {
    return "Neural";
  }

  @Override
  public void load()
  {
    functions.put("nn-create", new NeuralCreateFunction(this));
    functions.put("nn-destroy", new NeuralDestroyFunction(this));
    functions.put("nn-weights", new NeuralWeightsFunction(this));
    functions.put("nn-calculate", new NeuralCalculateFunction(this));
    functions.put("nn-learn", new NeuralLearnFunction(this));
  }

  public NeuralNetwork getNeuralNetwork(String netId)
  {
    return neuralNetworks.get(netId);
  }

  public String putNeuralNetwork(NeuralNetwork neuralNetwork)
  {
    UUID uuid = UUID.randomUUID();
    String netId = Long.toHexString(uuid.getMostSignificantBits()) +
      Long.toHexString(uuid.getLeastSignificantBits());
    neuralNetworks.put(netId, neuralNetwork);
    return netId;
  }

  public NeuralNetwork removeNeuralNetwork(String netId)
  {
    return neuralNetworks.remove(netId);
  }

  public int[] toIntegerArray(BList list)
  {
    int array[] = new int[list.size()];
    for (int i = 0; i < array.length; i++)
    {
      array[i] = ((Number)list.get(i)).intValue();
    }
    return array;
  }

  public double[] toDoubleArray(BList list)
  {
    double array[] = new double[list.size()];
    for (int i = 0; i < array.length; i++)
    {
      array[i] = ((Number)list.get(i)).doubleValue();
    }
    return array;
  }

  public BList toBList(int[] array)
  {
    BList list = new BList(array.length);
    for (int elem : array)
    {
      list.add(elem);
    }
    return list;
  }

  public BList toBList(double[] array)
  {
    BList list = new BList(array.length);
    for (double elem : array)
    {
      list.add(elem);
    }
    return list;
  }
}
