/* EditTextWidget */

Brain4it.EditTextWidget = function(dashboard, element)
{
  Brain4it.Widget.call(this, dashboard, element);
  this.getValueFunction = null;
  this.setValueFunction = null;
  this.invokeInterval = 100;
  this.autoScroll = null;
  this.invoker = null;
};

Brain4it.EditTextWidget.prototype = Object.create(Brain4it.Widget.prototype);

Brain4it.EditTextWidget.prototype.init = function(name, setup)
{
  var scope = this;

  this._onChange = this.onChange.bind(this);
  this._onRemoteChange = this.onRemoteChange.bind(this);

  var id = setup.getByName("id") ||
    'editText-' + Math.round(1000000 * Math.random());

  this.editTextElem = document.createElement("div");
  this.editTextElem.className = setup.getByName("className") || "editText";
  this.element.appendChild(this.editTextElem);

  var millis = setup.getByName("invoke-interval");
  if (typeof millis === "number")
  {
    this.invokeInterval = millis;
  }
  var label = setup.getByName("label") || "";
  if (label.length > 0)
  {
    this.labelElem = document.createElement("label");
    this.labelElem.htmlFor = id;
    this.labelElem.innerHTML = label;
    this.editTextElem.appendChild(this.labelElem);
  }

  this.divElem = document.createElement("div");
  this.editTextElem.appendChild(this.divElem);

  this.textareaElem = document.createElement("textarea");
  this.textareaElem.id = id;
  this.divElem.appendChild(this.textareaElem);

  var fontFamily = setup.getByName("font-family") || "arial";
  this.textareaElem.style.fontFamily = fontFamily;

  var fontSize = setup.getByName("font-size") || "14";
  this.textareaElem.style.fontSize = fontSize + "px";

  this.textareaElem.addEventListener("input", this._onChange, false);

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
      this.setValueFunction, this.invokeInterval);
  }
  else
  {
    this.textareaElem.readOnly = true;
  }

  this.autoScroll = setup.getByName("auto-scroll");

  window.addEventListener("resize",
    function() { scope.updateLayout(); }, false);
};

Brain4it.EditTextWidget.prototype.onChange = function()
{
  if (this.invoker)
  {
    this.invoker.invoke(this.textareaElem.value);
  }
};

Brain4it.EditTextWidget.prototype.onRemoteChange =
  function(functionName, value, serverTime)
{
  if (typeof value === "string")
  {
    if (this.invoker === null ||
      (!this.invoker.isSending() &&
        this.invoker.updateInvokeTime(serverTime)))
    {
      var scrollValue = this.textareaElem.scrollTop;

      var selStart = this.textareaElem.selectionStart;
      var selEnd = this.textareaElem.selectionEnd;
      this.textareaElem.value = value;
      if (selStart > value.length) selStart = value.length;
      if (selEnd > value.length) selEnd = value.length;
      this.textareaElem.selectionStart = selStart;
      this.textareaElem.selectionEnd = selEnd;

      this.doAutoScroll(scrollValue);
    }
  }
};

Brain4it.EditTextWidget.prototype.doAutoScroll = function(scrollValue)
{
  if (this.autoScroll === "top")
  {
    this.textareaElem.scrollTop = 0;
  }
  else if (this.autoScroll === "bottom")
  {
    this.textareaElem.scrollTop = this.textareaElem.scrollHeight;
  }
  else
  {
    this.textareaElem.scrollTop = scrollValue;
  }
};

Brain4it.EditTextWidget.prototype.updateLayout = function()
{
};

Brain4it.Dashboard.prototype.widgetTypes['editText'] = Brain4it.EditTextWidget;