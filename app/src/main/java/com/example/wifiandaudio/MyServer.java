package com.example.wifiandaudio;

import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;

class MyServer implements Runnable{

    private static final String TAG = "MyServer";
    ServerSocket ss;
    Socket s;
    DataInputStream dis;
    OnRecordSent onRecordSent;

    public void setOnRecordSent(OnRecordSent onRecordSent) {
        this.onRecordSent = onRecordSent;
    }

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


                        Record record=new Record();
                        record.setAudioPath(someFile.getPath());
                        onRecordSent.onRecordSent(record);



                    }
                });
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
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
