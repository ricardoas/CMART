package client.Tools;

public class PageTimePair {
	StringBuilder page;
	Stopwatch sw;

	public PageTimePair(){

	}
	public PageTimePair(StringBuilder page,Stopwatch sw){
		this.page=page;
		this.sw=sw;
	}

	public void setPage(StringBuilder page){
		this.page=new StringBuilder(page);
	}

	public StringBuilder getPage(){
		return new StringBuilder(page);
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
