package com.example.anesc.androidtarsosdsp;

import android.os.Environment;
import android.util.Log;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.beatroot.BeatRootOnsetEventHandler;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.onsets.ComplexOnsetDetector;
import be.tarsos.dsp.onsets.OnsetHandler;

public class MfccTest implements OnsetHandler {
    private List<Double> mList = new ArrayList();
    public void MFCCForSineTest  (final String[] str)  throws FileNotFoundException  {
        List<String[]> data = new ArrayList<String[]>();
        int sampleRate = 16000;
        int bufferSize = 1024;
        for(int i=0; i<str.length ;i++){

            File file = new File("/storage/emulated/0/Music/"+String.valueOf(i+1)+".mp3");
            AudioDispatcher audioDispatcher;
            audioDispatcher = AudioDispatcherFactory.fromPipe(file.getAbsolutePath(), sampleRate, bufferSize, 0);
            ComplexOnsetDetector detector = new ComplexOnsetDetector(bufferSize);
            final BeatRootOnsetEventHandler handler = new BeatRootOnsetEventHandler();
            detector.setHandler(handler);
            audioDispatcher.addAudioProcessor(detector);
            audioDispatcher.run();
            handler.trackBeats(this);

            String emotion;
            if(mList.size()==0) {
                emotion="NONE";
            }else {
                double[] differences = new double[mList.size() - 1];
                for (int z = 0; z < mList.size() - 1; z++) {
                    differences[z] = mList.get(z + 1) - mList.get(z);
                }
                Arrays.sort(differences);
                double median;
                double largest;
                double smallest;
                if (differences.length % 2 == 0)
                    median = (differences[differences.length / 2] + differences[differences.length / 2 - 1]) / 2;
                else
                    median = differences[differences.length / 2];

                largest = (60/differences[differences.length-1]);
                smallest = (60/differences[0]);
                double bpm = (60 / median);
                if (bpm <= 110) {
                    emotion = "Sadness";
                } else if (110 < bpm && bpm <= 140) {
                    emotion = "Happiness";
                } else {
                    emotion = "Anger";
                }
                Log.d("Emotion", file + ":" + emotion + "," + String.valueOf(bpm));
                String[] tempstringdata = new String[4];
                String[] filesplit = String.valueOf(file).split("/");
                tempstringdata[0] = filesplit[filesplit.length-1].split(".mp3")[0];
                tempstringdata[1] = String.valueOf(largest);
                tempstringdata[2] = String.valueOf(bpm);
                tempstringdata[3] = String.valueOf(smallest);
                data.add(tempstringdata);
            }
            mList.clear();
        }

        CSVWrite cw = new CSVWrite();
        cw.writeCsv(data);
    }

    @Override
    public void handleOnset(double time, double salience) {
        mList.add(time);
    }

    public class CSVWrite {

        private String filename = Environment.getExternalStorageDirectory().getAbsolutePath() +"/Csvfile.csv";

        public CSVWrite() {}

        public void writeCsv(List<String[]> data) {
            try {
                CSVWriter cw = new CSVWriter(new FileWriter(filename), ',', '"');
                Iterator<String[]> it = data.iterator();
                try {
                    while (it.hasNext()) {
                        String[] s = (String[]) it.next();
                        cw.writeNext(s);
                    }
                } finally {
                    cw.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}