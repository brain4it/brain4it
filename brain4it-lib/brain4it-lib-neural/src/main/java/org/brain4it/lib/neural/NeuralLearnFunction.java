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
import org.neuroph.core.data.DataSet;
import org.neuroph.core.events.LearningEvent;
import org.neuroph.core.events.LearningEventListener;
import org.neuroph.core.learning.IterativeLearning;
import org.neuroph.core.learning.LearningRule;
import org.neuroph.core.learning.SupervisedLearning;

/**
 *
 * @author realor
 */
public class NeuralLearnFunction extends NeuralFunction
{
  public NeuralLearnFunction(NeuralLibrary library)
  {
    super(library);
  }

  @Override
  public Object invoke(Context context, BList args) throws Exception
  {
    Utils.checkArguments(args, 2);
    String netId = (String)context.evaluate(args.get(1));
    NeuralNetwork neuralNetwork = library.getNeuralNetwork(netId);
    if (neuralNetwork == null) throw new RuntimeException("Invalid network");

    // stop previous learning process
    LearningRule learningRule = neuralNetwork.getLearningRule();
    learningRule.stopLearning();
    
    int inputCount = neuralNetwork.getInputsCount();
    int outputCount = neuralNetwork.getOutputsCount();
    
    DataSet dataSet = new DataSet(inputCount, outputCount);    
    
    BList data = (BList)context.evaluate(args.get(2));
    for (int i = 0; i < data.size(); i++)
    {
      BList row = (BList)data.get(i);
      BList input = (BList)row.get(0);
      double inputArray[] = library.toDoubleArray(input);
      if (row.size() > 1)
      {
        BList output = (BList)row.get(1);
        double outputArray[] = library.toDoubleArray(output);
        dataSet.addRow(inputArray, outputArray);
      }
      else
      {
        dataSet.addRow(inputArray);
      }
    }
    
    if (learningRule instanceof SupervisedLearning)
    {
      Number value = (Number)context.evaluate(args.get("max-error"));
      if (value == null)
      {
        ((SupervisedLearning)learningRule).setMaxError(0.01d); 
      }
      else
      {
        ((SupervisedLearning)learningRule).setMaxError(value.doubleValue());    
      }
    }
    if (learningRule instanceof IterativeLearning)
    {
      Number value = (Number)context.evaluate(args.get("max-iterations"));
      if (value == null)
      {
        ((IterativeLearning)learningRule).setMaxIterations(Integer.MAX_VALUE);
      }
      else
      {
        ((IterativeLearning)learningRule).setMaxIterations(value.intValue());
      }
    }
    
    Listener listener = new Listener();
    learningRule.addListener(listener);
    neuralNetwork.learn(dataSet, learningRule);
    learningRule.removeListener(listener);

    if (learningRule instanceof IterativeLearning)
    {
      return ((IterativeLearning)learningRule).getCurrentIteration();    
    }
    return -1;
  }

  public class Listener implements LearningEventListener
  {
    @Override
    public void handleLearningEvent(LearningEvent event)
    {
      if (Thread.currentThread().isInterrupted())
      {
        LearningRule learningRule = (LearningRule)event.getSource();
        learningRule.getNeuralNetwork().stopLearning();
      }
    }    
  }
}
