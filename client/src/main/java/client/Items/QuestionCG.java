package client.Items;

import java.util.Date;

public class QuestionCG {
	private long id;
	private long fromUserID;
	private long toUserID;
	private long itemID;
	private boolean isQuestion;
	private Date postDate;
	private long responseTo;
	private String content;
	
	public QuestionCG(){
	}
	
	public QuestionCG(long id, long fromUserID, long toUserID, long itemID, boolean isQuestion, Date postDate, long responseTo, String content){
		this.id=id;
		this.fromUserID=fromUserID;
		this.toUserID=toUserID;
		this.itemID=itemID;
		this.isQuestion=isQuestion;
		this.postDate=postDate;
		this.setResponseTo(responseTo);
		this.content=content;				
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getFromUserID() {
		return fromUserID;
	}

	public void setFromUserID(long fromUserID) {
		this.fromUserID = fromUserID;
	}

	public long getToUserID() {
		return toUserID;
	}

	public void setToUserID(long toUserID) {
		this.toUserID = toUserID;
	}

	public long getItemID() {
		return itemID;
	}

	public void setItemID(long itemID) {
		this.itemID = itemID;
	}

	public boolean isQuestion() {
		return isQuestion;
	}

	public void setQuestion(boolean isQuestion) {
		this.isQuestion = isQuestion;
	}

	public Date getPostDate() {
		return postDate;
	}

	public void setPostDate(Date postDate) {
		this.postDate = postDate;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * @return the responseTo
	 */
	public long getResponseTo() {
		return responseTo;
	}

	/**
	 * @param responseTo the responseTo to set
	 */
	public void setResponseTo(long responseTo) {
		this.responseTo = responseTo;
	}
	
	
}
