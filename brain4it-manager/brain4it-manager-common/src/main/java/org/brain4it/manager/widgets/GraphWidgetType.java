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
package org.brain4it.manager.widgets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.StringTokenizer;
import org.brain4it.lang.BList;
import org.brain4it.lang.BSoftReference;
import static org.brain4it.manager.widgets.WidgetProperty.LIST;
import static org.brain4it.manager.widgets.WidgetProperty.NUMBER;
import static org.brain4it.manager.widgets.WidgetProperty.STRING;
import static org.brain4it.manager.widgets.WidgetType.GET_VALUE;

/**
 *
 * @author realor
 */
public class GraphWidgetType extends WidgetType
{
  public static final String GET_HISTORY = "get-history";
  public static final String TIME_RANGE = "time-range";
  public static final String MAX_DATA = "max-data";
  public static final String DATE_FORMAT = "date-format";
  public static final String DATASET_NAMES = "dataset-names";

  public static final String DEFAULT_TIME_RANGE_NAME = "5m";
  public static final int DEFAULT_TIME_RANGE_INDEX = 2;

  public static final TimeRange[] TIME_RANGES = new TimeRange[]
  {
    new TimeRange("1s", 1000L, 250L), // 1 second, 250 millis
    new TimeRange("5s", 5 * 1000L, 1000L), // 5 seconds, 1 second
    new TimeRange("10s", 10 * 1000L, 2000L), // 10 seconds, 2 second
    new TimeRange("20s", 20 * 1000L, 5 * 1000L), // 20 seconds, 5 seconds
    new TimeRange("1m", 60 * 1000L, 15 * 1000L), // 1 minute, 15 seconds
    new TimeRange("2m", 2 * 60 * 1000L, 30 * 1000L), // 2 minutes, 30 seconds
    new TimeRange("5m", 5 * 60 * 1000L, 60 * 1000L), // 5 minutes, 1 minute
    new TimeRange("15m", 15 * 60 * 1000L, 3 * 60 * 1000L), // 15 minutes, 3 minutes
    new TimeRange("30m", 30 * 60 * 1000L, 5 * 60 * 1000L), // 30 minutes, 5 minutes
    new TimeRange("1h", 3600 * 1000L, 15 * 60 * 1000L), // 1 hour, 15 minutes
    new TimeRange("2h", 2 * 3600 * 1000L, 30 * 60 * 1000L), // 2 hours, 30 minutes
    new TimeRange("4h", 4 * 3600 * 1000L, 3600 * 1000L), // 4 hours, 1 hour
    new TimeRange("8h", 8 * 3600 * 1000L, 2 * 3600 * 1000L), // 8 hours, 2 hour
    new TimeRange("12h", 12 * 3600 * 1000L, 3 * 3600 * 1000L), // 12 hours, 3 hour
    new TimeRange("1d", 24 * 3600 * 1000L, 6 * 3600 * 1000L), // 1 day, 6 hours
    new TimeRange("2d", 2 * 24 * 3600 * 1000L, 12 * 3600 * 1000L), // 2 day, 12 hours
    new TimeRange("1w", 7 * 24 * 3600 * 1000L, 2 * 24 * 3600 * 1000L), // 1 week, 2 days
    new TimeRange("4w", 28 * 24 * 3600 * 1000L, 7 * 24 * 3600 * 1000L), // 4 weeks, 1 week
    new TimeRange("1y", 365 * 24 * 3600 * 1000L, 91 * 24 * 3600 * 1000L) // 365 days, 91 days
  };

  public GraphWidgetType()
  {
    addProperty(LABEL, STRING, false, "Graph");
    addProperty(TIME_RANGE, STRING, false, DEFAULT_TIME_RANGE_NAME);
    addProperty(MAX_DATA, NUMBER, false, 1000);
    addProperty(DATE_FORMAT, STRING, false, "dd/MM/yyyy");
    addProperty(DATASET_NAMES, STRING, false, null);
    addProperty(GET_VALUE, LIST, true, null);
    addProperty(GET_HISTORY, LIST, true, null);
  }

  @Override
  public String getWidgetType()
  {
    return GRAPH;
  }

  public String getTimeRangeName(BList properties) throws Exception
  {
    return getProperty(TIME_RANGE).getString(properties);
  }

  public int getTimeRangeIndex(BList properties) throws Exception
  {
    String timeRangeName = getProperty(TIME_RANGE).getString(properties);
    int index = 0;
    while (index < TIME_RANGES.length &&
      !TIME_RANGES[index].getName().equals(timeRangeName))
    {
      index++;
    }
    return index < TIME_RANGES.length ?
      index : DEFAULT_TIME_RANGE_INDEX;
  }

  public int getMaxData(BList properties) throws Exception
  {
    return getProperty(MAX_DATA).getInteger(properties);
  }

  public BSoftReference getGetHistoryFunction(BList properties) throws Exception
  {
    return getProperty(GET_HISTORY).getFunction(properties);
  }

