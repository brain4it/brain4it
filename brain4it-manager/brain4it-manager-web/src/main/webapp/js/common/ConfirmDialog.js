/**
 * ConfirmDialog.js
 * 
 * @author realor
 */

ConfirmDialog = function(title, message)
{
  WebDialog.call(this, title, 300, 200);
  var scope = this;
 
  this.addText(message, "confirm");
  this.addButton("confirm_accept", "Accept", function(){ scope.accept();});
  this.addButton("confirm_cancel", "Cancel", function(){ scope.cancel();});
};

ConfirmDialog.prototype = Object.create(WebDialog.prototype);

ConfirmDialog.prototype.accept = function()
{
  this.onAccept(this.server);
  WebDialog.prototype.hide.call(this);
};

ConfirmDialog.prototype.cancel = function()
{
  this.onCancel();
  WebDialog.prototype.hide.call(this);
};

ConfirmDialog.prototype.onAccept = function()
{
};

ConfirmDialog.prototype.onCancel = function()
{
};
