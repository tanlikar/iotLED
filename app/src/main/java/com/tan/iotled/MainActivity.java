package com.tan.iotled;

import android.app.Activity;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class MainActivity extends AppCompatActivity {

    String topic = "mqttLED";
    byte[] encodedPayload = new byte[0];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //mqtt connect
        String clientId = MqttClient.generateClientId();
        final MqttAndroidClient client =
                new MqttAndroidClient(this.getApplicationContext(), "tcp://broker.hivemq.com:1883",
                        clientId);

        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.d("mqtt", "onSuccess");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d("mqtt", "onFailure");

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        ImageView [] array = {(ImageView) findViewById(R.id.imgLED1), (ImageView) findViewById(R.id.imgLED2),(ImageView) findViewById(R.id.imgLED3),(ImageView) findViewById(R.id.imgLED4),
                (ImageView) findViewById(R.id.imgLED5),(ImageView) findViewById(R.id.imgLED6),(ImageView) findViewById(R.id.imgLED7),(ImageView) findViewById(R.id.imgLED8)};

        final List<ImageView> mLED = new ArrayList<>(Arrays.asList(array));

        Button mButtonSend = (Button) findViewById(R.id.buttonSend);


        mButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText mTextInput = (EditText) findViewById(R.id.userInput);

                try {
                    int  mUserInput = Integer.parseInt(mTextInput.getText().toString());
                    if (mUserInput > 255 || mUserInput < 0) {
                        Toast.makeText(MainActivity.this, "wrong input", Toast.LENGTH_SHORT).show();
                    } else {

                        List<Integer> buf = new ArrayList<>();

                        while (mUserInput > 0) {
                            int temp = mUserInput % 2;
                            buf.add(temp);
                            mUserInput = mUserInput / 2;
                        }

                        for (int x = buf.size(); x < 8; x++) {
                            buf.add(0);
                        }

                        Log.d("binary", "onClick: " + buf.toString());

                        for (int x = 0; x < buf.size(); x++) {
                            if (buf.get(x) == 1) {
                                mLED.get(7 - x).setImageResource(R.drawable.ic_launcher_background);
                            } else if (buf.get(x) == 0) {
                                mLED.get(7 - x).setImageResource(R.drawable.ic_action_name);
                            }
                        }

                        //mqtt publish
                        try {
                            encodedPayload = buf.toString().getBytes("UTF-8");
                            MqttMessage message = new MqttMessage(encodedPayload);
                            client.publish(topic, message);
                        } catch (UnsupportedEncodingException | MqttException e) {
                            e.printStackTrace();
                        }
                    }
                }catch(Exception ignored){

                }

            }
        });



    }
}
