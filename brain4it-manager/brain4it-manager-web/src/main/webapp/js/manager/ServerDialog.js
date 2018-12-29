/**
 * ServerDialog.js
 * 
 * @author realor
 */

ServerDialog = function(title, server)
{
  WebDialog.call(this, title, 400, 250);
  
  this.server = server;
  this.nameElem = this.addTextField("server_name", "Server name:", server.name);
  this.urlElem = this.addTextField("server_url", "Server URL:", server.url);
  this.keyElem = this.addTextField("server_key", "Access key:", 
    server.accessKey);
  this.keyElem.setAttribute("autocomplete", "off");
  this.messageElem = this.addText("", "error_message");
  
  var scope = this;
  this.addButton("server_accept", "Accept", function(){ scope.accept();});
  this.addButton("server_cancel", "Cancel", function(){ scope.cancel();});
};

ServerDialog.prototype = Object.create(WebDialog.prototype);

ServerDialog.prototype.accept = function()
{
  this.messageElem.innerHTML = "";
  
  var serverName = this.nameElem.value.trim();
  if (serverName.length === 0)
  {
    this.messageElem.innerHTML = "Server name is mandatory";
    return;
  }
  var serverUrl = this.urlElem.value.trim();
  if (serverUrl.trim().length === 0)
  {
    this.messageElem.innerHTML = "Server URL is mandatory";
    return;
  }
  if (!isValidURL(serverUrl))
  {
    this.messageElem.innerHTML = "Invalid URL";
    return;
  }
  var accessKey = this.keyElem.value.trim();

  this.server.name = serverName;
  this.server.url = serverUrl;
  this.server.setAccessKey(accessKey);
  
  this.onAccept(this.server);
  WebDialog.prototype.hide.call(this);
};

ServerDialog.prototype.cancel = function()
{
  this.onCancel();
  WebDialog.prototype.hide.call(this);
};

ServerDialog.prototype.onAccept = function(server)
{
};

ServerDialog.prototype.onCancel = function()
{
};
