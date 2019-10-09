package com.example.anesc.androidtarsosdsp;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.ZeroCrossingRateProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

public class PitchTest {
    int count;
    float sum;
    ZeroCrossingRateProcessor zero;
    public void MFCCForSineTest(final String[] str) throws FileNotFoundException {
        int sampleRate = 44100;
        int bufferSize = 5000;
        int bufferOverlap = 2500;
        final List<float[]> mfccList = new ArrayList<>();
        List<String> tempdata = new ArrayList<>();
        List<String[]> data = new ArrayList<String[]>();
        for(int i=0; i<str.length ;i++){
            sum = 0;
            count =0;
            File file = new File(str[i]);
            InputStream inStream = new FileInputStream(file);
            AudioDispatcher audioDispatcher;
            audioDispatcher = AudioDispatcherFactory.fromPipe(file.getAbsolutePath(), sampleRate, bufferSize, bufferOverlap);
            PitchDetectionHandler pdh = new PitchDetectionHandler() {
                @Override
                public void handlePitch(PitchDetectionResult res, AudioEvent e){
                    final float pitchInHz = res.getPitch();
                    if(pitchInHz!=-1){
                        count++;
                        sum = sum+pitchInHz;
                    }
                }
            };
            AudioProcessor pitchProcessor = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 44100, 5000, pdh);
            audioDispatcher.addAudioProcessor(pitchProcessor);
            audioDispatcher.addAudioProcessor(new AudioProcessor() {

                @Override
                public void processingFinished() {
                }

                @Override
                public boolean process(AudioEvent audioEvent) {

                    return true;
                }
            });
            audioDispatcher.run();
            Log.d("Pitch",String.valueOf(file) + ":" + String.valueOf(sum/count));
            }
    }
}
