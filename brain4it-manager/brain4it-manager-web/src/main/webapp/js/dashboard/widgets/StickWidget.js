/* StickWidget */

Brain4it.StickWidget = function(dashboard, element)
{
  Brain4it.Widget.call(this, dashboard, element);
  this.invokeInterval = 100;
  this.setValueFunction = null;
  this.firstDraw = true;
  this.lastX = 0;
  this.lastY = 0;
  this.deltaX = 0;
  this.deltaY = 0;
  this.drag = false;
  this.invoker = null;
};

Brain4it.StickWidget.prototype = Object.create(Brain4it.Widget.prototype);

Brain4it.StickWidget.prototype.init = function(name, setup)
{
  var scope = this;

  this._onMouseDown = this.onMouseDown.bind(this);
  this._onMouseUp = this.onMouseUp.bind(this);
  this._onMouseMove = this.onMouseMove.bind(this);
  this._onTouchStart = this.onTouchStart.bind(this);
  this._onTouchEnd = this.onTouchEnd.bind(this);
  this._onTouchMove = this.onTouchMove.bind(this);

  this.canvasElem = document.createElement("canvas");
  this.element.appendChild(this.canvasElem);

  var millis = setup.getByName("invoke-interval");
  if (typeof millis === "number")
  {
    this.invokeInterval = millis;
  }
  var func = setup.getByName("set-value");
  if (func instanceof Brain4it.Reference)
  {
    this.setValueFunction = func.value;
    this.invoker = new Brain4it.FunctionInvoker(this.dashboard.invoker, 
      this.setValueFunction, this.invokeInterval);
  }
  var scope = this;
  window.addEventListener("resize",
    function() { scope.updateLayout(); }, false);
  this.updateLayout();

  this.canvasElem.addEventListener("mousedown", this._onMouseDown, false);
  this.canvasElem.addEventListener("mouseup", this._onMouseUp, false);
  this.canvasElem.addEventListener("mousemove", this._onMouseMove, false);
  this.canvasElem.addEventListener("touchstart", this._onTouchStart, false);
  this.canvasElem.addEventListener("touchend", this._onTouchEnd, false);
  this.canvasElem.addEventListener("touchmove", this._onTouchMove, false);
};

Brain4it.StickWidget.prototype.updateLayout = function()
{
  var width = this.element.clientWidth;
  var height = this.element.clientHeight;
  var pixelRatio = window.devicePixelRatio;
  this.canvasElem.width = width * pixelRatio;
  this.canvasElem.height = height * pixelRatio;
  this.canvasElem.style.width = width + "px";
  this.canvasElem.style.height = height + "px";
  this.firstDraw = true;
  this.paint();
};

Brain4it.StickWidget.prototype.paint = function()
{
  var width = this.canvasElem.width;
  var height = this.canvasElem.height;
  var cx = Math.round(width / 2);
  var cy = Math.round(height / 2);
  var outerRadius = Math.min(cx, cy);
  var ballRadius = Math.round(outerRadius / 10);
  var radius = outerRadius - ballRadius;
  var pixelRatio = window.devicePixelRatio;

  if (this.firstDraw)
  {
    this.lastX = cx;
    this.lastY = cy;
    this.firstDraw = false;
  }

  var ctx = this.canvasElem.getContext("2d");
  ctx.lineWidth = 2 * pixelRatio;
  ctx.clearRect(0, 0, width, height);

  ctx.beginPath();
  ctx.arc(cx, cy, radius, 0, 2 * Math.PI);
  ctx.stroke();

  ctx.beginPath();
  ctx.arc(this.lastX, this.lastY, ballRadius, 0, 2 * Math.PI);
  ctx.stroke();

  // vertical line
  ctx.lineWidth = pixelRatio;
  ctx.beginPath();
  ctx.moveTo(cx, cy - radius);
  ctx.lineTo(cx, cy + radius);
  ctx.stroke();

  // horizontal line
  ctx.beginPath();
  ctx.moveTo(cx - radius, cy);
  ctx.lineTo(cx + radius, cy);
  ctx.stroke();

  // line center to ball
  ctx.lineWidth = 2 * pixelRatio;
  ctx.beginPath();
  ctx.moveTo(cx, cy);
  ctx.lineTo(this.lastX, this.lastY);
  ctx.stroke();

  var fontSize = Math.round(height / 15);
  ctx.font = "" + fontSize + "px Arial";
  ctx.fillText("X: " + Math.round(100 * this.deltaX), 0, fontSize);
  ctx.fillText("Y: " + Math.round(100 * this.deltaY), 0, 2 * fontSize);
};

