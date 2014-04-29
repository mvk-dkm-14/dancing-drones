package dancingdrones.webClient;

import java.util.LinkedList;

// Model 
public class WebClient {
	LinkedList<Object> listeners = new LinkedList<Object>();
	LinkedList<InstructionSet> instructionSets = new LinkedList<InstructionSet>();

	/* ADD AND REMOVE INSTRUCTION SETS */

	public void addInstructionSet(String title, int[] drones) {
		this.instructionSets.push(new InstructionSet(title, drones));
	}

	public InstructionSet getInstructionSet(int id) {
		for(int i = 0; i < this.instructionSets.size(); i++) {
			if(this.instructionSets.get(i).getId() == id) {
				return this.instructionSets.get(i);
			}
		}
		return null;
	}
	
	public void removeInstructionSet(int id) {
		for(int i = 0; i < instructionSets.size(); i++) {
			if(instructionSets.get(i).getId() == id) {
//				instructionSets .get(i) = null;
			}
		}
	}

	/* SEND INSTRUCTION SET TO SERVER */

	public void sendInstructionSet(int id) {
		//TODO
	}

	/* CLEARING MODEL */
	public void clearModel() {
		this.instructionSets.clear();
		this.notifyObservers("");

		//TODO MORE
	}

	//*** OBSERVABLE PATTERN ***
	

	public void notifyObservers(String args) {
	    for (int i = 0; i < listeners.size(); i++) {
//		        listeners.get(i).update(args);
	    }
	}

	public void addObserver(Object listener) {
	    listeners.add(listener);
	};
	//*** END OBSERVABLE PATTERN ***
	
}

