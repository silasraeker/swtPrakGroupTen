package ai.factories;

import ai.engine.Evaluator;
import ai.engine.Factory;
import ai.evaluators.SimpleEvaluator;
import client.game.Content;
import client.game.Move;
import client.game.Player.Color;
import java.util.List;

/**
 * Factory that creates simple evaluators.
 */
public class SimpleFactory extends Factory {

  @Override
  public Evaluator createInstance(Color playerColor, Content[][] field, List<Move> moves) {
    return new SimpleEvaluator(super.positionEvaluator, field, playerColor, moves);
  }

  @Override
  public String toString() {
    return "Simple";
  }
}
