package client.Items;

import org.codehaus.jackson.JsonNode;

public class SellerCG {
	private long id;
	private String name;
	private long rating;

	public SellerCG(JsonNode node) {
		this.id = node.get("seller").get("id").getLongValue();
		this.name = node.get("seller").get("name").getTextValue();
		this.rating = node.get("seller").get("rating").getLongValue();
	}

	/**
	 * @return the id
	 */
	public long getId() {
		return this.id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @return the rating
	 */
	public long getRating() {
		return this.rating;
	}

}
