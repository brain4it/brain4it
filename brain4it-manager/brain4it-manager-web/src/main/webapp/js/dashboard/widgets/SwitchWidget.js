/* SwitchWidget */

Brain4it.SwitchWidget = function(dashboard, element)
{
  Brain4it.Widget.call(this, dashboard, element);
  this.getValueFunction = null;
  this.setValueFunction = null;
  this.invoker = null;
};

Brain4it.SwitchWidget.prototype = Object.create(Brain4it.Widget.prototype);

Brain4it.SwitchWidget.prototype.init = function(name, setup)
{
  var scope = this;
  
  this._onChange = this.onChange.bind(this);
  this._onRemoteChange = this.onRemoteChange.bind(this);
  
  var id = setup.getByName("id") || 
    'switch-' + Math.round(1000000 * Math.random());

  this.labelElem = document.createElement("label");
  this.labelElem.className = setup.getByName("className") || "switch";
  this.labelElem.htmlFor = id;
  this.element.appendChild(this.labelElem);

  this.textElem = document.createElement("span");
  this.textElem.innerHTML = setup.getByName("label") || "";
  this.textElem.className = "text";
  this.labelElem.appendChild(this.textElem);

  this.checkboxElem = document.createElement("input");
  this.checkboxElem.id = id;
  this.checkboxElem.type = "checkbox";
  this.labelElem.appendChild(this.checkboxElem);

  this.spanElem = document.createElement("span");
  this.spanElem.className = "slider";
  this.labelElem.appendChild(this.spanElem);
  
  this.checkboxElem.addEventListener("change", this._onChange, false);
  
  var func = setup.getByName("get-value");
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
    this.checkboxElem.disabled = true;
    this.spanElem.className = "slider disabled";
  }
  window.addEventListener("resize",
    function() { scope.updateLayout(); }, false);
};

Brain4it.SwitchWidget.prototype.onChange = function()
{
  if (this.invoker)
  {
    this.invoker.invoke(this.checkboxElem.checked);
    
    navigator.vibrate = navigator.vibrate ||
      navigator.webkitVibrate || navigator.mozVibrate || navigator.msVibrate;

    if (navigator.vibrate)
    {
      navigator.vibrate(30);
    }
  }
};

Brain4it.SwitchWidget.prototype.onRemoteChange = 
  function(functionName, value, serverTime)
{
  if (this.invoker === null || 
      (!this.invoker.isSending() &&
        this.invoker.updateInvokeTime(serverTime)))
  {
    var active = value ? true : false;
    this.checkboxElem.checked = active;
  }
};

Brain4it.SwitchWidget.prototype.updateLayout = function()
{
};

Brain4it.Dashboard.prototype.widgetTypes['switch'] = Brain4it.SwitchWidget;