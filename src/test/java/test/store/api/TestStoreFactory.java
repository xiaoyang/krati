package test.store.api;

import java.io.File;
import java.util.Arrays;
import java.util.Random;

import test.util.FileUtils;
import test.util.RandomBytes;

import junit.framework.TestCase;

import krati.core.StoreFactory;
import krati.core.StoreParams;
import krati.core.array.basic.DynamicConstants;
import krati.core.segment.MappedSegmentFactory;
import krati.core.segment.SegmentFactory;
import krati.store.ArrayStore;
import krati.store.DataStore;

/**
 * TestStoreFactory
 * 
 * @author jwu
 * 06/09, 2011
 * 
 * <p>
 * 06/11, 2011 - Added new tests for creating stores
 */
public class TestStoreFactory extends TestCase {
    Random _rand = new Random();
    
    public void testInitLevel() {
        int unitCapacity = DynamicConstants.SUB_ARRAY_SIZE;
        int initialCapacity;
        
        initialCapacity = unitCapacity;
        assertEquals(0, StoreParams.getDynamicStoreInitialLevel(initialCapacity));
        
        initialCapacity = unitCapacity + unitCapacity / 2;
        assertEquals(1, StoreParams.getDynamicStoreInitialLevel(initialCapacity));
        
        initialCapacity = unitCapacity << 1;
        assertEquals(1, StoreParams.getDynamicStoreInitialLevel(initialCapacity));
        
        for(int i = 0, cnt = 10; i < cnt; i++) {
            int level = _rand.nextInt(14);
            
            initialCapacity = unitCapacity << level;
            assertEquals(level, StoreParams.getDynamicStoreInitialLevel(initialCapacity));
            
            initialCapacity = (unitCapacity << level) + 1;
            assertEquals(level + 1, StoreParams.getDynamicStoreInitialLevel(initialCapacity));
            
            initialCapacity = (unitCapacity << level) + (unitCapacity / 2);
            assertEquals(level + 1, StoreParams.getDynamicStoreInitialLevel(initialCapacity));
            
            initialCapacity = (unitCapacity << (level + 1)) - 1;
            assertEquals(level + 1, StoreParams.getDynamicStoreInitialLevel(initialCapacity));
        }
        
        initialCapacity = Integer.MAX_VALUE;
        assertEquals(15, StoreParams.getDynamicStoreInitialLevel(initialCapacity));
    }
    
    public void testCreateStaticArrayStore() throws Exception {
        File homeDir = FileUtils.getTestDir(getClass().getSimpleName());
        int length = 1000 + _rand.nextInt(1000000);
        int batchSize = StoreParams.BATCH_SIZE_DEFAULT;
        int numSyncBatches = StoreParams.NUM_SYNC_BATCHES_DEFAULT;
        int segmentFileSizeMB = StoreParams.SEGMENT_FILE_SIZE_MB_MIN;
        double segmentCompactFactor = StoreParams.SEGMENT_COMPACT_FACTOR_DEFAULT;
        SegmentFactory segmentFactory = new MappedSegmentFactory();
        
        /**
         * StaticArrayStore does not change length after the store has been created.
         */
        ArrayStore store = StoreFactory.createStaticArrayStore(
                homeDir,
                length,
                segmentFileSizeMB,
                segmentFactory);
        
        assertEquals(length, store.length());
        assertEquals(length, store.capacity());
        assertEquals(0, store.getIndexStart());
        store.clear();
        store.close();
        
        // Smaller length has no impact
        int smallerLength = length - 500;
        store = StoreFactory.createStaticArrayStore(
                homeDir,
                smallerLength,
                batchSize,
                numSyncBatches,
                segmentFileSizeMB,
                segmentFactory);
        
        assertEquals(length, store.length());
        assertEquals(length, store.capacity());
        assertEquals(0, store.getIndexStart());
        store.clear();
        store.close();
        
        // Larger length has no impact
        int largerLength = length + 500;
        store = StoreFactory.createStaticArrayStore(
                homeDir,
                largerLength,
                batchSize,
                numSyncBatches,
                segmentFileSizeMB,
                segmentFactory,
                segmentCompactFactor);
        
        assertEquals(length, store.length());
        assertEquals(length, store.capacity());
        assertEquals(0, store.getIndexStart());
        store.clear();
        store.close();
        
        FileUtils.deleteDirectory(homeDir);
    }
    
