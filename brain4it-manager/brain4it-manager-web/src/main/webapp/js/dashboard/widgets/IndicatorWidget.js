/* IndicatorWidget */

Brain4it.IndicatorWidget = function(dashboard, element)
{
  Brain4it.Widget.call(this, dashboard, element);
  this.getValueFunction = null;
  this.maxValueLength = 0;
};

Brain4it.IndicatorWidget.prototype = Object.create(Brain4it.Widget.prototype);

Brain4it.IndicatorWidget.prototype.init = function(name, setup)
{
  this._onRemoteChange = this.onRemoteChange.bind(this);
  this.indicatorElem = document.createElement("div");
  this.indicatorElem.className = setup.getByName("className") || "indicator";
  this.element.appendChild(this.indicatorElem);
    
  var outputId = name + "_output";

  var label = setup.getByName("label");
  if (label)
  {
    this.labelElem = document.createElement("label");
    this.labelElem.innerHTML = "" + label;
    this.labelElem.htmlFor = outputId;
    this.indicatorElem.appendChild(this.labelElem);
  }

  this.valueDivElem = document.createElement("div");
  this.valueDivElem.className = "value_div";
  this.indicatorElem.appendChild(this.valueDivElem);
  
  this.outputElem = document.createElement("output");
  this.outputElem.id = outputId;
  this.valueDivElem.appendChild(this.outputElem);

  var units = setup.getByName("units");
  if (units)
  {
    this.unitsElem = document.createElement("div");
    this.unitsElem.className = "units";
    this.unitsElem.innerHTML = "" + units;
    this.valueDivElem.appendChild(this.unitsElem);
  }

  var fontFamily = setup.getByName("font-family") || "Arial";
  this.outputElem.style.fontFamily = fontFamily;

  this.maxValueLength = Number(setup.getByName("max-value-length")) || 0;
  
  var func = setup.getByName("get-value");
  if (func instanceof Brain4it.Reference)
  {
    this.getValueFunction = func.value;
    this.dashboard.monitor.watch(this.getValueFunction, this._onRemoteChange);
  }
  var scope = this;
  window.addEventListener("resize",
    function() { scope.updateTextSize(); }, false);
};

Brain4it.IndicatorWidget.prototype.onRemoteChange = 
  function(functionName, value, serverTime)
{
  var printer = new Brain4it.Printer();
  var text = printer.print(value);
  if (this.maxValueLength > 0 && text.length > this.maxValueLength)
  {
    text = text.substring(0, this.maxValueLength);
  }
  this.outputElem.innerHTML = text;
  this.updateTextSize();
};

Brain4it.IndicatorWidget.prototype.updateTextSize = function()
{
  var width = this.outputElem.offsetWidth;
  var height = this.outputElem.offsetHeight;
  var fontRatio = 1.0;
  var text = this.outputElem.innerHTML;
  var length = this.maxValueLength === 0 ? text.length : this.maxValueLength;
  var fontSize = Math.min(height, width * fontRatio / length);
  this.outputElem.style.fontSize = fontSize + "px";
};

Brain4it.Dashboard.prototype.widgetTypes['indicator'] = Brain4it.IndicatorWidget;