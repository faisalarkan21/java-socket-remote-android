package com.faisalarkan.android.socketcontrol;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    Context context;
    PrefManagerHost prefHost;

    @BindView(R.id.left_btn)
    Button leftButton;

    @BindView(R.id.right_btn)
    Button rightButton;

    @BindView(R.id.middle_btn)
    Button middleButton;

    @BindView(R.id.mousePad)
    TextView mousePad;

    private boolean isConnected = false;
    private boolean mouseMoved = false;
    private Socket socket;
    private PrintWriter out;

    private float initX = 0;
    private float initY = 0;
    private float disX = 0;
    private float disY = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;

        ButterKnife.bind(this);

        leftButton.setOnClickListener(this);
        rightButton.setOnClickListener(this);
        middleButton.setOnClickListener(this);

        mousePad.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isConnected && out != null) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            //save X and Y positions when user touches the TextView
                            initX = event.getX();
                            initY = event.getY();
                            mouseMoved = false;
                            break;
                        case MotionEvent.ACTION_MOVE:
                            disX = event.getX() - initX; //Mouse movement in x direction
                            disY = event.getY() - initY; //Mouse movement in y direction
                            /*set init to new position so that continuous mouse movement
                            is captured*/
                            initX = event.getX();
                            initY = event.getY();
                            if (disX != 0 || disY != 0) {
                                out.println(disX + "," + disY); //send mouse movement to server
                            }
                            mouseMoved = true;
                            break;

                    }
                }
                return true;
            }
        });


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_connect) {

            prefHost = new PrefManagerHost(context);

            if (prefHost.getHostIp().equals("")){
                Toast.makeText(context, "Mohon isi IP dan Port pada menu opsi Setting disamping", Toast.LENGTH_LONG).show();
                return false;
            }

            ConnectPhoneTask connectPhoneTask = new ConnectPhoneTask();
            connectPhoneTask.execute(prefHost.getHostIp()); //try to connect to server in another thread

            Toast.makeText(context, "Mencoba terhubung dengan " + prefHost.getHostIp() + ":" + prefHost.getHostPort() , Toast.LENGTH_SHORT).show();

            return true;
        } else if (id == R.id.action_settings) {

            Intent activitySetting = new Intent(context, SettingActivity.class);
            context.startActivity(activitySetting);


        } else if (id == R.id.action_close_connection){
            if (isConnected){
                Toast.makeText(context, "Koneksi Dengan Server Ditutup", Toast.LENGTH_LONG).show();
                out.println("exit");
                return false;
            }
            Toast.makeText(context, "Koneksi Dengan Server Belum Terbuka", Toast.LENGTH_LONG).show();

        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.right_btn:
                if (isConnected && out != null) {
                    out.println(Constants.MOUSE_RIGHT_CLICK);
                }
                break;
            case R.id.left_btn:
                if (isConnected && out != null) {
                    out.println(Constants.MOUSE_LEFT_CLICK);
                }
                break;
            case R.id.middle_btn:
                if (isConnected && out != null) {
                    out.println(Constants.MOUSE_MIDDLE_CLICK);
                }
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isConnected && out != null) {
            try {
                out.println("exit"); //tell server to exit
                socket.close(); //close socket
            } catch (IOException e) {
                Log.e("remotedroid", "Error in closing socket", e);
            }
        }
    }

    public class ConnectPhoneTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            boolean result = true;
            try {
                prefHost = new PrefManagerHost(context);

                final int hostPort = Integer.parseInt(prefHost.getHostPort());

                InetAddress serverAddr = InetAddress.getByName(params[0]);
                socket = new Socket(serverAddr, hostPort);//Open socket on server IP and port
            } catch (IOException e) {
                Log.e("remotedroid", "Error while connecting", e);
                result = false;
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            isConnected = result;
            prefHost = new PrefManagerHost(context);

            Log.d("check", prefHost.getHostPort());
            if (prefHost.getHostIp().equals("")){
                Toast.makeText(context, "Mohon isi IP dan Port pada menu opsi Setting disamping", Toast.LENGTH_LONG).show();
                return;
            }else{
                Toast.makeText(context, isConnected ? "Remote Berhasil Terhubung!" : "Remote Gagal Terhubung!", Toast.LENGTH_LONG).show();
            }


            try {
                if (isConnected) {
                    out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket
                            .getOutputStream())), true); //create output stream to send data to server
                }
            } catch (IOException e) {
                Log.e("remotedroid", "Error while creating OutWriter", e);
                Toast.makeText(context, "Error while connecting" + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }


}


