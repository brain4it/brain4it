/**
 * AddModuleAction.js
 * 
 * @author realor
 */

Brain4it.AddModuleAction = function(name, label, className)
{
  this.name = name || "add_module";
  this.label = label || "Add module";
  this.className = className || "add_module";
};

Brain4it.AddModuleAction.prototype = Object.create(Brain4it.Action.prototype);

Brain4it.AddModuleAction.prototype.invoke = function()
{
  var server = Brain4it.Manager.selectedObject;
  if (server instanceof Brain4it.Server)
  {
    var module = new Brain4it.Module();
    var moduleDialog = new ModuleDialog("Add module", module);
    moduleDialog.onAccept = function()
    {
      server.addModule(module);
      Brain4it.Tree.addItem(Brain4it.Tree.selectedItemElem, module);
      Brain4it.Tree.setItemObject(Brain4it.Tree.selectedItemElem, server);
      Brain4it.Tree.setExpanded(Brain4it.Tree.selectedItemElem, true);
      Brain4it.Manager.saveWorkspace();
    };
    moduleDialog.show();
  }
};

Brain4it.AddModuleAction.prototype.isEnabled = function()
{
  var selectedObject = Brain4it.Manager.selectedObject;
  return selectedObject instanceof Brain4it.Server;
};