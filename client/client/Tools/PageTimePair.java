package client.Tools;

public class PageTimePair {
	StringBuffer page;
	Stopwatch sw;

	public PageTimePair(){

	}
	public PageTimePair(StringBuffer page,Stopwatch sw){
		this.page=page;
		this.sw=sw;
	}

	public void setPage(StringBuffer page){
		this.page=new StringBuffer(page);
	}

	public StringBuffer getPage(){
		return new StringBuffer(page);
	}

	public void newSw(){
		this.sw=new Stopwatch();
	}

	public void setSw(Stopwatch sw){
		this.sw=sw;
	}

	public Stopwatch getSw(){
		return this.sw;
	}
}
