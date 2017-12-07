package com.star.yvts10;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{
    static final String DB_NAME = "HotlineDB";
    static final String[] FROM = { "name", "phone", "email" };
    static final int MAX = 8;
    static final String TB_NAME = "hotlist";
    SimpleCursorAdapter adapter;
    Button btDelete;
    Button btInsert;
    Button btUpdate;
    Cursor cur;
    SQLiteDatabase db;
    EditText etEmail;
    EditText etName;
    EditText etPhone;
    ListView lv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etEmail = findViewById(R.id.etEmail);
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        btDelete = findViewById(R.id.btDelete);
        btInsert = findViewById(R.id.btInsert);
        btUpdate = findViewById(R.id.btUpdate);

        db = openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS hotlist " + "(_id INTEGER PRIMARY KEY AUTOINCREMENT, name VARCHAR(32), phone VARCHAR(16), email VARCHAR(64)) ");
        cur = db.rawQuery("SELECT * FROM hotlist " ,null);
//        if (cur.getCount() == 0 )
//        {
//            addData("XX公司" , "02-23969696" ,"service@aa.cc");
//        }
        adapter = new SimpleCursorAdapter(this ,R.layout.item ,cur ,FROM ,new int[]{R.id.name, R.id.phone, R.id.email} ,0 );
        lv = findViewById(R.id.lv);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(this);
        requery();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        cur.moveToPosition(position);
        etName.setText(cur.getString(cur.getColumnIndex(FROM[0])));
        etPhone.setText(cur.getString(cur.getColumnIndex(FROM[1])));
        etEmail.setText(cur.getString(cur.getColumnIndex(FROM[2])));
        btUpdate.setEnabled(true);
        btDelete.setEnabled(true);
    }
    public void update (String name ,String phone ,String email ,int roeId)
    {
        ContentValues values = new ContentValues(3);
        values.put(FROM[0] , name);
        values.put(FROM[1] , phone);
        values.put(FROM[2] , email);
        db.update(TB_NAME ,values ,"_id=" + roeId ,null);
    }

    public void addData(String name ,String phone ,String email ,int roeId)
    {
        ContentValues values = new ContentValues(3);
        values.put(FROM[0] , name);
        values.put(FROM[1] , phone);
        values.put(FROM[2] , email);
        db.insert(TB_NAME ,null ,values);
    }

    public void requery()
    {
        cur = db.rawQuery("SELECT * FROM hotlist " ,null);
        adapter.changeCursor(cur);
        if (cur.getCount() == MAX) {
            btInsert.setEnabled(false);
        }
        else
            {
            btInsert.setEnabled(true);
            }
        btUpdate.setEnabled(false);
        btDelete.setEnabled(false);
    }

    public void onDelete(View v)
    {
        db.delete(TB_NAME, "_id=" + cur.getInt(0), null);
        requery();
    }

    public void onInsertUpdate(View v)
    {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        if ((name.length() == 0) || (phone.length() == 0) || (email.length() == 0))
        {
            return;
        }
        if (v.getId() == R.id.btUpdate)
        {
            update(name ,phone ,email , cur.getInt(0));
        }
        else
        {
            addData(name, phone, email ,0);
        }
        requery();
    }

    public void callPhone(View v)
    {
        String uri = "tel:" + cur.getString(cur.getColumnIndex(FROM[1]));
        Intent intent = new Intent(Intent.ACTION_VIEW , Uri.parse(uri));
        startActivity(intent);

        MainActivityPermissionsDispatcher.callPhoneWithCheck(this);
    }

    @NeedsPermission(Manifest.permission.CALL_PHONE)
    void callPhone() {
//        String uri = "tel:" + cur.getString(cur.getColumnIndex(FROM[1]));
//        Intent intent = new Intent(Intent.ACTION_VIEW , Uri.parse(uri));
//        startActivity(intent);
//
//        MainActivityPermissionsDispatcher.callPhoneWithCheck(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @OnShowRationale(Manifest.permission.CALL_PHONE)
    void onShowRationale(final PermissionRequest request) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage("未允許「" + getString(R.string.app_name) + "」打電話及管理通話權限，將使「" + getString(R.string.app_name) + "」無法正常運作，是否重新設定權限？")
                .setPositiveButton("重新設定權限", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request.proceed();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request.cancel();
                    }
                })
                .create()
                .show();
    }

    @OnPermissionDenied(Manifest.permission.CALL_PHONE)
    void onPermissionDenied() {
        Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
    }

    @OnNeverAskAgain(Manifest.permission.CALL_PHONE)
    void onNeverAskAgain() {

        new AlertDialog.Builder(MainActivity.this)
                .setMessage("");
        Toast.makeText(this, "never ask again", Toast.LENGTH_LONG).show();
    }

    public void callMail(View v)
    {
        String uri = "mailto: " + cur.getString(cur.getColumnIndex(FROM[2]));
        Intent intent = new Intent(Intent.ACTION_SENDTO , Uri.parse(uri));
        startActivity(intent);
    }
}
