package me.nallar.bettermobspawning;

import java.util.*;

@SuppressWarnings("unchecked")
public class LongHashMap {
	private static final long EMPTY_KEY = Long.MIN_VALUE;
	private static final int BUCKET_SIZE = 8192;
	private final long[][] keys = new long[BUCKET_SIZE][];
	private final java.lang.Object[][] values = new java.lang.Object[BUCKET_SIZE][];
	private int size;

	public long[][] getKeys() {
		return keys;
	}

	public int getNumHashElements() {
		return size;
	}

	public boolean containsItem(long key) {
		return getValueByKey(key) != null;
	}

	public Object getValueByKey(long key) {
		int index = (int) (keyIndex(key) & (BUCKET_SIZE - 1));
		long[] inner = keys[index];
		if (inner == null) {
			return null;
		}

		for (int i = 0; i < inner.length; i++) {
			long innerKey = inner[i];
			if (innerKey == EMPTY_KEY) {
				return null;
			} else if (innerKey == key) {
				java.lang.Object[] value = values[index];
				if (value != null) {
					return (Object) value[i];
				}
			}
		}

		return null;
	}

	public void add(long key, java.lang.Object value) {
		put(key, value);
	}

	public synchronized Object put(long key, java.lang.Object value) {
		int index = (int) (keyIndex(key) & (BUCKET_SIZE - 1));
		long[] innerKeys = keys[index];
		java.lang.Object[] innerValues = values[index];

		if (innerKeys == null) {
			// need to make a new chain
			keys[index] = innerKeys = new long[8];
			Arrays.fill(innerKeys, EMPTY_KEY);
			values[index] = innerValues = new java.lang.Object[8];
			innerKeys[0] = key;
			innerValues[0] = value;
			size++;
		} else {
			int i;
			for (i = 0; i < innerKeys.length; i++) {
				// found an empty spot in the chain to put this
				long currentKey = innerKeys[i];
				if (currentKey == EMPTY_KEY) {
					size++;
				}
				if (currentKey == EMPTY_KEY || currentKey == key) {
					java.lang.Object old = innerValues[i];
					innerKeys[i] = key;
					innerValues[i] = value;
					return (Object) old;
				}
			}

			// chain is full, resize it and add our new entry
			keys[index] = innerKeys = Arrays.copyOf(innerKeys, i << 1);
			Arrays.fill(innerKeys, i, innerKeys.length, EMPTY_KEY);
			values[index] = innerValues = Arrays.copyOf(innerValues, i << 1);
			innerKeys[i] = key;
			innerValues[i] = value;
			size++;
		}
		return null;
	}

	public synchronized java.lang.Object remove(long key) {
		int index = (int) (keyIndex(key) & (BUCKET_SIZE - 1));
		long[] inner = keys[index];
		if (inner == null) {
			return null;
		}

		for (int i = 0; i < inner.length; i++) {
			// hit the end of the chain, didn't find this entry
			if (inner[i] == EMPTY_KEY) {
				break;
			}

			if (inner[i] == key) {
				java.lang.Object value = values[index][i];

				for (i++; i < inner.length; i++) {
					if (inner[i] == EMPTY_KEY) {
						break;
					}

					inner[i - 1] = inner[i];
					values[index][i - 1] = values[index][i];
				}

				inner[i - 1] = EMPTY_KEY;
				values[index][i - 1] = null;
				size--;
				return value;
			}
		}

		return null;
	}

	private static long keyIndex(long key) {
		key ^= key >>> 33;
		key *= 0xff51afd7ed558ccdL;
		key ^= key >>> 33;
		key *= 0xc4ceb9fe1a85ec53L;
		key ^= key >>> 33;
		return key;
	}
}
