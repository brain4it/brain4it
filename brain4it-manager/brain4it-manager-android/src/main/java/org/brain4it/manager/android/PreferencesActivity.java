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

package org.brain4it.manager.android;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 *
 * @author realor
 */
public class PreferencesActivity extends Activity
{
  private EditText textSizeEditText;
  private EditText indentSizeEditText;
  private EditText formatColumnsEditText;

  private Button okButton;

  /**
   * Called when the activity is first created.
   * @param icicle
   */
  @Override
  public void onCreate(Bundle icicle)
  {
    super.onCreate(icicle);

    ManagerApplication app = (ManagerApplication)getApplicationContext();
    app.setupActivity(this, true);

    setTitle(R.string.preferences);
    
    setContentView(R.layout.preferences);

    textSizeEditText = (EditText)findViewById(R.id.textSizeEditText);
    indentSizeEditText = (EditText)findViewById(R.id.indentSizeEditText);
    formatColumnsEditText = (EditText)findViewById(R.id.formatColumnsEditText);
    okButton = (Button)findViewById(R.id.setupOkButton);

    okButton.setOnClickListener(new Button.OnClickListener()
    {
      @Override
      public void onClick(View view)
      {
        savePreferences();
        finish();
      }
    });

    loadPreferences();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    switch (item.getItemId())
    {
      case android.R.id.home:
        finish();
        break;
    }
    return true;
  }

  private void loadPreferences()
  {
    ManagerApplication app = (ManagerApplication)getApplicationContext();
    SharedPreferences preferences = app.getPreferences();

    int textSize = preferences.getInt("textSize", 15);
    int indentSize = preferences.getInt("indentSize", 2);
    int formatColumns = preferences.getInt("formatColumns", 40);

    textSizeEditText.setText(String.valueOf(textSize));
    indentSizeEditText.setText(String.valueOf(indentSize));
    formatColumnsEditText.setText(String.valueOf(formatColumns));
  }

  private void savePreferences()
  {
    ManagerApplication app = (ManagerApplication)getApplicationContext();
    SharedPreferences preferences = app.getPreferences();

    SharedPreferences.Editor editor = preferences.edit();

    editor.putInt("textSize",
      Integer.parseInt(textSizeEditText.getText().toString()));
    editor.putInt("indentSize",
      Integer.parseInt(indentSizeEditText.getText().toString()));
    editor.putInt("formatColumns",
      Integer.parseInt(formatColumnsEditText.getText().toString()));

    editor.commit();
  }
}
