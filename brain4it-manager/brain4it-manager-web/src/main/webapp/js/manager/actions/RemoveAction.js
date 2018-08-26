/**
 * RemoveActionAction.js
 *
 * @author realor
 */

Brain4it.RemoveAction = function(name, label, className)
{
  this.name = name || "remove";
  this.label = label || "Remove";
  this.className = className || "remove";
};

Brain4it.RemoveAction.prototype = Object.create(Brain4it.Action.prototype);

Brain4it.RemoveAction.prototype.invoke = function()
{
  var object = Brain4it.Manager.selectedObject;
  if (object instanceof Brain4it.Server)
  {
    var server = object;
    var confirmDialog = new ConfirmDialog("Warning",
      "Remove server " + server.name + "?");
    confirmDialog.onAccept = function()
    {
      Brain4it.Manager.workspace.removeServer(server);
      Brain4it.Tree.removeItem(Brain4it.Tree.selectedItemElem);
      Brain4it.Manager.saveWorkspace();
    };
    confirmDialog.show();
  }
  else if (object instanceof Brain4it.Module)
  {
    var module = object;
    var server = module.server;
    var confirmDialog = new ConfirmDialog("Warning",
      "Remove module " + module.name + "?");
    confirmDialog.onAccept = function()
    {
      server.removeModule(module);
      var serverItemElem = Brain4it.Tree.getParentItemElem(
        Brain4it.Tree.selectedItemElem);
      Brain4it.Tree.removeItem(Brain4it.Tree.selectedItemElem);
      Brain4it.Tree.setItemObject(serverItemElem, server);
      Brain4it.Manager.saveWorkspace();
    };
    confirmDialog.show();
  }
};

Brain4it.RemoveAction.prototype.isEnabled = function()
{
  var selectedObject = Brain4it.Manager.selectedObject;
  return (selectedObject instanceof Brain4it.Server ||
          selectedObject instanceof Brain4it.Module);
};