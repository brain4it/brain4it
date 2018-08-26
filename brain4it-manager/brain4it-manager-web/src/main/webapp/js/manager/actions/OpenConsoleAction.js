/**
 * OpenConsoleAction.js
 * 
 * @author realor
 */

Brain4it.OpenConsoleAction = function(name, label, className)
{
  this.name = name || "console";
  this.label = label || "Console";
  this.className = className || "console";
};

Brain4it.OpenConsoleAction.prototype = Object.create(Brain4it.Action.prototype);

Brain4it.OpenConsoleAction.prototype.invoke = function()
{
  var module = Brain4it.Manager.selectedObject;
  if (module instanceof Brain4it.Module)
  {
    var server = module.server;
    var params = "serverUrl=" + server.url + "&module=" + module.name;
    var accessKey = module.getAccessKey();
    if (accessKey)
    {
      params += "&accessKey=" + accessKey;
    }
    console.info(module);
    console.info(params);
    var tabName = Brain4it.Manager.tabbedPanel.addTab(null, 
      server.name + ":" + module.name, "console.html?" + params, "console");
    Brain4it.Manager.tabbedPanel.showPanel(tabName);
    Brain4it.Manager.updateLayout('right');
  }
};

Brain4it.OpenConsoleAction.prototype.isEnabled = function()
{
  var selectedObject = Brain4it.Manager.selectedObject;
  return selectedObject instanceof Brain4it.Module;
};