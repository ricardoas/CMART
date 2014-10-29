package client.Pages;

import java.lang.reflect.InvocationTargetException;

public enum PageType {

	NONE("null", Page.class),
	LOGIN_PAGE_NUM("Login", LoginPage.class),
	REGISTER_PAGE_NUM("Register User", RegisterUserPage.class),
	ITEM_PAGE_NUM("View Item", ItemPage.class),
	MYACCOUNT_PAGE_NUM("My Account", MyAccountPage.class),
	UPDATEUSER_PAGE_NUM("Update User Details", UpdateUserPage.class),
	SELLITEM_PAGE_NUM("Sell Item", SellItemPage.class),
	BIDCONFIRM_PAGE_NUM("Bid Confirmed", BidConfirmPage.class),
	SELLITEMCONFIRM_PAGE_NUM("Sell Item Confirmed", SellItemConfirmPage.class),
	BROWSE_PAGE_NUM("Browse Category", BrowsePage.class),
	BUYITEM_PAGE_NUM("Buy Item", BuyItemPage.class),
	UPLOADIMAGES_PAGE_NUM("Upload images", UploadImagesPage.class),
	VIEWUSER_PAGE_NUM("View User", ViewUserPage.class),
	CONFIRMBUY_PAGE_NUM("Buy Confirmed", ConfirmBuyPage.class),
	LOGOUT_PAGE_NUM("Logout", LogOutPage.class),
	HOME_PAGE_NUM("Welcome to CMART", HomePage.class),
	SEARCH_PAGE_NUM("Search", SearchPage.class),
	BIDHISTORY_PAGE_NUM("Bid History", BidHistoryPage.class),
	ASKQUESTION_PAGE_NUM("Ask Question", AskQuestionPage.class),
	LEAVECOMMENT_PAGE_NUM("Leave Comment", LeaveCommentPage.class),
	CONFIRMCOMMENT_PAGE_NUM("Confirm Comment", ConfirmCommentPage.class),
	ANSWERQUESTION_PAGE_NUM("Answer Question", AnswerQuestionPage.class);

	private String title;
	private Class<? extends Page> page;

	private PageType(String title, Class<? extends Page> page) {
		this.title = title;
		this.page = page;
	}

	public int getCode(){
		return this.ordinal();
	}

	public String getTitle() {
		return this.title;
	}
	
	public Page buildPage(Page page){
		try {
			return (Page) this.page.getConstructors()[0].newInstance(page);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| SecurityException e) {
			System.err.println("Exception on building page: ");
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public static PageType getBasedOnTitle(String title) {

		for (PageType type : values()) {
			if(type.getTitle().equals(title)) return type;
		}
		throw new IllegalArgumentException();
	}

}