Brain4it.StickWidget.prototype.onMouseDown = function(event)
{
  this.drag = true;
  this.updateStick(event.offsetX, event.offsetY);
  return false;
};

Brain4it.StickWidget.prototype.onMouseUp = function(event)
{
  this.drag = false;
  var width = this.canvasElem.parentNode.clientWidth;
  var height = this.canvasElem.parentNode.clientHeight;
  var cx = Math.round(width / 2);
  var cy = Math.round(height / 2);
  this.updateStick(cx, cy);
  return false;
};

Brain4it.StickWidget.prototype.onMouseMove = function(event)
{
  if (this.drag)
  {
    this.updateStick(event.offsetX, event.offsetY);
  }
  return false;
};

Brain4it.StickWidget.prototype.onTouchStart = function(event)
{
  event.preventDefault();
  this.drag = true;
  var rect = event.target.getBoundingClientRect();
  var offsetX = event.targetTouches[0].pageX - rect.left;
  var offsetY = event.targetTouches[0].pageY - rect.top;
  this.updateStick(offsetX, offsetY);
  return false;
};

Brain4it.StickWidget.prototype.onTouchEnd = function(event)
{
  event.preventDefault();
  this.drag = false;
  var width = this.canvasElem.parentNode.clientWidth;
  var height = this.canvasElem.parentNode.clientHeight;
  var cx = Math.round(width / 2);
  var cy = Math.round(height / 2);
  this.updateStick(cx, cy);
  return false;
};

Brain4it.StickWidget.prototype.onTouchMove = function(event)
{
  event.preventDefault();
  if (this.drag)
  {
    var rect = event.target.getBoundingClientRect();
    var offsetX = event.targetTouches[0].pageX - rect.left;
    var offsetY = event.targetTouches[0].pageY - rect.top;
    this.updateStick(offsetX, offsetY);
  }
  return false;
};

Brain4it.StickWidget.prototype.updateStick = function(x, y)
{
  var pixelRatio = window.devicePixelRatio;
  this.lastX = x * pixelRatio;
  this.lastY = y * pixelRatio;

  var width = this.canvasElem.width;
  var height = this.canvasElem.height;
  var cx = Math.round(width / 2);
  var cy = Math.round(height / 2);
  var outerRadius = Math.min(cx, cy);
  var ballRadius = Math.round(outerRadius / 10);
  var radius = outerRadius - ballRadius;

  var dx = (this.lastX - cx) / radius;
  var dy = (cy - this.lastY) / radius;

  if (dx * dx + dy * dy > 1)
  {
    var angle = Math.atan2(dy, dx);
    dx = Math.cos(angle);
    dy = Math.sin(angle);
    this.lastX = Math.round(cx + radius * dx);
    this.lastY = Math.round(cy - radius * dy);
  }

  if (dx !== this.deltaX || dy !== this.deltaY)
  {
    this.deltaX = dx;
    this.deltaY = dy;
    this.paint();
    this.onChanged();
  }
};

Brain4it.StickWidget.prototype.onChanged = function()
{
  if (this.invoker)
  {
    var list = new Brain4it.List();
    list.add(this.deltaX);
    list.add(this.deltaY);
    this.invoker.invoke(list);
  }
};

Brain4it.Dashboard.prototype.widgetTypes['stick'] = Brain4it.StickWidget;