/* SelectWidget */

Brain4it.SelectWidget = function(dashboard, element)
{
  Brain4it.Widget.call(this, dashboard, element);
  this.getOptionsFunction = null;
  this.getValueFunction = null;
  this.setValueFunction = null;
  this.currentValue = null;
  this.invoker = null;
};

Brain4it.SelectWidget.prototype = Object.create(Brain4it.Widget.prototype);

Brain4it.SelectWidget.prototype.init = function(name, setup)
{
  this._onChange = this.onChange.bind(this);
  this._onRemoteChange = this.onRemoteChange.bind(this);
  
  var id = setup.getByName("id") || 'select-' + Math.round(1000000 * Math.random());
  
  this.element.style.display = "table";
  
  this.containerElem = document.createElement("div");
  this.containerElem.className = setup.getByName("className") || "select";
  this.element.appendChild(this.containerElem);
  
  this.labelElem = document.createElement("label");
  this.labelElem.innerHTML = setup.getByName("label") || null;
  this.labelElem.htmlFor = id;
  this.containerElem.appendChild(this.labelElem);

  this.selectElem = document.createElement("select");
  this.selectElem.id = id;
  this.containerElem.appendChild(this.selectElem);
  
  this.selectElem.addEventListener("change", this._onChange, false);
  
  var func = setup.getByName("get-options");
  if (func instanceof Brain4it.Reference)
  {
    this.getOptionsFunction = func.name;
    this.dashboard.monitor.watch(this.getOptionsFunction, this._onRemoteChange);    
  }
  func = setup.getByName("get-value");
  if (func instanceof Brain4it.Reference)
  {
    this.getValueFunction = func.name;
    this.dashboard.monitor.watch(this.getValueFunction, this._onRemoteChange);    
  }
  func = setup.getByName("set-value");
  if (func instanceof Brain4it.Reference)
  {
    this.setValueFunction = func.name;
    this.invoker = new Brain4it.FunctionInvoker(this.dashboard.invoker, 
      this.setValueFunction, 0);
  }
  else
  {
    this.selectElem.disabled = true;
  }
};

Brain4it.SelectWidget.prototype.onChange = function()
{
  if (this.invoker)
  {
    var value = this.selectElem.value;    
    this.invoker.invoke(value);
  }
};

Brain4it.SelectWidget.prototype.onRemoteChange = 
  function(functionName, value, serverTime)
{
  if (functionName === this.getValueFunction)
  {
    if (this.invoker === null || 
        (!this.invoker.isSending() &&
          this.invoker.updateInvokeTime(serverTime)))
    {
      this.setSelectedValue(value);
    }
  }
  else if (functionName === this.getOptionsFunction)
  {
    this.loadOptions(value);
  }
};

Brain4it.SelectWidget.prototype.setSelectedValue = function(value)
{
  this.currentValue = value;
  if (value === null) return;
  this.selectElem.value = value;
};

Brain4it.SelectWidget.prototype.loadOptions = function(options)
{
  try
  {
    this.currentOption = options;
    this.selectElem.innerHTML = "";
    for (var i = 0; i < options.size(); i++)
    {
      var option = options.getByIndex(i);
      var optionElem = document.createElement("option");
      optionElem.value = option.getByIndex(0);
      optionElem.label = option.getByIndex(1);
      optionElem.innerHTML = option.getByIndex(1);
      this.selectElem.appendChild(optionElem);
    }
    this.setSelectedValue(this.currentValue);  
  }
  catch (ex)
  {
  }
};

Brain4it.Dashboard.prototype.widgetTypes['select'] = Brain4it.SelectWidget;