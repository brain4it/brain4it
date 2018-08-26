/* GraphWidget */

Brain4it.GraphWidget = function(dashboard, element)
{
  Brain4it.Widget.call(this, dashboard, element);
  this.label = "Graph";
  this.timeRangeIndex = 2;
  this.datePattern = "dd/MM/yyyy";
  this.getValueFunction = null;
  this.getHistoryFunction = null;
  this.dataSetNames = [];
  this.model = null;
  this.timerId = null;
  this.mouseY = -Infinity;
  this.lastTimeRangeIndex = 0;
  this.lastDownTime = 0;
  this.frozen = false;
  this.frozenTime = 0;
};

Brain4it.GraphWidget.MARGIN = 4;

Brain4it.GraphWidget.TimeRange = function(name, period, division)
{
  this.name = name;
  this.period = period;
  this.division = division;
};

Brain4it.GraphWidget.Model = function()
{
  this.max = -Infinity;
  this.min = +Infinity;
  this.maxData = 1000;
  this.dataSets = {};
};

Brain4it.GraphWidget.Model.prototype = 
{
  addCurrentData : function(object)
  {
    if (typeof object === "number")
    {
      this.addData("", new Brain4it.GraphWidget.Data(object));
    }
    else if (object instanceof Brain4it.List)
    {
      var dataSetList = object;
      for (var i = 0; i < dataSetList.size(); i++)
      {
        var dataSetName = dataSetList.getName(i);
        var item = dataSetList.getByIndex(i);
        if (typeof item === "number")
        {
          this.addData(dataSetName, new Brain4it.GraphWidget.Data(item));
        }
        else if (item instanceof Brain4it.List)
        {
          var valueList = item;
          var value = valueList.getByIndex(0);
          var timestamp = valueList.getByIndex(1);
          if (typeof value === "number" && typeof timestamp === "number")
          {
            this.addData(dataSetName, 
              new Brain4it.GraphWidget.Data(value, timestamp));
          }
        }
      }
    }
  },

  addHistoryData : function(object)
  {
    if (object instanceof Brain4it.List)
    {
      var dataSetList = object;
      for (var i = 0; i < dataSetList.size(); i++)
      {
        var dataSetName = dataSetList.getName(i);
        var item = dataSetList.getByIndex(i);
        if (item instanceof Brain4it.List)
        {
          var historyList = item;
          for (var j = 0; j < historyList.size(); j++)
          {
            var valueList = historyList.getByIndex(j);
            var value = valueList.getByIndex(0);
            var timestamp = valueList.getByIndex(1);
            if (typeof value === "number" && typeof timestamp === "number")
            {
              this.addData(dataSetName, 
                new Brain4it.GraphWidget.Data(value, timestamp));
            }
          }
        }
      }
    }
  },

  addData : function(dataSetName, data)
  {
    var dataSet = this.dataSets[dataSetName];
    if (dataSet === undefined)
    {
      dataSet = [];
      this.dataSets[dataSetName] = dataSet;
    }
    else if (dataSet.length > 0)
    {
      var last = dataSet[dataSet.length - 1]; // most recent
      if (last.timestamp >= data.timestamp) return;
      if (!data.actualTime && last.value === data.value) return;     
    }
    if (dataSet.length >= this.maxData)
    {
      dataSet.splice(0, 1);
    }
    dataSet.push(data);
    if (data.value < this.min) this.min = data.value;
    if (data.value > this.max) this.max = data.value;
  },

  getDataSetNames : function()
  {
    return Object.keys(this.dataSets);
  },

  getDataSet : function(dataSetName)
  {
    return this.dataSets[dataSetName];
  }
};

Brain4it.GraphWidget.Data = function(value, timestamp)
{
  this.value = value;
  if (timestamp)
  {
    this.timestamp = timestamp;
    this.actualTime = true;
  }
  else
  {
    this.timestamp = (new Date()).getTime();
    this.actualTime = false;
  }
};

