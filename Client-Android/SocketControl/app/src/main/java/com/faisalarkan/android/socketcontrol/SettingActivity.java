package com.faisalarkan.android.socketcontrol;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.PrintWriter;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingActivity extends AppCompatActivity {

    PrefManagerHost prefHost;
    private PrintWriter out;

    @BindView(R.id.et_ip)
    EditText mEtIp;

    @BindView(R.id.et_port)
    EditText mEtPort;

    @BindView(R.id.btn_save_setting)
    Button mBtnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        ButterKnife.bind(this);

        prefHost = new PrefManagerHost(SettingActivity.this);

        if (prefHost.getHostIp() != null){
            mEtIp.setText(prefHost.getHostIp());
            mEtPort.setText(prefHost.getHostPort());
        }


        mBtnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String getIp = mEtIp.getText().toString();
                String getPort = mEtPort.getText().toString();

                prefHost = new PrefManagerHost(SettingActivity.this);

                if (prefHost.getHostIp() != null && prefHost.getHostPort() != null){
                    prefHost.removeSession();
                }

                prefHost.setSession(getIp, getPort);
                Toast.makeText(SettingActivity.this, "Setting Tersimpan", Toast.LENGTH_SHORT).show();
            }
        });




    }
}
