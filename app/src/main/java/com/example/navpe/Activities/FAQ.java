package com.example.navpe.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.navpe.Adapters.FaqAdapter;
import com.example.navpe.Models.GetFaq;
import com.example.navpe.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.AdView;

import java.util.ArrayList;

public class FAQ extends AppCompatActivity {
    RecyclerView FaqRecyclerView;
    ImageButton FaqBackButton;
    TextView faqContact;
    AdView adView;
    ArrayList<GetFaq> list;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq);

        list = updateFaq();
        FaqRecyclerView = findViewById(R.id.faq_recycler_view);
        faqContact = findViewById(R.id.faqContactUs);
        FaqAdapter FaqAdapter = new FaqAdapter(list);
        FaqRecyclerView.setAdapter(FaqAdapter);
        FaqRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        FaqBackButton = findViewById(R.id.faqBackButton);
        FaqBackButton.setOnClickListener(v -> onBackPressed());

        faqContact.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_SENDTO);
            i.setData(Uri.parse("mailto:"));
            i.putExtra(Intent.EXTRA_EMAIL, new String[]{"Navpe@gamail.com"});
            i.putExtra(Intent.EXTRA_SUBJECT, "Some Feedback regarding Application.");
            try {
                startActivity(i);
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(getApplicationContext(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
            }
        });

//        Code for Displaying Ad in application.
        adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        Toast.makeText (getApplicationContext(), "Ad is loading... ", Toast.LENGTH_LONG).show();

    }
    private ArrayList<GetFaq> updateFaq()
    {
        ArrayList<GetFaq> list = new ArrayList<>();
        //Adding data in our List
        list.add( new GetFaq("What is the use of this Application?", "Navpe help you to pay using you face which can help you do faster payment."));
        list.add( new GetFaq("Where to get Location?", "Click on the icon near the location text available in your Home screen after enabling it from profile." ));
        list.add( new GetFaq("Map not getting my current Location?", "Sometime the map will not show location it is because the location was just turned on which cause the map to not calibrate. If this problem occurs, you may just open google map in background once and the problem will be solved." ));
        list.add( new GetFaq("What about the security of this app", "Navpe is using the latest google face map comparing technology to give you extra security."));
        list.add( new GetFaq("Will the app work offline", "No ot will not work in the absence of the internet"));
        list.add( new GetFaq("Privacy", "Your data is complete secure. We don't have any third party services, So it's secure."));
        list.add( new GetFaq("Why there are only few options available?","As we are currently a new application, We will try to add new things when the app is updated."));
        return list;
    }
}
//        adView.setAdListener(new AdListener() {
//
//            @Override public void onAdLoaded() {
//                Toast.makeText(getApplicationContext(), "Ad is Loaded", Toast.LENGTH_LONG).show();
//            }
//            @Override public void onAdFailedToLoad(@NonNull LoadAdError adError) {
//                Toast.makeText(getApplicationContext(), "Ad Failed to Load ", Toast.LENGTH_LONG).show();
//            }
//            @Override public void onAdOpened() {Toast.makeText(getApplicationContext(), "Ad Opened", Toast.LENGTH_LONG).show();}
//            @Override public void onAdClicked() {Toast.makeText(getApplicationContext(), "Ad Clicked", Toast.LENGTH_LONG).show(); }
//            @Override public void onAdClosed() { Toast.makeText(getApplicationContext(), "Ad is Closed", Toast.LENGTH_LONG).show(); }
//        });