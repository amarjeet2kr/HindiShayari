package com.technovateria.loveshayari;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;

public class ContactUsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);


        //Go back to Home Page of Home screen
        MaterialToolbar btnBack  = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ContactUsActivity.this, MainActivity.class));
                finish();
            }
        });


        //send feedback message
        Button btnSend = findViewById(R.id.btn_send_feedback);
        EditText txtMessage = findViewById(R.id.txt_feedback_message);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailTo = "technovateria@gmail.com";
                String subject = "Feedback from Hindi Shayari App";
                String message = txtMessage.getText().toString();

                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setType("message/text");
                intent.setData(Uri.parse("mailto:"));
                intent.putExtra(android.content.Intent.EXTRA_EMAIL,new String[] { "technovateria@gmail.com" });
//                intent.putExtra(Intent.EXTRA_EMAIL, emailTo); // To field is not getting filled in gmail
                intent.putExtra(Intent.EXTRA_SUBJECT, subject);
                intent.putExtra(Intent.EXTRA_TEXT, message);

                if (message.isEmpty()){
                    Toast.makeText(ContactUsActivity.this, "Empty feedback message.", Toast.LENGTH_SHORT).show();
                }
                if(intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                } else{
                    Toast.makeText(ContactUsActivity.this, "No email clients installed.", Toast.LENGTH_SHORT).show();
                }
            }
        });



        //Follow us on social media
        ImageView btnLinkedIn = findViewById(R.id.btn_linkedin);
        ImageView btnYoutube = findViewById(R.id.bnt_youtube);
        ImageView btnFacebook = findViewById(R.id.btn_facebook);
        ImageView btnInstagram = findViewById(R.id.btn_instagram);

        btnLinkedIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoUrl("https://www.linkedin.com/in/amarjeetk/");
            }
        });

        btnYoutube.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoUrl("https://www.youtube.com/channel/UCNgLtXWrxp70bJ6-Utf9o2A");
            }
        });

        btnFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoUrl("https://www.facebook.com/amarjeet2kr/");
            }
        });

        btnInstagram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoUrl("https://www.instagram.com/amar.jeet_official/");
            }
        });

    }
    private void gotoUrl(String s){
        Uri uri = Uri.parse(s);
        startActivity(new Intent(Intent.ACTION_VIEW, uri));
    }
}