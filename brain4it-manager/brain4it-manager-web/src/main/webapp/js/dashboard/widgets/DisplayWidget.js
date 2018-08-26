/* DisplayWidget */

Brain4it.DisplayWidget = function(dashboard, element)
{
  Brain4it.Widget.call(this, dashboard, element);
  this.getValueFunction = null;
};

Brain4it.DisplayWidget.prototype = Object.create(Brain4it.Widget.prototype);

Brain4it.DisplayWidget.prototype.init = function(name, setup)
{
  this._onRemoteChange = this.onRemoteChange.bind(this);
  this.displayElem = document.createElement("div");
  this.displayElem.className = "display";
  this.displayElem.style.fontFamily = setup.getByName("font-family") || "lcd";
  this.lines = setup.getByName("lines") || 1;
  this.element.appendChild(this.displayElem);
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

Brain4it.DisplayWidget.prototype.onRemoteChange = 
  function(functionName, value, serverTime)
{
  var text;
  if (typeof value === 'string')
  {
    text = value;
  }
  else
  {
    text = String(value);
  }
  text = text.replace(/[\n\r]/g, '<br>');
  this.displayElem.innerHTML = text;
  this.updateLayout();
};

Brain4it.DisplayWidget.prototype.updateLayout = function()
{
  var height = this.displayElem.clientHeight;
  height = Math.floor(0.75 * height / this.lines);
  
  this.displayElem.style.fontSize = height + "px";
};

Brain4it.Dashboard.prototype.widgetTypes['display'] = Brain4it.DisplayWidget;