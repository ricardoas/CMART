package client.Items;

import org.codehaus.jackson.JsonNode;

public class SellerCG {
	private long id;
	private String name;
	private long rating;

	public SellerCG(JsonNode node) {
		this.id = node.get("id").getLongValue();
		this.name = node.get("name").getTextValue();
		this.rating = node.get("rating").getLongValue();
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