  public String getDateFormat(BList properties) throws Exception
  {
    return getProperty(DATE_FORMAT).getString(properties);
  }

  public Collection<String> getDataSetNames(BList properties) throws Exception
  {
    String names = getProperty(DATASET_NAMES).getString(properties);
    if (names == null) return Collections.<String> emptyList();
    StringTokenizer tokenizer = new StringTokenizer(names, " ");
    ArrayList<String> dataSetNames = new ArrayList<String>();
    while (tokenizer.hasMoreTokens())
    {
      String name = tokenizer.nextToken();
      dataSetNames.add(name);
    }
    return dataSetNames;
  }

  public static class TimeRange
  {
    private final String name;
    private final long period; // millis
    private final long division; // millis

    public TimeRange(String name, long period, long division)
    {
      this.name = name;
      this.period = period;
      this.division = division;
    }

    public String getName()
    {
      return name;
    }

    public long getPeriod()
    {
      return period;
    }

    public long getDivision()
    {
      return division;
    }

    @Override
    public String toString()
    {
      return "{period: " + period + ", division: " + division + "}";
    }
  }

  public static class Model
  {
    final HashMap<String, LinkedList<Data>> dataSets =
      new HashMap<String, LinkedList<Data>>();
    private double max = -Double.MAX_VALUE;
    private double min = Double.MAX_VALUE;
    private int maxData;

    public int getMaxData()
    {
      return maxData;
    }

    public void setMaxData(int maxData)
    {
      this.maxData = maxData;
    }

    // object can be:
    // 1) value
    // 2) (dataSetName? => value+)
    // 3) (dataSetName? => (value timestamp)+)

    public void addCurrentData(Object object)
    {
      if (object instanceof Number)
      {
        double value = ((Number)object).doubleValue();
        addData(null, new Data(value));
      }
      else if (object instanceof BList)
      {
        BList dataSetList = (BList)object;
        for (int i = 0; i < dataSetList.size(); i++)
        {
          String dataSetName = dataSetList.getName(i);
          Object item = dataSetList.get(i);
          if (item instanceof Number)
          {
            double value = ((Number)item).doubleValue();
            addData(dataSetName, new Data(value));
          }
          else if (item instanceof BList)
          {
            BList valueList = (BList)item;
            double value = ((Number)valueList.get(0)).doubleValue();
            long timestamp = ((Number)valueList.get(1)).longValue();
            addData(dataSetName, new Data(value, timestamp));
          }
        }
      }
    }

    // history object can be:
    // 1) (dataSetName? => ((value timestamp)+)+)
    public void addHistoryData(Object object)
    {
      if (object instanceof BList)
      {
        BList dataSetList = (BList)object;
        for (int i = 0; i < dataSetList.size(); i++)
        {
          String dataSetName = dataSetList.getName(i);
          Object item = dataSetList.get(i);
          if (item instanceof BList)
          {
            BList historyList = (BList)item;
            for (int j = 0; j < historyList.size(); j++)
            {
              BList valueList = (BList)historyList.get(j);
              double value = ((Number)valueList.get(0)).doubleValue();
              long timestamp = ((Number)valueList.get(1)).longValue();
              addData(dataSetName, new Data(value, timestamp));
            }
          }
        }
      }
    }

    public void addData(String dataSetName, Data data)
    {
      LinkedList<Data> dataSet = dataSets.get(dataSetName);
      if (dataSet == null)
      {
        dataSet = new LinkedList<Data>();
        dataSets.put(dataSetName, dataSet);
      }
      else if (dataSet.size() > 0)
      {
        Data last = dataSet.getLast(); // most recent
        if (last.timestamp >= data.timestamp) return;
        if (!data.actualTime && last.value == data.value) return;
      }
      dataSet.add(data);
      if (data.value < min) min = data.value;
      if (data.value > max) max = data.value;
      if (dataSet.size() > maxData)
      {
        dataSet.removeFirst();
      }
    }

    public Collection<String> getDataSetNames()
    {
      return Collections.unmodifiableCollection(dataSets.keySet());
    }

    public LinkedList<Data> getDataSet(String dataSetName)
    {
      return dataSets.get(dataSetName);
    }

    public double getMax()
    {
      return max;
    }

    public double getMin()
    {
      return min;
    }
  }

  public static class Data
  {
    double value;
    long timestamp;
    boolean actualTime;

    public Data(double value)
    {
      this.value = value;
      this.timestamp = System.currentTimeMillis();
      this.actualTime = false;
    }

    public Data(double value, long timestamp)
    {
      this.value = value;
      this.timestamp = timestamp;
      this.actualTime = true;
    }

    public double getValue()
    {
      return value;
    }

    public long getTimestamp()
    {
      return timestamp;
    }

    @Override
    public String toString()
    {
      return "{value: " + value + ", timestamp: " + timestamp + "}";
    }
  }
}