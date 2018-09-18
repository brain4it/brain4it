/* ButtonWidget */

Brain4it.ButtonWidget = function(dashboard, element)
{
  Brain4it.Widget.call(this, dashboard, element);
  this.onPressedFunction = null;
  this.onReleasedFunction = null;
  this.buttonId = null;
  this.invoker = null;
};

Brain4it.ButtonWidget.prototype = Object.create(Brain4it.Widget.prototype);

Brain4it.ButtonWidget.prototype.init = function(name, setup)
{
  var scope = this;

  var func = setup.getByName("on-pressed");
  if (func instanceof Brain4it.Reference)
  {
    this.onPressedFunction = func.name;
  }
  func = setup.getByName("on-released");
  if (func instanceof Brain4it.Reference)
  {
    this.onReleasedFunction = func.name;
  }

  if (this.onPressedFunction || this.onReleasedFunction)
  {
    this.invoker = this.dashboard.invoker;
  }

  var value = setup.getByName("button-id");
  if (value !== null)
  {
    this.buttonId = String(value);
  }

  this.buttonElem = createButton(null,
    String(setup.getByName("label")),
    setup.getByName("className") || "round_button",
    function()
    {
      if (scope.invoker && scope.onPressedFunction)
      {
        scope.invoker.invoke(scope.onPressedFunction, scope.buttonId, false);
        if (navigator.vibrate)
        {
          navigator.vibrate(30);
        }
      }
    },
    function()
    {
      if (scope.invoker && scope.onReleasedFunction)
      {
        scope.invoker.invoke(scope.onReleasedFunction, scope.buttonId, false);
      }
    });

  var fontFamily = setup.getByName("font-family") || "arial";
  this.buttonElem.style.fontFamily = fontFamily;

  var fontSize = setup.getByName("font-size") || "14";
  this.buttonElem.style.fontSize = fontSize + "px";

  this.element.appendChild(this.buttonElem);
};

Brain4it.Dashboard.prototype.widgetTypes['button'] = Brain4it.ButtonWidget;