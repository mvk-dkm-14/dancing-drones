package dancingdrones.webClient;

import java.util.LinkedList;

public class InstructionSet {
	int id;
	String title;
	int drones[];
	int length;
	LinkedList<State> states = new LinkedList<State>(); // array of states
	
	public InstructionSet(String title, int[] drones) {
		this.title = title;
		this.drones = drones;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getId() {
		return this.id;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getTitle() {
		return this.title;
	}
	
	public void setDrones(int[] drones) {
		this.drones = drones;
	}
	
	public int[] getDrones() {
		return this.drones;
	}
	
	public void setLength(int seconds) {
		for(int i = 0; i < seconds; i++) {
			states.add(new State(5, ""));
		}
	}
	
	public int getLength(int seconds) {
		return (this.states.size()-1) / 2;
	}
		
}
