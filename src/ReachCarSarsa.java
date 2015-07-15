
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Random;

import rlpark.plugin.rltoys.agents.functions.FunctionProjected2D;
import rlpark.plugin.rltoys.agents.functions.ValueFunction2D;
import rlpark.plugin.rltoys.algorithms.control.acting.ControlPolicyAdapter;
import rlpark.plugin.rltoys.algorithms.control.acting.EpsilonGreedy;
import rlpark.plugin.rltoys.algorithms.control.acting.Greedy;
import rlpark.plugin.rltoys.algorithms.control.sarsa.Sarsa;
import rlpark.plugin.rltoys.algorithms.control.sarsa.SarsaControl;
import rlpark.plugin.rltoys.algorithms.functions.stateactions.TabularAction;
import rlpark.plugin.rltoys.algorithms.functions.states.Projector;
import rlpark.plugin.rltoys.algorithms.representations.discretizer.partitions.AbstractPartitionFactory;
import rlpark.plugin.rltoys.algorithms.representations.discretizer.partitions.BoundedSmallPartitionFactory;
import rlpark.plugin.rltoys.algorithms.representations.tilescoding.TileCoders;
import rlpark.plugin.rltoys.algorithms.representations.tilescoding.TileCodersHashing;
import rlpark.plugin.rltoys.algorithms.representations.tilescoding.TileCodersNoHashing;
import rlpark.plugin.rltoys.algorithms.representations.tilescoding.hashing.Hashing;
import rlpark.plugin.rltoys.algorithms.representations.tilescoding.hashing.MurmurHashing;
import rlpark.plugin.rltoys.algorithms.traces.RTraces;
import rlpark.plugin.rltoys.envio.actions.Action;
import rlpark.plugin.rltoys.envio.policy.Policy;
import rlpark.plugin.rltoys.envio.rl.TRStep;
import rlpark.plugin.rltoys.math.ranges.Range;
import rlpark.plugin.rltoys.math.vector.BinaryVector;
import rlpark.plugin.rltoys.math.vector.RealVector;
import rlpark.plugin.rltoys.math.vector.implementations.Vectors;
import rlpark.plugin.rltoys.problems.ProblemBounded;
import rlpark.plugin.rltoys.problems.mountaincar.MountainCar;
import rlpark.plugin.rltoys.problems.puddleworld.PuddleWorld;
import zephyr.plugin.core.api.Zephyr;
import zephyr.plugin.core.api.monitoring.annotations.Monitor;
import zephyr.plugin.core.api.synchronization.Clock;

@Monitor
public class ReachCarSarsa implements Runnable {
  //final FunctionProjected2D valueFunctionDisplay;
  private final ReachCarProblem problem;
  private final SarsaControl control;
  private final Sarsa sarsa;
  private final Projector projector;
  private final Clock clock = new Clock("SarsaMountainCar");

  static private Hashing createHashing(Random random) {
	    return new MurmurHashing(random, 1000000);
	  }

	  static private void setTileCoders(TileCoders projector) {
		projector.addFullTilings(8, 700);
	    projector.includeActiveFeature();
	  }

	  static private AbstractPartitionFactory createPartitionFactory(Random random, Range[] observationRanges) {
	    AbstractPartitionFactory partitionFactory = new BoundedSmallPartitionFactory(observationRanges);
	    partitionFactory.setRandom(random, .2);
	    return partitionFactory;
	  }

	  static public Projector createProjector(Random random, ReachCarProblem problem) {
	    final Range[] observationRanges = ((ProblemBounded) problem).getObservationRanges();
	    final AbstractPartitionFactory discretizerFactory = createPartitionFactory(random, observationRanges);
	    Hashing hashing = createHashing(random);
	    TileCodersHashing projector = new TileCodersHashing(hashing, discretizerFactory, observationRanges.length);
	    setTileCoders(projector);
	    return projector;
	  }
  
  public ReachCarSarsa() {
	Random rand = new Random();
    problem = new ReachCarProblem(null,90000);
    projector = createProjector(rand,  problem);
    TabularAction toStateAction = new TabularAction(problem.actions(), projector.vectorNorm(), projector.vectorSize());
    toStateAction.includeActiveFeature();
    double alpha = .15 / projector.vectorNorm();
    double gamma = 1.0;
    double lambda = .95;
    sarsa = new Sarsa(alpha, gamma, lambda, toStateAction.vectorSize(), new RTraces());
    double epsilon = 0.01;
    Policy acting = new EpsilonGreedy(new Random(0), problem.actions(), toStateAction, sarsa, epsilon);
    control = new SarsaControl(acting, toStateAction, sarsa);
    //valueFunctionDisplay = new ValueFunction2D(projector, problem, sarsa);
    Zephyr.advertise(clock, this);
  }

  @Override
  public void run() {
    TRStep step = problem.initialize();
    int nbEpisode = 0;
    RealVector x_t = null;
    while (clock.tick()) {
      RealVector x_tp1 = projector.project(step.o_tp1);
      Action action = control.step(x_t, step.a_t, x_tp1, step.r_tp1);
      x_t = Vectors.bufferedCopy(x_tp1, x_t);
      if (step.isEpisodeEnding()) {
        System.out.println(String.format("Episode %d: %d steps", nbEpisode, step.time));
        step = problem.initialize();
        x_t = null;
        if(nbEpisode == 4){
        	
        	try{
        		// Serialize data object to a file
        		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("reachcarsarsapolicy.ser"));
        		out.writeObject(control.acting());
        		out.close();
        		
        		 out = new ObjectOutputStream(new FileOutputStream("reachcarsarsaprojector.ser"));
        		out.writeObject(projector);
        		out.close();
        		} catch (IOException e) {
        			e.printStackTrace();
        		}
        	
        	
        }
        nbEpisode++;
      } else
        step = problem.step(action);
    }
  }

  public static void main(String[] args) {
    new ReachCarSarsa().run();
  }
}
