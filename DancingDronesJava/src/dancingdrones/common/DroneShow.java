package dancingdrones.common;
import java.sql.Time;



public class DroneShow {
	private DroneCommand top;
	private DroneCommand root;
	private DroneCommand current;
	private int size;
	
	public DroneShow() {
		size = 0;
	}
	
	public void add(int time, byte[] command){
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
		private int timing;
		private byte[] command;
		private DroneCommand next;
		
		public DroneCommand(int time, byte[] command, DroneCommand next) {
			this.command = command;
			this.timing = time;
			this.next = next;
		}
	}
}