    public void testCreateDynamicArrayStore() throws Exception {
        File homeDir = FileUtils.getTestDir(getClass().getSimpleName());
        int initialLength = DynamicConstants.SUB_ARRAY_SIZE << 2;
        int batchSize = StoreParams.BATCH_SIZE_DEFAULT;
        int numSyncBatches = StoreParams.NUM_SYNC_BATCHES_DEFAULT;
        int segmentFileSizeMB = StoreParams.SEGMENT_FILE_SIZE_MB_MIN;
        double segmentCompactFactor = StoreParams.SEGMENT_COMPACT_FACTOR_DEFAULT;
        SegmentFactory segmentFactory = new MappedSegmentFactory();
        
        /**
         * DynamicArrayStore only grows its capacity/length after the store has been created.
         */
        ArrayStore store = StoreFactory.createDynamicArrayStore(
                homeDir,
                initialLength,
                segmentFileSizeMB,
                segmentFactory);
        
        assertEquals(initialLength, store.length());
        assertEquals(initialLength, store.capacity());
        assertEquals(0, store.getIndexStart());
        store.clear();
        store.close();
        
        // Smaller length has no impact
        int smallerLength = initialLength - 500;
        store = StoreFactory.createDynamicArrayStore(
                homeDir,
                smallerLength,
                batchSize,
                numSyncBatches,
                segmentFileSizeMB,
                segmentFactory);
        
        assertEquals(initialLength, store.length());
        assertEquals(initialLength, store.capacity());
        assertEquals(0, store.getIndexStart());
        store.clear();
        store.close();
        
        // Larger length caused the store to grow capacity
        int largerLength = initialLength + 500;
        int expectedLength = initialLength + DynamicConstants.SUB_ARRAY_SIZE;
        store = StoreFactory.createDynamicArrayStore(
                homeDir,
                largerLength,
                batchSize,
                numSyncBatches,
                segmentFileSizeMB,
                segmentFactory,
                segmentCompactFactor);
        
        assertEquals(expectedLength, store.length());
        assertEquals(expectedLength, store.capacity());
        assertEquals(0, store.getIndexStart());
        store.clear();
        store.close();
        
        FileUtils.deleteDirectory(homeDir);
    }
    
    public void testCreateStaticDataStore() throws Exception {
        File homeDir = FileUtils.getTestDir(getClass().getSimpleName());
        int capacity = 1000 + _rand.nextInt(1000000);
        int batchSize = StoreParams.BATCH_SIZE_DEFAULT;
        int numSyncBatches = StoreParams.NUM_SYNC_BATCHES_DEFAULT;
        int segmentFileSizeMB = StoreParams.SEGMENT_FILE_SIZE_MB_MIN;
        double segmentCompactFactor = StoreParams.SEGMENT_COMPACT_FACTOR_DEFAULT;
        SegmentFactory segmentFactory = new MappedSegmentFactory();

        DataStore<byte[], byte[]> store;
        byte[] key = RandomBytes.getBytes(32);
        byte[] value = RandomBytes.getBytes(1024);
        
        store = StoreFactory.createStaticDataStore(
                homeDir,
                capacity,
                segmentFileSizeMB,
                segmentFactory);
        
        store.put(key, value);
        assertTrue(Arrays.equals(value, store.get(key)));
        store.close();
        
        store = StoreFactory.createStaticDataStore(
                homeDir,
                capacity,
                batchSize,
                numSyncBatches,
                segmentFileSizeMB,
                segmentFactory);
        
        assertTrue(Arrays.equals(value, store.get(key)));
        store.close();
        
        store = StoreFactory.createStaticDataStore(
                homeDir,
                capacity,
                batchSize,
                numSyncBatches,
                segmentFileSizeMB,
                segmentFactory,
                segmentCompactFactor);
        
        assertTrue(Arrays.equals(value, store.get(key)));
        store.close();
        
        FileUtils.deleteDirectory(homeDir);
    }
    
    public void testCreateDynamicDataStore() throws Exception {
        File homeDir = FileUtils.getTestDir(getClass().getSimpleName());
        int capacity = DynamicConstants.SUB_ARRAY_SIZE << 2;
        int batchSize = StoreParams.BATCH_SIZE_DEFAULT;
        int numSyncBatches = StoreParams.NUM_SYNC_BATCHES_DEFAULT;
        int segmentFileSizeMB = StoreParams.SEGMENT_FILE_SIZE_MB_MIN;
        double segmentCompactFactor = StoreParams.SEGMENT_COMPACT_FACTOR_DEFAULT;
        double hashLoadFactor = StoreParams.HASH_LOAD_FACTOR_DEFAULT;
        SegmentFactory segmentFactory = new MappedSegmentFactory();
        
        DataStore<byte[], byte[]> store;
        byte[] key = RandomBytes.getBytes(32);
        byte[] value = RandomBytes.getBytes(1024);
        
        store = StoreFactory.createDynamicDataStore(
                homeDir,
                capacity,
                segmentFileSizeMB,
                segmentFactory);
        
        store.put(key, value);
        assertTrue(Arrays.equals(value, store.get(key)));
        store.close();
        
        store = StoreFactory.createDynamicDataStore(
                homeDir,
                capacity,
                batchSize,
                numSyncBatches,
                segmentFileSizeMB,
                segmentFactory);

        assertTrue(Arrays.equals(value, store.get(key)));
        store.close();
        
        store = StoreFactory.createDynamicDataStore(
                homeDir,
                capacity,
                batchSize,
                numSyncBatches,
                segmentFileSizeMB,
                segmentFactory,
                segmentCompactFactor);
        
        assertTrue(Arrays.equals(value, store.get(key)));
        store.close();
        
        store = StoreFactory.createDynamicDataStore(
                homeDir,
                capacity,
                batchSize,
                numSyncBatches,
                segmentFileSizeMB,
                segmentFactory,
                segmentCompactFactor,
                hashLoadFactor);
        
        assertTrue(Arrays.equals(value, store.get(key)));
        store.close();
        
        FileUtils.deleteDirectory(homeDir);
    }
}