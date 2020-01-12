package cn.oomkiller.paxos4j.log.fs;

import cn.oomkiller.paxos4j.utils.Constant;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j
public class ArrayMemoryIndex implements MemoryIndex {
  private long keys[];
  private long offsets[];
  private int position;
  private int capacity;

  public ArrayMemoryIndex() {
    this(0);
  }

  public ArrayMemoryIndex(int initialCapacity) {
    if (initialCapacity < (Constant._4MB / Constant.INDEX_LENGTH)) {
      initialCapacity = (Constant._4MB / Constant.INDEX_LENGTH);
    }
    keys = new long[initialCapacity];
    offsets = new long[initialCapacity];
    capacity = initialCapacity;
    position = -1;
  }

  @Override
  public int getSize() {
    return position + 1;
  }

  @Override
  public void init() {
    // the log is ordered naturally
    // sortAndCompact();
  }

  @Override
  public void insertIndexCache(long key, long value) {
    position += 1;
    if (position >= capacity) {
      capacity <<= 1;
      keys = Arrays.copyOf(keys, capacity);
      offsets = Arrays.copyOf(offsets, capacity);
      log.debug("extend memory array size to " + capacity);
    }

    keys[position] = key;
    offsets[position] = value;
  }

  @Override
  public long get(long key) {
    return binarySearchPosition(key);
  }

  @Override
  public long[] getKeys() {
    return keys;
  }

  @Override
  public long[] getOffsets() {
    return offsets;
  }

  private long binarySearchPosition(long key) {
    int index = binarySearch(0, getSize(), key);
    if (index >= 0) {
      return offsets[index];
    } else {
      return -1L;
    }
  }

  private int binarySearch(int fromIndex, int toIndex, long key) {
    int low = fromIndex;
    int high = toIndex - 1;

    while (low <= high) {
      int mid = (low + high) >>> 1;
      long midVal = keys[mid];
      int cmp = Long.compare(midVal, key);
      if (cmp < 0) {
        low = mid + 1;
      } else if (cmp > 0) {
        high = mid - 1;
      } else {
        return mid; // keys found
      }
    }
    return -(low + 1); // keys not found.
  }

  //  /** sort the index and compact the same key */
  //  private void sortAndCompact() {
  //    if (capacity != 0) {
  //      sort(0, capacity - 1);
  //      if (capacity > 60000 && capacity < 64000) {
  //        return;
  //      }
  //      compact();
  //    }
  //  }
  //
  //  private void compact() {
  //    long[] newKeys = new long[capacity];
  //    long[] newOffsetInts = new long[capacity];
  //
  //    int curIndex = 0;
  //    newOffsetInts[0] = offsets[0];
  //    newKeys[0] = keys[0];
  //    for (int i = 1; i < capacity; i++) {
  //      if (keys[i] != keys[i - 1]) {
  //        curIndex++;
  //        newKeys[curIndex] = keys[i];
  //        newOffsetInts[curIndex] = offsets[i];
  //      } else {
  //        newOffsetInts[curIndex] = Math.max(newOffsetInts[curIndex], offsets[i]);
  //      }
  //    }
  //    capacity = curIndex + 1;
  //    offsets = newOffsetInts;
  //    keys = newKeys;
  //  }

  //  private void sort(int low, int high) {
  //    int start = low;
  //    int end = high;
  //    long key = keys[low];
  //
  //    while (end > start) {
  //      while (end > start && keys[end] >= key) end--;
  //      if (keys[end] <= key) {
  //        swap(start, end);
  //      }
  //      // 从前往后比较
  //      while (end > start && keys[start] <= key) start++;
  //      if (keys[start] >= key) {
  //        swap(start, end);
  //      }
  //    }
  //    if (start > low) sort(low, start - 1);
  //    if (end < high) sort(end + 1, high);
  //  }

  //  private void swap(int i, int j) {
  //    if (i == j) return;
  //    keys[i] ^= keys[j];
  //    keys[j] ^= keys[i];
  //    keys[i] ^= keys[j];
  //
  //    offsets[i] ^= offsets[j];
  //    offsets[j] ^= offsets[i];
  //    offsets[i] ^= offsets[j];
  //  }
}
