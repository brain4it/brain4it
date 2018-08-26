/**
 * ToolBar.js
 *
 * @author realor
 */
Brain4it.ToolBar = function(elementId)
{
  this.element = document.getElementById(elementId);

  this.actions = [];
  this.actionsByName = {};
};

Brain4it.ToolBar.prototype = 
{
  addAction : function(action)
  {
    this.actions.push(action);
    this.actionsByName[action.name] = action;
    
    var actionElem = document.createElement("button");
    actionElem.id = this.element.id + "_" + action.name;
    actionElem.className = "action " + action.className;
    actionElem.title = action.label;
    this.element.appendChild(actionElem);
    actionElem.addEventListener("click", function(event)
    {
      action.invoke();
    });
    action.element = actionElem;
    
    return action;
  },
  
  update : function()
  {
    for (var i = 0; i < this.actions.length; i++)
    {
      var action = this.actions[i];
      if (action.isEnabled())
      {
        action.element.style.display = null;
      }
      else
      {
        action.element.style.display = "none";        
      }
    }
  }
};

Brain4it.Action = function(name, label, className)
{
  this.name = name;
  this.label = label;
  this.className = className || name;
  this.element = null;
};

Brain4it.Action.prototype =
{
  invoke : function()
  {
  },
  
  isEnabled : function()
  {
    return true;
  }
};


