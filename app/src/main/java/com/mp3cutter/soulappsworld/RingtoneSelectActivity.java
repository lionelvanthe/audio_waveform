/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mp3cutter.soulappsworld;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.MergeCursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.mp3cutter.soulappsworld.soundfile.CheapSoundFile;
import com.wellytech.audiotrim.R;

/**
 * Main screen that shows up when you launch Ringtone.  Handles selecting
 * an audio file or using an intent to record a new one, and then
 * launches RingtoneEditActivity from here.
 */
public class RingtoneSelectActivity extends ListActivity {
    private SearchView mFilter;

    AlertDialog.Builder builder;
    private SimpleCursorAdapter mAdapter;
    private boolean mWasGetContentIntent;
    private boolean mShowAll;
    // Result codes
    private static final int REQUEST_CODE_EDIT = 1;
    private static final int REQUEST_CODE_CHOOSE_CONTACT = 2;

    // Context menu
    private static final int CMD_EDIT = 4;
    private static final int CMD_DELETE = 5;
    private static final int CMD_SET_AS_DEFAULT = 6;
    private static final int CMD_SET_AS_CONTACT = 7;

    public RingtoneSelectActivity() {
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

//        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
//        if (Build.VERSION.SDK_INT >= 24) {
//            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().build());
//        }
//
//        try {
//            ViewConfiguration viewConfiguration = ViewConfiguration.get(this);
//            Field declaredField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
//            if (declaredField != null) {
//                declaredField.setAccessible(true);
//                declaredField.setBoolean(viewConfiguration, false);
//            }
//        } catch (Exception unused) {
//        }

        Double d = 9.0;
        Log.i("Gia tri: ", String.valueOf(d));

//nhac_tre.mp3
        String filename = "/storage/emulated/0/Android/data/com.miui.player/files/Music/built/Funk Down.mp3";
        String filename2 = "/storage/emulated/0/VideoSlideShowPro/Templates/defaultmusic/0x0700000000000070/Love The Sky.m4a";
        ArrayList<String> fileNames= new ArrayList<>();
        fileNames.add(filename2);
        fileNames.add(filename);
        try {
//            RingtoneEditActivity
            Intent intent = new Intent(Intent.ACTION_EDIT, Uri.parse(filename));
            intent.putExtra("was_get_content_intent", mWasGetContentIntent);
            intent.putStringArrayListExtra("file_to_mix", fileNames);
            intent.setClassName(getPackageName(), "com.mp3cutter.soulappsworld.MixAudioActivity");
            startActivityForResult(intent, REQUEST_CODE_EDIT);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Ringtone", "Couldn't start editor: ");
        }

//
//        mShowAll = false;
//
//        String status = Environment.getExternalStorageState();
//        if (status.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
//            showFinalAlert(getResources().getText(R.string.sdcard_readonly));
//            return;
//        }
//        if (status.equals(Environment.MEDIA_SHARED)) {
//            showFinalAlert(getResources().getText(R.string.sdcard_shared));
//            return;
//        }
//        if (!status.equals(Environment.MEDIA_MOUNTED)) {
//            showFinalAlert(getResources().getText(R.string.no_sdcard));
//            return;
//        }
//
//        Intent intent = getIntent();
//        mWasGetContentIntent = intent.getAction().equals(Intent.ACTION_GET_CONTENT);
//
//        // Inflate our UI from its XML layout description.
//        setContentView(R.layout.media_select);
//
//        SplashHandler mHandler = new SplashHandler();
//
//
//        Message msg = new Message();
//        //Assign a unique code to the message.
//        //Later, this code will be used to identify the message in Handler class.
//        msg.what = 0;
//        // Send the message with a delay of 3 seconds(3000 = 3 sec).
//        mHandler.sendMessageDelayed(msg, 10000);
//
//        try {
//            mAdapter = new SimpleCursorAdapter(this,
//                    // Use a template that displays a text view
//                    R.layout.media_select_row,
//                    // Give the cursor to the list adatper
//                    createCursor(""),
//                    // Map from database columns...
//                    new String[]{MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media._ID, MediaStore.Audio.Media._ID},
//                    // To widget ids in the row layout...
//                    new int[]{R.id.row_artist, R.id.row_album, R.id.row_title, R.id.row_icon, R.id.row_options_button});
//
//            setListAdapter(mAdapter);
//
//            getListView().setItemsCanFocus(true);
//
//            // Normal click - open the editor
//            getListView().setOnItemClickListener(new OnItemClickListener() {
//                public void onItemClick(AdapterView parent, View view, int position, long id) {
//                    startRingdroidEditor();
//                }
//            });
//
//        } catch (SecurityException e) {
//            // No permission to retrieve audio?
//            Log.e("Ringtone", e.toString());
//
//            // todo error 1
//        } catch (IllegalArgumentException e) {
//            // No permission to retrieve audio?
//            Log.e("Ringtone", e.toString());
//
//            // todo error 2
//        }
//
//        mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
//            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
//                if (view.getId() == R.id.row_options_button) {
//                    // Get the arrow image view and set the onClickListener to open the context menu.
//                    ImageView iv = (ImageView) view;
//                    iv.setOnClickListener(new View.OnClickListener() {
//                        public void onClick(View v) {
//                            openContextMenu(v);
//                        }
//                    });
//                    return true;
//                } else if (view.getId() == R.id.row_icon) {
//                    setSoundIconFromCursor((ImageView) view, cursor);
//                    return true;
//                }
//
//                return false;
//            }
//        });
//
//        // Long-press opens a context menu
//        registerForContextMenu(getListView());
    }

