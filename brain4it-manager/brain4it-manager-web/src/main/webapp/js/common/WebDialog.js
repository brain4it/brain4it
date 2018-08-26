/*
 * WebDialog.js
 *
 * author: realor
 */

WebDialog = function(title, width, height)
{
  this.curtainElem = document.createElement("div");
  this.curtainElem.className = "web_dialog_curtain";
  this.curtainElem.style.position = "absolute";
  this.curtainElem.style.top = "0";
  this.curtainElem.style.bottom = "0";
  this.curtainElem.style.left = "0";
  this.curtainElem.style.right = "0";

  this.dialogElem = document.createElement("div");
  this.dialogElem.className = "web_dialog";
  this.dialogElem.style.position = "absolute";
  this.dialogElem.style.width = width + "px";
  this.dialogElem.style.height = height + "px";
  this.dialogElem.style.left = "50%";
  this.dialogElem.style.marginLeft = "-" + (width / 2) + "px";
  this.dialogElem.style.top = "50%";
  this.dialogElem.style.marginTop = "-" + (height / 2) + "px";

  this.headerElem = document.createElement("div");
  this.headerElem.innerHTML = title;
  this.headerElem.className = "header";
  this.dialogElem.appendChild(this.headerElem);

  this.bodyElem = document.createElement("div");
  this.bodyElem.className = "body";
  this.dialogElem.appendChild(this.bodyElem);

  this.footerElem = document.createElement("div");
  this.footerElem.className = "footer";
  this.dialogElem.appendChild(this.footerElem);
};

WebDialog.prototype =
{
  addText : function(text, className)
  {
    var textElem = document.createElement("span");
    textElem.innerHTML = text;
    if (className)
    {
      textElem.className = className;
    }
    this.bodyElem.appendChild(textElem);
    return textElem;
  },

  addTextField : function(name, label, value, className)
  {
    var groupElem = document.createElement("div");
    groupElem.className = className || "text_field";
    this.bodyElem.appendChild(groupElem);

    var labelElem = document.createElement("label");
    labelElem.htmlFor = name;
    labelElem.innerHTML = label;
    labelElem.style.display = "block";
    groupElem.appendChild(labelElem);

    var inputElem = document.createElement("input");
    inputElem.id = name;
    inputElem.name = name;
    inputElem.type = "text";
    inputElem.spellcheck = false;
    inputElem.style.display = "block";
    if (value) inputElem.value = value;
    groupElem.appendChild(inputElem);

    return inputElem;
  },

  addSelectField : function(name, label, options, value, className)
  {
    var groupElem = document.createElement("div");
    groupElem.className = className || "select_field";
    this.bodyElem.appendChild(groupElem);

    var labelElem = document.createElement("label");
    labelElem.htmlFor = name;
    labelElem.innerHTML = label;
    labelElem.style.display = "block";
    groupElem.appendChild(labelElem);

    var selectElem = document.createElement("select");
    selectElem.id = name;
    selectElem.name = name;
    selectElem.style.display = "block";
    groupElem.appendChild(selectElem);

    var found = false;
    for (var i = 0; i < options.length; i++)
    {
      var option = options[i];
      var optionElem = document.createElement("option");
      if (option instanceof Array)
      {
        optionElem.value = option[0];
        optionElem.innerHTML = option[1];
      }
      else
      {
        optionElem.value = option;
        optionElem.innerHTML = option;
      }
      selectElem.appendChild(optionElem);
      if (optionElem.value == value) found = true;
    }
    if (value)
    {
      if (!found)
      {
        var optionElem = document.createElement("option");
        optionElem.value = value;
        optionElem.innerHTML = value;
        selectElem.appendChild(optionElem);
      }
      selectElem.value = value;
    }
    return selectElem;
  },

  addRadioButtons : function(name, options, value, className)
  {
    var groupElem = document.createElement("div");
    groupElem.className = className || "radio_buttons";
    this.bodyElem.appendChild(groupElem);

    var hiddenElem = document.createElement("input");
    hiddenElem.type = "hidden";
    groupElem.appendChild(hiddenElem);

    for (var i = 0; i < options.length; i++)
    {
      var option = options[i];

      var radioElem = document.createElement("input");
      radioElem.id = name + "_" + i;
      radioElem.type = "radio";
      radioElem.name = name;
      radioElem.value = option instanceof Array ? option[0] : option;
      if (value == radioElem.value)
      {
        radioElem.checked = true;
        hiddenElem.value = radioElem.value;
      }
      radioElem.addEventListener("click", function(event)
      {
        var elem = event.target || event.srcElement;
        hiddenElem.value = elem.value;
      }, false);
      groupElem.appendChild(radioElem);

      var labelElem = document.createElement("label");
      labelElem.innerHTML = option instanceof Array ? option[1] : option;
      labelElem.htmlFor = radioElem.id;
      groupElem.appendChild(labelElem);
    }
    return hiddenElem;
  },

  addButton : function(name, label, action)
  {
    var scope = this;
    var buttonElem = document.createElement("button");
    buttonElem.id = name;
    buttonElem.innerHTML = label;
    buttonElem.addEventListener("click", function(){action(scope);}, false);
    this.footerElem.appendChild(buttonElem);
  },

  show : function(parentNode)
  {
    if (!this.dialogElem.parentNode)
    {
      parentNode = parentNode || document.body;
      parentNode.appendChild(this.curtainElem);
      parentNode.appendChild(this.dialogElem);
      var inputElems = this.dialogElem.getElementsByTagName("input");
      if (inputElems.length > 0)
      {
        inputElems[0].focus();
      }      
      this.onShow();
    }
  },

  hide : function()
  {
    var parentNode = this.dialogElem.parentNode;
    if (parentNode)
    {
      parentNode.removeChild(this.dialogElem);
      parentNode.removeChild(this.curtainElem);
      this.onHide();
    }
  },

  onShow : function()
  {
  },

  onHide : function()
  {
  }
};
