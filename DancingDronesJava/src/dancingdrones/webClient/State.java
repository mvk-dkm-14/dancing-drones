package dancingdrones.webClient;

public class State {
	int id;
	int height;
	String comment;
	
	public State(int height, String comment) {
		this.height = height;
		this.comment = comment;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getId() {
		return this.id;
	}
	
	public void setheight(int height) {
		this.height = height;
	}
	
	public int getHeight() {
		return this.height;
	}
	
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public String getComment() {
		return this.comment;
	}
	
}
