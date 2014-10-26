package populator;

/**
 * This class is used to hold a key/value pair and the frequency associated with them, i.e.
 * the probability that the key or value will be chosen
 * @author andy
 *
 * @param <K> Class K of key
 * @param <V> Class of value
 */
public class SelectorItem<K extends Object, V> {
	private Double freq;
	private K key;
	private V value;
	
	/**
	 * Construct the selector item
	 * @param freq The frequency value (that will be normalized)
	 * @param key The key
	 * @param value The value
	 */
	public SelectorItem(Double freq, K key, V value){
		this.freq = freq;
		this.key = key;
		this.value = value;
	}
	
	/**
	 * Get the frequency of the key/value
	 * @return the frequency
	 */
	public Double getFreq(){
		return this.freq;
	}
	
	/**
	 * Get the key value
	 * @return get the key
	 */
	public K getKey(){
		return this.key;
	}
	
	/**
	 * Get the value
	 * @return the value
	 */
	public V getValue(){
		return this.value;
	}
	
	/**
	 * Normalize the frequency compared to the other SeclectorItems
	 * @param total The total number of 'counts' in the frequency column
	 */
	public void normalize(Double total){
		this.freq = this.freq / total;
	}
}
