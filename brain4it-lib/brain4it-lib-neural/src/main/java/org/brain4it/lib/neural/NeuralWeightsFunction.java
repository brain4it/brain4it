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
package org.brain4it.lib.neural;

import org.brain4it.lang.BList;
import org.brain4it.lang.Context;
import org.brain4it.lang.Utils;
import org.brain4it.lib.NeuralLibrary;
import org.neuroph.core.NeuralNetwork;

/**
 *
 * @author realor
 */
public class NeuralWeightsFunction extends NeuralFunction
{
  public NeuralWeightsFunction(NeuralLibrary library)
  {
    super(library);
  }

  @Override
  public Object invoke(Context context, BList args) throws Exception
  {
    Utils.checkArguments(args, 1);
    String netId = (String)context.evaluate(args.get(1));
    NeuralNetwork neuralNetwork = library.getNeuralNetwork(netId);
    if (neuralNetwork == null) throw new RuntimeException("Invalid network");

    if (args.size() > 2)
    {
      BList weights = (BList)context.evaluate(args.get(2));
      if (weights == null)
      {
        neuralNetwork.randomizeWeights();
        return Utils.toBList(neuralNetwork.getWeights());
      }
      else
      {
        double[] weightsArray = library.toDoubleArray(weights);
        neuralNetwork.setWeights(weightsArray);
        return weights;
      }
    }
    else
    {
      Double[] weights = neuralNetwork.getWeights();
      return Utils.toBList(weights);
    }
  }
}
