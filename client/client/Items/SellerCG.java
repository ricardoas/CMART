package client.Items;

public class SellerCG {
	private long id;
	private String name;
	private long rating;

	public SellerCG(){
	}
	
	public SellerCG(long id, String name, long rating){
		this.id=id;
		this.name=name;
		this.rating=rating;
	}

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the rating
	 */
	public long getRating() {
		return rating;
	}

	/**
	 * @param rating the rating to set
	 */
	public void setRating(long rating) {
		this.rating = rating;
	}


}
