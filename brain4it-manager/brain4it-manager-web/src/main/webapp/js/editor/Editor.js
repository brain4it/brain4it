/* TextEditor.js */

Brain4it.TextEditor = function(serverUrl, module, accessKey)
{
  this.serverUrl = serverUrl;
  this.module = module;
  this.accessKey = accessKey;
  this.path = "rules";
  this.editorElem = null;
  this.toolBarElem = null;
  this.bodyElem = null;
  this.pathElem = null;
  this.helper = new Brain4it.Helper(serverUrl, module, accessKey);
  CodeMirror.Brain4itHelper = this.helper;
};

Brain4it.TextEditor.prototype =
{
  init : function(containerElem, options)
  {
    this.editorElem = document.createElement("div");
    this.editorElem.className = "editor";
    containerElem.appendChild(this.editorElem);

    this.toolBarElem = document.createElement("div");
    this.toolBarElem.className = "toolbar";
    this.editorElem.appendChild(this.toolBarElem);

    this.bodyElem = document.createElement("div");
    this.bodyElem.className = "editor_body";
    this.editorElem.appendChild(this.bodyElem);

    this.pathElem = document.createElement("div");
    this.pathElem.className = "path_name";
    this.bodyElem.appendChild(this.pathElem);
    
    this.textAreaElem = document.createElement("div");
    this.textAreaElem.className = "text_area";
    this.bodyElem.appendChild(this.textAreaElem);

    var textArea = document.createElement("textarea");
    this.textAreaElem.appendChild(textArea);
    
    this.editor = CodeMirror.fromTextArea(textArea, options);
    this.editor.focus();
     
    var scope = this;

    this.loadButton = createButton("load_button", "Load", "load", 
      function() { scope.loadPath(); });
    this.toolBarElem.appendChild(this.loadButton);

    this.saveButton = createButton("save_button", "Save", "save", 
      function() { scope.savePath(); });
    this.toolBarElem.appendChild(this.saveButton);

    this.updateLayout();

    window.addEventListener("resize", 
      function() { scope.updateLayout(); }, false);

    this.helper.loadFunctions(function(){ scope.load(); });
  },

  show : function()
  {
    this.editor.focus();
  },

  load : function()
  {
    var scope = this;
    var path = this.module + "/" + this.path;
    var client = new Brain4it.Client(this.serverUrl, path, this.accessKey);
    client.method = "GET";
    client.callback = function(status, output)
    {
      if (status === 200)
      {
        scope.pathElem.innerHTML = scope.path + ":";
        var formatter = new Brain4it.Formatter();
        var text = formatter.format(output);
        scope.editor.setValue(text);
      }
      else
      {
        var messageDialog = new MessageDialog("Error",
          "Load failed: " + status, "error");
        messageDialog.show();
      }
    };
    client.send();
  },

  save : function()
  {
    var scope = this;
    var data = scope.editor.getValue();

    var path = this.module + "/" + this.path;
    var client = new Brain4it.Client(this.serverUrl, path, this.accessKey);
    client.method = "PUT";
    client.callback = function(status, output)
    {
      if (status === 200)
      {
        scope.pathElem.innerHTML = scope.path;
      }
      else
      {
        var messageDialog = new MessageDialog("Error",
          "Save failed: " + status + "<br>" + output, "error");
        messageDialog.show();
      }
    };
    client.send(data);
  },
  
  loadPath : function()
  {
    var scope = this;
    var pathDialog = new InputDialog("Load", "Path:", this.path);
    pathDialog.onAccept = function(path)
    {
      scope.path = path;
      scope.load();
      scope.editor.focus();
    };
    pathDialog.onCancel = function()
    {
      scope.editor.focus();
    };
    pathDialog.show();
  },
  
  savePath : function()
  {
    var scope = this;
    var pathDialog = new InputDialog("Save", "Path:", this.path);
    pathDialog.onAccept = function(path)
    {
      scope.path = path;
      scope.save();
    };
    pathDialog.show();
  },
  
  updateLayout: function()
  {
     var height = this.toolBarElem.clientHeight + 1;
     this.bodyElem.style.top = height  + "px";
  }  
};


