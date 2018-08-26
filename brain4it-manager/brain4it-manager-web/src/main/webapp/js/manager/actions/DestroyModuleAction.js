/**
 * DestroyModuleAction.js
 * 
 * @author realor
 */

Brain4it.DestroyModuleAction = function(name, label, className)
{
  this.name = name || "destroy_module";
  this.label = label || "Destroy module";
  this.className = className || "destroy_module";
};

Brain4it.DestroyModuleAction.prototype = Object.create(Brain4it.Action.prototype);

Brain4it.DestroyModuleAction.prototype.invoke = function()
{
  var module = Brain4it.Manager.selectedObject;
  if (module instanceof Brain4it.Module)
  {
    var server = module.server;
    var confirmDialog = new ConfirmDialog("Warning",
      "Destroy module " + module.name + "?");
    confirmDialog.onAccept = function()
    {
      var client = new Brain4it.Client(server.url, module.name, 
        server.accessKey);
      client.method = "DELETE";
      client.callback = function(status, response)
      {
        if (status === 200)
        {
          server.removeModule(module);
          var serverItemElem = Brain4it.Tree.getParentItemElem(
            Brain4it.Tree.selectedItemElem);
          Brain4it.Tree.removeItem(Brain4it.Tree.selectedItemElem);
          Brain4it.Tree.setItemObject(serverItemElem, server);
          Brain4it.Manager.saveWorkspace();
          var messageDialog = new MessageDialog("Destroy module", 
            "Module " + module.name + " destroyed.", "message");
          messageDialog.show();
        }
        else
        {
          var messageDialog = new MessageDialog("Error", 
            "Can't destroy module: " + status, "error");
          messageDialog.show();
        }
      };
      client.send();
    };
    confirmDialog.show();
  }
};

Brain4it.DestroyModuleAction.prototype.isEnabled = function()
{
  var selectedObject = Brain4it.Manager.selectedObject;
  return selectedObject instanceof Brain4it.Module;
};