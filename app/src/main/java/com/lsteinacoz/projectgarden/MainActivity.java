package com.lsteinacoz.projectgarden;

import android.app.Activity;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.*;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.enums.PNStatusCategory;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;

import java.util.Arrays;

public class MainActivity extends Activity  {

    //private MediaPlayer mediaPlayer;
   // private SurfaceHolder vidHolder;
    private WebView webSurface;
    String vidAddress = "http://192.168.43.241/view";

    PubnubConfig pubnubConfig;
    PubNub pubnub;

    //create button widgets
    Button btn_up, btn_down, btn_right, btn_left;

    //create textview widgets
    TextView status_txtView,temp_txtView, humdity_txtView, s_moisture_txtView;



    //create Switch Widgets
    Switch irrigate_switch;



    //pubnub channels
    private final String IRRIGATE = "IRRIGATE";
    private final String SENSOR_DATA = "SENSOR_DATA";
    private final String FEEDBACKS = "FEEDBACKS";
    private final String SENSORS_SOIL = "SENSORS_SOIL";
    private final String SENSORS_TEMP = "SENSORS_TEMP";
    private final String SENSORS_HUMID = "SENSORS_HUMID";
    private final String CAMERA = "CAMERA";
    private final String MODE = "MODE";


    //pubnub messages/payload
    //pubnub payloads
    //get sensors' data
    private final String sensors_Data = "get data";


    //operating camera
    private final String camera_Right = "RIGHT";
    private final String camera_Left = "LEFT";
    private final String camera_up = "UP";
    private final String camera_down = "DOWN";


    //detecting motion
    private final String MOTION_PIR1 = "PIR1 MOTION";
    private final String MOTION_PIR2 = "PIR2 MOTION";


    //irrigation
    private final String irrigation_Start = "START";
    private final String irrigation_Stop = "STOP";


    PubConSubThread pubConSubThread;// new PubConSubThread();
    PubPublishThread pubPublishThread;// new PubPublishThread();

    // message and channel that will be used during publishing
    String payload;
    String channel;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialize Pubnub Configuration and PubNub
        pubnubConfig = new PubnubConfig();
        pubnub = new PubNub(pubnubConfig.pConfig());


        webSurface = (WebView) findViewById(R.id.webView);
        webSurface.setWebChromeClient(new WebChromeClient());
        webSurface.getSettings().setPluginState(WebSettings.PluginState.ON_DEMAND);
        webSurface.getSettings().setJavaScriptEnabled(true);
        webSurface.loadUrl(vidAddress);

        //getting the buttons
        btn_up = (Button) findViewById(R.id.btn_up);
        btn_down = (Button) findViewById(R.id.btn_down);
        btn_right = (Button) findViewById(R.id.btn_right);
        btn_left = (Button) findViewById(R.id.btn_left);




        //getting textviews

        status_txtView = (TextView) findViewById(R.id.status_txt);
        humdity_txtView = (TextView) findViewById(R.id.humid_txt);
        s_moisture_txtView = (TextView) findViewById(R.id.soil_txt);
        s_moisture_txtView = (TextView) findViewById(R.id.soil_txt);
        temp_txtView = (TextView) findViewById(R.id.temp_txt);

        irrigate_switch = (Switch) findViewById(R.id.irrigation_switch);

        //subscribe to pubnub channels
        pubnubSubscribe();


