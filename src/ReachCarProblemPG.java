import java.io.IOException;
import java.util.Random;

import rlpark.plugin.rltoys.envio.actions.Action;
import rlpark.plugin.rltoys.envio.actions.ActionArray;
import rlpark.plugin.rltoys.envio.observations.Legend;
import rlpark.plugin.rltoys.envio.rl.TRStep;
import rlpark.plugin.rltoys.math.ranges.Range;
import rlpark.plugin.rltoys.problems.ProblemBounded;
import rlpark.plugin.rltoys.problems.ProblemContinuousAction;
import rlpark.plugin.rltoys.problems.ProblemDiscreteAction;
import zephyr.plugin.core.api.monitoring.annotations.Monitor;

public class ReachCarProblemPG implements ProblemBounded, ProblemDiscreteAction, ProblemContinuousAction {
  static private final double MaxActionValue = 1.0;
  public static final ActionArray UP = new ActionArray(0.0, MaxActionValue);
  public static final ActionArray DOWN = new ActionArray(0.0, -MaxActionValue);
  public static final ActionArray NOSTEER = new ActionArray(MaxActionValue, 0.0);
  public static final ActionArray RIGHT = new ActionArray(MaxActionValue, MaxActionValue);
  public static final ActionArray LEFT = new ActionArray(-MaxActionValue, MaxActionValue);
  public static final ActionArray BRAKE = new ActionArray(-MaxActionValue, MaxActionValue);
  protected static final Action[] Actions = { UP,DOWN,LEFT,RIGHT, NOSTEER, BRAKE };
  static public final Range ActionRange = new Range(-MaxActionValue, MaxActionValue);

  public static final String VELOCITY = "velocity";
  public static final String POSITION = "position";
  public static final Legend legend = new Legend(POSITION, VELOCITY);

  protected Track track;
  protected Race race;
  protected String trackName;
  protected Vehicle car;
  @Monitor
  protected double positionX;
  @Monitor
  protected double positionY;
  @Monitor
  protected double velocityX = 0.0;
  @Monitor
  protected double velocityY = 0.0;
  @Monitor
  protected int sectionCount = 0;
  protected int maxSec = 0;
  protected int startingSection = 0;
  protected Range positionXRange = new Range(-1.2, 0.6);
  protected Range positionYRange = new Range(-1.2, 0.6);
  protected Range velocityXRange = new Range(-0.07, 0.07);
  protected Range velocityYRange = new Range(-0.07, 0.07);


  private final Random random;
  private TRStep step;
  private final int episodeLengthMax;

  public ReachCarProblemPG(Random random) {
    this(random, -1);
  }

  public ReachCarProblemPG(Random random, int episodeLengthMax) {
    this(random, episodeLengthMax, "donington.track");
  }
  
  public ReachCarProblemPG(Random random, int episodeLengthMax, String trackString) {
	    this.random = random;
	    this.episodeLengthMax = episodeLengthMax;
	    this.trackName = trackString;
        this.track = new Track();
        java.io.Reader in;
		try {
			in = new java.io.BufferedReader( new java.io.FileReader(this.trackName) );
			track.load( in );
			track.calcDimensions();
			positionXRange = new Range(0.0,(double)track.getWidth());
			positionYRange = new Range(0.0,(double)track.getHeight());
			velocityXRange = new Range(-20.0,20.0);
			velocityYRange = new Range(-20.0,20.0);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		

  }

  protected void update(ActionArray action) {
      this.race.integrate(0.05f);
      if(action.equals(DOWN)) {
    	  this.car.setMotorOn( false );
    	  this.car.reverse(true);
      }
      else if(action.equals(UP)) {
    	  this.car.setMotorOn( true );
    	  this.car.reverse(false); 	  
      }
      else if(action.equals(LEFT)) {
    	  this.car.steerLeft(true);
    	  this.car.steerRight(false);

      }
      if(action.equals(NOSTEER)) {
    	  this.car.steerLeft(false);
    	  this.car.steerRight(false);
      }
      else if(action.equals(RIGHT)) {
    	  this.car.steerLeft(false);
    	  this.car.steerRight(true);
    	  
      if(action.equals(BRAKE)) {
    	  this.car.setMotorOn( false );
    	  this.car.reverse( false );
      }
    	  
     }
    positionX = this.car.currentState().x;
  	positionY = this.car.currentState().y;
  	velocityX = this.car.currentState().vx;
  	velocityY = this.car.currentState().vy;
  	this.sectionCount = this.race.getCurrentSections()[0];
  }

  @Override
  public TRStep step(Action action) {
	int totalSecCount = this.track.sections.length;
	int numOfLaps = this.race.getTrackCounts()[0];
	int oldSec = this.sectionCount + numOfLaps*totalSecCount;
	int nextSection = (this.sectionCount + 1) % totalSecCount;
	
	double oldDist = this.track.sections[nextSection].distanceToEnd(this.car);
	double oldPosX = positionX;
	update((ActionArray) action);
	
	double newDist = this.track.sections[nextSection].distanceToEnd(this.car);
	double newPosX = positionX;
	int newSec = this.sectionCount + numOfLaps*totalSecCount;
    double reward = ((double)(newSec-oldSec))*1500.0 + (numOfLaps>0?20000.0:0.0) - (newSec - oldSec!=0?1.0:(newDist - oldDist>0?0.5:1.0));
	step = new TRStep(step, action, new double[] { this.car.currentState().x, this.car.currentState().y, this.car.currentState().vx, this.car.currentState().vy }, reward);
    if (isGoalReached())
      forceEndEpisode();
    return step;
  }

  @Override
  public TRStep forceEndEpisode() {
    step = step.createEndingStep();
    return step;
  }

  private boolean isGoalReached() {
    return  this.race.getTrackCounts()[0]>0 || (episodeLengthMax > 0 && step != null && step.time > episodeLengthMax);
  }

  @Override
  public TRStep initialize() {
	this.car = new Vehicle(0, 0);
	Vehicle[] varr = {this.car};
		
	
	  
	
	
	this.race = new Race(track, varr);
	if (random == null) {
		   this.track.toStartingPositions(varr);
	    } else {
	    	this.startingSection = random.nextInt(this.track.sections.length);
	      this.track.toOffsetPositions(varr, this.startingSection);
	      this.startingSection = this.race.getCurrentSections()[0];
	    } 
	positionX = this.car.currentState().x;
	positionY = this.car.currentState().y;
	velocityX = this.car.currentState().vx;
	velocityY = this.car.currentState().vy;
    step = new TRStep(new double[] { this.car.currentState().x, this.car.currentState().y, this.car.currentState().vx, this.car.currentState().vy }, -1);
    return step;
  }

  @Override
  public Legend legend() {
    return legend;
  }

  @Override
  public Action[] actions() {
    return Actions;
  }


  @Override
  public Range[] getObservationRanges() {
    return new Range[] { positionXRange,positionYRange,velocityXRange, velocityYRange };
  }
  
  public Range[] getPositionRanges() {
    return new Range[] { positionXRange,positionYRange };
  }

  @Override
  public Range[] actionRanges() {
    return new Range[] { ActionRange, ActionRange };
  }

  @Override
  public TRStep lastStep() {
    return step;
  }

  static public double height(double position) {
    return Math.sin(3.0 * position);
  }
}