/*
* Copyright 2013 The Android Open Source Project
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


package com.example.android.bluetoothchat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.example.android.common.activities.SampleActivityBase;
import com.example.android.common.logger.Log;
import com.example.android.common.logger.LogFragment;
import com.example.android.common.logger.LogWrapper;
import com.example.android.common.logger.MessageOnlyLogFilter;

import java.io.File;
import java.io.FileInputStream;

/**
 * A simple launcher activity containing a summary sample description, sample log and a custom
 * {@link android.support.v4.app.Fragment} which can display a view.
 * <p>
 * For devices with displays with a width of 720dp or greater, the sample log is always visible,
 * on other devices it's visibility is controlled by an item on the Action Bar.
 */
public class MainActivity extends SampleActivityBase implements View.OnClickListener{

    public static final String TAG = "MainActivity";
    private String filename;

    // Whether the Log Fragment is currently shown
    private boolean mLogShown;
    /**
     * Member object for the chat services
     */
    private BluetoothChatService mChatService = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            BluetoothChatFragment fragment = new BluetoothChatFragment();
            transaction.replace(R.id.sample_content_fragment, fragment);
            transaction.commit();
            mChatService = fragment.mChatService;
        }

        findViewById(R.id.button_select).setOnClickListener(this);
        findViewById(R.id.button_send).setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem logToggle = menu.findItem(R.id.menu_toggle_log);
        logToggle.setVisible(findViewById(R.id.sample_output) instanceof ViewAnimator);
        logToggle.setTitle(mLogShown ? R.string.sample_hide_log : R.string.sample_show_log);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_toggle_log:
                mLogShown = !mLogShown;
//                ViewAnimator output = (ViewAnimator) findViewById(R.id.sample_output);
//                if (mLogShown) {
//                    output.setDisplayedChild(1);
//                } else {
//                    output.setDisplayedChild(0);
//                }
                supportInvalidateOptionsMenu();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /** Create a chain of targets that will receive log data */
    @Override
    public void initializeLogging() {
        // Wraps Android's native log framework.
        LogWrapper logWrapper = new LogWrapper();
        // Using Log, front-end to the logging chain, emulates android.util.log method signatures.
        Log.setLogNode(logWrapper);

        // Filter strips out everything except the message text.
        MessageOnlyLogFilter msgFilter = new MessageOnlyLogFilter();
        logWrapper.setNext(msgFilter);

        // On screen logging via a fragment with a TextView.
        LogFragment logFragment = (LogFragment) getSupportFragmentManager()
                .findFragmentById(R.id.log_fragment);
//        msgFilter.setNext(logFragment.getLogView());

        Log.i(TAG, "Ready");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button_select:
                Intent intent = new Intent(MainActivity.this, FileActivity.class);
                startActivityForResult(intent,1);
                break;
            case R.id.button_send:
                if(filename!=null){
                    sendFileName(filename);
                }else{
                    Toast.makeText(this, "未选择发送文件", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    private void sendFileName(String filePath){
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (filePath.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] encryptFile = {1,1,1,1};
            byte[] file = filePath.getBytes();
            byte[] send = new byte[encryptFile.length+file.length];
            System.arraycopy(encryptFile,0,send,0,encryptFile.length);
            System.arraycopy(file,0,send,encryptFile.length,file.length);
            mChatService.write(send);
        }
    }


    private void sendFile(String filePath){
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (filePath.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            File file = new File(filePath);
            byte[] send = null;
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                send = new byte[10240];
                fileInputStream.read(send);
            } catch (Exception e) {
                e.printStackTrace();
            }

            mChatService.write(send);
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==1){
            Toast.makeText(this,data.getStringExtra("filename"),Toast.LENGTH_SHORT).show();
            TextView tx = (TextView) findViewById(R.id.text_filename);
            tx.setText("FileName: "+ data.getStringExtra("filename"));
            filename = data.getStringExtra("filename");
        }
    }

}
