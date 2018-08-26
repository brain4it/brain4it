/**
 * OpenEditorAction.js
 * 
 * @author realor
 */

Brain4it.OpenEditorAction = function(name, label, className)
{
  this.name = name || "editor";
  this.label = label || "Editor";
  this.className = className || "editor";
};

Brain4it.OpenEditorAction.prototype = Object.create(Brain4it.Action.prototype);

Brain4it.OpenEditorAction.prototype.invoke = function()
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
      server.name + ":" + module.name, "editor.html?" + params, 
      "editor");
    Brain4it.Manager.tabbedPanel.showPanel(tabName);
    Brain4it.Manager.updateLayout('right');
  }
};

Brain4it.OpenEditorAction.prototype.isEnabled = function()
{
  var selectedObject = Brain4it.Manager.selectedObject;
  return selectedObject instanceof Brain4it.Module;
};