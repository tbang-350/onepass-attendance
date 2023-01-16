package com.fgtit.onepass;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SystemActivity extends Activity {

    private ListView listView;
    private List<Map<String, Object>> mData;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system);

        this.getActionBar().setDisplayHomeAsUpEnabled(true);

        listView = (ListView) findViewById(R.id.listView1);
        SimpleAdapter adapter = new SimpleAdapter(this, getData(), R.layout.listview_menuitem,
                new String[]{"title", "info", "img"},
                new int[]{R.id.title, R.id.info, R.id.img});
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                //Map<String, Object> item = (Map<String, Object>)parent.getItemAtPosition(pos);
                switch (pos) {
                    case 0: {
                        Intent intent = new Intent(SystemActivity.this, AdminActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                    }
                    break;
                    case 1: {
                    }
                    break;
                    case 2: {
                        Intent intent = new Intent(SystemActivity.this, SettingsActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                    }
                    break;
                    case 3: {
                        startActivity(new Intent(Settings.ACTION_SETTINGS));
                        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                    }
                    break;
                    case 4: {
                        Intent intent = new Intent(SystemActivity.this, SysParametersActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                    }
                    break;
                }
            }
        });
    }

    private List<Map<String, Object>> getData() {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

        Map<String, Object> map = new HashMap<String, Object>();

        map = new HashMap<String, Object>();
        map.put("title", getString(R.string.txt_set1));
        map.put("info", getString(R.string.txt_set2));
        map.put("img", R.drawable.key);
        list.add(map);

        map = new HashMap<String, Object>();
        map.put("title", getString(R.string.txt_set5));
        map.put("info", getString(R.string.txt_set6));
        map.put("img", R.drawable.wifi);
        list.add(map);

        map = new HashMap<String, Object>();
        map.put("title", getString(R.string.txt_set7));
        map.put("info", getString(R.string.txt_set8));
        map.put("img", R.drawable.dna_helix);
        list.add(map);

        map = new HashMap<String, Object>();
        map.put("title", getString(R.string.txt_set3));
        map.put("info", getString(R.string.txt_set4));
        map.put("img", R.drawable.engineering);
        list.add(map);

        map = new HashMap<String, Object>();
        map.put("title", getString(R.string.txt_set9));
        map.put("info", getString(R.string.txt_set10));
        map.put("img", R.drawable.android);
        list.add(map);

        mData = list;
        return list;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.system, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                this.finish();
                return true;
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
