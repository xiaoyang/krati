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

package krati.store;

/**
 * StoreReader
 * 
 * @author jwu
 * @since 09/15, 2011
 */
public interface StoreReader<K, V> {
    
    /**
     * Returns the value to which the specified <code>key</code> is mapped in this store.
     * 
     * @param key - the key
     * @return the value associated with the <code>key</code>,
     *         or <code>null</code> if the <code>key</code> is not known to this store. 
     * @throws Exception if this operation cannot be completed.
     */
    public V get(K key) throws Exception;
    
}
