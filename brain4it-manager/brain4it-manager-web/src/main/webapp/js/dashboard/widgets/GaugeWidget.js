/* GaugeWidget */

Brain4it.GaugeWidget = function(dashboard, element)
{
  Brain4it.Widget.call(this, dashboard, element);
  this.label = null;
  this.getValuefunction = null;
  this.min = 0;
  this.max = 100;
  this.divisions = 10;
  this.decimals = 0;
  this.value = 0;
  this.remoteValue = 0;
  this.timerId = 0;
};

Brain4it.GaugeWidget.prototype = Object.create(Brain4it.Widget.prototype);

Brain4it.GaugeWidget.prototype.init = function(name, setup)
{
  var scope = this;
  
  this._onRemoteChange = this.onRemoteChange.bind(this);
  
  this.canvasElem = document.createElement("canvas");
  this.element.appendChild(this.canvasElem);
  var func = setup.getByName("get-value");
  if (func instanceof Brain4it.Reference)
  {
    this.getValueFunction = func.name;
    this.dashboard.monitor.watch(this.getValueFunction, this._onRemoteChange);
  }
  var value = setup.getByName("label");
  if (value)
  {
    this.label = String(value);
  }
  value = setup.getByName("min");
  if (Brain4it.isNumber(value))
  {
    this.min = Number(value);
  }
  value = setup.getByName("max");
  if (Brain4it.isNumber(value))
  {
    this.max = Number(value);
  }
  if (this.min >= this.max) 
    throw "max must be greater than min!";

  var value = setup.getByName("divisions");
  if (Brain4it.isNumber(value))
  {
    this.divisions = Number(value);
  }
  if (this.divisions < 5)
    throw "divisions must be greater than 4";    

  value = setup.getByName("decimals");
  if (Brain4it.isNumber(value))
  {
    this.decimals = Number(value);
  }
  if (this.decimals < 0)
    throw "decimals must be positive or zero";    

  var scope = this;
  window.addEventListener("resize",
    function() { scope.updateLayout(); }, false);
  this.updateLayout();
};

Brain4it.GaugeWidget.prototype.updateLayout = function()
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

Brain4it.GaugeWidget.prototype.paint = function()
{
  var width = this.canvasElem.width;
  var height = this.canvasElem.height;
  var size = Math.min(width, height);
  var margin = size / 10;
  var pointRadius = Math.round(size / 20);
  size -=  margin;
  var cx = width / 2;
  var cy = height / 2;
  var gaugeRadius = Math.round(size / 2);
  var pixelRatio = window.devicePixelRatio;

  var ctx = this.canvasElem.getContext("2d");
  ctx.clearRect(0, 0, width, height);

  // circle
  ctx.lineWidth = 2 * pixelRatio;
  ctx.beginPath();
  ctx.arc(cy, cy, gaugeRadius, 0, 2 * Math.PI);
  ctx.stroke();

  // ball
  ctx.lineWidth = pixelRatio;
  ctx.beginPath();
  ctx.arc(cx, cy, pointRadius, 0, 2 * Math.PI);
  ctx.stroke();

  var fontSize = Math.round(size / 15);
  ctx.font = "" + fontSize + "px Arial";

  var step = (this.max - this.min) / this.divisions;
  var radius1 = size / 2;
  var radius2 = radius1 - 0.1 * radius1;
  var radius3 = radius1 - 0.2 * radius1;

  var angle = 225;
  var stepAngle = 270.0 / this.divisions;

  for (var d = 0; d <= this.divisions; d++)
  {
    var i = Math.round(this.min + (d * step));
    var radians = angle * Math.PI / 180;
    var cosAngle = Math.cos(radians);
    var sinAngle = Math.sin(radians);

    var px = cx + radius1 * cosAngle;
    var py = cy - radius1 * sinAngle;
    var qx = cx + radius2 * cosAngle;
    var qy = cy - radius2 * sinAngle;
    var tx = cx + radius3 * cosAngle;
    var ty = cy - radius3 * sinAngle;

    ctx.beginPath();
    ctx.moveTo(px, py);
    ctx.lineTo(qx, qy);
    ctx.stroke();

    var valueLabel = String(i);

    var textWidth = ctx.measureText(valueLabel).width;

    var ox = textWidth / 2;

    ctx.fillText(valueLabel, tx - ox, ty + 0.3 * fontSize);

    angle -= stepAngle;
  }
  
  var div = Math.pow(10, this.decimals);
  var roundedValue = Math.round(this.remoteValue * div) / div;
  var valueString = String(roundedValue);

  var textWidth = ctx.measureText(valueString).width;
  var tx = cx - 0.5 * textWidth;
  var ty = cy + radius3 - fontSize;
  ctx.fillText(valueString, tx, ty);
  
  if (this.label !== null)
  {
    textWidth = ctx.measureText(this.label).width;
    tx = cx - 0.5 * textWidth;
    ty = cy + radius3 + 0.3 * fontSize;
    ctx.fillText(this.label, tx, ty);
  }

  var valueRatio = (this.value - this.min) / (this.max - this.min);
  if (valueRatio > 1) valueRatio = 1;
  else if (valueRatio < 0) valueRatio = 0;

  var valueAngle = 225 - valueRatio * 270;
  var radians = valueAngle * Math.PI / 180;
  var vx = cx + radius3 * Math.cos(radians);
  var vy = cy - radius3 * Math.sin(radians);

  ctx.lineWidth = 2 * pixelRatio;
  ctx.beginPath();
  ctx.moveTo(cx, cy);
  ctx.lineTo(vx, vy);
  ctx.stroke();
};

Brain4it.GaugeWidget.prototype.onRemoteChange = 
  function(functionName, value, serverTime)
{
  if (typeof value === "number")
  {
    this.remoteValue = value;
    this.animateGauge();
  }
};

Brain4it.GaugeWidget.prototype.animateGauge = function()
{
  if (this.timerId)
  {
    clearTimeout(this.timerId);
  }

  if (this.value !== this.remoteValue)
  {
    var dif = Math.abs(this.remoteValue - this.value);
    var variation = (this.max - this.min) / 100;
    if (dif < variation)
    {
      this.value = this.remoteValue;
    }
    else
    {
      this.value = this.value < this.remoteValue ? 
        this.value + variation : this.value - variation;
      var scope = this;
      this.timerId = setTimeout(function(){ scope.animateGauge(); }, 20);
    }
    this.paint();
  }  
};

Brain4it.Dashboard.prototype.widgetTypes['gauge'] = Brain4it.GaugeWidget;