/* RangeWidget */

Brain4it.RangeWidget = function(dashboard, element)
{
  Brain4it.Widget.call(this, dashboard, element);
  this.getValueFunction = null;
  this.setValueFunction = null;
  this.tracking = false;
  this.invoker = null;
};

Brain4it.RangeWidget.prototype = Object.create(Brain4it.Widget.prototype);

Brain4it.RangeWidget.prototype.init = function(name, setup)
{
  var scope = this;
  
  this._onChange = this.onChange.bind(this);
  this._onRemoteChange = this.onRemoteChange.bind(this);
  
  var id = setup.getByName("id") || 'range-' + Math.round(1000000 * Math.random());
  
  this.element.style.display = "table";
  
  this.containerElem = document.createElement("div");
  this.containerElem.className = setup.getByName("className") || "range";
  this.element.appendChild(this.containerElem);  
  
  this.infoElem = document.createElement("div");
  this.infoElem.className = "info";
  this.containerElem.appendChild(this.infoElem);
  
  this.labelElem = document.createElement("label");
  this.labelElem.innerHTML = setup.getByName("label") || "Value: ";
  this.labelElem.htmlFor = id;
  this.infoElem.appendChild(this.labelElem);

  this.outputElem = document.createElement("output");
  this.infoElem.appendChild(this.outputElem);

  this.rangeElem = document.createElement("input");
  this.rangeElem.id = id;
  this.rangeElem.type = "range";
  this.rangeElem.min = Number(setup.getByName("min")) || 0;
  this.rangeElem.max = Number(setup.getByName("max")) || 1023;
  this.containerElem.appendChild(this.rangeElem);

  this.rangeElem.addEventListener("input", 
    function()
    {
      scope.tracking = true; 
      scope.outputElem.value = scope.rangeElem.value; 
    }, false);  
  
  this.rangeElem.addEventListener("change", this._onChange, false);
  
  var func = setup.getByName("get-value");
  if (func instanceof Brain4it.Reference)
  {
    this.getValueFunction = func.value;
    this.dashboard.monitor.watch(this.getValueFunction, this._onRemoteChange);    
  }
  func = setup.getByName("set-value");
  if (func instanceof Brain4it.Reference)
  {
    this.setValueFunction = func.value;
    this.invoker = new Brain4it.FunctionInvoker(this.dashboard.invoker, 
      this.setValueFunction, 0);
  }
  else
  {
    this.rangeElem.disabled = true;
  }
};

Brain4it.RangeWidget.prototype.onChange = function()
{
  this.tracking = false;
  if (this.invoker)
  {
    this.invoker.invoke(parseInt(this.rangeElem.value));
  }
};

Brain4it.RangeWidget.prototype.onRemoteChange = 
  function(functionName, value, serverTime)
{
  if (typeof value === "number")
  {
    if (!this.tracking)
    {
      if (this.invoker === null ||
         (!this.invoker.isSending() &&
           this.invoker.updateInvokeTime(serverTime)))
      {
        var intValue = Math.floor(value);
        this.outputElem.value = intValue;
        this.rangeElem.value = intValue;
        this.updateLayout();
      }
    }
  }
};

Brain4it.Dashboard.prototype.widgetTypes['range'] = Brain4it.RangeWidget;