/**
 * MessageDialog.js
 * 
 * @author realor
 */

MessageDialog = function(title, message, className)
{
  WebDialog.call(this, title, 300, 200);
  var scope = this;
 
  this.addText(message, className);
  this.addButton("confirm_accept", "Accept", function(){ scope.accept();});
};

MessageDialog.prototype = Object.create(WebDialog.prototype);

MessageDialog.prototype.accept = function()
{
  this.onAccept(this.server);
  WebDialog.prototype.hide.call(this);
};

MessageDialog.prototype.onAccept = function()
{
};
