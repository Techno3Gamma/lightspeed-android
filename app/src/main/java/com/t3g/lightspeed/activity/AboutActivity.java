package com.t3g.lightspeed.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.t3g.lightspeed.R;

public class AboutActivity extends AppCompatActivity {
    private TextView t3g_link;
    private FirebaseAnalytics mFirebaseAnalytics;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        this.t3g_link = (TextView) findViewById(R.id.t3g_link);
        String text ="From the developers of Students Guide - Techno3Gamma";
        SpannableString ss = new SpannableString(text);

        ClickableSpan sg = new ClickableSpan(){
            @Override
            public void onClick(@NonNull View view) {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://bit.ly/students_guide"));
                startActivity(i);

            }
        };
        ClickableSpan t3g = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View view) {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://techno3gamma.in"));
                startActivity(i);

            }
        };
        //    Define start and end for clickAction
        ss.setSpan(sg,23,37, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(t3g,40,52, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);


        t3g_link.setText(ss);
        t3g_link.setMovementMethod(LinkMovementMethod.getInstance());
    }
    }
