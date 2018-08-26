/* Console.js */

Brain4it.Console = function(serverUrl, module, accessKey)
{
  this.serverUrl = serverUrl;
  this.module = module;
  this.accessKey = accessKey;
  this.consoleElem = null;
  this.toolBarElem = null;
  this.bodyElem = null;
  this.inputElem = null;
  this.outputElem = null;
  this.editor = null;
  this.PROMPT_HTML = "&gt;&nbsp;";
  this.formatStrings = true;
  this.HISTORY_SIZE = 10;
  this.history = [];
  this.historyIndex = 0;
  this.helper = new Brain4it.Helper(serverUrl, module, accessKey);
  CodeMirror.Brain4itHelper = this.helper;
};

Brain4it.Console.prototype =
{
  init: function(containerElem, options)
  {
    this.consoleElem = document.createElement("div");
    this.consoleElem.className = "console";
    containerElem.appendChild(this.consoleElem);

    this.toolBarElem = document.createElement("div");
    this.toolBarElem.className = "toolbar";
    this.consoleElem.appendChild(this.toolBarElem);

    this.bodyElem = document.createElement("div");
    this.bodyElem.className = "console_body";
    this.consoleElem.appendChild(this.bodyElem);

    this.scrollElem = document.createElement("div");
    this.scrollElem.className = "console_scroll";
    this.bodyElem.appendChild(this.scrollElem);

    this.outputElem = document.createElement("div");
    this.outputElem.className = "console_output";
    this.outputElem.style.bottom = 0;
    this.scrollElem.appendChild(this.outputElem);

    this.inputElem = document.createElement("div");
    this.inputElem.className = "console_input";
    this.bodyElem.appendChild(this.inputElem);

    var textArea = document.createElement("textarea");
    textArea.setAttribute("autocapitalize", "off");
    this.inputElem.appendChild(textArea);

    this.editor = CodeMirror.fromTextArea(textArea, options);

    var scope = this;
    this.editor.on("keydown", function(cm, evt) {
      scope.onEditorKeyDown(scope, cm, evt);
    });

    this.editor.focus();

    this.clearButton = createButton("clear_button", "Clear", "clear", 
      function() { scope.clearConsole(); });
    this.toolBarElem.appendChild(this.clearButton);

    this.functionsButton = createButton("functions_button", "Functions", "functions", 
      function() { scope.findFunctions(); });
    this.toolBarElem.appendChild(this.functionsButton);

    this.nextHistoryButton = createButton("next_history_button", "Next", "next_history", 
      function() { scope.loadNextHistory(); });
    this.toolBarElem.appendChild(this.nextHistoryButton);

    this.previousHistoryButton = createButton("previous_history_button", "Previous", "previous_history", 
      function() { scope.loadPreviousHistory(); });
    this.toolBarElem.appendChild(this.previousHistoryButton);

    this.helper.loadFunctions();

    this.updateLayout();

    window.addEventListener("resize", 
      function() { scope.updateLayout(); }, false);
    window.addEventListener("resize",
      function() { scope.updateScroll(); }, false);
  },

  show: function()
  {
    this.editor.refresh();
    this.editor.focus();
  },

  onEditorKeyDown: function(cons, cm, evt)
  {
    if (evt.keyCode === 13) // ENTER
    {
      var command = cons.editor.getValue();
      if (this.isValidCommand(command) && this.isCursorAtEnd())
      {
        evt.preventDefault();    
        cons.consoleExecute();
      }
    }
    else if (evt.keyCode === 38 && evt.altKey) // CURSOR UP
    {
      evt.preventDefault();    
      cons.loadPreviousHistory();
      evt.preventDefault();
    }
    else if (evt.keyCode === 40 && evt.altKey) // CURSOR DOWN
    {
      evt.preventDefault();      
      cons.loadNextHistory();
      evt.preventDefault();
    }
  },

  loadPreviousHistory : function()
  {
    if (this.history.length > 0)
    {
      if (this.historyIndex <= 0)
      {
        this.historyIndex = this.history.length - 1;
      }
      else
      {
        this.historyIndex--;        
      }
      this.editor.setValue(this.history[this.historyIndex]);
      this.editor.execCommand("goDocEnd");
      this.editor.focus();
    }
  },
  
  loadNextHistory : function()
  {
    if (this.history.length > 0)
    {
      if (this.historyIndex >= this.history.length - 1)
      {
        this.historyIndex = 0;
      }
      else
      { 
        this.historyIndex++;
      }
      this.editor.setValue(this.history[this.historyIndex]);
      this.editor.execCommand("goDocEnd");
      this.editor.focus();
    }
  },

  isValidCommand : function(command)
  {
    var count = 0;
    var ch, i;
    for (i = 0; i < command.length; i++)
    {
      ch = command.charAt(i);
      if (ch === '(') count++;
      else if (ch === ')') count--;
    }
    return count === 0;
  },

  isCursorAtEnd : function()
  {
    var cursor = this.editor.getCursor();
    var line = cursor.line;
    var pos = cursor.ch;
    var lineCount = this.editor.lineCount();
    var atEnd = true;
    while (atEnd && line < lineCount)
    {
      var lineText = this.editor.getLine(line);
      var ch = lineText.charAt(pos);
      atEnd = ch.length === 0 || ch.charCodeAt(0) <= 32;
      pos++;
      if (pos >= lineText.length)
      {
        pos = 0;
        line++;
      }
    }
    return atEnd;
  },

  consoleExecute: function()
  {
    var input = this.editor.getValue();
    if (input.charAt(input.length - 1) === '\n')
      input = input.substring(0, input.length - 1);
    if (input.length > 0)
    {
      var index = this.history.indexOf(input);
      if (index === -1)
      {
        if (this.history.length > this.HISTORY_SIZE)
        {
          this.history.shift();
        }
      }
      else
      {
        this.history.splice(index, 1);
      }
      this.history.push(input);
      this.historyIndex = this.history.length;
    }

    var scope = this;
    var client = new Brain4it.Client(this.serverUrl, this.module, this.accessKey);
    client.callback = function(status, output)
    {
      scope.showOutput(output, status !== 200, true);
      scope.editor.focus();
    };
    this.editor.setValue("");
    this.showInput(input);

    client.send(input);
  },

  updateLayout: function()
  {
     var height = this.toolBarElem.clientHeight + 1;
     this.bodyElem.style.top = height  + "px";
  },

  updateScroll : function()
  {
    if (this.outputElem.clientHeight <= this.scrollElem.clientHeight)
    {
      this.outputElem.style.bottom = 0;
    }
    else
    {
      this.outputElem.style.bottom = null;
      this.scrollElem.scrollTop = this.scrollElem.scrollHeight;
    }
  },

  showInput: function(input)
  {
    var lineInputElem = document.createElement("div");
    lineInputElem.className = "cm-s-default";
    this.outputElem.appendChild(lineInputElem);
    CodeMirror.runMode(input, "brain4it", lineInputElem);
    var promptElem = document.createElement("span");
    promptElem.className = "prompt";
    promptElem.innerHTML = this.PROMPT_HTML;
    lineInputElem.insertBefore(promptElem, lineInputElem.firstChild);
    this.updateScroll();
  },

  showOutput: function(output, error, formatted)
  {
    var lineOutputElem = document.createElement("div");
    this.outputElem.appendChild(lineOutputElem);
    if (output.indexOf('"') === 0 && this.formatStrings)
    {
      var string = Brain4it.unescapeString(output);
      lineOutputElem.className = "formatted_string";
      lineOutputElem.innerHTML = toHTML(string);
    }
    else
    {
      if (error)
      {
        lineOutputElem.className = "error";
        lineOutputElem.innerHTML = output;
      }
      else
      {
        lineOutputElem.className = "cm-s-default";
        if (formatted)
        {
          var formatter = new Brain4it.Formatter();
          try
          {
            output = formatter.format(output);
          }
          catch (ex)
          {
          }
        }
        this.highlightResult(output, lineOutputElem);
      }
    }
    this.updateScroll();
  },

  clearConsole : function()
  {
    this.outputElem.innerHTML = "";
    this.updateScroll();
    this.editor.setValue("");
    this.editor.focus();
  },

  findFunctions : function()
  {
    var scope = this;
    this.showInput(this.helper.LOAD_FUNCTIONS);

    this.helper.loadFunctions(function(functions, output)
    {
      scope.showOutput(output, false, false);
      scope.updateScroll();
      scope.editor.focus();
    });
  },
  
  highlightResult : function(result, lineOutputElem)
  {
    var lastTokenEndPosition = -1;
    var tokenizer = new Brain4it.Tokenizer(result);
    var token = new Brain4it.Token();
    tokenizer.readToken(token);
    while (token.type !== Brain4it.Token.EOF)
    {
      if (lastTokenEndPosition !== -1)
      {
        var sep = result.substring(lastTokenEndPosition, token.startPosition);
        if (sep === ' ')
        {
          var textNode = document.createTextNode(" ");      
          lineOutputElem.appendChild(textNode);
        }
        else
        {
          for (var i = 0; i < sep.length; i++)
          {
            var ch = sep[i];
            if (ch === ' ') 
            {
              var textNode = document.createTextNode("\u00A0");
              lineOutputElem.appendChild(textNode);
            }
            else if (ch === '\n')
            {
              var breakElem = document.createElement("br");
              lineOutputElem.appendChild(breakElem);
            }
          }
        }
      }
      var text = token.text;
      var className = "cm-default";
      if (token.type === Brain4it.Token.STRING)
      {
        className = "cm-string";
      }
      else if (token.type === Brain4it.Token.NULL)
      {
        className = "cm-atom";
      }
      else if (token.type === Brain4it.Token.BOOLEAN)
      {
        className = "cm-atom";                
      }
      else if (token.type === Brain4it.Token.NUMBER)
      {
        className = "cm-number";        
      }
      else if (token.type === Brain4it.Token.OPEN_LIST)
      {
        className = "cm-bracket";                        
      }      
      else if (token.type === Brain4it.Token.CLOSE_LIST)
      {
        className = "cm-bracket";                                
      }
      else if (token.type === Brain4it.Token.REFERENCE)
      {
        className = this.helper.isFunction(token.text) ?
          "cm-keyword" : "cm-default";
      }
      else if (token.type === Brain4it.Token.TAG)
      {
        className = "cm-tag";
      }
      var spanElem = document.createElement("span");
      spanElem.innerHTML = toHTML(text);
      spanElem.className = className;
      lineOutputElem.appendChild(spanElem);
      lastTokenEndPosition = token.endPosition;
      tokenizer.readToken(token);
    }
  }
};