        //methods that handle button events
        btn_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnUpPressed();
            }
        });

        btn_down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnDownPressed();
            }
        });

        btn_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnRightPressed();
            }
        });

        btn_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnLeftPressed();
            }
        });

        irrigate_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                irrigate_switchPressed(b);
            }
        });






    }





    void pubnubSubscribe(){

        pubPublishThread = new PubPublishThread();
        pubConSubThread = new PubConSubThread();


        try {

            pubConSubThread.start();
            pubPublishThread.start();
        }catch (Exception e){
            status_txtView.setText(e.getMessage());
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    //this method is called from the PubConSub thread
    public void updateStatusTxtView(String message){
        status_txtView.setText(message);
    }

    /**this method is called from the Pubpublish thread
     and updates the status textView **/
    public void updatePubTextView(int colour, String publishStatus){
        status_txtView.setTextColor(colour);
        status_txtView.setText(publishStatus);
    }

    /**this method is called from the PubConSub thread
     and updates the connect textView **/
    public void updateConnectTextView(int colour, String connectStatus){
        status_txtView.setTextColor(colour);
        status_txtView.setText(connectStatus);
    }

    /**this method is called from the Pubpublish thread
     and updates the amb. temp textView**/
    public void updateTempTextView(String value){
      //  if (Integer.parseInt(value) > 30){
       //     temp_txtView.setTextColor(Color.RED);
            temp_txtView.setText(value);
      //  }else{
       //     temp_txtView.setTextColor(Color.GREEN);
      //      temp_txtView.setText(value);
       // }

    }


    /**this method is called from the Pubpublish thread
     and updates the status textView **/
    public void updateHumdityTextView(String value){
        humdity_txtView.setText(value);
    }

    public void updateSoilMositureTextView(String value){
        s_moisture_txtView.setText(value);
    }

    void btnUpPressed(){
        payload = "UP";
        channel = CAMERA;
        pubPublishThread.pubnubPublish(payload, channel);

    }

    void btnDownPressed(){

        payload = "DOWN";
        channel = CAMERA;
        pubPublishThread.pubnubPublish(payload, channel);

    }

    void btnRightPressed(){

        payload = "RIGHT";
        channel = CAMERA;
        pubPublishThread.pubnubPublish(payload, channel);


    }

    void btnLeftPressed(){

        payload = "LEFT";
        channel = CAMERA;
        pubPublishThread.pubnubPublish(payload, channel);

    }

    void irrigate_switchPressed(boolean chk) {
        if (chk) {
            pubPublishThread.pubnubPublish(irrigation_Start, IRRIGATE);
        } else {
            pubPublishThread.pubnubPublish(irrigation_Stop, IRRIGATE);
        }
    }



    /**
     * Created by General Steinacoz on 11/21/2017.
     */
    public class PubConSubThread extends Thread {
        public static final String Tag = "Pubnub Connect Subscribe Thread";
        private static final int DELAY = 5000;

        PubPublishThread pp = new PubPublishThread();

        //MainActivity mainActivity = new MainActivity();
        private String updateToast (String tt){
            return tt;
        }
        @Override
        public void run() {
            try {
                pubnubConSubscribe();
            }catch (Exception e){
                final String err = e.getMessage();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Toast.makeText(MainActivity.this, err, Toast.LENGTH_LONG).show();
                    }
                });
            }
        }

        //pubnub connect and subscribe
        private void pubnubConSubscribe(){

            connectProgress(Color.RED, "Connecting");

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "inside pubnubConSubscribe method", Toast.LENGTH_LONG).show();
                }
            });


            //final String direction = "direction";


            pubnub.addListener(new SubscribeCallback() {
                @Override
                public void status(PubNub pubnub, PNStatus status) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "inside status pubnub addlistener", Toast.LENGTH_LONG).show();
                        }
                    });

                    if (status.getCategory() == PNStatusCategory.PNUnexpectedDisconnectCategory){
                        connectProgress(Color.RED, "Disconnected");
                        pubnub.reconnect();
                    }else if (status.getCategory() == PNStatusCategory.PNConnectedCategory){
                        pp.pubnubPublish(sensors_Data, SENSOR_DATA);
                        if (status.getCategory() == PNStatusCategory.PNConnectedCategory){
                            connectProgress(Color.GREEN, "Connected");
                            //pubnub.subscribe().channels(Arrays.asList(FEEDBACKS, SENSOR_DATA, SENSORS_HUMID, SENSORS_SOIL, SENSORS_TEMP, CAMERA, IRRIGATE )).execute();
                        }else if (status.getCategory() == PNStatusCategory.PNReconnectedCategory){
                            pp.pubnubPublish(sensors_Data, SENSOR_DATA);
                            connectProgress(Color.GREEN, "Reconnected");
                           // pubnub.subscribe().channels(Arrays.asList(FEEDBACKS, SENSOR_DATA, SENSORS_HUMID, SENSORS_SOIL, SENSORS_TEMP, CAMERA, IRRIGATE )).execute();
                        }else if (status.getCategory() == PNStatusCategory.PNDecryptionErrorCategory){
                            pubnub.reconnect();
                        }else if (status.getCategory() == PNStatusCategory.PNTimeoutCategory){
                            connectProgress(Color.RED, "Network Timeout");
                            pubnub.reconnect();
                        }else {
                            pubnub.reconnect();

                            connectProgress(Color.RED, "No Connection");
                        }
                    }
                }

                @Override
                public void message(PubNub pubnub, final PNMessageResult message) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if (message.getChannel().equalsIgnoreCase(FEEDBACKS)) {
                                //updateStatusTxtView(message.getMessage().getAsString());

                            }else if (message.getChannel().equalsIgnoreCase(SENSORS_TEMP)){
                               updateTempTextView(message.getMessage().getAsString());

                            }else if (message.getChannel().equalsIgnoreCase(SENSOR_DATA)){
                                updateStatusTxtView(message.getMessage().getAsString());
                            }else if (message.getChannel().equalsIgnoreCase(SENSORS_HUMID)){
                                updateHumdityTextView(message.getMessage().getAsString());
                            }else if (message.getChannel().equalsIgnoreCase(SENSORS_SOIL)){
                                updateSoilMositureTextView(message.getMessage().getAsString());
                            }else if (message.getChannel().equalsIgnoreCase(IRRIGATE)){
                                //updateSoilMositureTextView(message.getMessage().getAsString());
                            }else if (message.getChannel().equalsIgnoreCase(CAMERA)){
                                updateStatusTxtView(message.getMessage().getAsString());
                            }

                        }
                    });
                }

                @Override
                public void presence(PubNub pubnub, PNPresenceEventResult presence) {

                }
            });

            pubnub.subscribe().channels(Arrays.asList(FEEDBACKS, SENSOR_DATA, SENSORS_HUMID, SENSORS_SOIL, SENSORS_TEMP, CAMERA, IRRIGATE )).execute();
        }

        private void connectProgress(int colour, String connectStatus){
            final int col = colour;
            final String cStatus = connectStatus;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateConnectTextView(col, cStatus);
                }
            });

        }


    }



    /**
     * Created by General Steinacoz on 11/21/2017.
     */
    public class PubPublishThread extends Thread {
        public static final String Tag = "Pubnub Publish Thread";
        private static final int DELAY = 5000;

        MainActivity mainActivity = new MainActivity();


        @Override
        public void run() {
            super.run();

        }



        public void pubnubPublish(String payload, String channel){

            try {
                pubnub.publish().message(payload).channel(channel).async(new PNCallback<PNPublishResult>() {
                    @Override
                    public void onResponse(PNPublishResult result, PNStatus status) {
                        if (!status.isError()) {
                           // publishProgress(Color.GREEN, "publish success");
                        } else {
                           // publishProgress(Color.RED, status.getCategory().toString());
                            status.retry();
                        }

                    }
                });
            }catch (Exception e){
                final String err = e.getMessage();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, err, Toast.LENGTH_LONG).show();
                    }
                });
            }
        }

        private void publishProgress(final int colour, final String publishStatus){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updatePubTextView(colour, publishStatus);
                }
            });
        }
    }




}





