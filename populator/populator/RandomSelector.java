package populator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.TreeMap;

/**
 * The random selector can be used to get data drawn from statistical distributions
 * 
 * The file format is: frequency, key, value (optional)
 * 
 * for example
 * 50, $5
 * 25, $10
 * 25, $15
 * 
 * When we get a key we will receive $5 with 50% probability, $10 with 25% probability
 * and $15 with 25% probability. As there is no 'value' we pass false for 'readValues'
 * 
 * or
 * 
 * 100, 1, 1st item
 * 50, 2, 2nd item
 * 25, 3, 3rd item
 * 25, 4, All other
 * 
 * In this case, we get get a random key of 1, 2, 3, 4 with a probability of 0.5, 0.25, 0.125, 0.125
 * respectively. We can then use that key to also get the value associated with it.
 * 
 * We can specify if the key and values are ints, longs, doubles, or strings. The frequencies are converted
 * to doubles to allow us to calculate the probability a key should be chosen
 * 
 * The general usage is to send a file with the class of the key/values in the file, e.g.
 * RandomSelector("datafile.csv", Integer, String)
 * 
 */
public class RandomSelector<K extends Object, V>{
	/* Whether or not there are 'values' present with the keys */
	private boolean readValues;
	
	/* Limit the number of rows that should be read from the file */
	private int limit;
	
	/* To read in the file with the key/values */
	private BufferedReader bin;
	
	/* The list of key/value and frequency values that are read from the file */
	private ArrayList<SelectorItem<K, V>> keys = new ArrayList<SelectorItem<K, V>>();
	
	/* The total frequency count to be used for normalization of the frequencies */
	private double totalFreq = 0.0;
	
	/* The class of the keys - int, long, double, String, Object */
	private Class<K> keyClass;
	
	/* The class of the keys - int, long, double, String, Object */
	private Class<V> valueClass;
	
	/* The Class the keys are cast to once they are read */
	private int keyCast = 0;
	
	/* The Class the values are cast to once they are read*/
	private int valueCast = 0;
	
	/* Random number generator to choose the random key */
	private Random ran = new Random();
	
	/**
	 * Construct and only read keys
	 * 
	 * @param filename The file to read the frequencies and keys from
	 * @param keyClass The Class of the keys
	 */
	public RandomSelector(String filename, Class<K> keyClass){
		this(filename, false, -1, keyClass, null);
	}
	
	/**
	 * Construct and read both keys and values
	 * 
	 * @param filename The file to read the frequencies, keys and values from
	 * @param keyClass The Class of the keys
	 * @param valueClass The Class of the values
	 */
	public RandomSelector(String filename, Class<K> keyClass, Class<V> valueClass){
		this(filename, true, -1, keyClass, valueClass);
	}
	
	/**
	 * Construct and specify if the values should be read
	 * 
	 * @param filename  The file to read the frequencies, keys and values from
	 * @param readValues Should the values be read? true/false
	 * @param keyClass The Class of the keys
	 * @param valueClass The Class of the values
	 */
	public RandomSelector(String filename, boolean readValues, Class<K> keyClass, Class<V> valueClass){
		this(filename, readValues, -1, keyClass, valueClass);
	}
	
