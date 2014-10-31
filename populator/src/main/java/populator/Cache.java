//http://java-planet.blogspot.com/2005/08/how-to-set-up-simple-lru-cache-using.html

package populator;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

@SuppressWarnings("serial")
public class Cache extends
LinkedHashMap<Long, Long>
{
private final int capacity;
private long accessCount = 0;
private long hitCount = 0;

public Cache(int capacity)
{
super(capacity + 1, 1.1f, true);
this.capacity = capacity;
}

public Long get(Object key)
{
accessCount++;
if (containsKey(key))
{
  hitCount++;
}
Long value = super.get(key);
return value;
}

protected boolean removeEldestEntry(Entry eldest)
{
return size() > capacity;
}

public long getAccessCount()
{
return accessCount;
}

public long getHitCount()
{
return hitCount;
}
}
