package client.Items;

import java.text.ParseException;
import java.util.Date;

import org.codehaus.jackson.JsonNode;

import client.Tools.DateParser;

public class QuestionCG {
	private long id;
	private long fromUserID;
	private long toUserID;
	private long itemID;
	private boolean isQuestion;
	private Date postDate;
	private long responseTo;
	private String content;

	public QuestionCG(JsonNode node) throws ParseException {
		this.id=node.get("id").getLongValue();
		this.fromUserID = node.get("fromUserID").getLongValue();
		this.toUserID = node.get("toUserID").getLongValue();
		this.itemID=node.get("itemID").getLongValue();
		this.isQuestion = node.get("isQuestion").getBooleanValue();
		this.postDate =DateParser.stringToDate(node.get("postDate").getTextValue());
		this.responseTo=node.get("responseTo").getLongValue();
		this.content=node.get("content").getTextValue();

	}

	public long getId() {
		return this.id;
	}

	public long getFromUserID() {
		return this.fromUserID;
	}

	public long getToUserID() {
		return this.toUserID;
	}

	public long getItemID() {
		return this.itemID;
	}

	public boolean isQuestion() {
		return this.isQuestion;
	}

	public Date getPostDate() {
		return this.postDate;
	}

	public String getContent() {
		return this.content;
	}

	public long getResponseTo() {
		return this.responseTo;
	}

}
