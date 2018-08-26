/**
 * ListModulesAction.js
 *
 * @author realor
 */

Brain4it.ListModulesAction = function(name, label, className)
{
  this.name = name || "list_modules";
  this.label = label || "List modules";
  this.className = className || "list_modules";
};

Brain4it.ListModulesAction.prototype = Object.create(Brain4it.Action.prototype);

Brain4it.ListModulesAction.prototype.invoke = function()
{
  var selectedItemElem = Brain4it.Tree.selectedItemElem;
  var server = Brain4it.Manager.selectedObject;
  if (server instanceof Brain4it.Server)
  {
    var client = new Brain4it.Client(server.url, null, server.accessKey);
    client.method = "GET";
    client.callback = function(status, response)
    {
      if (status === 200)
      {
        var parser = new Brain4it.Parser();
        var moduleList = parser.parse(response);
        server.modules = [];
        for (var i = 0; i < moduleList.size(); i++)
        {
          var moduleName;
          var metadata;
          var info = moduleList.getByIndex(i);
          if (info instanceof Brain4it.List)
          {
            moduleName = info.getByIndex(0);
            metadata = info.getByIndex(1);
          }
          else
          {
            moduleName = info;
            metadata = null;
          }
          var module = new Brain4it.Module(moduleName);
          module.metadata = metadata;
          server.addModule(module);
        }
        Brain4it.Tree.setItemObject(selectedItemElem, server);
        Brain4it.Tree.updateList(selectedItemElem, server.modules);
        Brain4it.Tree.setExpanded(selectedItemElem, true);
        Brain4it.Manager.saveWorkspace();
        var messageDialog = new MessageDialog("List modules",
          moduleList.size() + " modules found.", "message");
        messageDialog.show();
      }
      else
      {
        var messageDialog = new MessageDialog("Error",
          "Can't list modules: " + status, "error");
        messageDialog.show();
      }
    };
    client.send();
  }
};

Brain4it.ListModulesAction.prototype.isEnabled = function()
{
  var selectedObject = Brain4it.Manager.selectedObject;
  return selectedObject instanceof Brain4it.Server;
};