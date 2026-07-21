package ai.factories;

import ai.engine.Evaluator;
import ai.engine.Factory;
import ai.evaluators.AlphaBetaEvaluator;
import ai.helper.EvaluatorHelper;
import client.game.Content;
import client.game.Move;
import client.game.Player.Color;
import java.util.List;

public class AlphaBetaFactory extends Factory {

  /**
   * Creates a factory using the default position evaluator 'voroni'
   *
   */
  public AlphaBetaFactory() {
    super(EvaluatorHelper::voronoi);
  }

  @Override
  public Evaluator createInstance(Color playerColor, Content[][] field, List<Move> moves) {
    return new AlphaBetaEvaluator(super.positionEvaluator, field, playerColor, moves);
  }

  @Override
  public String toString() {
    return "AlphaBeta";
  }
}
