package ai.factories;

import ai.engine.Evaluator;
import ai.engine.Factory;
import ai.evaluators.RandomEvaluator;
import client.game.Content;
import client.game.Move;
import client.game.Player.Color;
import java.util.List;

public class RandomFactory extends Factory {

  @Override
  public Evaluator createInstance(Color playerColor, Content[][] field, List<Move> moves) {
    return new RandomEvaluator(field, playerColor, moves);
  }

  @Override
  public String toString() {
    return "Random";
  }
}
