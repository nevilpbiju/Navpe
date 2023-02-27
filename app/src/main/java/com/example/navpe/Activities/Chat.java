package com.example.navpe.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.navpe.Adapters.MessageAdapter;
import com.example.navpe.Models.GetMessage;
import com.example.navpe.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class Chat extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    DatabaseReference databaseReference;
    MessageAdapter messageAdapter;
    String uid,receiverUid = "";
    String message,currentDateAndTime, receiverPhone;
    String chatToken,receiverChatToken;
    int chatCounter, receiverChatCounter;
    RecyclerView recyclerView;
    TextView chatMobile,chatName;
    EditText sendMessage;
    List<String> messageData;

    final private String FCM_API = "https://fcm.googleapis.com/fcm/send";
    final private String serverKey = "key="+ "AAAAWD1O2P0:APA91bH80zgY3eKZ1o0kDpduLVIfdL3n4WGP7NJb8I9ZkaK-hp4IRedjJ90ntOE5Jy2V3NfMAfJ8ciR0NP2eQshDil1erAUlZZGjqKC_kk_lAWHu0afVWZlzDB9X3TH1aB7Jkxmiujpt";
    final private String contentType = "application/json";
    final String TAG = "NOTIFICATION TAG";
    String NOTIFICATION_TITLE;
    String NOTIFICATION_MESSAGE;
    String TOPIC;

    ArrayList<GetMessage> getMessages;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        chatMobile = findViewById(R.id.chatMobile);
        chatName = findViewById(R.id.chatName);
        sendMessage = findViewById(R.id.sendMessage);
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        uid = user != null ? user.getUid() : null;
        databaseReference = FirebaseDatabase.getInstance().getReference().child("chat").child(uid).child("message");
        Intent in = getIntent();
        String dx = in.getStringExtra("Name");
        receiverPhone = in.getStringExtra("receiverPhone");
        recyclerView = findViewById(R.id.chatRecycler);
        getMessages = new ArrayList<>();
        messageData = new ArrayList<>();
        getReceiverId();
        if(receiverPhone.length() == 10) {
            chatMobile.setVisibility(View.VISIBLE);
            chatMobile.setText(receiverPhone);
        }
        chatName.setText(dx);
        sendMessage.setOnKeyListener((v, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                sendMethod();
                return true;
            }
            return false;
        });
        sendMessage.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            if(event.getAction() == MotionEvent.ACTION_UP) {
                if(event.getRawX() >= (sendMessage.getRight() - sendMessage.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    sendMethod();
                    return true;
                }
            }
            return false;
        });
    }
    private void sendMethod() {
        message = sendMessage.getText().toString();
        if(message.length() > 0){
            sendMessage.setText("");
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
            currentDateAndTime = sdf.format(new Date());
            if (receiverUid.length() > 0){
//                Log.e("ReceiverId", receiverUid);
                GetMessage getMessage = new GetMessage(message, currentDateAndTime,uid, Objects.requireNonNull(user.getPhoneNumber()).substring(3));
                FirebaseDatabase.getInstance().getReference().child("chat").child(receiverUid).child("message").child(receiverChatToken).child(String.valueOf(receiverChatCounter)).setValue(getMessage);
                receiverChatCounter = receiverChatCounter + 1;
                FirebaseDatabase.getInstance().getReference().child("Transaction").child(receiverUid).child("chatCounter").setValue(receiverChatCounter);
                pushMessage();
            }
            StoreDatabase(message,currentDateAndTime,uid, Objects.requireNonNull(user.getPhoneNumber()).substring(3));
        }
    }

    private void setChatRecordToken(String uid1) {
        DatabaseReference transactionReference = FirebaseDatabase.getInstance().getReference().child("Transaction").child(uid1).child("chatCounter");
        transactionReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int counter = dataSnapshot.getValue() != null ? Integer.parseInt(dataSnapshot.getValue().toString()) : 1000;
                if(uid1.equals(uid)) {
                    chatCounter = counter;
                }else {
                    receiverChatCounter = counter;
                }
                if(chatCounter != 0 && receiverChatCounter != 0){
                    new Timer().scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            getChat();
                        }
                    }, 0, 3500);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("", "Failed to read value.", error.toException());
            }
        });
    }
    public void getChat(){
        getMessages.clear();
        databaseReference.child(chatToken).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    messageData.clear();
                    for (DataSnapshot snapshot1 : postSnapshot.getChildren()) {
                        messageData.add(Objects.requireNonNull(snapshot1.getValue()).toString());
                    }
                    if (messageData.size() != 0) {
                        if (uid.equals(messageData.get(3))) {
                            getMessages.add(new GetMessage(messageData.get(1), messageData.get(2), messageData.get(3), GetMessage.SENT_MESSAGE, messageData.get(0)));
                        } else {
                            getMessages.add(new GetMessage(messageData.get(1), messageData.get(2), messageData.get(3), GetMessage.RECEIVE_MESSAGE, messageData.get(0)));
                        }
                    }
                }
                messageAdapter = new MessageAdapter(getMessages);
                recyclerView.setAdapter(messageAdapter);
                LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
                llm.setStackFromEnd(true);
                llm.setReverseLayout(false);
                recyclerView.setLayoutManager(llm);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {Log.e("Error","Error in the database" + error);}
        });
    }
    public void getReceiverId(){
        FirebaseDatabase.getInstance().getReference().child("Registered").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    if(receiverPhone.contains(Objects.requireNonNull(postSnapshot.getValue()).toString())){
                        receiverUid = postSnapshot.getKey();
//                        Log.e("xyz", receiverUid);
                        if(receiverUid != null){
                            chatToken = uid.substring(0,3) + "-" + receiverUid.substring(0,3);
                            receiverChatToken = receiverUid.substring(0,3) + "-" + uid.substring(0,3);
                        }
                    }
                }
                setChatRecordToken(uid);
                setChatRecordToken(receiverUid);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("", "Failed to read value.", error.toException());
            }
        });
    }
    @SuppressLint("NotifyDataSetChanged")
    public void StoreDatabase(String message, String currentDateAndTime, String uid1, String receiverPhone) {
        GetMessage getMessage = new GetMessage(message, currentDateAndTime,uid1, receiverPhone);
        databaseReference.child(chatToken).child(String.valueOf(chatCounter)).setValue(getMessage);
        chatCounter = chatCounter + 1;
        FirebaseDatabase.getInstance().getReference().child("Transaction").child(uid).child("chatCounter").setValue(chatCounter);
        Toast.makeText(this, "Message sent successfully", Toast.LENGTH_SHORT).show();
        getChat();
    }
