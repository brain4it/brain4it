/**
 * EditAction.js
 *
 * @author realor
 */

Brain4it.EditAction = function(name, label, className)
{
  this.name = name || "edit";
  this.label = label || "Edit";
  this.className = className || "edit";
};

Brain4it.EditAction.prototype = Object.create(Brain4it.Action.prototype);

Brain4it.EditAction.prototype.invoke = function()
{
  var object = Brain4it.Manager.selectedObject;
  if (object instanceof Brain4it.Server)
  {
    var server = object;
    var serverDialog = new ServerDialog("Edit server", server);
    serverDialog.onAccept = function()
    {
      Brain4it.Tree.setItemObject(Brain4it.Tree.selectedItemElem, server);
      Brain4it.Manager.saveWorkspace();
    };
    serverDialog.show();
  }
  else if (object instanceof Brain4it.Module)
  {
    var module = object;
    var currentAccessKey = module.accessKey;
    var moduleDialog = new ModuleDialog("Edit module", module);
    moduleDialog.onAccept = function()
    {
      Brain4it.Tree.setItemObject(Brain4it.Tree.selectedItemElem, module);
      Brain4it.Manager.saveWorkspace();
      module.saveAccessKey(currentAccessKey, null);
    };
    moduleDialog.show();
  }
};

Brain4it.EditAction.prototype.isEnabled = function()
{
  var selectedObject = Brain4it.Manager.selectedObject;
  return (selectedObject instanceof Brain4it.Server ||
          selectedObject instanceof Brain4it.Module);
};