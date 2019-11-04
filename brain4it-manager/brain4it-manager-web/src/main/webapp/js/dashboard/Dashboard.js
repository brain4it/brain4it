/* Dashboard.js */

Brain4it.Dashboard = function(serverUrl, module, accessKey)
{
  this.serverUrl = serverUrl;
  this.module = module;
  this.accessKey = accessKey;
  function s4()
  {
    return Math.floor((1 + Math.random()) * 0x10000).toString(16).substring(1);
  }
  var sessionId = s4() + s4() + '-' + s4() + '-' + s4() + '-' + s4() +
    '-' + s4() + s4() + s4();
  this.sessionId = sessionId;
  this.dashboards = null;
  this.widgets = [];
  this.dashboardIndex = 0;
  this.monitor =
    new Brain4it.Monitor(serverUrl, module, accessKey, sessionId);
  this.dashboardsMonitor =
    new Brain4it.Monitor(serverUrl, module, accessKey, sessionId);
  var client = new Brain4it.Client(serverUrl, null, accessKey, sessionId);
  this.invoker = new Brain4it.Invoker(client, module);
};

Brain4it.Dashboard.prototype =
{
  widgetTypes : {},

  init : function(containerElem)
  {
    this.toolBarElem = document.createElement("div");
    this.toolBarElem.className = "toolbar";
    containerElem.appendChild(this.toolBarElem);

    this.dashboardElem = document.createElement("div");
    this.dashboardElem.className = "dashboard";
    containerElem.appendChild(this.dashboardElem);

    var _dashboardsListener = this.dashboardsListener.bind(this);

    var scope = this;

    this.refreshButton = createButton("refresh_button", "Refresh", "refresh",
      function() {
        scope.dashboardsMonitor.unwatchAll();
        scope.dashboardsMonitor.watch(Brain4it.DASHBOARDS_FUNCTION_NAME,
          _dashboardsListener
        );
      });
    this.toolBarElem.appendChild(this.refreshButton);

    this.dashboardSelect = document.createElement("select");
    this.dashboardSelect.className = "dashboard_selector";
    this.toolBarElem.appendChild(this.dashboardSelect);

    this.dashboardSelect.addEventListener("change", function(event) {
      scope.createDashboard(scope.dashboardSelect.selectedIndex); }, false);

    window.addEventListener("resize", function() { scope.doLayout(); }, false);
    this.dashboardsMonitor.watch(Brain4it.DASHBOARDS_FUNCTION_NAME,
      _dashboardsListener
    );
  },

  show : function()
  {
  },

  hide : function()
  {
    // call unwatch in sync mode to ensure the connection is closed
    this.monitor.unwatchAll(true);
    this.dashboardsMonitor.unwatchAll(true);
  },

  dashboardsListener : function(functionName, value, serverTime)
  {
    this.loadDashboards(value);
  },

  loadDashboards : function(value)
  {
    this.dashboards = null;
    this.dashboardIndex = 0;
    this.dashboardElem.innerHTML = "";
    this.dashboardSelect.innerHTML = "";

    if (value instanceof Brain4it.List)
    {
      this.dashboards = value;
      if (this.dashboards.size() > 0)
      {
        for (var i = 0; i < this.dashboards.size(); i++)
        {
          var optionElem = document.createElement("option");
          optionElem.value = i;
          var dashboardName = this.dashboards.getName(i);
          if (dashboardName === null) dashboardName = "dashboard-" + i;
          optionElem.innerHTML = dashboardName;
          this.dashboardSelect.appendChild(optionElem);
        }
      }
    }
    if (this.dashboards !== null && this.dashboards.size() > 0)
    {
      this.createDashboard(0);
    }
    else
    {
      // module has no dashboards
      this.monitor.unwatchAll();
    }
  },

  createDashboard : function(index)
  {
    try
    {
      this.monitor.unwatchAll();
      this.widgets = [];
      this.dashboardElem.innerHTML = "";
      this.dashboardIndex = index;
      this.doLayout();
    }
    catch (ex)
    {
      console.info(ex);
    }
  },

  doLayout : function()
  {
    var dashboard = this.dashboards.getByIndex(this.dashboardIndex);
    if (dashboard === null) return;

    var widgetDefs = dashboard.getByName("widgets");
    var layouts = dashboard.getByName("layouts");
    var pollingInterval = dashboard.getByName("polling-interval");
    this.monitor.pollingInterval = pollingInterval;
    var layout = this.selectLayout(layouts);
    if (widgetDefs === null || layouts === null) return;

    var dimensions = layout.getByName("dimensions");
    var stretch = layout.getByName("stretch") ? true : false;
    var layoutWidth = dimensions.getByIndex(0);
    var layoutHeight = dimensions.getByIndex(1);
    var dbWidth = this.dashboardElem.clientWidth;
    var dbHeight = this.dashboardElem.clientHeight;
    var cellWidth = dbWidth / layoutWidth;
    var cellHeight = dbHeight / layoutHeight;
    var xOffset = 0;
    var yOffset = 0;
    if (!stretch)
    {
      var cellSize = Math.min(cellWidth, cellHeight);
      if (cellWidth > cellSize)
      {
        xOffset += 0.5 * (dbWidth - (layoutWidth * cellSize));
        cellWidth = cellSize;
      }
      else if (cellHeight > cellSize)
      {
        yOffset += 0.5 * (dbHeight - (layoutHeight * cellSize));
        cellHeight = cellSize;
      }
    }
    var types = Brain4it.Dashboard.prototype.widgetTypes;

    var widgetLayouts = layout.getByName("widgets");
    for (var w = 0; w < widgetLayouts.size(); w++)
    {
      var widgetLayout = widgetLayouts.getByIndex(w);
      var widgetName = widgetLayout.getByIndex(0);
      var widgetDef = widgetDefs.getByName(widgetName);
      if (widgetDef)
      {
        var widgetType = widgetDef.getByName("type");
        if (types[widgetType])
        {
          var x = widgetLayout.getByIndex(1);
          var y = widgetLayout.getByIndex(2);
          var widgetWidth = 1;
          var widgetHeight = 1;
          if (widgetLayout.size() >= 5)
          {
            widgetWidth = widgetLayout.getByIndex(3);
            widgetHeight = widgetLayout.getByIndex(4);
          }
          var widget;
          var widgetElem;
          var doInit = false;
          if (w < this.widgets.length) // widget already created
          {
            widget = this.widgets[w];
            widgetElem = widget.element;
          }
          else // widget and element must be created created
          {
            widgetElem = document.createElement("div");
            widgetElem.className = "widget";
            this.dashboardElem.appendChild(widgetElem);
            var widget = new types[widgetType](this, widgetElem);
            this.widgets.push(widget);
            doInit = true;
          }
          // update layout
          widgetElem.style.width = (widgetWidth * cellWidth) + "px";
          widgetElem.style.height = (widgetHeight * cellHeight) + "px";
          widgetElem.style.left = (xOffset + x * cellWidth) + "px";
          widgetElem.style.top = (yOffset + y * cellHeight) + "px";
          if (doInit)
          {
            console.info("init " + widgetName);
            widget.init(widgetName, widgetDef);
          }
        }
      }
    }
  },

  selectLayout : function(layouts)
  {
    return layouts.size() > 0 ? layouts.getByIndex(0) : null;
  }
};


