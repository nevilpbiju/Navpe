package com.example.navpe.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.navpe.MainActivity;
import com.example.navpe.Models.GetAccount;
import com.example.navpe.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AccountActivity extends AppCompatActivity {
    EditText acc,reAcc,ifsc, holderName,bankName;
    Button confirm;
    FirebaseUser user;
    DatabaseReference databaseReference;
    String accountNo, IFSC,Name, reAccountNo;
    String imageUrl,uid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        acc = findViewById(R.id.accountNo);
        reAcc = findViewById(R.id.reAccount);
        ifsc = findViewById(R.id.ifsc);
        holderName = findViewById(R.id.holderName);
        confirm = findViewById(R.id.confirm2);
        bankName = findViewById(R.id.bankName);
        imageUrl = "https://i.pinimg.com/originals/48/69/d9/4869d9a4c0d3b680ac66274dd0a88501.png";

        user = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Accounts");
        databaseReference.keepSynced(true);
        uid = user.getUid();

        confirm.setOnClickListener(v -> {
            accountNo = acc.getText().toString();
            reAccountNo = reAcc.getText().toString();
            IFSC = ifsc.getText().toString();
            Name = bankName.getText().toString();
            if(Name.contains("Federal Bank")){
                imageUrl = "https://media.glassdoor.com/sqll/473210/federal-bank-squareLogo-1618807967695.png";
            }else if(Name.contains("Paytm")){
                imageUrl = "https://image3.mouthshut.com/images/imagesp/925917139s.jpeg";
            }else if(Name.contains("Canara Bank")){
                imageUrl = "https://content.jdmagicbox.com/comp/mumbai/16/022p9002616/catalogue/canara-bank-goregaon-east-mumbai-nationalised-banks-q99wtj.jpg";
            }
            if(!accountNo.isEmpty() && !reAccountNo.isEmpty() && !IFSC.isEmpty() && !Name.isEmpty()){
                if(accountNo.equals(reAccountNo)){
                    storeDataBase(accountNo,IFSC,Name,imageUrl);
                    Intent in = new Intent(AccountActivity.this, MainActivity.class);
                    startActivity(in);
                    finish();
                }else{
                    Toast.makeText(this, "Account No doesn't match re-enter",Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(this, "Enter the details Properly", Toast.LENGTH_SHORT).show();
            }

        });
    }
    public void storeDataBase(String accountNo, String IFSC, String BankName, String imageUrl){
        GetAccount getAccount = new GetAccount(BankName,imageUrl,accountNo);
        databaseReference.child(uid).child(IFSC).setValue(getAccount);
        Toast.makeText(this, "Details Updated", Toast.LENGTH_SHORT).show();
    }
}