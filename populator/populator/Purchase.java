package populator;

/**
 * A purchase of an item
 * @author andy
 *
 */
public class Purchase {
	private long id;
	private Item item;
	private int quantity;
	private float price;
	private Boolean paid;
	
	public Purchase(long id, Item item, int quantity, float price, Boolean paid){
		this.id = id;
		this.item = item;
		this.quantity = quantity;
		this.price = price;
		this.paid = paid;
	}
	
	public long getID(){
		return this.id;
	}
	
	public Item getItem(){
		return this.item;
	}
	
	public int getQuantity(){
		return this.quantity;
	}
	
	public float getPrice(){
		return this.price;
	}
	
	public Boolean getPaid(){
		return this.paid;
	}
}
