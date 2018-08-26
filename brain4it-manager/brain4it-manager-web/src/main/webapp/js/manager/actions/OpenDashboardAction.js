/**
 * OpenDashboardAction.js
 * 
 * @author realor
 */

Brain4it.OpenDashboardAction = function(name, label, className)
{
  this.name = name || "dashboard";
  this.label = label || "Dashboard";
  this.className = className || "dashboard";
};

Brain4it.OpenDashboardAction.prototype = Object.create(Brain4it.Action.prototype);

Brain4it.OpenDashboardAction.prototype.invoke = function()
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
    console.info(params);
    var tabName = Brain4it.Manager.tabbedPanel.addTab(null, 
      server.name + ":" + module.name, "dashboard.html?" + params, 
      "dashboard");
    Brain4it.Manager.tabbedPanel.showPanel(tabName);
    Brain4it.Manager.updateLayout('right');
  }
};

Brain4it.OpenDashboardAction.prototype.isEnabled = function()
{
  var selectedObject = Brain4it.Manager.selectedObject;
  return selectedObject instanceof Brain4it.Module;
};