Brain4it.GraphWidget.COLORS = 
[
  "blue", 
  "green", 
  "red", 
  "orange", 
  "yellow", 
  "magenta", 
  "pink", 
  "cyan"
];

Brain4it.GraphWidget.TIME_RANGES = 
[
  new Brain4it.GraphWidget.TimeRange("1s", 1000, 250), // 1 second, 250 millis
  new Brain4it.GraphWidget.TimeRange("5s", 5 * 1000, 1000), // 5 seconds, 1 second
  new Brain4it.GraphWidget.TimeRange("10s", 10 * 1000, 2000), // 10 seconds, 2 second
  new Brain4it.GraphWidget.TimeRange("20s", 20 * 1000, 5 * 1000), // 20 seconds, 5 seconds
  new Brain4it.GraphWidget.TimeRange("1m", 60 * 1000, 15 * 1000), // 1 minute, 15 seconds
  new Brain4it.GraphWidget.TimeRange("2m", 2 * 60 * 1000, 30 * 1000), // 2 minutes, 30 seconds
  new Brain4it.GraphWidget.TimeRange("5m", 5 * 60 * 1000, 60 * 1000), // 5 minutes, 1 minute
  new Brain4it.GraphWidget.TimeRange("15m", 15 * 60 * 1000, 3 * 60 * 1000), // 15 minutes, 3 minutes
  new Brain4it.GraphWidget.TimeRange("30m", 30 * 60 * 1000, 5 * 60 * 1000), // 30 minutes, 5 minutes
  new Brain4it.GraphWidget.TimeRange("1h", 3600 * 1000, 15 * 60 * 1000), // 1 hour, 15 minutes
  new Brain4it.GraphWidget.TimeRange("2h", 2 * 3600 * 1000, 30 * 60 * 1000), // 2 hours, 30 minutes
  new Brain4it.GraphWidget.TimeRange("4h", 4 * 3600 * 1000, 3600 * 1000), // 4 hours, 1 hour
  new Brain4it.GraphWidget.TimeRange("8h", 8 * 3600 * 1000, 2 * 3600 * 1000), // 8 hours, 2 hour
  new Brain4it.GraphWidget.TimeRange("12h", 12 * 3600 * 1000, 3 * 3600 * 1000), // 12 hours, 3 hour
  new Brain4it.GraphWidget.TimeRange("1d", 24 * 3600 * 1000, 6 * 3600 * 1000), // 1 day, 6 hours
  new Brain4it.GraphWidget.TimeRange("2d", 2 * 24 * 3600 * 1000, 12 * 3600 * 1000), // 2 day, 12 hours
  new Brain4it.GraphWidget.TimeRange("1w", 7 * 24 * 3600 * 1000, 2 * 24 * 3600 * 1000), // 1 week, 2 days
  new Brain4it.GraphWidget.TimeRange("4w", 28 * 24 * 3600 * 1000, 7 * 24 * 3600 * 1000), // 4 weeks, 1 week
  new Brain4it.GraphWidget.TimeRange("1y", 365 * 24 * 3600 * 1000, 91 * 24 * 3600 * 1000) // 365 days, 91 days  
];

Brain4it.GraphWidget.prototype = Object.create(Brain4it.Widget.prototype);

Brain4it.GraphWidget.prototype.formatDate = function(timestamp, datePattern)
{
  // TODO: support other datePatterns
  var date = new Date(timestamp);
  if (datePattern === "MM-dd-yyyy")
  {
    return this.pad(date.getMonth() + 1) + "-" + 
      this.pad(date.getDate()) + "-" + date.getFullYear();    
  }
  return this.pad(date.getDate()) + "/" + this.pad(date.getMonth() + 1) + "/" + 
    date.getFullYear();
};

