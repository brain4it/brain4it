/* LedWidget */

Brain4it.LedWidget = function(dashboard, element)
{
  Brain4it.Widget.call(this, dashboard, element);
  this.getValueFunction = null;
};

Brain4it.LedWidget.prototype = Object.create(Brain4it.Widget.prototype);

Brain4it.LedWidget.prototype.init = function(name, setup)
{
  this._onRemoteChange = this.onRemoteChange.bind(this);
  this.ledElem = document.createElement("div");
  this.ledElem.className = setup.getByName("className") || "led";

  this.ledColor = setup.getByName("color") || "#FFFF00";

  this.element.appendChild(this.ledElem);
  
  var outputId = name + "_output";  
  this.outputElem = document.createElement("output");
  this.outputElem.id = outputId;  
  this.ledElem.appendChild(this.outputElem);
  
  var label = setup.getByName("label");
  if (label)
  {
    this.labelElem = document.createElement("label");
    this.labelElem.innerHTML = "" + label;
    this.labelElem.htmlFor = outputId;
    this.ledElem.appendChild(this.labelElem);
  }
  var func = setup.getByName("get-value");
  if (func instanceof Brain4it.Reference)
  {
    this.getValueFunction = func.value;
    this.dashboard.monitor.watch(this.getValueFunction, this._onRemoteChange);
  }
  var scope = this;
  window.addEventListener("resize",
    function() { scope.updateLayout(); }, false);
};

Brain4it.LedWidget.prototype.onRemoteChange = 
  function(functionName, value, serverTime)
{
  if (value)
  {
    this.outputElem.style.backgroundColor = this.ledColor;
    this.outputElem.style.boxShadow = "4px 4px 52px 0px " + this.ledColor;
  }
  else
  {
    this.outputElem.style.backgroundColor = "gray";
    this.outputElem.style.boxShadow = "none";    
  }
};

Brain4it.LedWidget.prototype.updateLayout = function()
{
  var height = this.labelElem.clientHeight;
  height = Math.floor(0.5 * height);
  
  if (height > 40) height = 30;
  else if (height < 14) height = 14;
  
  this.labelElem.style.fontSize = height + "px";
};

Brain4it.Dashboard.prototype.widgetTypes['led'] = Brain4it.LedWidget;