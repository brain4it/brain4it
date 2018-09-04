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
  this.element.appendChild(this.ledElem);

  this.ledColor = setup.getByName("color") || "#FFFF00";
  
  this.divElem = document.createElement("div");
  this.ledElem.appendChild(this.divElem);
  
  var outputId = name + "_output";
  this.outputElem = document.createElement("output");
  this.outputElem.id = outputId;  
  this.divElem.appendChild(this.outputElem);
  
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
  this.updateLayout();
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
  var width = this.divElem.offsetWidth;
  var height = this.divElem.offsetHeight;
  var size = Math.min(width, height);

  this.outputElem.style.top = ((height - size) / 2) + "px";
  this.outputElem.style.bottom = ((height - size) / 2) + "px";
  this.outputElem.style.left = ((width - size) / 2) + "px";
  this.outputElem.style.right = ((width - size) / 2) + "px";
};

Brain4it.Dashboard.prototype.widgetTypes['led'] = Brain4it.LedWidget;