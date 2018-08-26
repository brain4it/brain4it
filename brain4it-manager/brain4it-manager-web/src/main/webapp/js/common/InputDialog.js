/**
 * InputDialog.js
 * 
 * @author realor
 */

InputDialog = function(title, label, value)
{
  WebDialog.call(this, title, 300, 140);
  
  if (value === undefined) value = null;
  
  this.inputElem = this.addTextField("input_text", label, value);
  
  var scope = this;
  this.addButton("item_accept", "Accept", function(){ scope.accept();});
  this.addButton("item_cancel", "Cancel", function(){ scope.cancel();});
};

InputDialog.prototype = Object.create(WebDialog.prototype);

InputDialog.prototype.accept = function()
{
  var value = this.inputElem.value;
  
  this.onAccept(value);
  WebDialog.prototype.hide.call(this);
};

InputDialog.prototype.cancel = function()
{
  this.onCancel();
  WebDialog.prototype.hide.call(this);
};

InputDialog.prototype.onAccept = function(value)
{
};

InputDialog.prototype.onCancel = function()
{
};
