package ai.factories;

import ai.engine.Evaluator;
import ai.engine.Factory;
import ai.evaluators.AlphaBetaEvaluator;
import client.game.Content;
import client.game.Move;
import client.game.Player.Color;
import java.util.List;

public class AlphaBetaFactory extends Factory {

  @Override
  public Evaluator createInstance(Color playerColor, Content[][] field, List<Move> moves) {
    return new AlphaBetaEvaluator(field, playerColor, moves);
  }

  @Override
  public String toString() {
    return "AlphaBeta";
  }
}
