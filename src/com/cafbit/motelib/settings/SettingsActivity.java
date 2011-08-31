/*
 * Copyright 2011 David Simmons
 * http://cafbit.com/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cafbit.motelib.settings;

import com.cafbit.motelib.MoteContext;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;
import android.widget.Toast;

// http://www.kaloer.com/android-preferences

public class SettingsActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        MoteContext moteContext = MoteContext.getInstance(this);
        addPreferencesFromResource(moteContext.getSettingsXmlResource());
        
        // devices preferences
        Preference devicesPref = (Preference) findPreference("devicesPref");
        devicesPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                /*
                Toast.makeText(getBaseContext(),
                                "Show devices...",
                                Toast.LENGTH_LONG).show();
                                */
                

                Intent intent = new Intent(SettingsActivity.this, DevicesActivity.class);
                startActivity(intent);

                /*
                SharedPreferences customSharedPreference = getSharedPreferences(
                                "myCustomSharedPrefs", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = customSharedPreference
                                .edit();
                editor.putString("myCustomPref",
                                "The preference has been clicked");
                editor.commit();
                */
                return true;
            }
        });
        
        Preference resetPref = (Preference) findPreference("resetPref");
        resetPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                builder.setPositiveButton("OK", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MoteContext moteContext = MoteContext.getInstance(SettingsActivity.this);
                        moteContext.coldReset();
                        Toast.makeText(getBaseContext(),
                            "Resetting to factory defaults...",
                            Toast.LENGTH_LONG).show();
                        SettingsActivity.this.finish();
                    }
                });
                builder.setNegativeButton("Cancel", null);
                AlertDialog alertDialog = builder.create();
                alertDialog.setTitle("Are you sure?");
                alertDialog.setMessage("Are you sure you want to erase all configured devices, components, and other settings?");
                alertDialog.show();
                
                return true;
            }
        });
    }
}
