package dancingdrones.common;
import java.sql.Time;



public class DroneShow {
	private DroneCommand top;
	private DroneCommand root;
	private DroneCommand current;
	private int size;
	
	public DroneShow() {
		super();
		size = 0;
	}
	
	public void add(Time time, Protocol.Value command){
		root = new DroneCommand(time, command, top);
		if( size == 0){
			current = top;
			root = top;
		}
		size++;
	}

	public void reset(){
		current = root;
	}
	
	public DroneCommand next(){
		DroneCommand tmp = current;
		current = current.next;
		return tmp;
	}
	public class DroneCommand {
		private Time timing;
		private Protocol.Value command;
		private DroneCommand next;
		
		public DroneCommand(Time timing, Protocol.Value command, DroneCommand next) {
			this.command = command;
			this.timing = timing;
			this.next = next;
		}
	}
}
