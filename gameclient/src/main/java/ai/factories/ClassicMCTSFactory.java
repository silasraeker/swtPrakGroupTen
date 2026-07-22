package ai.factories;

import ai.engine.Evaluator;
import ai.engine.Factory;
import ai.evaluators.ClassicMCTSEvaluator;
import client.game.Content;
import client.game.Move;
import client.game.Player.Color;
import java.util.List;

/**
 * Factory for creating Monte Carlo Tree Search (MCTS) evaluators. Stores the depth calculator used
 * to determine the search depth.
 */
public class ClassicMCTSFactory extends Factory {



  public Evaluator createInstance(Color playerColor, Content[][] field, List<Move> moves) {
    return new ClassicMCTSEvaluator(field, playerColor, moves);
  }

  @Override
  public String toString() {
    return "Classic MCTS";
  }
}
