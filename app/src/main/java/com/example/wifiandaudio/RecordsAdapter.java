package com.example.wifiandaudio;

import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecordsAdapter extends RecyclerView.Adapter<RecordsAdapter.RecordViewHolder> {
    private static final String TAG = "RecordsAdapter";
Context context;
List<Record>records;
MediaPlayer mediaPlayer;
    public RecordsAdapter(Context context, List<Record> records) {
        this.context = context;
        this.records = records;
    }

    @NonNull
    @Override
    public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view= LayoutInflater.from(context).inflate(R.layout.record_list_item,null,false);

        return new RecordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecordViewHolder recordViewHolder, int i) {
    recordViewHolder.bind(records.get(i));
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    public class RecordViewHolder extends RecyclerView.ViewHolder{
        TextView deviceName,dateTxt;
        public RecordViewHolder(@NonNull View itemView) {
            super(itemView);
             deviceName=itemView.findViewById(R.id.deviceName);
             dateTxt=itemView.findViewById(R.id.dateTxt);

        }

        public void bind(final Record record){

                mediaPlayer=new MediaPlayer();
                Log.d(TAG, "onClick: "+record.getAudioPath());

                                        itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                  if ((mediaPlayer.isPlaying())){
                                      mediaPlayer.stop();
                                  }
                                  else{
                                      try {
                                          mediaPlayer.reset();
                                          mediaPlayer.setDataSource(record.getAudioPath());
                                          mediaPlayer.prepare();
                                          mediaPlayer.start();

                                      } catch (IOException e) {
                                          e.printStackTrace();
                                      }

                                  }


                                }
                            });

        }

    }
}
