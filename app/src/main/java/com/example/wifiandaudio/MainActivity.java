package com.example.wifiandaudio;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements OnRecordSent{
    private static final String TAG = "MainActivity";
    Button sosBtn;
    ImageButton sendtxt;
    RecyclerView recordList;
    final int PERMISSION_REQUEST=2000;
    private MediaRecorder myAudioRecorder;
    private MediaPlayer mediaPlayer;
    private String outputFile;
    List<Record> records=new ArrayList<>();
    Context mContext=MainActivity.this;
    RecordsAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(!havePermission()){
            requestPermission();
        }
        sendtxt=findViewById(R.id.sendBtn);
        recordList=findViewById(R.id.recordsList);
       sosBtn=findViewById(R.id.sosBtn);

        setupRecorder();
        populateList();

        sosBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              // record voice for 20 seconds and save it

                if(myAudioRecorder==null)
                setupRecorder();
                try {
                    myAudioRecorder.prepare();
                    myAudioRecorder.start();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            // yourMethod();
                            stopRecording();
                            sendtxt.setVisibility(View.VISIBLE);

                        }
                    }, 10000);

                } catch (IllegalStateException ise) {
                    // make something ...
                } catch (IOException ioe) {
                    // make something
                }
                sosBtn.setEnabled(false);

                Toast.makeText(getApplicationContext(), "Recording started", Toast.LENGTH_LONG).show();
            }


        });
        sendtxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: ****************"+outputFile);
                BackgroundTsk backgroundTsk=new BackgroundTsk();
                backgroundTsk.execute("192.168.1.6",outputFile);
            }
        });
        MyServer myServer=new MyServer();
        myServer.setOnRecordSent(this);
        Thread myThread=new Thread(myServer);
        myThread.start();
    }

    private void populateList() {
         adapter=new RecordsAdapter(mContext,records);
        recordList.setLayoutManager(new LinearLayoutManager(mContext));
        recordList.setAdapter(adapter);
        recordList.addItemDecoration(new DividerItemDecoration(mContext,DividerItemDecoration.VERTICAL));
    }


    private void stopRecording() {
        myAudioRecorder.stop();
        myAudioRecorder.release();
        myAudioRecorder = null;
        sosBtn.setEnabled(true);
        Toast.makeText(getApplicationContext(), "Audio Recorder successfully", Toast.LENGTH_LONG).show();
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                                                   Manifest.permission.RECORD_AUDIO
        },PERMISSION_REQUEST);
    }

    private boolean havePermission() {
        int permission_storage= ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permission_audio=ActivityCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO);

        return (permission_storage== PackageManager.PERMISSION_GRANTED)&&(permission_audio==PackageManager.PERMISSION_GRANTED);
    }






    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==PERMISSION_REQUEST){
            if (grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED&&grantResults[1]==PackageManager.PERMISSION_GRANTED){}
            else {
                finish();
            }
        }
    }

    private void setupRecorder(){
        outputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/"+ UUID.randomUUID().toString()+"audio_record.3gp ";
        myAudioRecorder = new MediaRecorder();
        myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        myAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        myAudioRecorder.setOutputFile(outputFile);
    }


    @Override
    public void onRecordSent(Record record) {
        records.add(record);
        adapter.notifyDataSetChanged();
    }
}
