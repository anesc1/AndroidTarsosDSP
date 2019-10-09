package com.example.anesc.androidtarsosdsp;

import android.os.Environment;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.util.fft.FFT;

public class fftTest {
    int k;
    float sum;
    float in[][];
    float[] sumarr;
    public float[][] Test (final String[] str) throws FileNotFoundException {
        List<String[]> data = new ArrayList<String[]>();

        in = new float[str.length][1];
                for(int i=0; i<1000 ;i++) {
                    File file = new File("/storage/emulated/0/Music/"+String.valueOf(i+1)+".mp3");
                    sumarr = new float[1000];
                    sum = 0;
                    k=0;
                    final int bufferSize = 4096;
                    final int fftSize = bufferSize / 2;
                    final int sampleRate = 44100;
                    AudioDispatcher audioDispatcher;
                    audioDispatcher = AudioDispatcherFactory.fromPipe(file.getAbsolutePath(), sampleRate, bufferSize, 0);
                    audioDispatcher.addAudioProcessor(new AudioProcessor() {

                        FFT fft = new FFT(bufferSize);
                        final float[] amplitudes = new float[fftSize];
                        public boolean process(AudioEvent audioEvent) {
                            float[] audioBuffer = audioEvent.getFloatBuffer();
                            fft.forwardTransform(audioBuffer);
                            fft.modulus(audioBuffer, amplitudes);
                            for (int j = 0; j < amplitudes.length; j++) {
                                sum = sum + amplitudes[j];
                                k++;
                                if(k<=1000000 && k%1000==0){
                                    sumarr[k/1000-1] = sum/1000;
                                    sum = 0;
                                }
                            }
                            return true;
                        }

                        @Override
                        public void processingFinished() {

                        }
                    });
                    audioDispatcher.run();
                    in[i][0] = sum/k;
                    String[] tempstringdata = new String[1001];
                    String[] filesplit = String.valueOf(file).split("/");
                    tempstringdata[0] = filesplit[filesplit.length-1].split(".mp3")[0];
                    for(int g=1; g<=1000;g++){
                        tempstringdata[g] = String.valueOf(sumarr[g-1]);
                    }
                    data.add(tempstringdata);
                }
        CSVWrite cw = new CSVWrite();
        cw.writeCsv(data);
        return in;

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
