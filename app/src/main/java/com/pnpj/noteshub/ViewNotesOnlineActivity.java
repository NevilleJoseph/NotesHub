package com.pnpj.noteshub;

import android.animation.Animator;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.io.File;

public class ViewNotesOnlineActivity extends AppCompatActivity {

    FullScreenImageAdapter fullScreenImageAdapter;
    GestureDetectorCompat gestureDetectorCompat;
    LinearLayout seekbarLayout;
    Button btn;
    boolean isShown = true;

    int numPagesViewed = 0;

    InterstitialAd interstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_notes_online);

        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId("ca-app-pub-9819992292681972/6338936398");
        AdRequest adRequestInterstitial = new AdRequest.Builder().build();
        interstitialAd.loadAd(adRequestInterstitial);
        interstitialAd.setAdListener(new AdListener()
        {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                interstitialAd.loadAd(new AdRequest.Builder().build());
            }

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                new AlertDialog.Builder(ViewNotesOnlineActivity.this).setTitle("Error").setMessage("It seems you are on a slow network or that the network you are connected to is restricting ads. To view the rest of the notes, please switch to another network or use mobile data.").setNeutralButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                }).show();
            }
        });

        gestureDetectorCompat = new GestureDetectorCompat(this,new GestureDetector.SimpleOnGestureListener()
        {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if(isShown)
                    hideall();
                else {
                    showall();
                }
                return super.onSingleTapConfirmed(e);
            }
        });

        seekbarLayout = findViewById(R.id.viewNotesSeekbarLinearLayout);

        final ViewPager viewPager = findViewById(R.id.pager);
        final Intent intent = getIntent();
        String pushID = intent.getStringExtra("pushID");
        String subject = intent.getStringExtra("subject");
        final int numOfPages = intent.getIntExtra("numberOfPages",0);
        final Boolean isDownloaded = intent.getBooleanExtra("isDownloaded",false);
        final Boolean isRestricted = intent.getBooleanExtra("isRestricted",false);
        setTitle(subject);

        if(isDownloaded)
        {
            String[] paths = new String[numOfPages];
            for(int i =1;i<=numOfPages;i++)
            {
                String dirPath = getFilesDir().getAbsolutePath() + File.separator + pushID + File.separator + i + ".jpg";
                paths[i-1] = dirPath;
            }
            fullScreenImageAdapter = new FullScreenImageAdapter(ViewNotesOnlineActivity.this,paths,numOfPages);
        }
        else {
            fullScreenImageAdapter = new FullScreenImageAdapter(ViewNotesOnlineActivity.this, numOfPages, pushID);
        }
        viewPager.setAdapter(fullScreenImageAdapter);

        Handler progresshandler = new Handler();
        progresshandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                hideall();
            }
        }, 3000);

        btn = findViewById(R.id.btnClose);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        final TextView page = findViewById(R.id.viewNotesOnlinePageTextView);
        final SeekBar seekBar = findViewById(R.id.viewNotesOnlineSeekBar);

        page.setText("Page 1/"+numOfPages);
        seekBar.setMax(numOfPages-1);
        seekBar.setProgress(0);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                page.setText("Page "+(i+1)+"/"+numOfPages);
                viewPager.setCurrentItem(i,true);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                seekBar.setProgress(position);
                numPagesViewed++;
                if(isDownloaded)
                {
                    if(numPagesViewed==20) {
                        interstitialAd.show();
                        numPagesViewed = 0;
                    }
                }
                else
                {
                    if(numPagesViewed==20) {
                        interstitialAd.show();
                        numPagesViewed = 0;
                    }
                }
                if(isRestricted && (position==6))
                {
                    new AlertDialog.Builder(ViewNotesOnlineActivity.this).setTitle("Error").setMessage("It seems you are on a slow network or that the network you are connected to is restricting ads. To view the rest of the notes, please switch to another network or use mobile data.").setNeutralButton("Close", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    }).show();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        gestureDetectorCompat.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    public void showall()
    {
        seekbarLayout.animate().translationY(0);
        btn.animate().translationX(0);
        isShown = true;
    }

    public void hideall()
    {
        seekbarLayout.animate().translationY(seekbarLayout.getHeight()).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if(isShown)
                    getSupportActionBar().show();
                else
                    getSupportActionBar().hide();
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        btn.animate().translationX(btn.getWidth() + btn.getRight());
        isShown = false;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    /*View.SYSTEM_UI_FLAG_LAYOUT_STABLE */
                           /* | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION */
                             View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                          /* | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION */
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }
}
