/*
 * Brain4it
 * 
 * Copyright (C) 2018, Ajuntament de Sant Feliu de Llobregat
 * 
 * This program is licensed and may be used, modified and redistributed under 
 * the terms of the European Public License (EUPL), either version 1.1 or (at 
 * your option) any later version as soon as they are approved by the European 
 * Commission.
 * 
 * Alternatively, you may redistribute and/or modify this program under the 
 * terms of the GNU Lesser General Public License as published by the Free 
 * Software Foundation; either  version 3 of the License, or (at your option) 
 * any later version. 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *   
 * See the licenses for the specific language governing permissions, limitations 
 * and more details.
 *   
 * You should have received a copy of the EUPL1.1 and the LGPLv3 licenses along 
 * with this program; if not, you may find them at: 
 *   
 *   https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 *   http://www.gnu.org/licenses/ 
 *   and 
 *   https://www.gnu.org/licenses/lgpl.txt
 */

package org.brain4it.server.android;

import org.brain4it.server.standalone.HttpServer;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.PrintWriter;
import java.util.Map;

public class ServerActivity extends Activity
{
  private static ServerActivity instance;
  private TextView outputText;
  private Button startStopButton;
  private TextView logViewer;

  public static ServerActivity getInstance()
  {
    return instance;
  }
  
  /**
   * Called when the activity is first created.
   *
   * @param savedInstanceState
   */
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    ServerApplication app = (ServerApplication)getApplicationContext();
    app.setupActivity(this, false);

    setContentView(R.layout.main);
    
    outputText = (TextView)findViewById(R.id.output);
    startStopButton = (Button)findViewById(R.id.startStopButton);
    logViewer = (TextView)findViewById(R.id.logViewer);
    logViewer.setMovementMethod(new ScrollingMovementMethod());
    
    startStopButton.setOnClickListener(new OnClickListener()
    {
      @Override
      public void onClick(View view)
      {
        Intent intent = new Intent(ServerActivity.this, AndroidService.class);
        AndroidService service = AndroidService.getInstance();
        if (service == null)
        {
          startStopButton.setText(R.string.starting);
          startStopButton.setEnabled(false);
          startService(intent);
        }
        else
        {
          startStopButton.setText(R.string.stopping);
          startStopButton.setEnabled(false);
          stopService(intent);
        }
      }
    });
    instance = this;
  }
  
  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.server_setup_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    switch (item.getItemId())
    {
      case R.id.setup:
        setupServer();
        break;
      case R.id.clear:
        logViewer.setText("");
        break;
      case R.id.threadDump:
        createThreadDump();
        break;
      case R.id.about:
        about();
        break;
    }
    return true;
  }
  
  @Override
  public void onStart()
  {
    super.onStart();
    updateViews();
  }
  
  public void logMessage(final String message)
  {
    logViewer.post(new Runnable()
    {
      @Override
      public void run()
      {
        logViewer.append(message + "\n");
        final int scrollAmount = logViewer.getLayout().getLineTop(
          logViewer.getLineCount()) - logViewer.getHeight();
        if (scrollAmount > 0)
          logViewer.scrollTo(0, scrollAmount);
        else
          logViewer.scrollTo(0, 0);
      }
    });
  }
  
  public void refresh()
  {
    runOnUiThread(new Runnable()
    {
      @Override
      public void run()
      {
        updateViews();
      }
    });
  }
  
  private void setupServer()
  {
    Intent intent = new Intent(ServerActivity.this, ServerSetupActivity.class);
    startActivity(intent);    
  }

  private void about()
  {
    Intent intent = new Intent(ServerActivity.this, AboutActivity.class);
    startActivity(intent);    
  }
  
  private void updateViews()
  {
    AndroidService service = AndroidService.getInstance();
    if (service == null)
    {
      outputText.setText("Server is stopped.");
      startStopButton.setText(R.string.start);
      startStopButton.setEnabled(true);
    }
    else
    {
      HttpServer server = service.getHttpServer();
      String address = server.getAddress();
      int port = server.getPort();
      outputText.setText("Listening on " + address + ":" + port + "...");
      startStopButton.setText(R.string.stop);
      startStopButton.setEnabled(true);
    }
  }  

  private void createThreadDump()
  {
    try
    {
      File baseDir = new File(Environment.getExternalStorageDirectory(), 
        "brain4it");
      baseDir.mkdirs();
      File file = new File(baseDir + "/threads.txt");
      PrintWriter writer = new PrintWriter(file);

      Map<Thread, StackTraceElement[]> stacks = Thread.getAllStackTraces();
      for (Map.Entry<Thread, StackTraceElement[]> entry : stacks.entrySet())
      {
        Thread thread = entry.getKey();
        writer.println("Thread: " + thread.getName() + 
          " id:" + thread.getId() + ":");
        StackTraceElement[] stack = entry.getValue();
        for (StackTraceElement elem : stack)
        {
          writer.println(elem);
        }
        writer.println("-------------------------------------");
      }
      writer.flush();
      writer.close();

      MimeTypeMap myMime = MimeTypeMap.getSingleton();
      String mimeType = myMime.getMimeTypeFromExtension("txt");
      Intent newIntent = new Intent(Intent.ACTION_VIEW);
      newIntent.setDataAndType(Uri.fromFile(file), mimeType);
      newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      try 
      {
        startActivity(newIntent);
      } 
      catch (ActivityNotFoundException e) 
      {
        Toast toast = Toast.makeText(this, "No handler for this type of file.", 
          Toast.LENGTH_LONG);
        toast.show();
      }
    }
    catch (Exception ex)
    {
      Toast toast = Toast.makeText(this, ex.toString(), 
        Toast.LENGTH_LONG);
      toast.show();
    }
  }
}
