package ai.factories;

import ai.engine.Evaluator;
import ai.engine.Factory;
import ai.evaluators.MCTSEvaluator;
import client.game.Content;
import client.game.Move;
import client.game.Player.Color;
import java.util.List;

public class MCTSFactory extends Factory {

  @Override
  public Evaluator createInstance(Color playerColor, Content[][] field, List<Move> moves) {
    return new MCTSEvaluator(field, playerColor, moves);
  }

  @Override
  public String toString() {
    return "MCTS";
  }
}
