/**
 * CreateModuleAction.js
 *
 * @author realor
 */

Brain4it.CreateModuleAction = function(name, label, className)
{
  this.name = name || "create_module";
  this.label = label || "Create module";
  this.className = className || "create_module";
};

Brain4it.CreateModuleAction.prototype = Object.create(Brain4it.Action.prototype);

Brain4it.CreateModuleAction.prototype.invoke = function()
{
  var server = Brain4it.Manager.selectedObject;
  if (server instanceof Brain4it.Server)
  {
    var module = new Brain4it.Module();
    var moduleDialog = new ModuleDialog("Create module", module);
    moduleDialog.onAccept = function()
    {
      var client = new Brain4it.Client(server.url, module.name,
        server.accessKey);
      client.method = "PUT";
      client.callback = function(status)
      {
        if (status === 200)
        {
          server.addModule(module);
          Brain4it.Tree.addItem(Brain4it.Tree.selectedItemElem, module);
          Brain4it.Tree.setItemObject(Brain4it.Tree.selectedItemElem, server);
          Brain4it.Tree.setExpanded(Brain4it.Tree.selectedItemElem, true);
          Brain4it.Manager.saveWorkspace();

          module.saveAccessKey(server.accessKey, function(status)
          {
            if (status === 200)
            {
              var messageDialog = new MessageDialog("Create module",
                "Module " + module.name + " created.", "message");
              messageDialog.show();
            }
            else
            {
              var messageDialog = new MessageDialog("Error",
                "Module " + module.name +
                " created, but could not set the access key.", "error");
              messageDialog.show();
            }
          });
        }
        else
        {
          var messageDialog = new MessageDialog("Error",
            "Can't create module: " + status, "error");
          messageDialog.show();
        }
      };
      client.send();
    };
    moduleDialog.show();
  }
};

Brain4it.CreateModuleAction.prototype.isEnabled = function()
{
  var selectedObject = Brain4it.Manager.selectedObject;
  return selectedObject instanceof Brain4it.Server;
};