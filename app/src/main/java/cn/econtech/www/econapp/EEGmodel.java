package cn.econtech.www.econapp;

import com.androidplot.xy.*;

import java.lang.ref.WeakReference;
import java.util.Arrays;

class EEGModel implements XYSeries {

        private final double[] data;
        private final int cacheSize, winSize;
        private final static int DEFAULT_CACHE_RATIO = 10;
        private int currentStartIdx, currentStopIdx;

         private WeakReference<AdvancedLineAndPointRenderer> rendererRef;

        /**
         *
         * @param winSize Sample size contained within this model
         */
        public EEGModel(final int winSize) {
            this(winSize, winSize * DEFAULT_CACHE_RATIO);
        }

        /**
         *
         * @param winSize Sample size contained within this model
         * @param cacheSize cache data to imporve performance
         */
        public EEGModel(final int winSize, final int cacheSize) {
            this.winSize = winSize;
            this.cacheSize = cacheSize > winSize ? cacheSize : winSize;
            data = new double[cacheSize];
            for(int i = 0; i < data.length; i++) {
                data[i] = 0;
            }
            currentStopIdx = winSize;
            currentStartIdx = 0;
        }

        public void setRendererRef(final WeakReference<AdvancedLineAndPointRenderer> rendererRef) {
            this.rendererRef = rendererRef;
        }

        public void updateData(double signal[]) {
            if (null == signal || 0 == signal.length) {
                return ;
            }
            int dataIdx = currentStopIdx, signalIdx = 0;
            if (signal.length >= winSize) { // signal is redundant
                signalIdx = signal.length - winSize;
                dataIdx = 0;
                currentStartIdx = 0;
                currentStopIdx = winSize;
            } else {
                int nextStopIdx = currentStopIdx + signal.length;
                if (nextStopIdx > cacheSize) { //cache is full
                    currentStartIdx = 0;
                    currentStopIdx = winSize;
                    dataIdx = winSize - signal.length;
                    for (int i = 0, j = (nextStopIdx - winSize); i < dataIdx; ++i, ++j) {
                        data[i] = data[j];
                    }
                } else {
                    currentStopIdx = nextStopIdx;
                    currentStartIdx = nextStopIdx - winSize;
                }
            }
            while(signalIdx < signal.length) {
                data[dataIdx] = signal[signalIdx];
                ++dataIdx;
                ++signalIdx;
            }
//            if (rendererRef.get() != null)
//                rendererRef.get().setLatestIndex(currentStopIdx);
        }

        @Override
        public int size() {
            return winSize;
        }

        @Override
        public Number getX(int index) {
            return index;
        }

        @Override
        public Number getY(int index) {
            return data[index + currentStartIdx];
        }

        @Override
        public String getTitle() {
            return "Signal";
        }

        public void show() {
        	System.out.println(Arrays.toString(
        		Arrays.copyOfRange(data, currentStartIdx, currentStopIdx)
        		));
        }
    }