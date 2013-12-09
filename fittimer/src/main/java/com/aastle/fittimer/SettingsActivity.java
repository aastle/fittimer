package com.aastle.fittimer;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.provider.MediaStore;

import static android.content.Intent.*;
import static android.provider.MediaStore.*;
import static android.provider.MediaStore.Images.*;


public class SettingsActivity extends PreferenceActivity {
    public static String PHOTO_WIDTH = "";
    public static String PHOTO_HEIGHT = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add 'general' preferences.
        addPreferencesFromResource(R.xml.preferences);
        Preference button = (Preference)getPreferenceManager().findPreference("picpickerbutton");
        if(button != null)
        {
            button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference arg0) {
                    launchImagePicker();
                    return true;
                }
            });
        }

    }

    private void launchImagePicker(){
        Intent intent = new Intent(ACTION_PICK,
        Media.INTERNAL_CONTENT_URI);

        intent.setType("image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("scale", true);
        intent.putExtra("outputX", PHOTO_WIDTH);
        intent.putExtra("outputY", PHOTO_HEIGHT);
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, 1);
    }




}
