/* ImageWidget */

Brain4it.ImageWidget = function(dashboard, element)
{
  Brain4it.Widget.call(this, dashboard, element);
  this.urlFunction = null;
};

Brain4it.ImageWidget.prototype = Object.create(Brain4it.Widget.prototype);

Brain4it.ImageWidget.prototype.init = function(name, setup)
{
  this._onRemoteChange = this.onRemoteChange.bind(this);
  
  this.divElem = document.createElement("div");
  this.divElem.className = "image_area";
  this.element.appendChild(this.divElem);

  this.imageElem = document.createElement("img");
  this.divElem.appendChild(this.imageElem);

  var func = setup.getByName("url");
  if (func instanceof Brain4it.Reference)
  {
    this.urlFunction = func.name;
    this.dashboard.monitor.watch(this.urlFunction, this._onRemoteChange);
  }
};

Brain4it.ImageWidget.prototype.onRemoteChange = 
  function(functionName, value, serverTime)
{
  if (typeof value === 'string')
  {
    this.imageElem.src = value;
  }
  else
  {
    this.imageElem.src = null;
  }
};

Brain4it.Dashboard.prototype.widgetTypes['image'] = Brain4it.ImageWidget;