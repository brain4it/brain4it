/**
 * ServerDialog.js
 * 
 * @author realor
 */

ServerDialog = function(title, server)
{
  WebDialog.call(this, title, 300, 220);
  
  this.server = server;
  this.nameElem = this.addTextField("server_name", "Name:", server.name);
  this.urlElem = this.addTextField("server_url", "Url:", server.url);
  this.keyElem = this.addTextField("server_key", "Access key:", 
    server.accessKey);
  
  var scope = this;
  this.addButton("server_accept", "Accept", function(){ scope.accept();});
  this.addButton("server_cancel", "Cancel", function(){ scope.cancel();});
};

ServerDialog.prototype = Object.create(WebDialog.prototype);

ServerDialog.prototype.accept = function()
{
  this.server.name = this.nameElem.value;
  this.server.url = this.urlElem.value;
  this.server.accessKey = this.keyElem.value;
  
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