Brain4it.GraphWidget.prototype.formatHour = function(timestamp, hourPattern)
{
  var date = new Date(timestamp);  
  var s;
  s = "" + date.getHours() + ":" + this.pad(date.getMinutes()) + ":" + 
    this.pad(date.getSeconds());
  if (hourPattern === "HH:mm:ss:SSS")
  {
    s += ":" + this.pad(date.getMilliseconds(), 3);
  }
  return s;
};

Brain4it.GraphWidget.prototype.pad = function(value, size)
{
  if (size === undefined) size = 2;
  value = String(value);
  while (value.length < size) value = "0" + value;
  return value;
};

Brain4it.GraphWidget.prototype.formatNumber = function(value)
{
  return String(Math.round(value * 100) / 100);
};

Brain4it.GraphWidget.prototype.init = function(name, setup)
{
  var scope = this;
  
  this.model = new Brain4it.GraphWidget.Model();
  
  this._onRemoteChange = this.onRemoteChange.bind(this);
  this._onDoubleClick = this.onDoubleClick.bind(this);
  this._onMouseDown = this.onMouseDown.bind(this);
  this._onMouseUp = this.onMouseUp.bind(this);
  this._onMouseMove = this.onMouseMove.bind(this);
  this._onMouseOut = this.onMouseOut.bind(this);
  this._onTouchStart = this.onTouchStart.bind(this);
  this._onTouchEnd = this.onTouchEnd.bind(this);
  this._onTouchMove = this.onTouchMove.bind(this);
  
  this.canvasElem = document.createElement("canvas");
  this.element.appendChild(this.canvasElem);

  this.label = setup.getByName("label") || "Graph";

  var names = setup.getByName("dataset-names");
  if (typeof names === "string")
  {
    this.dataSetNames = names.split(" ");
  }
  
  this.datePattern = setup.getByName("date-pattern", "dd/MM/yyyy");
  
  var timeRangeName = setup.getByName("time-range");
  if (timeRangeName)
  {
    var index = 0;
    var TIME_RANGES = Brain4it.GraphWidget.TIME_RANGES;
    while (index < TIME_RANGES.length &&
      TIME_RANGES[index].name !== timeRangeName)
    {
      index++;
    }
    this.timeRangeIndex = index < TIME_RANGES.length ? index : 2;
  }
  else this.timeRangeIndex = 2;
  
  var func;
  func = setup.getByName("get-history");
  if (func instanceof Brain4it.Reference)
  {
    this.getHistoryFunction = func.value;
    this.loadHistory();
  }

  func = setup.getByName("get-value");
  if (func instanceof Brain4it.Reference)
  {
    this.getValueFunction = func.value;
    if (this.getHistoryFunction === null)
    {
      this.dashboard.monitor.watch(this.getValueFunction, this._onRemoteChange);
    }
  }
    
  var num = setup.getByName("max-data");
  if (num)
  {
    try
    {
      this.model.maxData = parseInt(num);
    }
    catch (ex)
    {
    }
  }
    
  var scope = this;
  window.addEventListener("resize",
    function() { scope.updateLayout(); }, false);
  this.updateLayout();
  
  this.canvasElem.addEventListener("dblclick", this._onDoubleClick, false);
  this.canvasElem.addEventListener("mousedown", this._onMouseDown, false);
  this.canvasElem.addEventListener("mouseup", this._onMouseUp, false);
  this.canvasElem.addEventListener("mousemove", this._onMouseMove, false);
  this.canvasElem.addEventListener("mouseout", this._onMouseOut, false);
  this.canvasElem.addEventListener("touchstart", this._onTouchStart, false);
  this.canvasElem.addEventListener("touchend", this._onTouchEnd, false);
  this.canvasElem.addEventListener("touchmove", this._onTouchMove, false);  
};

Brain4it.GraphWidget.prototype.updateLayout = function()
{
  var width = this.element.clientWidth;
  var height = this.element.clientHeight;
  var pixelRatio = window.devicePixelRatio;
  this.canvasElem.width = width * pixelRatio;
  this.canvasElem.height = height * pixelRatio;
  this.canvasElem.style.width = width + "px";
  this.canvasElem.style.height = height + "px";
  this.paint();
};

