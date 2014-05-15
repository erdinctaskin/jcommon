/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tr.com.serkanozal.jcommon.map;

import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author Serkan ÖZAL
 * 
 * Holds and indexes entities by Judy tree based structure and gets at O(1) complexity.
 * Complexity doesn't depends on count of entities. 
 * Normally in CHM or HM,  complexity is O(1+n/k) where 
 * 		k is the number of buckets,
 * 		n is the number of entities.
 * In this map implementation, complexity is O(1) at every entity counts. But it uses more memory.
 * 
 * In Judy tree based indexing structure, there are 4 levels for 4 byte of hash code as integer.
 * Last level (level 4 or leaf node) is hold as values.
 */
public class JudyHashMap<K, V> extends AbstractMap<K, V> {

	private final static int BITS_IN_BYTE = 8;
	private final static int MAX_LEVEL = (Integer.SIZE / BITS_IN_BYTE) - 1; 
	private final static int NODE_SIZE = 256;
	
	private JudyTree root = new JudyTree();
	
	public JudyHashMap() {

	}
	
	@Override
	public int size() {
		return root.size;
	}
	
	@Override
	public boolean isEmpty() {
		return root.size == 0;
	}
    
	@Override
	public boolean containsValue(Object value) {
		throw new UnsupportedOperationException("\"containsValue(Object value)\" operation is not supported right now !");
	}
    
    @Override
	public Set<K> keySet() {
    	return new JudyKeySet();
    }
    
    @Override
	public Collection<V> values() {
    	return new JudyValueCollection();
    }
    
    @Override
	public Set<Map.Entry<K, V>> entrySet() {
    	return new JudyEntrySet();
    }
	
