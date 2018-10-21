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
import org.brain4it.lib.NeuralLibrary;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.nnet.Hopfield;
import org.neuroph.nnet.Instar;
import org.neuroph.nnet.Kohonen;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.Perceptron;

/**
 *
 * @author realor
 */
public class NeuralCreateFunction extends NeuralFunction
{
  public NeuralCreateFunction(NeuralLibrary library)
  {
    super(library);
  }

  @Override
  public Object invoke(Context context, BList args) throws Exception
  {
    String type = (String)context.evaluate(args.get("type"));
    if (type == null) type = "MultiLayerPerceptron";

    BList neurons = (BList)context.evaluate(args.get("neurons"));
    if (neurons == null)
      throw new RuntimeException("neurons must be specified");

    NeuralNetwork neuralNetwork;
    if (type.equals("MultiLayerPerceptron"))
    {
      neuralNetwork = new MultiLayerPerceptron(library.toIntegerArray(neurons));
    }
    else if (type.equals("Kohonen"))
    {
      int numInput = ((Number)neurons.get(0)).intValue();
      int numOutput = ((Number)neurons.get(1)).intValue();
      neuralNetwork = new Kohonen(numInput, numOutput);
    }
    else if (type.equals("Hopfield"))
    {
      int numNeurons = ((Number)neurons.get(0)).intValue();
      neuralNetwork = new Hopfield(numNeurons);
    }
    else if (type.equals("Instar"))
    {
      int numNeurons = ((Number)neurons.get(0)).intValue();
      neuralNetwork = new Instar(numNeurons);
    }
    else if (type.equals("Perceptron"))
    {
      int numInput = ((Number)neurons.get(0)).intValue();
      int numOutput = ((Number)neurons.get(1)).intValue();
      neuralNetwork = new Perceptron(numInput, numOutput);
    }
    else throw new RuntimeException("Invalid type: " + type);

    return library.putNeuralNetwork(neuralNetwork);
  }
}
