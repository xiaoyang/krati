/*
 * Copyright (c) 2010-2011 LinkedIn, Inc
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package test.driver.raw;

import java.util.List;
import java.util.Random;

import test.LatencyStats;
import test.driver.StoreWriter;

/**
 * Write driver for data store.
 * 
 * @author jwu
 *
 * @param <S> Data Store
 */
public class BytesWriteDriver<S> implements Runnable {
    private final S _store;
    private final StoreWriter<S, byte[], byte[]> _writer;
    private final LatencyStats _latencyStats = new LatencyStats();
    private final Random _rand = new Random();
    private final List<String> _lineSeedData;
    private final int _lineSeedCount;
    private final int _keyCount;
    
    volatile long _cnt = 0;
    volatile boolean _running = true;
    
    public BytesWriteDriver(S ds, StoreWriter<S, byte[], byte[]> writer, List<String> lineSeedData, int keyCount) {
        this._store = ds;
        this._writer = writer;
        this._lineSeedData = lineSeedData;
        this._lineSeedCount = lineSeedData.size();
        this._keyCount = keyCount;
    }
    
    public LatencyStats getLatencyStats() {
        return this._latencyStats;
    }
    
    public long getWriteCount() {
        return this._cnt;
    }
    
    public void stop() {
        _running = false;
    }
    
    @Override
    public void run() {
        long prevTime = System.nanoTime();
        long currTime = prevTime;
        
        while (_running) {
            write();

            currTime = System.nanoTime();
            _latencyStats.countLatency((int)(currTime - prevTime)/1000);
            prevTime = currTime;
        }
    }

    protected void write() {
        try {
            int i = _rand.nextInt(_keyCount);
            String s = _lineSeedData.get(i%_lineSeedCount);
            String k = s.substring(0, 30) + i;
            _writer.put(_store, k.getBytes(), s.getBytes());
            _cnt++;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}