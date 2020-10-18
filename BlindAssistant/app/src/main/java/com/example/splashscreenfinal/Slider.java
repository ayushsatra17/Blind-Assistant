package com.example.splashscreenfinal;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import java.util.Locale;

public class Slider extends AppCompatActivity {
        private ViewPager viewPager;
        private MyViewPagerAdapter myViewPagerAdapter;
        private LinearLayout dotsLayout;
        private TextView[] dots;
        private int[] layouts;
        private Button btnSkip, btnNext;
        private PrefManager prefManager;
        TextToSpeech tts;
        int flag = 0;

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // Checking for first time launch - before calling setContentView()
            prefManager = new PrefManager(this);
            if (prefManager.isFirstTimeLaunch()) {
                launchDynamicScreen();
                finish();
            } else {
                flag = 1;
                prefManager.setFirstTimeLaunch(true);
            }
            // Making notification bar transparent
            if (Build.VERSION.SDK_INT >= 21) {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            }
            setContentView(R.layout.activity_slider);
            viewPager = (ViewPager) findViewById(R.id.view_pager);
            dotsLayout = (LinearLayout) findViewById(R.id.layoutDots);
            btnSkip = (Button) findViewById(R.id.btn_skip);
            btnNext = (Button) findViewById(R.id.btn_next);
            // layouts of all welcome sliders
            // add few more layouts if you want
            layouts = new int[]{
                    R.layout.slide1,
                    R.layout.slide2,
                    R.layout.slide3,
                    R.layout.slide4,
                    R.layout.slide5,
                    R.layout.slide6,
                    R.layout.slide7};
            // adding bottom dots
            addBottomDots(0);
            // making notification bar transparent
            changeStatusBarColor();
            myViewPagerAdapter = new MyViewPagerAdapter();
            viewPager.setAdapter(myViewPagerAdapter);
            viewPager.addOnPageChangeListener(viewPagerPageChangeListener);
            btnSkip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tts.shutdown();
                    launchDynamicScreen();
                }
            });
            btnNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // checking for last page
                    // if last page home screen will be launched
                    int current = getItem(+1);
                    if (current < layouts.length) {
                        // move to next screen
                        viewPager.setCurrentItem(current);
                    } else {
                        tts.shutdown();
                        launchDynamicScreen();
                    }
                }
            });
            tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status == TextToSpeech.SUCCESS) {
                        tts.setLanguage(Locale.ENGLISH);
                        tts.setSpeechRate(Float.parseFloat(prefManager.getSPEECH_RATE()));
                        if(prefManager.isFirstTimeLaunch() && (flag==1)){
                            tts.speak("Send and Read Emails",TextToSpeech.QUEUE_FLUSH,null,"");
                            flag = 0;
                        }
                    } else if (status == TextToSpeech.ERROR) {
                        Toast.makeText(getApplicationContext(), "Text To Speech - Installation Error", Toast.LENGTH_LONG).show();
                    }
                }
            });
            tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {

                }

                @Override
                public void onDone(String utteranceId) {
                    switch (utteranceId){
                        case "100":
                            tts.speak("You're now done with knowing all the Gestures that the app supports",TextToSpeech.QUEUE_FLUSH,null,"101");
                            break;
                        case "101":
                            tts.speak("Perform these gestures in the next step to determine what action you want me to perform on each one of them ",TextToSpeech.QUEUE_FLUSH,null,"102");
                            break;
                        case "102":
                            launchDynamicScreen();
                        default:
                            break;
                    }
                }

                @Override
                public void onError(String utteranceId) {

                }
            });
        }

        @Override
        protected void onDestroy() {
            tts.shutdown();
            super.onDestroy();
        }


        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        private void addBottomDots(int currentPage) {
            dots = new TextView[layouts.length];

            int[] colorsActive = getResources().getIntArray(R.array.array_dot_active);
            int[] colorsInactive = getResources().getIntArray(R.array.array_dot_inactive);

            dotsLayout.removeAllViews();
            for (int i = 0; i < dots.length; i++) {
                dots[i] = new TextView(this);
                dots[i].setText(Html.fromHtml("&#8226;"));
                dots[i].setTextSize(35);
                dots[i].setTextColor(colorsInactive[currentPage]);
                dotsLayout.addView(dots[i]);
            }

            if (dots.length > 0)
                dots[currentPage].setTextColor(colorsActive[currentPage]);
        }

        private int getItem(int i) {
            return viewPager.getCurrentItem() + i;
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        private void launchDynamicScreen() {
            prefManager.setFirstTimeLaunch(true);
            Intent i = new Intent(Slider.this, Dynamic.class);
            startActivity(i);
            finish();
        }

        //  viewpager change listener
        ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onPageSelected(int position) {
                addBottomDots(position);
                if (position == layouts.length - 1) {
                    // last page. make button text to GOT IT
                    btnNext.setText(getString(R.string.start));
                    btnSkip.setVisibility(View.GONE);
                } else {
                    // still pages are left
                    btnNext.setText(getString(R.string.next));
                    btnSkip.setVisibility(View.VISIBLE);
                }
            }

            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
                switch (arg0){
                    case 0:
                        tts.speak("Send and Read Emails",TextToSpeech.QUEUE_FLUSH,null,"");
                        break;
                    case 1:
                        tts.speak("Hear Music with the Automatic Bluetooth Connectivity",TextToSpeech.QUEUE_FLUSH,null,"");
                        break;
                    case 2:
                        tts.speak("Set Alarms and Timers",TextToSpeech.QUEUE_FLUSH,null,"");
                        break;
                    case 3:
                        tts.speak("Call and Message Anyone",TextToSpeech.QUEUE_FLUSH,null,"");
                        break;
                    case 4:
                        tts.speak("Browse Relevant Places Around You",TextToSpeech.QUEUE_FLUSH,null,"");
                        break;
                    case 5:
                        tts.speak("Alert your Guardian with our Live Location Tracking",TextToSpeech.QUEUE_FLUSH,null,"");
                        break;
                    case 6:
                        tts.speak("Our App supports Single Tap, Double Tap, Long Press, Swipe Up, Swipe Down, Swipe Left and Swipe Right",TextToSpeech.QUEUE_FLUSH,null,"100");
                        break;
                    default:
                        break;
                }
            }

            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onPageScrollStateChanged(int arg0) {}

        };

        /**
         * Making notification bar transparent
         */
        private void changeStatusBarColor() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.TRANSPARENT);
            }
        }

        /**
         * View pager adapter
         */
        public class MyViewPagerAdapter extends PagerAdapter {

            private LayoutInflater layoutInflater;

            public MyViewPagerAdapter() {

            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View view = layoutInflater.inflate(layouts[position], container, false);
                container.addView(view);
                return view;
            }

            @Override
            public int getCount() {
                return layouts.length;
            }

            @Override
            public boolean isViewFromObject(View view, Object obj) {
                return view == obj;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                View view = (View) object;
                container.removeView(view);
            }

        }

}

