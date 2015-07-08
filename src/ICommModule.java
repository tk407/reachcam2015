public interface ICommModule {

	public static class State {
		public float x;
		public float vx;
		public float y;
		public float vy;
		public float angle;
		public float angularVelocity;
		public boolean updated = false;
	}
	
	void setRacingGame(RacingGame racingGame);

	void reset();

	void start();

	void stop();

	int getAssignedID();

	void sendNewClientDetails(String address, int port);

	void sendCommand(String command);

	String getTrackName();

	void nextTrack();

	void stopWaiting();

	String listenForCommand();

	State[] getVehicleStates();

	/** Broadcast the state of a vehicle to all of the other players. **/

	void broadcastState(Vehicle vehicle, Body.State state);

	boolean ready();

	byte getStatus();

	void receiveState();

}