Brain4it.GraphWidget.prototype.paint = function()
{
  var density = window.devicePixelRatio;
  var defaultFontSize = 14 * density;

  var margin = Brain4it.GraphWidget.MARGIN * density;
  var width = this.canvasElem.width - 2 * margin;
  var height = this.canvasElem.height - 2 * margin;
  var model = this.model;

  var ctx = this.canvasElem.getContext("2d");
  ctx.translate(margin, margin);
  ctx.beginPath();
  ctx.moveTo(0, 0);
  ctx.lineTo(width, 0);
  ctx.lineTo(width, height);
  ctx.lineTo(0, height);
  ctx.closePath();
  ctx.clip();
  ctx.clearRect(0, 0, width, height);
  
  var timeRange = Brain4it.GraphWidget.TIME_RANGES[this.timeRangeIndex];
  var period = timeRange.period;

  var nowDate = new Date();
  var now = this.frozen ? this.frozenTime : nowDate.getTime();

  var padding = height / 6.0;
  var tmax, tmin;
  if (model.max === model.min)
  {
    tmax = model.max + 0.5;
    tmin = model.min - 0.5;
  }
  else
  {
    tmax = model.max;
    tmin = model.min;
  }
  var widthPixels = width / period;
  var heightPixels = (height - 2 * padding) / (tmax - tmin);

  // paint vertical grid (time)
  var hourPattern = period < 2000 ? "HH:mm:ss:SSS" : "HH:mm:ss";
  var division = timeRange.division;
  var divisionPixels = 1.2 * widthPixels * division;
  fontSize = Math.min(divisionPixels / hourPattern.length, defaultFontSize);
  ctx.font = "" + fontSize + "px Arial";

  ctx.lineWidth = density;
  ctx.strokeStyle = "#c0c0c0";
    
  var offset = -nowDate.getTimezoneOffset() * 60000;

  var rnow = Math.round((now + offset) / division) * division - offset;

  for (var time = rnow; time >= now - period - division; time -= division)
  {
    var x = width - widthPixels * (now - time);
    ctx.strokeStyle = "#c0c0c0";
    ctx.beginPath();
    ctx.moveTo(x, 0);
    ctx.lineTo(x, height);
    ctx.stroke();

    var date = new Date(time);
    var labelWidth;
    var leftX;
    ctx.fillStyle = "#404040";
    var dateLabel = this.formatDate(date, this.datePattern);
    labelWidth = ctx.measureText(dateLabel).width;
    var leftX = x - 0.5 * labelWidth;
    ctx.fillText(dateLabel, leftX, height - defaultFontSize - 4 * density);

    var hourLabel = this.formatHour(date, hourPattern);
    labelWidth = ctx.measureText(hourLabel).width;
    leftX = x - 0.5 * labelWidth;
    ctx.fillText(hourLabel, leftX, height - 4 * density);
  }

  // paint horizontal grid (values)
  ctx.lineWidth = density;

  var delta = this.valueDivider(tmax - tmin, 5);
  var rmin = Math.round(tmin / delta) * delta;
  var rmax = Math.round(tmax / delta) * delta;
  var fontSize = 0.7 * heightPixels * delta;
  fontSize = Math.min(fontSize, defaultFontSize);
  ctx.font = "" + fontSize + "px Arial";
  ctx.lineWidth = density;

  for (var vy = rmin; vy <= rmax; vy += delta)
  {
    var y = padding + heightPixels * (tmax - vy);
    ctx.strokeStyle = "#c0c0c0";
    ctx.beginPath();
    ctx.moveTo(0, y);
    ctx.lineTo(width, y);
    ctx.stroke();
    ctx.fillStyle = "#404040";
    ctx.fillText(this.formatNumber(vy), 4 * density, y - 2 * density);
  }

  // paint data set names
  ctx.font = "" + defaultFontSize + "px Arial";
  var names = this.dataSetNames.length === 0 ?
    model.getDataSetNames() : this.dataSetNames;
  
  var legend = names.join(" ");
  var legendWidth = ctx.measureText(legend).width;
  var xoffset = (width - legendWidth) / 2;
  var yoffset = 2 * defaultFontSize + 4 * density;
  var xdelta = 0;
  var i = 0;
  var COLORS = Brain4it.GraphWidget.COLORS;
  for (var i = 0; i < names.length; i++)
  {
    var dataSetName = names[i];
    if (dataSetName !== null)
    {
      var dataSetColor = COLORS[i % COLORS.length];
      ctx.fillStyle = dataSetColor;
      ctx.fillText(dataSetName, xoffset + xdelta, yoffset);
      xdelta += ctx.measureText(dataSetName + " ").width;
    }
  }

  // paint data
  i = 0;
  for (var i = 0; i < names.length; i++)
  {
    var dataSetName = names[i];
    var dataSet = this.model.getDataSet(dataSetName);
    if (dataSet && dataSet.length > 0)
    {
      var dataSetColor = COLORS[i % COLORS.length];
      ctx.strokeStyle = dataSetColor;
      ctx.strokeWidth = 2 * density;

      var last = dataSet[dataSet.length - 1];
      var idx = dataSet.length - 1;
      var x1 = width;
      var y1 = padding + heightPixels * (tmax - last.value);
      while (idx >= 0 && x1 > 0)
      {
        var data = dataSet[idx];
        var x2 = width - widthPixels * (now - data.timestamp);
        var y2 = padding + heightPixels * (tmax - data.value);
        ctx.beginPath();
        ctx.moveTo(x1, y1);
        if (x2 < 0)
        {
          // fix big negative coordinates clipping error
          var angle = Math.abs((y1 - y2) / (x1 - x2));
          if (angle < 0.1)
          {
            x2 = 0;
            y2 = y1;
          }
        }
        ctx.lineTo(x2, y2);
        ctx.stroke();
        x1 = x2;
        y1 = y2;
        idx--;
      }
    }
  }
  // paint border
  ctx.lineWidth = 3 * density;
  ctx.strokeStyle = "black";
  ctx.strokeRect(0, 0, width, height - 1);

  // paint label
  if (this.label !== null)
  {
    ctx.fillStyle = "black";
    var labelWidth = ctx.measureText(this.label).width;
    ctx.fillText(this.label, 0.5 * width - 0.5 * labelWidth,
      defaultFontSize + 2 * density);
  }
  // paint timeRange
  ctx.font = "" + defaultFontSize + "px Arial";
  ctx.fillStyle = "black";
  var timeRangeName = timeRange.name;
  var labelWidth = ctx.measureText(timeRangeName).width;
  ctx.fillText(timeRangeName, width - labelWidth - 16 * density,
    defaultFontSize + 2 * density);
    
  // frozen indicator
  if (this.frozen)
  {
    ctx.fillStyle = "red";
    ctx.fillRect(width - 12 * density, 4 * density, 
      8 * density, 8 * density);
  }
  // refresh
  if (this.timerId === null)
  {
    this.updateTimerTask();
  }
  ctx.translate(-margin, -margin);
};