//}
    private void pushMessage() {
   //" message_id": 1119173013384467321
        TOPIC = "/topics/notification_Chat" +  receiverUid.substring(0,4);
//        Log.e("Topic Error", TOPIC);
        NOTIFICATION_TITLE = "Message from " + user.getPhoneNumber();
        NOTIFICATION_MESSAGE = message;
        JSONObject notification = new JSONObject();
        JSONObject notificationBody = new JSONObject();
        try {
            notificationBody.put("title", NOTIFICATION_TITLE);
            notificationBody.put("message", NOTIFICATION_MESSAGE);
            notification.put("to", TOPIC);
            notification.put("data", notificationBody);
        } catch (JSONException e) {
            Log.e(TAG, "onCreate: " + e.getMessage() );
        }
        sendNotification(notification);
    }
    private void sendNotification(JSONObject notification) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,FCM_API, notification, response -> Log.i(TAG, "onResponse: " + response.toString()),
                error -> {
                    Toast.makeText(Chat.this, "Request error", Toast.LENGTH_LONG).show();
                    Log.i(TAG, "onErrorResponse: Didn't work");}    ){
            @Override
            public Map<String,String> getHeaders() {
                Map<String,String> params = new HashMap<>();
                params.put("Authorization", serverKey);
                params.put("Content-Type", contentType);
                return params;
            }
        };
        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);
    }

}