	/**
	 * Construct and specify if the values should be read and limit the number read in
	 * 
	 * @param filename The file to read the frequencies, keys and values from
	 * @param readValues Should the values be read? true/false
	 * @param limit The maximum number of rows to read
	 * @param keyClass The Class of the keys
	 * @param valueClass The Class of the values
	 */
	public RandomSelector(String filename, boolean readValues, int limit, Class<K> keyClass, Class<V> valueClass){
		this.setVariables(readValues, limit, keyClass, valueClass);
		
		// Create the buffered reader
		try {
			this.bin = new BufferedReader(new FileReader(new File(filename)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		readFile(null);
	}
	
	/**
	 * Use input stream to read the frequencies and keys
	 * @param is
	 * @param keyClass
	 */
	public RandomSelector(InputStream is, Class<K> keyClass){
		this(is, false, -1, keyClass, null, null);
	}
	
	/**
	 * Use input stream to read the frequencies, keys and values
	 * @param is
	 * @param keyClass
	 * @param keys A list of keys can be passed to override those in the file, if only frequencies are present for example, or if we want custom keys
	 */
	public RandomSelector(InputStream is, Class<K> keyClass, ArrayList<K> keys){
		
		this(is, false, -1, keyClass, null, keys);
	}
	
	/**
	 * Use input stream to read the frequencies, keys and values
	 * @param is
	 * @param keyClass
	 * @param valueClass
	 */
	public RandomSelector(InputStream is, Class<K> keyClass, Class<V> valueClass){
		this(is, true, -1, keyClass, valueClass, null);
	}
	
	/**
	 * Use input stream to read the frequencies, keys and values
	 * @param is
	 * @param readValues
	 * @param keyClass
	 * @param valueClass
	 */
	public RandomSelector(InputStream is, boolean readValues, Class<K> keyClass, Class<V> valueClass){
		this(is, readValues, -1, keyClass, valueClass, null);
	}
	
	/**
	 * Use input stream to read the frequencies, keys and values
	 * @param is
	 * @param readValues
	 * @param limit
	 * @param keyClass
	 * @param valueClass
	 * @param keys A list of keys can be passed to override those in the file, if only frequencies are present for example, or if we want custom keys
	 */
	public RandomSelector(InputStream is, boolean readValues, int limit, Class<K> keyClass, Class<V> valueClass, ArrayList<K> keys){
		this.setVariables(readValues, limit, keyClass, valueClass);

		// Create the buffered reader
		this.bin = new BufferedReader(new InputStreamReader(is));
		
		readFile(keys);
	}
	
	/**
	 * Set the variables needed to read from the file, key/value types, limit
	 * 
	 * @param readValues
	 * @param limit
	 * @param keyClass
	 * @param valueClass
	 */
	private void setVariables(boolean readValues, int limit, Class<K> keyClass, Class<V> valueClass){
		// Set the variables needed
		this.readValues = readValues;
		this.limit = limit;
		this.keyClass = keyClass;
		this.valueClass = valueClass;

		// Set the cast type for the key, values
		if (keyClass.isAssignableFrom(Integer.class))
			keyCast = 1;
		else if (keyClass.isAssignableFrom(Long.class))
			keyCast = 2;
		else if (keyClass.isAssignableFrom(Double.class))
			keyCast = 3;
		else if (keyClass.isAssignableFrom(String.class))
			keyCast = 4;
		else if (keyClass.isAssignableFrom(Object.class))
			keyCast = 5;

		if(readValues && valueClass != null)
		if (valueClass.isAssignableFrom(Integer.class))
			valueCast = 1;
		else if (valueClass.isAssignableFrom(Long.class))
			valueCast = 2;
		else if (valueClass.isAssignableFrom(Double.class))
			valueCast = 3;
		else if (valueClass.isAssignableFrom(String.class))
			valueCast = 4;
		else if (valueClass.isAssignableFrom(Object.class))
			valueCast = 5;
	}
	
	/**
	 * Read the frequencies, keys, values from the file. 
	 * @param keys
	 */
	private void readFile(ArrayList<K> keys){
		try {
			boolean keepReading = true; // decide when to stop reading
			int count = 0;
			if(keys != null) this.limit = keys.size();
			//System.out.println("limit : " + this.limit + ", count: " + count + ", keep reading : " + keepReading);
			
			if(this.limit >=0 && count>=this.limit) keepReading = false;
			
			//System.out.println("limit : " + this.limit+ ", count: " + count + ", keep reading : " + keepReading );
			
			// Read in the keys and values
			while(this.bin.ready() && keepReading){
				if(this.limit >=0 && count>=(this.limit-1)) keepReading = false;
				String lineIn = bin.readLine();

				String[] splitLine = lineIn.split(",");

				K key;
				V value = null;
				
				// Read the key from the passed values or the file
				if (keys != null)
					key = keys.get(count);
				else
					switch (keyCast) {
					case 1:
						key = keyClass.cast(new Integer(splitLine[1]));
						break;
					case 2:
						key = keyClass.cast(new Long(splitLine[1]));
						break;
					case 3:
						key = keyClass.cast(new Double(splitLine[1]));
						break;
					case 4:
						key = keyClass.cast(new String(splitLine[1]));
						break;
					case 5:
						key = keyClass.cast(splitLine[1]);
						break;
					default:
						key = null;
					}
				

				if(readValues && splitLine.length>=2){
					switch (valueCast) {
					case 1:
						value = valueClass.cast(new Integer(splitLine[2]));
						break;
					case 2:
						value = valueClass.cast(new Long(splitLine[2]));
						break;
					case 3:
						value = valueClass.cast(new Double(splitLine[2]));
						break;
					case 4:
						value = valueClass.cast(new String(splitLine[2]));
						break;
					case 5:
						value = valueClass.cast(splitLine[2]);
						break;
					default:
						value = null;
					}
				}
				
				this.keys.add(new SelectorItem<K, V>(new Double(totalFreq), key, value));
				totalFreq += Double.valueOf(splitLine[0]);
				
				count++;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Normalize the frequencies to zero
		for(SelectorItem<K, V> si : this.keys)
			si.normalize(totalFreq);
	}

	/**
	 * Get a key using a uniform distribution rather than the frequencies from the file
	 * @return the key selected
	 */
	public K getRandomUniformKey(){
		return this.keys.get(ran.nextInt(this.keys.size())).getKey();
	}
	
	/**
	 * Get a key using the distribution from the file
	 * @return the key selected
	 */
	public K getRandomKey(){
		return this.keys.get(searchKeyIndex(ran.nextDouble(), 0, this.keys.size()-1)).getKey();
	}
	
	/**
	 * Get a random value using the distribution from the file
	 * @return the value selected
	 */
	public V getRandomValue(){
		return this.keys.get(searchKeyIndex(ran.nextDouble(), 0, this.keys.size()-1)).getValue();
	}
	
	/**
	 * Get a random value using a uniform distribution
	 * @return the value selected
	 */
	public V getRandomUniformValue(){
		return this.keys.get(ran.nextInt(this.keys.size())).getValue();
	}
	
	/**
	 * search for the key/value using the distribution from the file
	 * @param value
	 * @param min
	 * @param max
	 * @return
	 */
	private int searchKeyIndex(Double value, int min, int max){
		int mid = (min + max) / 2;
		
		// Urg, some hacking below :-/ Need to improve the normalization
		// just to make sure no crashes
		if(min==max) return max;
		else if(min<0) return 0;
		
		// If the value is in the middle, found the exact value
		if(keys.get(mid).getFreq() < value && keys.get(mid+1).getFreq() > value)
			return mid;
		
		// If the min and max are next to each other we found the value
		// min gets floored, so check
		if(max <= min+1){
			if(value > keys.get(mid+1).getFreq())
				return mid+1;
			else return mid;
		}
		
		// if value too big, search lower half
		if(keys.get(mid).getFreq() > value)
			return searchKeyIndex(value, min, mid-1);
		
		// if value too small search upper half
		else if(keys.get(mid).getFreq() < value)
			return searchKeyIndex(value, mid+1, max);
		
		System.out.println("bin search " +value + " " + mid);
		
		// If we got here they must be equal
		return mid;
	}
}
