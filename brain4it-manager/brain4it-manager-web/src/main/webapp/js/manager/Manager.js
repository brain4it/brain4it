/**
 * Manager.js
 *
 * @author realor
 */

if (typeof Brain4it === "undefined")
{
  var Brain4it = {};
}

Brain4it.Manager =
{
  _selectedObject : null,
  workspace : null,
  tabbedPanel : null,
  toolBar : null,
  workspaceLabel : null,
  treeWidth : 280,
  configUrl : null,
  visiblePanel : 'left',

  get selectedObject()
  {
    return this._selectedObject;
  },

  set selectedObject(object)
  {
    if (object !== this._selectedObject)
    {
      this._selectedObject = object;
      this.toolBar.update();
    }
  },

  init: function()
  {
    this.workspace = new Brain4it.Workspace();

    document.getElementById("main").style.display = "block";
    this.tabbedPanel = new Brain4it.TabbedPanel("content");

    this.workspaceLabel = document.getElementById("workspace_label");
    this.workspaceLabel.addEventListener("click", function(e)
    {
      Brain4it.Manager.renameWorkspace();
    }, false);

    this.toolBar = new Brain4it.ToolBar("toolbar");
    this.toolBar.addAction(new Brain4it.AddServerAction());
    this.toolBar.addAction(new Brain4it.CreateModuleAction());
    this.toolBar.addAction(new Brain4it.AddModuleAction());
    this.toolBar.addAction(new Brain4it.DestroyModuleAction());
    this.toolBar.addAction(new Brain4it.ListModulesAction());
    this.toolBar.addAction(new Brain4it.RemoveAction());
    var editAction =
      this.toolBar.addAction(new Brain4it.EditAction());
    var consoleAction =
      this.toolBar.addAction(new Brain4it.OpenConsoleAction());
    this.toolBar.addAction(new Brain4it.OpenEditorAction());
    this.toolBar.addAction(new Brain4it.OpenDashboardAction());

    this.toolBar.update();

    var sliderElem = document.getElementById("slider");

    this.leftPanelButton = createButton("lp_button", null, "ball_button",
      function() { Brain4it.Manager.updateLayout('left'); });
    sliderElem.appendChild(this.leftPanelButton);

    this.rightPanelButton = createButton("rp_button", null, "ball_button",
      function() { Brain4it.Manager.updateLayout('right'); });
    sliderElem.appendChild(this.rightPanelButton);

    var menuElem = document.getElementById("menu");
    this.newWorkspaceButton = createButton("new", "New", "new_ws",
      function(e)
      {
        Brain4it.Manager.newWorkspace();
      });
    menuElem.appendChild(this.newWorkspaceButton);

    this.openWorkspaceButton = createFileChooserButton("open_ws", "Open",
      "open_ws", function(e)
      {
        var file = e.target.files[0];
        e.target.value = "";
        Brain4it.Manager.openWorkspaceFile(file);
      });
    menuElem.appendChild(this.openWorkspaceButton);

    this.saveWorkspaceButton = createButton("save_ws", "Save", "save_ws",
      function(e)
      {
        Brain4it.Manager.saveWorkspaceFile();
      });
    menuElem.appendChild(this.saveWorkspaceButton);

    Brain4it.Tree.init();
    Brain4it.Tree.serverAction = editAction;
    Brain4it.Tree.moduleAction = consoleAction;

    this.updateWorkspaceLabel();
    this.loadWorkspace();
    this.updateLayout();
  },

  updateWorkspaceLabel : function()
  {
    this.workspaceLabel.innerHTML = this.workspace.name + ":";
  },

  updateLayout : function(panel)
  {
    if (panel)
    {
      this.visiblePanel = panel;
    }

    var sliderElem = document.getElementById("slider");
    var configElem = document.getElementById("config");
    var contentElem = document.getElementById("content");
    var width = document.body.clientWidth;
    if (width < 500)
    {
      sliderElem.style.display = null;
      if (this.visiblePanel === 'left')
      {
        configElem.style.width = width + "px";
        configElem.style.display = null;
        contentElem.style.display = "none";
        this.leftPanelButton.className = "ball_button selected";
        this.rightPanelButton.className = "ball_button";
      }
      else // right
      {
        configElem.style.display = "none";
        contentElem.style.left = "0px";
        contentElem.style.width = width + "px";
        contentElem.style.display = null;
        this.tabbedPanel.updateLayout();
        this.leftPanelButton.className = "ball_button";
        this.rightPanelButton.className = "ball_button selected";
      }
    }
    else
    {
      sliderElem.style.display = "none";
      configElem.style.width = this.treeWidth + "px";
      configElem.style.display = null;
      contentElem.style.left = this.treeWidth + "px";
      contentElem.style.display = null;
      contentElem.style.width = null;
    }
  },

  renameWorkspace : function()
  {
    var inputDialog = new InputDialog("Rename workspace", "Name:",
      this.workspace.name);
    inputDialog.onAccept = function(value)
    {
      Brain4it.Manager.workspace.name = value;
      Brain4it.Manager.updateWorkspaceLabel();
      Brain4it.Manager.saveWorkspace();
    };
    inputDialog.show();
  },

  newWorkspace : function()
  {
    var inputDialog = new InputDialog("New workspace", "Name:",
      "workspace");
    inputDialog.onAccept = function(value)
    {
      Brain4it.Manager.workspace = new Brain4it.Workspace();
      Brain4it.Manager.workspace.name = value;
      Brain4it.Manager.selectedObject = null;
      Brain4it.Tree.update();
      Brain4it.Manager.toolBar.update();
      Brain4it.Manager.tabbedPanel.removeTabs();
      Brain4it.Manager.updateWorkspaceLabel();
      Brain4it.Manager.saveWorkspace();
    };
    inputDialog.show();
  },

  openWorkspaceFile : function(file)
  {
    var reader = new FileReader();
    var scope = this;
    reader.onload = function(e)
    {
      var workspaceText = e.target.result;
      try
      {
        scope.workspace = scope.readWorkspace(workspaceText);
        Brain4it.Tree.update();
        Brain4it.Manager.updateWorkspaceLabel();
        scope.saveWorkspace();
      }
      catch (ex)
      {
        var messageDialog = new MessageDialog("Error",
          "Can't read file.", "error");
        messageDialog.show();
      }
    };
    reader.readAsText(file);
  },

  saveWorkspaceFile : function()
  {
    var workspaceText = this.writeWorkspace(this.workspace);
    var file = new Blob([workspaceText], {type: 'text/plain'});
    var filename = this.workspace.name + ".bws";

    if (window.navigator.msSaveOrOpenBlob) // IE10+
    {
      window.navigator.msSaveOrOpenBlob(file, filename);
    }
    else
    {
      var linkElem = document.createElement("a");
      var url = window.URL.createObjectURL(file);
      linkElem.href = url;
      linkElem.download = filename;
      document.body.appendChild(linkElem);
      linkElem.click();
      setTimeout(function()
      {
        document.body.removeChild(linkElem);
        window.URL.revokeObjectURL(url);
      }, 0);
    }
  },

  loadWorkspace : function()
  {
    if (window.localStorage)
    {
      var workspaceText = window.localStorage["workspace"];
      if (workspaceText)
      {
        this.workspace = this.readWorkspace(workspaceText);
        Brain4it.Tree.update();
        Brain4it.Manager.updateWorkspaceLabel();
      }
    }
  },

  saveWorkspace: function()
  {
    if (window.localStorage)
    {
      var workspaceText = this.writeWorkspace(this.workspace);
      window.localStorage["workspace"] = workspaceText;
    }
  },

  readWorkspace : function(workspaceText)
  {
    var parser = new Brain4it.Parser();
    var workspaceList = parser.parse(workspaceText);
    var workspace = new Brain4it.Workspace();
    workspace.name = workspaceList.getByIndex(1);
    for (var i = 2; i < workspaceList.size(); i++)
    {
      var serverList = workspaceList.getByIndex(i);
      var server = new Brain4it.Server();
      server.name = serverList.getByIndex(1);
      server.url = serverList.getByIndex(2);
      server.setAccessKey(serverList.getByIndex(3));
      workspace.addServer(server);

      for (var j = 4; j < serverList.size(); j++)
      {
        var moduleList = serverList.getByIndex(j);
        var module = new Brain4it.Module();
        module.name = moduleList.getByIndex(1);
        module.setAccessKey(moduleList.getByIndex(2));
        if (moduleList.size() > 3)
        {
          module.metadata = moduleList.getByIndex(3);
        }
        server.addModule(module);
      }
    }
    return workspace;
  },

  writeWorkspace : function(workspace)
  {
    var workspaceList = new Brain4it.List();
    workspaceList.add(new Brain4it.Reference("workspace"));
    workspaceList.add(workspace.name);
    for (var i = 0; i < workspace.servers.length; i++)
    {
      var server = workspace.servers[i];
      var serverList = new Brain4it.List();
      serverList.add(new Brain4it.Reference("server"));
      serverList.add(server.name);
      serverList.add(server.url);
      serverList.add(server.accessKey);
      workspaceList.add(serverList);
      for (var j = 0; j < server.modules.length; j++)
      {
        var module = server.modules[j];
        var moduleList = new Brain4it.List();
        moduleList.add(new Brain4it.Reference("module"));
        moduleList.add(module.name);
        moduleList.add(module.accessKey);
        if (module.metadata !== null)
        {
          moduleList.add(module.metadata);
        }
        serverList.add(moduleList);
      }
    }
    var printer = new Brain4it.Printer();
    return printer.print(workspaceList);
  }
};

