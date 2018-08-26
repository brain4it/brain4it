/**
 * AddServerAction.js
 * 
 * @author realor
 */

Brain4it.AddServerAction = function(name, label, className)
{
  this.name = name || "add_server";
  this.label = label || "Add server";
  this.className = className || "add_server";
};

Brain4it.AddServerAction.prototype = Object.create(Brain4it.Action.prototype);

Brain4it.AddServerAction.prototype.invoke = function()
{
  var server = new Brain4it.Server();
  var serverDialog = new ServerDialog("Add server", server);
  serverDialog.onAccept = function()
  {
    Brain4it.Manager.workspace.addServer(server);
    Brain4it.Tree.addItem(null, server);
    Brain4it.Manager.saveWorkspace();
  };
  serverDialog.show();
};
