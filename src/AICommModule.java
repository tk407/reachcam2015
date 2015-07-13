import java.util.LinkedList;

import rlpark.plugin.rltoys.envio.policy.Policy;

/**
 * 
 */

/**
 * @author tamas.kispeter
 *
 */
public class AICommModule implements ICommModule {

	
	private LinkedList<Policy> AIPolicies;
	private LinkedList<Vehicle> Vehicles;
	private RacingGame game;
	/* (non-Javadoc)
	 * @see ICommModule#setRacingGame(RacingGame)
	 */
	@Override
	public void setRacingGame(RacingGame racingGame) {
		// TODO Auto-generated method stub
        game = racingGame;
	}

	public void registerPolicy(Policy p){
		AIPolicies.add(p);
		reset();
		
	}
	/* (non-Javadoc)
	 * @see ICommModule#reset()
	 */
	@Override
	public void reset() {
		// TODO Auto-generated method stub
        Vehicles = new LinkedList<Vehicle>();
        for(byte i = 0; i < (byte)AIPolicies.size(); i++){
        	
        	Vehicles.add(new Vehicle(i, 0, 0));
        }
        Vehicles.add(new Vehicle((byte)AIPolicies.size(), 0, 0));
        
	}

	/* (non-Javadoc)
	 * @see ICommModule#start()
	 */
	@Override
	public void start() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see ICommModule#stop()
	 */
	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see ICommModule#getAssignedID()
	 */
	@Override
	public int getAssignedID() {
		// TODO Auto-generated method stub
		return AIPolicies.size();
	}

	/* (non-Javadoc)
	 * @see ICommModule#sendNewClientDetails(java.lang.String, int)
	 */
	@Override
	public void sendNewClientDetails(String address, int port) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see ICommModule#sendCommand(java.lang.String)
	 */
	@Override
	public void sendCommand(String command) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see ICommModule#getTrackName()
	 */
	@Override
	public String getTrackName() {
		// TODO Auto-generated method stub
		return "circle.track";
	}

	/* (non-Javadoc)
	 * @see ICommModule#nextTrack()
	 */
	@Override
	public void nextTrack() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see ICommModule#stopWaiting()
	 */
	@Override
	public void stopWaiting() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see ICommModule#listenForCommand()
	 */
	@Override
	public String listenForCommand() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see ICommModule#getVehicleStates()
	 */
	@Override
	public State[] getVehicleStates() {
		// TODO Auto-generated method stub
		
		State[] states = new State[this.Vehicles.size()];
		
		return states;
	}

	/* (non-Javadoc)
	 * @see ICommModule#broadcastState(Vehicle, Body.State)
	 */
	@Override
	public void broadcastState(Vehicle vehicle, Body.State state) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see ICommModule#ready()
	 */
	@Override
	public boolean ready() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see ICommModule#getStatus()
	 */		
	@Override
	public byte getStatus() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see ICommModule#receiveState()
	 */
	@Override
	public void receiveState() {
		// TODO Auto-generated method stub

	}

}
