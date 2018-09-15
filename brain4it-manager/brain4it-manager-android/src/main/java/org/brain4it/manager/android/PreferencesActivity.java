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
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

/**
 *
 * @author realor
 */
public class PreferencesActivity extends Activity
{
  private Spinner languageSpinner;
  private EditText textSizeEditText;
  private EditText indentSizeEditText;
  private EditText formatColumnsEditText;

  private Button okButton;

  private static final String LANGUAGES[] =
  {"en", "es", "ca"};

  private static final String LANGUAGE_NAMES[] =
  {"English", "Español", "Català"};

  /**
   * Called when the activity is first created.
   * @param icicle
   */
  @Override
  public void onCreate(Bundle icicle)
  {
    super.onCreate(icicle);
    getActionBar().setDisplayHomeAsUpEnabled(true);

    setTitle(R.string.preferences);

    setContentView(R.layout.preferences);

    languageSpinner = (Spinner)findViewById(R.id.languageSpinner);
    textSizeEditText = (EditText)findViewById(R.id.textSizeEditText);
    indentSizeEditText = (EditText)findViewById(R.id.indentSizeEditText);
    formatColumnsEditText = (EditText)findViewById(R.id.formatColumnsEditText);
    okButton = (Button)findViewById(R.id.setupOkButton);

    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
      android.R.layout.simple_spinner_item, LANGUAGE_NAMES);
    adapter.setDropDownViewResource(
      android.R.layout.simple_spinner_dropdown_item);
    languageSpinner.setAdapter(adapter);

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
    SharedPreferences preferences =
      getSharedPreferences(ManagerApplication.PREFERENCES, MODE_PRIVATE);

    ManagerApplication app = (ManagerApplication)getApplicationContext();

    String language = preferences.getString("language", app.getLanguage());
    int textSize = preferences.getInt("textSize", 15);
    int indentSize = preferences.getInt("indentSize", 2);
    int formatColumns = preferences.getInt("formatColumns", 40);

    int index = findLanguageIndex(language);
    languageSpinner.setSelection(index);
    textSizeEditText.setText(String.valueOf(textSize));
    indentSizeEditText.setText(String.valueOf(indentSize));
    formatColumnsEditText.setText(String.valueOf(formatColumns));
  }

  private void savePreferences()
  {
    SharedPreferences preferences =
      getSharedPreferences(ManagerApplication.PREFERENCES, MODE_PRIVATE);

    SharedPreferences.Editor editor = preferences.edit();

    int index = languageSpinner.getSelectedItemPosition();
    String language = LANGUAGES[index];

    editor.putString("language", language);
    editor.putInt("textSize",
      Integer.parseInt(textSizeEditText.getText().toString()));
    editor.putInt("indentSize",
      Integer.parseInt(indentSizeEditText.getText().toString()));
    editor.putInt("formatColumns",
      Integer.parseInt(formatColumnsEditText.getText().toString()));

    editor.commit();

    ManagerApplication app = (ManagerApplication)getApplicationContext();
    String previousLanguage = app.getLanguage();

    if (!previousLanguage.equals(language))
    {
      app.setLanguage(language);

      // relaunch server list activity
      Intent intent = new Intent(PreferencesActivity.this,
        ServerListActivity.class);
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |
        Intent.FLAG_ACTIVITY_NEW_TASK);
      startActivity(intent);
    }
  }

  private int findLanguageIndex(String language)
  {
    int index = -1;
    int i = 0;
    while (i < LANGUAGES.length && index == -1)
    {
      if (LANGUAGES[i].equals(language))
      {
        index = i;
      }
      else
      {
        i++;
      }
    }
    return index == -1 ? 0 : index;
  }
}
