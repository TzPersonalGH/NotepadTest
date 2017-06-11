package com.example.administrator.notepadtest;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements AbsListView.OnScrollListener,
        AdapterView.OnItemClickListener,AdapterView.OnItemLongClickListener {

    private Context mContent;
    private ListView listView;
    private SimpleAdapter simpleAdapter;
    private List<Map<String,Object>> dataList;
    private Button addNote;
    private TextView tv_content;
    private NoteDateBase DB;
    private SQLiteDatabase DBread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        tv_content = (TextView) findViewById(R.id.text_view_content);
        listView = (ListView) findViewById(R.id.list_view);
        dataList = new ArrayList<Map<String,Object>>();
        addNote = (Button) findViewById(R.id.btn_add_note);
        mContent = this;
        addNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Main2Activity.ENTER_STATE = 0;
                Intent intent = new Intent(mContent,Main2Activity.class);
                Bundle bundle = new Bundle();
                bundle.putString("info","");
                intent.putExtras(bundle);
                startActivityForResult(intent,1);
            }
        });

        DB = new NoteDateBase(this);
        DBread = DB.getReadableDatabase();
        RefreshNotesList();

        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);
        listView.setOnScrollListener(this);
    }

    private void RefreshNotesList() {
        int size = dataList.size();
        if (size>0){
            dataList.removeAll(dataList);
            simpleAdapter.notifyDataSetChanged();
            listView.setAdapter(simpleAdapter);
        }
        simpleAdapter = new SimpleAdapter(this,getData(),R.layout.note_item,
                new String[]{"tv_content","tv_date"},new int[]{
                R.id.text_view_content,R.id.text_view_date});
        listView.setAdapter(simpleAdapter);
    }

    private List<Map<String,Object>> getData(){
        Cursor cursor = DBread.query("note",null,"content!=\"\"",null,null,null,null);
        while (cursor.moveToNext()){
            String name = cursor.getString(cursor.getColumnIndex("content"));
            String date = cursor.getString(cursor.getColumnIndex("date"));
            Map<String,Object> map = new HashMap<String,Object>();
            map.put("tv_content",name);
            map.put("tv_date",date);
            dataList.add(map);
        }
        cursor.close();
        return dataList;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        switch (scrollState){
            case SCROLL_STATE_FLING:
                Log.i("main","...");
            case SCROLL_STATE_IDLE:
                Log.i("'main","...");
            case SCROLL_STATE_TOUCH_SCROLL:
                Log.i("main","...");
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Main2Activity.ENTER_STATE = 1;
        String content = listView.getItemAtPosition(position)+"";
        String content1 = content.substring(content.indexOf("=")+1,
        content.indexOf(","));
        Log.d("CONTENT",content1);
        Cursor c = DBread.query("note",null,"content="+"'"+content1+"'",null,null,null,null);
        while (c.moveToNext()){
            String No = c.getString(c.getColumnIndex("id"));
            Log.d("TEXT",No);
            Intent myIntent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putString("info",content1);
            Main2Activity.id = Integer.parseInt(No);
            myIntent.putExtras(bundle);
            myIntent.setClass(MainActivity.this,Main2Activity.class);
            startActivityForResult(myIntent,1);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode ==2){
            RefreshNotesList();
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        final int n = position;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("删除该日志");
        builder.setMessage("确认删除吗？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String content = listView.getItemAtPosition(n)+"";
                String content1 = content.substring(content.indexOf("=")+1,content.indexOf(","));
                Cursor c =DBread.query("note",null,"content="+"'"+content1+"'",null,null,null,null);
                while (c.moveToNext()){
                    String id = c.getString(c.getColumnIndex("id"));
                    String sql_del = "update note set content=''where id="+id;
                    DBread.execSQL(sql_del);
                    RefreshNotesList();
                }
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.create();
        builder.show();
        return  true;
    }
}
