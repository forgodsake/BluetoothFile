package com.example.android.bluetoothchat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileActivity extends Activity {

    private String sdPath = Environment.getExternalStorageDirectory().getPath();
    private String currentPath = sdPath;
    private SimpleAdapter adapter;
    private List<Map<String,String>> files;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);
        File file = new File(sdPath);
        files = new ArrayList<>();
        for(File fileitem:file.listFiles()){
            Map<String,String> map = new HashMap<>();
            map.put("name",fileitem.getName());
            files.add(map);
        }
        adapter = new SimpleAdapter(this,files,android.R.layout.activity_list_item,new String []{"name"},new int[]{android.R.id.text1});
        ListView listView = (ListView) findViewById(R.id.listview_file);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                File file = new File(currentPath).listFiles()[position];
                if(file.isDirectory()){
                    files.clear();
                    for(File fileitem:file.listFiles()){
                        Map<String,String> map = new HashMap<>();
                        map.put("name",fileitem.getName());
                        files.add(map);
                    }
                    adapter.notifyDataSetChanged();
                    currentPath = file.getPath();
                }else{
                    Intent intent = new Intent(FileActivity.this,MainActivity.class);
                    intent.putExtra("filename",file.getPath());
                    setResult(1,intent);
                    finish();
                }
                Toast.makeText(FileActivity.this,file.getPath(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(!currentPath.equals(sdPath)){
            File file = new File(currentPath).getParentFile();
            files.clear();
            for(File fileitem:file.listFiles()){
                Map<String,String> map = new HashMap<>();
                map.put("name",fileitem.getName());
                files.add(map);
            }
            adapter.notifyDataSetChanged();
            currentPath = file.getPath();
        }else {
            super.onBackPressed();
        }

    }
}