/* Workspace class */

Brain4it.Workspace = function(name)
{
  this.name = name || "workspace";
  this.servers = [];
};

Brain4it.Workspace.prototype =
{
  addServer : function(server)
  {
    if (server.workspace === null)
    {
      server.workspace = this;
      this.servers.push(server);
    }
  },

  removeServer : function(server)
  {
    var index = this.servers.indexOf(server);
    if (index !== -1)
    {
      this.servers.splice(index, 1);
    }
  }
};

/* Server class */

Brain4it.Server = function(name, url, accessKey)
{
  this.workspace = null;
  this.name = name || null;
  this.url = url || null;
  this.accessKey = accessKey || null;
  this.modules = [];
};

Brain4it.Server.prototype =
{
  setAccessKey : function(accessKey)
  {
    if (accessKey !== null)
    {
      accessKey = accessKey.trim();
      if (accessKey.length === 0) accessKey = null;      
    }
    this.accessKey = accessKey;
  },
  
  addModule : function(module)
  {
    if (module.server === null)
    {
      module.server = this;
      this.modules.push(module);
    }
  },

  removeModule : function(module)
  {
    var index = this.modules.indexOf(module);
    if (index !== -1)
    {
      this.modules.splice(index, 1);
    }
  }
};

/* Module class */