Brain4it.GraphWidget.prototype.valueDivider = function(value, minDivisions)
{
  value = Math.abs(value);
  var divider = Math.pow(10, Math.floor(Math.log10(value)));
  while (value / divider < minDivisions) divider *= 0.5;

  return divider;
};

Brain4it.GraphWidget.prototype.changeTimeRange = function(offsetY)
{
  var density = window.devicePixelRatio;
  var delta = (this.mouseY - offsetY) / (10 * density);
  this.timeRangeIndex = this.lastTimeRangeIndex + Math.round(delta);
  if (this.timeRangeIndex < 0) this.timeRangeIndex = 0;
  else if (this.timeRangeIndex >= Brain4it.GraphWidget.TIME_RANGES.length)
    this.timeRangeIndex = Brain4it.GraphWidget.TIME_RANGES.length - 1;
  this.paint();
  this.updateTimerTask();  
};

Brain4it.GraphWidget.prototype.onDoubleClick = function(event)
{
  this.frozen = !this.frozen;
  this.frozenTime = (new Date()).getTime();
  this.paint();
  return false;
};

Brain4it.GraphWidget.prototype.onMouseDown = function(event)
{
  this.mouseY = event.offsetY;
  this.lastTimeRangeIndex = this.timeRangeIndex;
  return false;
};

