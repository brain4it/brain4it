/**
 * Tree.js
 * 
 * @author realor
 */
Brain4it.Tree =
{
  elementId : "tree",
  sequence : 0,
  treeElem : null,
  serversListElem : null,
  selectedItemElem : null,
  serverAction : null,
  moduleAction : null,
  
  buttonListener : function(event)
  {
    var buttonElem = event.target || event.srcElement;
    var itemElem = buttonElem.parentNode;

    if (this.isExpanded(itemElem))
    {
      this.setExpanded(itemElem, false);
    }
    else
    {
      this.setExpanded(itemElem, true);
    }
  },

  linkListener : function(event)
  {
    var elem = (event.target || event.srcElement);
    while (elem.nodeName !== 'LI') elem = elem.parentNode;
    this.selectItem(elem);
  },

  actionListener : function(event)
  {
    var elem = (event.target || event.srcElement);
    while (elem.nodeName !== 'LI') elem = elem.parentNode;
    var object = elem._object;

    if (object instanceof Brain4it.Server && this.serverAction)
    {
      this.serverAction.invoke();
    }
    else if (object instanceof Brain4it.Module && this.moduleAction)
    {
      this.moduleAction.invoke();
    }  
  },
  
  init : function()
  {
    this.treeElem = document.getElementById(this.elementId);
    this.serversListElem = document.createElement("ul");
    this.treeElem.appendChild(this.serversListElem);
    
    this._buttonListener = this.buttonListener.bind(this);
    this._linkListener = this.linkListener.bind(this);
    this._actionListener = this.actionListener.bind(this);
  },
  
  isExpanded : function(itemElem)
  {
    return itemElem._listElem.style.display !== "none";
  },
  
  setExpanded : function(itemElem, expanded)
  {
    if (expanded)
    {
      itemElem._buttonElem.className =  "collapse";
      itemElem._listElem.style.display =  "block";
    }
    else
    {
      itemElem._buttonElem.className = "expand";
      itemElem._listElem.style.display = "none";      
    }
  },
  
  selectItem : function(itemElem)
  {
    var className;
    if (this.selectedItemElem !== null)
    {
      className = this.selectedItemElem.className;
      this.selectedItemElem.className = className.replace(" selected", "");
    }
    this.selectedItemElem = itemElem;

    className = this.selectedItemElem.className;
    this.selectedItemElem.className = className + " selected";
    
    this.selectedItemElem._linkElem.focus();

    Brain4it.Manager.selectedObject = this.selectedItemElem._object;    
  },
  
  addItem : function(parentItemElem, object)
  {
    var itemElem = document.createElement("li");
    this.sequence++;
    itemElem.id = "node_" + this.sequence;
    
    var linkElem = document.createElement("a");
    linkElem.href = "#";
    linkElem.setAttribute("role", "button");
    linkElem.addEventListener("click", this._linkListener, false);
    linkElem.addEventListener("dblclick", this._actionListener, false);
    itemElem.appendChild(linkElem);
    itemElem._linkElem = linkElem;

    var nameElem = document.createElement("span");
    nameElem.innerHTML = "name";
    nameElem.className = "name";
    linkElem.appendChild(nameElem);
    itemElem._nameElem = nameElem;

    var infoElem = document.createElement("span");
    infoElem.innerHTML = "info";
    infoElem.className = "info";
    linkElem.appendChild(infoElem);
    itemElem._infoElem = infoElem;

    var listElem = parentItemElem ? 
      parentItemElem._listElem : this.serversListElem;
    
    if (listElem)
    {
      // parentItemElem already has listElem
    }
    else
    {
      // create button & list for parentItemElem
      var buttonElem = document.createElement('button');
      buttonElem.className = "expand";
      buttonElem.addEventListener("click", this._buttonListener);
      parentItemElem.insertBefore(buttonElem, parentItemElem.childNodes[0]);
      parentItemElem._buttonElem = buttonElem;

      listElem = document.createElement("ul");
      listElem.style.display = "none";
      parentItemElem.appendChild(listElem);
      parentItemElem._listElem = listElem;    
    }
    listElem.appendChild(itemElem);

    if (object)
    {
      this.setItemObject(itemElem, object);
    }
    return itemElem;
  },
  
  removeItem : function(itemElem)
  {
    itemElem.parentNode.removeChild(itemElem);
  },
  
  setItemObject : function(itemElem, object)
  {
    itemElem._object = object;

    if (object instanceof Brain4it.Server)
    {
      var server = object;
      itemElem._nameElem.innerHTML = server.name + 
        " (" + server.modules.length + ")";
      itemElem._infoElem.innerHTML = server.url;
      itemElem.className = "server";
    }
    else if  (object instanceof Brain4it.Module)
    {
      var module = object;
      itemElem._nameElem.innerHTML = module.name;
      itemElem._infoElem.innerHTML = "Module " + module.name;
      itemElem.className = "module";

      var metadata = module.metadata;
      if (metadata)
      {
        var description = metadata.getByName("description");
        if (description !== null)
        {
          itemElem._infoElem.innerHTML = description;
        }
        var icon = metadata.getByName("icon");
        if (icon !== null)
        {
          itemElem.className = "module " + icon;
        }
      }
    }  
  },
  
  getParentItemElem : function(itemElem)
  {
    return itemElem.parentNode.parentNode;
  },
  
  update : function()
  {
    this.serversListElem.innerHTML = "";

    var servers = Brain4it.Manager.workspace.servers;
    for (var i = 0; i < servers.length; i++)
    {
      var server = servers[i];      
      var serverItemElem = this.addItem(null, server);
      this.updateList(serverItemElem, server.modules);
    }
  },
    
  updateList : function(itemElem, objects)
  {
    if (itemElem._listElem)
    {
      itemElem._listElem.innerHTML = "";
    }
    for (var j = 0; j < objects.length; j++)
    {
      this.addItem(itemElem, objects[j]);
    }
  }
};
