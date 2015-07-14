
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Random;

import rlpark.plugin.rltoys.agents.functions.FunctionProjected2D;
import rlpark.plugin.rltoys.agents.functions.ValueFunction2D;
import rlpark.plugin.rltoys.algorithms.control.acting.EpsilonGreedy;
import rlpark.plugin.rltoys.algorithms.control.acting.Greedy;
import rlpark.plugin.rltoys.algorithms.control.sarsa.Sarsa;
import rlpark.plugin.rltoys.algorithms.control.sarsa.SarsaControl;
import rlpark.plugin.rltoys.algorithms.functions.stateactions.TabularAction;
import rlpark.plugin.rltoys.algorithms.representations.tilescoding.TileCodersNoHashing;
import rlpark.plugin.rltoys.algorithms.traces.RTraces;
import rlpark.plugin.rltoys.envio.actions.Action;
import rlpark.plugin.rltoys.envio.policy.Policy;
import rlpark.plugin.rltoys.envio.rl.TRStep;
import rlpark.plugin.rltoys.math.vector.BinaryVector;
import rlpark.plugin.rltoys.math.vector.RealVector;
import rlpark.plugin.rltoys.math.vector.implementations.Vectors;
import rlpark.plugin.rltoys.problems.mountaincar.MountainCar;
import zephyr.plugin.core.api.Zephyr;
import zephyr.plugin.core.api.monitoring.annotations.Monitor;
import zephyr.plugin.core.api.synchronization.Clock;

@Monitor
public class ReachCarSarsaPG implements Runnable {
  //final FunctionProjected2D valueFunctionDisplay;
  private final ReachCarProblem problem;
  private final SarsaControl control;
  private final Sarsa sarsa;
  private final TileCodersNoHashing projector;
  private final Clock clock = new Clock("SarsaMountainCar");

  public ReachCarSarsaPG() {
    problem = new ReachCarProblem(null,90000);
    projector = new TileCodersNoHashing(problem.getObservationRanges());
    projector.addFullTilings(10, 600);
    projector.includeActiveFeature();
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
      BinaryVector x_tp1 = projector.project(step.o_tp1);
      Action action = control.step(x_t, step.a_t, x_tp1, step.r_tp1);
      x_t = Vectors.bufferedCopy(x_tp1, x_t);
      if (step.isEpisodeEnding()) {
        System.out.println(String.format("Episode %d: %d steps", nbEpisode, step.time));
        step = problem.initialize();
        x_t = null;
        if(nbEpisode == 5){
        	
        	try{
        		// Serialize data object to a file
        		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("reachcarsarsapolicy.ser"));
        		out.writeObject(control.acting());
        		out.close();
        		
        		 out = new ObjectOutputStream(new FileOutputStream("reachcarsarsaproblem.ser"));
        		out.writeObject(control.acting());
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
    new ReachCarSarsaPG().run();
  }
}
