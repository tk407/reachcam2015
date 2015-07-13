
import java.util.Random;

import rlpark.plugin.rltoys.agents.functions.FunctionProjected2D;
import rlpark.plugin.rltoys.agents.functions.ValueFunction2D;
import rlpark.plugin.rltoys.agents.rl.LearnerAgentFA;
import rlpark.plugin.rltoys.algorithms.control.acting.EpsilonGreedy;
import rlpark.plugin.rltoys.algorithms.control.qlearning.QLearning;
import rlpark.plugin.rltoys.algorithms.control.qlearning.QLearningControl;
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
import rlpark.plugin.rltoys.math.vector.implementations.PVector;
import rlpark.plugin.rltoys.math.vector.implementations.Vectors;
import rlpark.plugin.rltoys.problems.mountaincar.MountainCar;
import zephyr.plugin.core.api.Zephyr;
import zephyr.plugin.core.api.monitoring.annotations.Monitor;
import zephyr.plugin.core.api.synchronization.Clock;

@Monitor
public class ReachCarQ implements Runnable {
  final FunctionProjected2D valueFunctionDisplay;
  private final ReachCarProblem problem;
  private final PVector occupancy;
  private final LearnerAgentFA agent;
  private final QLearningControl control;
  private final TileCodersNoHashing projector;
  private final Clock clock = new Clock("SarsaMountainCar");

  public ReachCarQ() {
    problem = new ReachCarProblem(null,90000);
    projector = new TileCodersNoHashing(problem.getObservationRanges());
    projector.addFullTilings(10, 600);
    projector.includeActiveFeature();
    occupancy = new PVector(projector.vectorSize());
    TabularAction toStateAction = new TabularAction(problem.actions(), projector.vectorNorm(), projector.vectorSize());
    double alpha = .15 / projector.vectorNorm();
    double gamma = 1.0;
    double lambda = 0.6;
    QLearning qlearning = new QLearning(problem.actions(), alpha, gamma, lambda, toStateAction, new RTraces());
    double epsilon = 0.3;
    Policy acting = new EpsilonGreedy(new Random(0), problem.actions(), toStateAction, qlearning, epsilon);
    control = new QLearningControl(acting, qlearning);
    agent = new LearnerAgentFA(control, projector);
    valueFunctionDisplay = new ValueFunction2D(projector, problem, qlearning);
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
        nbEpisode++;
      } else
        step = problem.step(action);
    }
  }

  public static void main(String[] args) {
    new ReachCarQ().run();
  }
}
