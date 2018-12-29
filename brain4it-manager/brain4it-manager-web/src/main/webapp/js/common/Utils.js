/**
 * Utils.js
 * 
 * @author realor
 */
function getQueryParams()
{
  var queryString = {};
  var query = window.location.search.substring(1);
  var pairs = query.split("&");
  for (var i = 0; i < pairs.length; i++)
  {
    var index = pairs[i].indexOf("=");
    if (index !== -1)
    {
      var name = decodeURIComponent(pairs[i].substring(0, index));
      var value = decodeURIComponent(pairs[i].substring(index + 1));
      if (typeof queryString[name] === "undefined") 
      {
        queryString[name] = value;
      }
      else if (typeof queryString[name] === "string") 
      {
        var arr = [queryString[name], value];
        queryString[name] = arr;
      }
      else 
      {
        queryString[name].push(value);
      }
    }
  }
  return queryString;  
};

function toHTML(text)
{
  var html = "";
  var ch, i;
  for (i = 0; i < text.length; i++)
  {
    ch = text.charAt(i);
    if (ch === '<') html += "&lt;";
    else if (ch === '>') html += "&gt;";
    else if (ch === '&') html += "&amp;";
    else if (ch === '\n') html += "<br>";
    else html += ch;
  }
  return html;  
};

function createButton(id, label, className, downListener, upListener)
{
  var buttonElem = document.createElement("button");
  if (id)
  {
    buttonElem.id = id;
  }
  if (label)
  {
    buttonElem.innerHTML = label;
  }
  buttonElem.className = className;

  buttonElem.addEventListener("touchstart", function(e) {
    e.preventDefault();
    buttonElem.className = className + " active";
    if (downListener) downListener(e); }, false);

  buttonElem.addEventListener("mousedown", function(e) { 
    e.preventDefault();
    buttonElem.className = className + " active"; 
    if (downListener) downListener(e); }, false);
  
  buttonElem.addEventListener("touchend", function(e) { 
    e.preventDefault();
    if (buttonElem.className === className + " active")
    {
      buttonElem.className = className;
    }
    if (upListener) upListener(e); }, false);  

  buttonElem.addEventListener("mouseup", function(e) { 
    e.preventDefault();
    if (buttonElem.className === className + " active")
    {
      buttonElem.className = className;
    }
    if (upListener) upListener(e); }, false);

  return buttonElem;
};

function createFileChooserButton(id, label, className, listener)
{
  var spanElem = document.createElement("span");
  var inputElem = document.createElement("input");
  inputElem.type = "file";
  spanElem.appendChild(inputElem);
  inputElem.style.display = "none";
  var labelElem = document.createElement("label");
  spanElem.appendChild(labelElem);
  if (label)
  {
    labelElem.innerHTML = label;
  }
  spanElem.className = className;
  inputElem.id = id;
  labelElem.htmlFor = id;

  inputElem.addEventListener('change', listener, false);

  return spanElem;
};

function isValidURL(str)
{
  var pattern = /(http|https):\/\/(\w+:{0,1}\w*@)?(\S+)(:[0-9]+)?(\/|\/([\w#!:.?+=&%@!\-\/]))?/;
  return pattern.test(str);
};
