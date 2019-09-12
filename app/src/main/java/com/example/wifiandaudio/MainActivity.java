package com.example.wifiandaudio;

import android.Manifest;
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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    Button sendtxt,sosBtn;
    EditText messagetxt,iptxt;
    Button playBtn;
    final int PERMISSION_REQUEST=2000;
    private MediaRecorder myAudioRecorder;
    private MediaPlayer mediaPlayer;
    private String outputFile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(!havePermission()){
            requestPermission();
        }
        setupRecorder();
        sendtxt=findViewById(R.id.sendtxt);
        messagetxt=findViewById(R.id.messagetxt);
        iptxt=findViewById(R.id.iptxt);
        playBtn=findViewById(R.id.play);
       sosBtn=findViewById(R.id.sosBtn);



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

                        }
                    }, 20000);

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
                backgroundTsk.execute(iptxt.getText().toString(),outputFile);
            }
        });

        Thread myThread=new Thread(new MyServer());
        myThread.start();
    }



    private void sendRecordToServer() {
        File f = new File(outputFile);
        FileChannel in = null;
        try {
            in = new FileInputStream(f).getChannel();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

//        mSocket.send("sTART");
//
//        sendAudioBytes(in);
//
//        mSocket.send(END);

        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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


    class MyServer implements Runnable{

        ServerSocket ss;
        Socket s;
        DataInputStream dis;
        String message;
        Handler handler=new Handler();
        @Override
        public void run() {
            try {
                ss=new ServerSocket(9700);
                while (true){
                    if(ss==null){
                        break;
                    }
                    s=ss.accept();
                    dis=new DataInputStream(s.getInputStream());

                    //message=dis.
                    Log.d(TAG, "run: ////////////////////////////////"+s.getInputStream().toString());
                    final File someFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/"+ UUID.randomUUID().toString()+"sos.3gp");

                    FileOutputStream fos = new FileOutputStream(someFile);

                    fos.write(readBytes(s.getInputStream()));
                    fos.flush();
                    fos.close();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            playBtn.setVisibility(View.VISIBLE);
                            playBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                   mediaPlayer=new MediaPlayer();
                                    try {
                                        Log.d(TAG, "onClick: "+someFile.getPath());
                                        mediaPlayer.setDataSource(someFile.getPath());
                                        mediaPlayer.prepare();
                                       // mediaPlayer.start();
                                        mediaPlayer.start();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    });
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class BackgroundTsk extends AsyncTask<String,Void,Void> {

        Socket socket;
        DataOutputStream dos;


        @Override
        protected Void doInBackground(String... strings) {

            try {

                socket=new Socket(strings[0],9700);
                dos=new DataOutputStream(socket.getOutputStream());
                //dos.writeUTF(strings[1]);

                File f = new File(strings[1]);
                FileInputStream fis = new FileInputStream(f);
                Log.d(TAG, "doInBackground: "+fis.toString());
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] buf = new byte[1024];
                try {
                    for (int readNum; (readNum = fis.read(buf)) != -1;) {
                        bos.write(buf, 0, readNum); //no doubt here is 0
                        //Writes len bytes from the specified byte array starting at offset off to this byte array output stream.
                        System.out.println("read " + readNum + " bytes,");
                    }
                } catch (IOException ex) {

                }
                byte[] bytes = bos.toByteArray();
             dos.write(bytes);


            } catch (IOException e) {
                e.printStackTrace();
            }

            finally {
                try {
                    socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    dos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            return null;}
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

    public byte[] readBytes(InputStream inputStream) throws IOException {
        // this dynamically extends to take the bytes you read
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

        // this is storage overwritten on each iteration with bytes
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        // we need to know how may bytes were read to write them to the byteBuffer
        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }

        // and then we can return your byte array.
        return byteBuffer.toByteArray();
    }
}
