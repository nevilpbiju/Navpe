package com.example.navpe.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.navpe.R;

public class MobilePay extends AppCompatActivity {

    ListView listView;
    EditText phone;
    TextView back;
    Button button;
    Boolean fetched=false;
    String[] from, contacts;
    String number;
    SimpleCursorAdapter simpleCursorAdapter;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_pay);
        listView = findViewById(R.id.listview);
        phone = findViewById(R.id.ph);
        button = findViewById(R.id.mobpay);
        back = findViewById(R.id.back6);

        back.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_UP) {
                if(event.getRawX() <= back.getTotalPaddingLeft()) {
                    onBackPressed();
                    return true;
                }
            }
            return true;
        });
        button.setOnClickListener(view -> {
            number=phone.getText().toString();
            if(number.length()==10){
                Intent intent = new Intent(getApplicationContext(), PayActivity.class);
                intent.putExtra("Payment",number);
                startActivity(intent);
            }
        });
        phone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        phone.setOnTouchListener((view, motionEvent) -> {
            if(!fetched) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                    get(view);
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MobilePay.this, Manifest.permission.READ_CONTACTS)) {
                        ActivityCompat.requestPermissions(MobilePay.this, new String[]{Manifest.permission.READ_CONTACTS}, 0);
                    } else {
                        ActivityCompat.requestPermissions(MobilePay.this, new String[]{Manifest.permission.READ_CONTACTS}, 0);
                    }
                }
                fetched=!fetched;
            }
            return false;
        });

        listView.setOnItemClickListener((adapterView, view, i, l) -> {
//            Cursor cursor = (Cursor) simpleCursorAdapter.getItem(i);
//            Log.e("",cursor.toString());
//            Toast.makeText(this, cursor., Toast.LENGTH_SHORT).show();
        });
    }

    public void get(View v){
        Cursor cursor =getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,null,null,null);
        startManagingCursor(cursor);
        from = new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone._ID};
        int[] to ={android.R.id.text1,android.R.id.text2};
        simpleCursorAdapter= new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2,cursor,from,to);
        listView.setAdapter(simpleCursorAdapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

    }
}