    @SuppressLint("ResourceType")
    private void setSoundIconFromCursor(ImageView view, Cursor cursor) {
        if (0 != cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_RINGTONE))) {
            view.setImageResource(R.drawable.type_ringtone);
            ((View) view.getParent()).setBackgroundColor(getResources().getColor(R.drawable.type_bkgnd_ringtone));
        } else if (0 != cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_ALARM))) {
            view.setImageResource(R.drawable.type_alarm);
            ((View) view.getParent()).setBackgroundColor(getResources().getColor(R.drawable.type_bkgnd_alarm));
        } else if (0 != cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_NOTIFICATION))) {
            view.setImageResource(R.drawable.type_notification);
            ((View) view.getParent()).setBackgroundColor(getResources().getColor(R.drawable.type_bkgnd_notification));
        } else if (0 != cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_MUSIC))) {
            view.setImageResource(R.drawable.type_music);
            ((View) view.getParent()).setBackgroundColor(getResources().getColor(R.drawable.type_bkgnd_music));
        }

        String filename = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
        if (!CheapSoundFile.isFilenameSupported(filename)) {
            ((View) view.getParent()).setBackgroundColor(getResources().getColor(R.drawable.type_bkgnd_unsupported));
        }
    }

    /**
     * Called with an Activity we started with an Intent returns.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent dataIntent) {
        if (requestCode != REQUEST_CODE_EDIT) {
            return;
        }

        if (resultCode != RESULT_OK) {
            return;
        }

        setResult(RESULT_OK, dataIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.select_options, menu);

        mFilter = (SearchView) menu.findItem(R.id.action_search_filter).getActionView();
        if (mFilter != null) {
            mFilter.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                public boolean onQueryTextChange(String newText) {
                    refreshListView();
                    return true;
                }

                public boolean onQueryTextSubmit(String query) {
                    refreshListView();
                    return true;
                }
            });
        }

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_about).setVisible(true);
        menu.findItem(R.id.action_record).setVisible(true);
        menu.findItem(R.id.action_privacy).setVisible(true);
        menu.findItem(R.id.action_show_all_audio).setVisible(true);
        menu.findItem(R.id.action_show_all_audio).setEnabled(!mShowAll);
        return true;
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.action_about:
//                RingtoneEditActivity.onAbout(this);
//                return true;
//            case R.id.action_record:
//                onRecord();
//                return true;
//            case R.id.action_privacy:
//                showPrivacyDialog();
//                return true;
//            case R.id.action_show_all_audio:
//                mShowAll = true;
//                refreshListView();
//                return true;
//            default:
//                return false;
//        }
//    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        Cursor c = mAdapter.getCursor();
        String title = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));

        menu.setHeaderTitle(title);

        menu.add(0, CMD_EDIT, 0, R.string.context_menu_edit);
        menu.add(0, CMD_DELETE, 0, R.string.context_menu_delete);

        // Add items to the context menu item based on file type
        if (0 != c.getInt(c.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_RINGTONE))) {
            menu.add(0, CMD_SET_AS_DEFAULT, 0, R.string.context_menu_default_ringtone);
            menu.add(0, CMD_SET_AS_CONTACT, 0, R.string.context_menu_contact);
        } else if (0 != c.getInt(c.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_NOTIFICATION))) {
            menu.add(0, CMD_SET_AS_DEFAULT, 0, R.string.context_menu_default_notification);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case CMD_EDIT:
                startRingdroidEditor();
                return true;
            case CMD_DELETE:
                confirmDelete();
                return true;
            case CMD_SET_AS_DEFAULT:
                setAsDefaultRingtoneOrNotification();
                return true;
            case CMD_SET_AS_CONTACT:
                return chooseContactForRingtone(item);
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void showPrivacyDialog() {
        try {
            Intent intent = new Intent(Intent.ACTION_EDIT, Uri.parse(""));
            intent.putExtra("privacy", true);
            intent.setClassName("com.mp3cutter.soulappsworld", "com.mp3cutter.soulappsworld.RingtoneEditActivity");
            startActivityForResult(intent, REQUEST_CODE_EDIT);
        } catch (Exception e) {
            Log.e("Ringtone", "Couldn't show privacy dialog");
        }
    }

    private void setAsDefaultRingtoneOrNotification() {
        Cursor c = mAdapter.getCursor();

        // If the item is a ringtone then set the default ringtone,
        // otherwise it has to be a notification so set the default notification sound
        if (0 != c.getInt(c.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_RINGTONE))) {
            RingtoneManager.setActualDefaultRingtoneUri(RingtoneSelectActivity.this, RingtoneManager.TYPE_RINGTONE, getUri());
            Toast.makeText(RingtoneSelectActivity.this, R.string.default_ringtone_success_message, Toast.LENGTH_SHORT).show();
        } else {
            RingtoneManager.setActualDefaultRingtoneUri(RingtoneSelectActivity.this, RingtoneManager.TYPE_NOTIFICATION, getUri());
            Toast.makeText(RingtoneSelectActivity.this, R.string.default_notification_success_message, Toast.LENGTH_SHORT).show();
        }
    }

    private Uri getUri() {
        //Get the uri of the item that is in the row
        Cursor c = mAdapter.getCursor();
        int uriIndex = c.getColumnIndex("\"" + MediaStore.Audio.Media.INTERNAL_CONTENT_URI + "\"");
        if (uriIndex == -1) {
            uriIndex = c.getColumnIndex("\"" + MediaStore.Audio.Media.EXTERNAL_CONTENT_URI + "\"");
        }
        String itemUri = c.getString(uriIndex) + "/" + c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
        return (Uri.parse(itemUri));
    }

    private boolean chooseContactForRingtone(MenuItem item) {
        try {
            //Go to the choose contact activity
            Intent intent = new Intent(Intent.ACTION_EDIT, getUri());
            intent.setClassName("com.mp3cutter.soulappsworld", "com.mp3cutter.soulappsworld.ChooseContactActivity");
            startActivityForResult(intent, REQUEST_CODE_CHOOSE_CONTACT);
        } catch (Exception e) {
            Log.e("Ringtone", "Couldn't open Choose Contact window");
        }
        return true;
    }

    private void confirmDelete() {
        // See if the selected list item was created by Ringtone to
        // determine which alert message to show
        Cursor c = mAdapter.getCursor();
        int artistIndex = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
        String artist = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
        CharSequence ringdroidArtist = getResources().getText(R.string.artist_name);

        CharSequence message;
        if (artist.equals(ringdroidArtist)) {
            message = getResources().getText(R.string.confirm_delete);
        } else {
            message = getResources().getText(R.string.confirm_delete_non);
        }

        CharSequence title;
        if (0 != c.getInt(c.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_RINGTONE))) {
            title = getResources().getText(R.string.delete_ringtone);
        } else if (0 != c.getInt(c.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_ALARM))) {
            title = getResources().getText(R.string.delete_alarm);
        } else if (0 != c.getInt(c.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_NOTIFICATION))) {
            title = getResources().getText(R.string.delete_notification);
        } else if (0 != c.getInt(c.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_MUSIC))) {
            title = getResources().getText(R.string.delete_music);
        } else {
            title = getResources().getText(R.string.delete_audio);
        }

        new AlertDialog.Builder(RingtoneSelectActivity.this).setTitle(title).setMessage(message).setPositiveButton(R.string.delete_ok_button, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                onDelete();
            }
        }).setNegativeButton(R.string.delete_cancel_button, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        }).setCancelable(true).show();
    }

    private void onDelete() {
        Cursor c = mAdapter.getCursor();
        int dataIndex = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        String filename = c.getString(dataIndex);

        int uriIndex = c.getColumnIndex("\"" + MediaStore.Audio.Media.INTERNAL_CONTENT_URI + "\"");
        if (uriIndex == -1) {
            uriIndex = c.getColumnIndex("\"" + MediaStore.Audio.Media.EXTERNAL_CONTENT_URI + "\"");
        }
        if (uriIndex == -1) {
            showFinalAlert(getResources().getText(R.string.delete_failed));
            return;
        }

        if (!new File(filename).delete()) {
            showFinalAlert(getResources().getText(R.string.delete_failed));
        }

        String itemUri = c.getString(uriIndex) + "/" + c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
        getContentResolver().delete(Uri.parse(itemUri), null, null);
    }

    private void showFinalAlert(CharSequence message) {
        new AlertDialog.Builder(RingtoneSelectActivity.this).setTitle(getResources().getText(R.string.alert_title_failure)).setMessage(message).setPositiveButton(R.string.alert_ok_button, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                finish();
            }
        }).setCancelable(false).show();
    }

    private void onRecord() {
        try {
            Intent intent = new Intent(Intent.ACTION_EDIT, Uri.parse("record"));
            intent.putExtra("was_get_content_intent", mWasGetContentIntent);
            intent.setClassName("com.mp3cutter.soulappsworld", "com.mp3cutter.soulappsworld.RingtoneEditActivity");
            startActivityForResult(intent, REQUEST_CODE_EDIT);
        } catch (Exception e) {
            Log.e("Ringtone", "Couldn't start editor");
        }
    }

    private void startRingdroidEditor() {
        Cursor c = mAdapter.getCursor();
        int dataIndex = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        String filename = c.getString(dataIndex);
        try {
            Intent intent = new Intent(Intent.ACTION_EDIT, Uri.parse(filename));
            intent.putExtra("was_get_content_intent", mWasGetContentIntent);
            intent.setClassName("com.mp3cutter.soulappsworld", "com.mp3cutter.soulappsworld.MixAudioActivity");
            startActivityForResult(intent, REQUEST_CODE_EDIT);
        } catch (Exception e) {
            Log.e("Ringtone", "Couldn't start editor");
        }
    }

    private Cursor getInternalAudioCursor(String selection, String[] selectionArgs) {
        return managedQuery(MediaStore.Audio.Media.INTERNAL_CONTENT_URI, INTERNAL_COLUMNS, selection, selectionArgs, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
    }

    private Cursor getExternalAudioCursor(String selection, String[] selectionArgs) {
        return managedQuery(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, EXTERNAL_COLUMNS, selection, selectionArgs, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
    }

    Cursor createCursor(String filter) {
        ArrayList<String> args = new ArrayList<String>();
        String selection;

        if (mShowAll) {
            selection = "(_DATA LIKE ?)";
            args.add("%");
        } else {
            selection = "(";
            for (String extension : CheapSoundFile.getSupportedExtensions()) {
                args.add("%." + extension);
                if (selection.length() > 1) {
                    selection += " OR ";
                }
                selection += "(_DATA LIKE ?)";
            }
            selection += ")";

            selection = "(" + selection + ") AND (_DATA NOT LIKE ?)";
            args.add("%espeak-data/scratch%");
        }

        if (filter != null && filter.length() > 0) {
            filter = "%" + filter + "%";
            selection = "(" + selection + " AND " + "((TITLE LIKE ?) OR (ARTIST LIKE ?) OR (ALBUM LIKE ?)))";
            args.add(filter);
            args.add(filter);
            args.add(filter);
        }

        String[] argsArray = args.toArray(new String[args.size()]);

        Cursor external = getExternalAudioCursor(selection, argsArray);
        Cursor internal = getInternalAudioCursor(selection, argsArray);

        Cursor c = new MergeCursor(new Cursor[]{getExternalAudioCursor(selection, argsArray), getInternalAudioCursor(selection, argsArray)});
        startManagingCursor(c);
        return c;
    }

    private void refreshListView() {
        String filterStr = mFilter.getQuery().toString();
        mAdapter.changeCursor(createCursor(filterStr));
    }

    private static final String[] INTERNAL_COLUMNS = new String[]{MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.IS_RINGTONE, MediaStore.Audio.Media.IS_ALARM, MediaStore.Audio.Media.IS_NOTIFICATION, MediaStore.Audio.Media.IS_MUSIC, "\"" + MediaStore.Audio.Media.INTERNAL_CONTENT_URI + "\""};

    private static final String[] EXTERNAL_COLUMNS = new String[]{MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.IS_RINGTONE, MediaStore.Audio.Media.IS_ALARM, MediaStore.Audio.Media.IS_NOTIFICATION, MediaStore.Audio.Media.IS_MUSIC, "\"" + MediaStore.Audio.Media.EXTERNAL_CONTENT_URI + "\""};

    private class SplashHandler extends Handler {

        //This method is used to handle received messages
        public void handleMessage(Message msg) {
            // switch to identify the message by its code
            switch (msg.what) {
                default:
                case 0:
                    super.handleMessage(msg);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            builder = new AlertDialog.Builder(RingtoneSelectActivity.this);
        } else {
            builder = new AlertDialog.Builder(RingtoneSelectActivity.this, AlertDialog.THEME_HOLO_LIGHT);
        }
        builder.setTitle("Thanks :-)");
        builder.setMessage("Thank You For using Our Application");
        builder.setNegativeButton("RATE APP", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id=com.mp3cutter.soulappsworld"));
                startActivity(intent);
                Toast.makeText(RingtoneSelectActivity.this, "Thank you for your Rating", Toast.LENGTH_SHORT).show();

            }
        });
        builder.setPositiveButton("QUIT", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setNeutralButton("MORE", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://search?q=pub:SoulAppsWorld"));
                startActivity(intent);

            }
        });

        builder.show();

    }
}