	@Override
	public V put(K key, V value) {
		return (V) root.put(key, value);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public V get(Object key) {
		return (V) root.get((K) key);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public V remove(Object key) {
		return (V) root.remove((K) key);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean containsKey(Object key) {
		return root.containsKey((K) key);
	}
	
	@Override
	public void clear() {
		root.clear();
	}
	
	class JudyEntrySet extends AbstractSet<Map.Entry<K, V>> {

		@Override
		public Iterator<Map.Entry<K, V>> iterator() {
			return new JudyEntryIterator(root.firstEntry);
		}

		@Override
		public int size() {
			return root.size;
		}
		
	}
	
	class JudyEntryIterator implements Iterator<Map.Entry<K, V>> {

		JudyEntry currentEntry;
		
		JudyEntryIterator(JudyEntry firstEntry) {
			currentEntry = firstEntry;
		}
		
		@Override
		public boolean hasNext() {
			if (currentEntry == null) {
				return false;
			}
			else {
				return currentEntry.next != null;
			}
		}

		@Override
		public JudyEntry next() {
			if (currentEntry != null) {
				JudyEntry nextEntry = currentEntry;
				currentEntry = currentEntry.next;
				return nextEntry;
			}
			else {
				return null;
			}	
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("\"remove()\" operation is not supported by JudyEntryIterator !");
		}
		
	}
	
	class JudyKeySet extends AbstractSet<K> {

		@Override
		public Iterator<K> iterator() {
			return new JudyKeyIterator(new JudyEntryIterator(root.firstEntry));
		}

		@Override
		public int size() {
			return root.size;
		}
		
	}
	
	class JudyKeyIterator implements Iterator<K> {

		JudyEntryIterator entryIterator;
		
		JudyKeyIterator(JudyEntryIterator entryIterator) {
			this.entryIterator = entryIterator;
		}
		
		@Override
		public boolean hasNext() {
			return entryIterator.hasNext();
		}

		@Override
		public K next() {
			JudyEntry entry = entryIterator.next();
			if (entry == null) {
				return null;
			}
			else {
				return entry.key;
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("\"remove()\" operation is not supported by JudyEntryIterator !");
		}
		
	}
	
	class JudyValueCollection extends AbstractCollection<V> {

		@Override
		public Iterator<V> iterator() {
			return new JudyValueIterator(new JudyEntryIterator(root.firstEntry));
		}

		@Override
		public int size() {
			return root.size;
		}
		
	}
	
	class JudyValueIterator implements Iterator<V> {

		JudyEntryIterator entryIterator;
		
		JudyValueIterator(JudyEntryIterator entryIterator) {
			this.entryIterator = entryIterator;
		}
		
		@Override
		public boolean hasNext() {
			return entryIterator.hasNext();
		}

		@Override
		public V next() {
			JudyEntry entry = entryIterator.next();
			if (entry == null) {
				return null;
			}
			else {
				return entry.value;
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("\"remove()\" operation is not supported by JudyEntryIterator !");
		}
		
	}
	
	class JudyEntry implements Map.Entry<K, V> {

		K key;
		V value;
		JudyEntry prev;
		JudyEntry next;
		
		JudyEntry(K key) {
			this.key = key;
		}
		
		JudyEntry(K key, V value) {
			this.key = key;
			this.value = value;
		}
		
		@Override
		public K getKey() {
			return key;
		}

		@Override
		public V getValue() {
			return value;
		}

		@Override
		public V setValue(V value) {
			return this.value = value;
		}
		
	}
	
	abstract class JudyNode {
		
		abstract V get(int hash, byte level);
		abstract V put(int hash, K key, V value, byte level);
		abstract V remove(int hash, byte level);
		abstract boolean containsKey(int hash, byte level);
		abstract void clear(byte level);
		
	}
	
	class JudyIntermediateNode extends JudyNode {
		
		@SuppressWarnings("rawtypes")
		JudyHashMap.JudyNode[] children;
		
		JudyIntermediateNode() {
			init();
		}
		
		void init() {
			children = new JudyHashMap.JudyNode[NODE_SIZE];
		}
		
		void initIfNeeded() {
			if (children == null) {
				init();
			}
		}
		
		@SuppressWarnings("unchecked")
		@Override
		V get(int hash, byte level) {
			initIfNeeded();
			// Find related byte for using as index in current level
			byte nextLevel = (byte) (level + 1);
			short index = (short)(((hash >> (32 - (nextLevel << 3))) & 0x000000FF));
			JudyNode child = children[index];
			if (child != null) {
				return child.get(hash, nextLevel);
			}
			return null;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		V put(int hash, K key, V value, byte level) {
			initIfNeeded();
			// Find related byte for using as index in current level
			byte nextLevel = (byte) (level + 1);
			short index = (short)(((hash >> (32 - (nextLevel << 3))) & 0x000000FF));
			JudyNode child = children[index];
			if (child == null) {
				if (nextLevel < MAX_LEVEL) {
					child = new JudyIntermediateNode();
				}
				else {
					child = new JudyLeafNode();
				}
				children[index] = child;
			}
			return child.put(hash, key, value, nextLevel);
		}
		
		@SuppressWarnings("unchecked")
		@Override
		V remove(int hash, byte level) {
			initIfNeeded();
			// Find related byte for using as index in current level
			byte nextLevel = (byte) (level + 1);
			short index = (short)(((hash >> (32 - (nextLevel << 3))) & 0x000000FF));
			JudyNode child = children[index];
			if (child != null) {
				return child.remove(hash, nextLevel);
			}
			return null;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		boolean containsKey(int hash, byte level) {
			initIfNeeded();
			// Find related byte for using as index in current level
			byte nextLevel = (byte) (level + 1);
			short index = (short)(((hash >> (32 - (nextLevel << 3))) & 0x000000FF));
			JudyNode child = children[index];
			if (child != null) {
				return child.containsKey(hash, nextLevel);
			}
			return false;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		void clear(byte level) {
			if (children != null) {
				// Clear child nodes
				for (JudyNode child : children) {
					if (child != null) {
						child.clear((byte) (level + 1));
					}	
					child = null; // Now it can be collected by GC
				}
			}
			children = null; // Now it can be collected by GC
		}
		
	}
	
	class JudyLeafNode extends JudyNode {
		
		@SuppressWarnings("rawtypes")
		JudyHashMap.JudyEntry[] entries;
		
		JudyLeafNode() {
			init();
		}
		
		void init() {
			entries = new JudyHashMap.JudyEntry[NODE_SIZE];
		}
		
		void initIfNeeded() {
			if (entries == null) {
				init();
			}
		}
		
		@SuppressWarnings("unchecked")
		@Override
		V get(int hash, byte level) {
			initIfNeeded();
			// Find related byte for using as index in current level
			byte nextLevel = (byte) (level + 1);
			short index = (short)(((hash >> (32 - (nextLevel << 3))) & 0x000000FF));
			JudyEntry entry = entries[index];
			if (entry != null) {
				return entry.value;
			}
			return null;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		V put(int hash, K key, V value, byte level) {
			initIfNeeded();
			// Find related byte for using as index in current level
			byte nextLevel = (byte) (level + 1);
			short index = (short)(((hash >> (32 - (nextLevel << 3))) & 0x000000FF));
			JudyEntry entry = entries[index];
			if (entry == null) {
				entry = new JudyEntry(key);
				entries[index] = entry;
				synchronized (root) {
					if (root.firstEntry == null) {
						root.firstEntry = entry;
					}
					if (root.lastEntry != null) {
						entry.prev = root.lastEntry;
						root.lastEntry.next = entry;
					}
					root.lastEntry = entry;
				}	
			}
			entry.value = value;
			return value;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		V remove(int hash, byte level) {
			initIfNeeded();
			// Find related byte for using as index in current level
			byte nextLevel = (byte) (level + 1);
			short index = (short)(((hash >> (32 - (nextLevel << 3))) & 0x000000FF));
			JudyEntry entryToRemove = entries[index];
			if (entryToRemove != null) {
				entries[index] = null;
				synchronized (root) {
					if (entryToRemove == root.firstEntry) {
						root.firstEntry = entryToRemove.next;
					}
					if (entryToRemove == root.lastEntry) {
						root.lastEntry = entryToRemove.prev;
					}
				}
				if (entryToRemove.prev != null) {
					synchronized (entryToRemove.prev) {
						entryToRemove.prev.next = entryToRemove.next;
					}
				}
				if (entryToRemove.next != null) {
					synchronized (entryToRemove.next) {
						entryToRemove.next.prev = entryToRemove.prev;
					}	
				}
				return entryToRemove.value;
			}
			return null;
		}
		
		@Override
		boolean containsKey(int hash, byte level) {
			initIfNeeded();
			// Find related byte for using as index in current level
			byte nextLevel = (byte) (level + 1);
			short index = (short)(((hash >> (32 - (nextLevel << 3))) & 0x000000FF));
			return entries[index] != null;
		}
		
		@Override
		void clear(byte level) {
			entries = null; // Now it can be collected by GC
		}
		
	}
	
	/**
	 * Root node for Judy tree based indexing nodes
	 */
	class JudyTree {
		
		@SuppressWarnings("unchecked")
		JudyIntermediateNode[] nodes = new JudyHashMap.JudyIntermediateNode[NODE_SIZE];
		JudyEntry firstEntry;
		JudyEntry lastEntry;
		volatile int size;
		
		JudyTree() {
			// Create and initialize first level nodes
			for (int i = 0 ; i < nodes.length; i++) {
				nodes[i] = new JudyIntermediateNode();
			}
		}
		
		V get(K key) {
			// Use most significant byte as first level index
			short index = (short)((key.hashCode() >> 24) & 0x000000FF);
			return nodes[index].get(key.hashCode(), (byte) 1);
		}
		
		V put(K key, V value) {
			// Use most significant byte as first level index
			short index = (short)((key.hashCode() >> 24) & 0x000000FF);
			V obj = nodes[index].put(key.hashCode(), key, value, (byte) 1);
			if (obj != null) {
				size++;
			}
			return obj;
		}
		
		V remove(K key) {
			// Use most significant byte as first level index
			short index = (short)((key.hashCode() >> 24) & 0x000000FF);
			V obj = nodes[index].remove(key.hashCode(), (byte) 1);
			if (obj != null) {
				size--;
			}
			return obj;
		}
		
		boolean containsKey(K key) {
			// Use most significant byte as first level index
			short index = (short)((key.hashCode() >> 24) & 0x000000FF);
			return nodes[index].containsKey(key.hashCode(), (byte) 1);
		}
		
		void clear() {
			// Start clearing from first level child nodes
			for (int i = 0 ; i < nodes.length; i++) {
				nodes[i].clear((byte) 1);
			}
			size = 0;
		}
		
	}
	
}
