/**
 * TabbedPanel.js
 * 
 * @author realor
 */
Brain4it.TabbedPanel = function(elementId)
{
  this.element = document.getElementById(elementId);

  this.tabsElem = document.createElement("div");
  this.tabsElem.className = "tab_selectors";
  this.element.appendChild(this.tabsElem);

  this.panelsElem = document.createElement("div");
  this.panelsElem.className = "tab_panels";
  this.element.appendChild(this.panelsElem);

  this.panelNames = [];
  this._activePanelName;
  this.counter = 0;
  
  var scope = this;
  window.addEventListener("resize", function(){scope.updateLayout();}, false);
};

Brain4it.TabbedPanel.prototype =
{
  addTab : function(name, label, url, className)
  {
    var scope = this;
    if (name === null)
    {
      name = "panel_" + this.counter;
      this.counter++;
    }

    var tabElem = document.createElement("div");
    tabElem.id = this.element.id + "_t_" + name;
    this.tabsElem.appendChild(tabElem);
    
    var spanElem = document.createElement("span");
    spanElem.innerHTML = label ? label : "";
    if (className)
    {
      spanElem.className = className;
    }
    tabElem.appendChild(spanElem);

    var buttonElem = document.createElement("button");
    buttonElem.className = "close_button";
    tabElem.appendChild(buttonElem);

    var panelElem = document.createElement("div");
    panelElem.id = this.element.id + "_p_" + name;
    panelElem.className = "panel";
    this.panelsElem.appendChild(panelElem);

    tabElem.addEventListener('click', function(event)
    {
      scope.showPanel(name);
    }, false);

    buttonElem.addEventListener('click', function(event)
    {
      event.preventDefault();
      scope.removeTab(name);
    }, false);

    if (this.panelNames.length === 0)
    {
      panelElem.style.display = "block";
      tabElem.className = "tab selected";
      this.activePanelName = name;
    }
    else
    {
      panelElem.style.display = "none";
      tabElem.className = "tab";
    }

    this.panelNames.push(name);

    if (url)
    {
      var iframeElem = document.createElement("iframe");
      iframeElem.id = this.element.id + "_i_" + name;
      iframeElem.className = "external";
      iframeElem.src = url;
      iframeElem.width = "100%";
      iframeElem.height = "100%";
      iframeElem.frameBorder = "0";
      panelElem.appendChild(iframeElem);
      
      var expandElem = document.createElement("a");
      expandElem.className = "expand_tab";
      expandElem.target = "_blank";
      expandElem.title = "Expand";
      expandElem.href = url;
      panelElem.appendChild(expandElem);
    }
    this.updateLayout();
    return name;
  },

  removeTab : function(name)
  {
    var index = this.panelNames.indexOf(name);
    if (index >= 0)
    {
      var iframe = this.getIFrame(name);
      if (iframe && iframe.contentWindow.hide)
      {
        try
        {
          iframe.contentWindow.hide();
        }
        catch (ex)
        {
          // ignore
        }
      }
      this.panelNames.splice(index, 1);
      var tabElem = this.getTab(name);
      var panelElem = this.getPanel(name);
      this.tabsElem.removeChild(tabElem);
      this.panelsElem.removeChild(panelElem);
      if (this._activePanelName === name && this.panelNames.length > 0)
      {
        this.showPanel(this.panelNames[0]);
      }
    }
    this.updateLayout();
  },

  removeTabs : function()
  {
    this.tabsElem.innerHTML = "";
    this.panelsElem.innerHTML = "";
    this.panelNames = [];
    this._activePanelName = null;
    this.updateLayout();
  },

  showPanel : function(name)
  {
    var index = this.panelNames.indexOf(name);
    if (index >= 0)
    {
      for (var i = 0; i < this.panelNames.length; i++)
      {
        var iname = this.panelNames[i];
        var tabElem = this.getTab(iname);
        var panelElem = this.getPanel(iname);
        if (name === iname)
        {
          tabElem.className = "tab selected";
          panelElem.style.display = "block";
        }
        else
        {
          tabElem.className = "tab";
          panelElem.style.display = "none";
        }
      }
      this._activePanelName = name;
    }
    var iframe = this.getIFrame(name);
    if (iframe && iframe.contentWindow.show)
    {
      iframe.contentWindow.show();
    }
  },

  updateLayout : function()
  {
    var height = this.tabsElem.clientHeight + 1;
    this.panelsElem.style.top = height + "px";
  },

  getTab : function(name)
  {
    return document.getElementById(this.element.id + "_t_" + name);
  },

  getPanel : function(name)
  {
    return document.getElementById(this.element.id + "_p_" + name);
  },

  getIFrame : function(name)
  {
    return document.getElementById(this.element.id + "_i_" + name);
  }
};

