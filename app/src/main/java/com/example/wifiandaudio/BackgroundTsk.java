package com.example.wifiandaudio;

import android.os.AsyncTask;
import android.support.v7.widget.DialogTitle;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;

class BackgroundTsk extends AsyncTask<String,Void,Void> {

    Socket socket;
    DataOutputStream dos;
    private static final String TAG = "BackgroundTsk";
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