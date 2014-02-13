package dancingdrones.server;

public class ARDrone implements Drone {
	private int id;
	private float targetHeight;
	private int group;
	
	public ARDrone(int id){
		this.id = id;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean setHeight(float m) {
		targetHeight = m;
		return true;
	}

	@Override
	public boolean setGroup(int group) {
		this.group = group;
		return true;
	}

	@Override
	public boolean start() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean stop() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean move(int forward, int right, int up) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int[] getMovement() {
		// TODO Auto-generated method stub
		return null;
	}

}
