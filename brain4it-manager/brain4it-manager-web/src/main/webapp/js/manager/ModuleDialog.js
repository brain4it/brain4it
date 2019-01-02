/**
 * ModuleDialog.js
 *
 * @author realor
 */

ModuleDialog = function(title, module)
{
  WebDialog.call(this, title, 400, 200);

  this.module = module;
  this.nameElem = this.addTextField("module_name", "Module name:", module.name);
  this.keyElem = this.addTextField("module_key",
    "Access key (leave blank to access with server key):", module.accessKey);
  this.nameElem.setAttribute("autocomplete", "off");
  this.keyElem.setAttribute("autocomplete", "off");
  this.messageElem = this.addText("", "error_message");

  if (module.name)
  {
    this.nameElem.readOnly = true;
  }
  var scope = this;
  this.addButton("module_accept", "Accept", function(){ scope.accept();});
  this.addButton("module_cancel", "Cancel", function(){ scope.cancel();});
};

ModuleDialog.prototype = Object.create(WebDialog.prototype);

ModuleDialog.prototype.accept = function()
{
  var moduleName = this.nameElem.value.trim();
  if (moduleName.length === 0)
  {
    this.messageElem.innerHTML = "Module name is mandatory";
    return;
  }
  var accessKey = this.keyElem.value;

  this.module.name = moduleName;
  this.module.setAccessKey(accessKey);

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