Brain4it.Module = function(name, accessKey)
{
  this.server = null;
  this.name = name || null;
  this.accessKey = accessKey || null;
  this.metadata = null;
};

Brain4it.Module.prototype =
{
  setAccessKey : function(accessKey)
  {
    if (accessKey !== null)
    {
      accessKey = accessKey.trim();
      if (accessKey.length === 0) accessKey = null;      
    }
    this.accessKey = accessKey;
  },
  
  getAccessKey : function()
  {
    if (this.accessKey === null || this.accessKey.length === 0)
    {
      return this.server.accessKey;
    }
    return this.accessKey;
  },

  saveAccessKey : function(currentAccessKey, callback)
  {
    if (currentAccessKey === null)
    {
      currentAccessKey = this.server.accessKey;
    }
    if (this.accessKey === null || currentAccessKey === null)
    {
      // not necessary to update key in the server module
      if (callback !== null)
      {
        callback(200);
      }
    }
    else if (currentAccessKey === this.accessKey)
    {
      // accessKey not changed
      if (callback !== null)
      {
        callback(200);
      }
    }
    else
    {
      // accessKey changed
      var client = new Brain4it.Client(this.server.url,
        this.name + "/" + Brain4it.MODULE_ACCESS_KEY_VAR, currentAccessKey);
      client.method = "PUT";
      client.callback = callback;
      client.send(Brain4it.escapeString(this.accessKey));
    }
  }
};

window.addEventListener('load', function() {Brain4it.Manager.init();}, false);
window.addEventListener('resize', function() {Brain4it.Manager.updateLayout();}, false);