Brain4it.GraphWidget.prototype.onMouseUp = function(event)
{
  this.mouseY = -Infinity;
  return false;
};

Brain4it.GraphWidget.prototype.onMouseMove = function(event)
{
  if (this.mouseY !== -Infinity)
  {
    this.changeTimeRange(event.offsetY);
  }
  return false;
};

Brain4it.GraphWidget.prototype.onMouseOut = function(event)
{
  this.mouseY = -Infinity;
  return false;
};

Brain4it.GraphWidget.prototype.onTouchStart = function(event)
{
  event.preventDefault();
  var rect = event.target.getBoundingClientRect();
  var offsetY = event.targetTouches[0].pageY - rect.top;  
  this.mouseY = offsetY;
  this.lastTimeRangeIndex = this.timeRangeIndex;
  this.lastDownTime = (new Date()).getTime();
  return false;  
};

Brain4it.GraphWidget.prototype.onTouchEnd = function(event)
{
  event.preventDefault();
  this.mouseY = -Infinity;
  var now = (new Date()).getTime();
  if (now - this.lastDownTime < 100)
  {
    this.frozen = !this.frozen;
    this.frozenTime = now;
    this.paint();
  }
  return false;
};

Brain4it.GraphWidget.prototype.onTouchMove = function(event)
{
  event.preventDefault();
  var rect = event.target.getBoundingClientRect();
  var offsetY = event.targetTouches[0].pageY - rect.top;
  this.changeTimeRange(offsetY);
  return false;
};

Brain4it.GraphWidget.prototype.onRemoteChange = 
  function(functionName, value, serverTime)
{
  this.model.addCurrentData(value);
};

Brain4it.GraphWidget.prototype.updateTimerTask = function()
{
  if (this.timerId !== null)
  {
    clearTimeout(this.timerId);
    this.timerId = null;
  }
  if (this.canvasElem.offsetParent) // still visible
  {
    var density = window.devicePixelRatio;
    var margin = Brain4it.GraphWidget.MARGIN * density;
    var width = this.canvasElem.width - 2 * margin;
    var timeRange = Brain4it.GraphWidget.TIME_RANGES[this.timeRangeIndex];
    var period = timeRange.period;
    var repaintPeriod = Math.max(period / width, 16);
    var scope = this;
    this.timerId = setTimeout(function()
    {
      scope.paint();
      scope.updateTimerTask();
    }, repaintPeriod);
  }
};

Brain4it.GraphWidget.prototype.loadHistory = function()
{
  if (this.getHistoryFunction)
  {
    var path = this.dashboard.module + "/" + this.getHistoryFunction;
    var client = new Brain4it.Client(this.dashboard.serverUrl, path,
      this.dashboard.accessKey, this.dashboard.sessionId);
    client.method = "POST";
    var scope = this;
    client.callback = function(status, responseText)
    {
      try
      {
        var parser = new Brain4it.Parser();
        scope.model.addHistoryData(parser.parse(responseText));
        scope.paint();
      }
      catch (ex)
      {
        // ignore
      }
      finally
      {
        if (scope.getValueFunction !== null)
        {
          scope.dashboard.monitor.watch(scope.getValueFunction, scope._onChange);        
        }
      }
    };
    client.send();
  }
};

Brain4it.Dashboard.prototype.widgetTypes['graph'] = Brain4it.GraphWidget;