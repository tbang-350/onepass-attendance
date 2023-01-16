package com.fgtit.onepass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fgtit.data.GlobalData;
import com.fgtit.onepass.R;
import com.fgtit.utils.ExtApi;
import com.fgtit.utils.ToastUtil;
import com.google.gson.Gson;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

public class UtilitiesActivity extends Activity {

    private ListView listView;
    private String urlPath;
    private ProgressDialog progressDialog;
    private String strBack;
    //	private List<Map<String, Object>> mData;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_utilities);

        this.getActionBar().setDisplayHomeAsUpEnabled(true);

        GlobalData.getInstance().LoadFileList();
        GlobalData.getInstance().LoadUsersList();
        GlobalData.getInstance().LoadConfig();
        GlobalData.getInstance().LoadRecordsList();
        GlobalData.getInstance().LoadWorkList();
        GlobalData.getInstance().LoadLineList();
        GlobalData.getInstance().LoadDeptList();

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
                       /* if (ExtApi.isNetworkConnected(UtilitiesActivity.this)) {
                            urlPath = "http://124.95.130.38/oa_admin/rest/usersign/api/signdata/upload.json";
                            if (GlobalData.getInstance().recordList.size() > 0) {
                                progressDialog = ProgressDialog.show(UtilitiesActivity.this, "请稍等...", "上传考勤数据中...", true);
                                Gson gson = new Gson();
                                String json = gson.toJson(GlobalData.getInstance().recordList).toString();
                                strBack = postDate(urlPath, json);
                                if (getMessage(strBack).equals("200")) {
                                    ToastUtil.showToastTop(UtilitiesActivity.this, "上传成功");
                                    progressDialog.dismiss();
                                    // GlobalData.getInstance().ClearRecordsList();
                                } else {
                                    ToastUtil.showToastTop(UtilitiesActivity.this, "上传失败");
                                    progressDialog.dismiss();
                                }
                            } else {
                                ToastUtil.showToastTop(UtilitiesActivity.this, "当前无考勤信息");
                            }
                        } else {
                            ToastUtil.showToastTop(UtilitiesActivity.this, "网络错误,请联网操作");
                        }*/
                    }

                    break;
                    case 1: {
                    }
                    break;
                    case 2: {
                    }
                    break;
                }
            }
        });
    }

    private String getMessage(String JsonData) {
        String message = "";
        if (JsonData != null) {
            try {
                JSONObject jsonObject = new JSONObject(JsonData);
                message = jsonObject.getString("status");
                return message;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public String postDate(String urlPath, String data) {
        try {
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(urlPath);
            StringEntity se;
            se = new StringEntity(data);
            httpPost.setEntity(se);
            HttpResponse httpResponse = httpClient.execute(httpPost);
            return EntityUtils.toString(httpResponse.getEntity());
        } catch (Exception ex) {
            ToastUtil.showToastTop(UtilitiesActivity.this, ex.toString());
        }
        return null;
    }

    private List<Map<String, Object>> getData() {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

        Map<String, Object> map = new HashMap<String, Object>();

        map = new HashMap<String, Object>();
        map.put("title", getString(R.string.txt_netup1));
        map.put("info", getString(R.string.txt_netup2));
        map.put("img", R.drawable.upload);
        list.add(map);

        map = new HashMap<String, Object>();
        map.put("title", getString(R.string.txt_netdn1));
        map.put("info", getString(R.string.txt_netdn2));
        map.put("img", R.drawable.download);
        list.add(map);

        map = new HashMap<String, Object>();
        map.put("title", getString(R.string.txt_netup3));
        map.put("info", getString(R.string.txt_netup4));
        map.put("img", R.drawable.upload);
        list.add(map);

        return list;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.utilities, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
