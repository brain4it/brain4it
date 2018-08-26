/**
 * ModuleDialog.js
 * 
 * @author realor
 */

ModuleDialog = function(title, module, action)
{
  WebDialog.call(this, title, 300, 180);

  this.module = module;
  this.nameElem = this.addTextField("module_name", "Name:", module.name);
  this.keyElem = this.addTextField("module_key", "Access key:", 
    module.accessKey);

  var scope = this;
  this.addButton("module_accept", "Accept", function(){ scope.accept();});
  this.addButton("module_cancel", "Cancel", function(){ scope.cancel();});
};

ModuleDialog.prototype = Object.create(WebDialog.prototype);

ModuleDialog.prototype.accept = function()
{
  this.module.name = this.nameElem.value;
  this.module.accessKey = this.keyElem.value;
  
  this.onAccept(this.module);
  WebDialog.prototype.hide.call(this);
};

ModuleDialog.prototype.cancel = function()
{
  this.onCancel();
  WebDialog.prototype.hide.call(this);
};

ModuleDialog.prototype.onAccept = function(module)
{
};

ModuleDialog.prototype.onCancel = function()
{
};
