package ai.evaluators;
import ai.engine.Evaluator;
import client.game.Content;
import client.game.Move;
import client.game.Player.Color;
import ai.helper.EvaluatorHelper;
import java.util.ArrayList;
import java.util.List;

/**
 * Evaluator implementation using Monte Carlo Tree Search (MCTS).
 * Differs to MCTSEvaluator through using no heuristics, actually using a tree
 * and applying UCT (Upper Confidence Bound for Trees)
 */
public class ClassicMCTSEvaluator extends Evaluator {


  class Node {
    final Move move;              // move that led here (null for root)
    final Node parent;
    final Color colorToMove;      // whose turn it is AT this node
    final List<Node> children = new ArrayList<>();
    final List<Move> untriedMoves;
    static final double ROOTTWO = Math.sqrt(2);
    int visits = 0;
    double totalValue = 0.0;

    Node(Move move, Node parent, Color colorToMove, List<Move> untriedMoves) {
      this.move = move;
      this.parent = parent;
      this.colorToMove = colorToMove;
      this.untriedMoves = untriedMoves;
    }

    boolean isFullyExpanded() {
      return untriedMoves.isEmpty();
    }

    boolean isLeaf() {
      return children.isEmpty();
    }

    double uct() {
      if (visits == 0) return Double.POSITIVE_INFINITY;
      double exploitation = 1-(totalValue / visits);
      return exploitation + ROOTTWO * Math.sqrt(Math.log(parent.visits) / visits);
    }
  }
  /**
   * Creates a Classic MCTS evaluator.
   *
   * @param field             the position for which the moves are evaluated
   * @param playerColor       the color of the player whose turn it is
   * @param moves             the list of moves to evaluate
   */
  public ClassicMCTSEvaluator(Content[][] field, Color playerColor, List<Move> moves) {
    super((f)->500, field, playerColor, moves);
  }

  /**
   * Selects the Child of the node with the maximal UCT value
   * @param node which children are to be looked at
   * @return child with maximal UCT value
   */
  public Node selectBestChild(Node node) {
    Node currentNode = null;
    double eval = Double.NEGATIVE_INFINITY;
    for  (Node child : node.children) {
      if (child.uct()>eval){
        eval = child.uct();
        currentNode = child;
      }
    }
    return currentNode;
  }

  public double rollout (Color playerColor) {
    Content[][] fieldCopy = EvaluatorHelper.copyField(this.field);
    Color currentColor = playerColor;
    while (EvaluatorHelper.countAllMovesAndArrows(fieldCopy, currentColor) > 0) {
      EvaluatorHelper.makeMove(fieldCopy, EvaluatorHelper.getRandomMove(fieldCopy, currentColor));
      currentColor = (currentColor == Color.BLACK)? Color.WHITE : Color.BLACK;

    }
    if (currentColor == playerColor) {
      return 0;
    }
    else {
      return 1;
    }
  }

  @Override
  public void run() {
    Node root = new Node(null, null, this.playerColor, new ArrayList<>(this.moves));
    root.visits = 1;
    while (!this.isInterrupted()) {
      Node node = root;
      // Selection
      while (node.isFullyExpanded() && !node.isLeaf()) {
        node = selectBestChild(node);
        EvaluatorHelper.makeMove(this.field, node.move);
      }

      // Expansion
      if (!node.isFullyExpanded()) {
        int toBeRemoved = (int) (Math.random() * node.untriedMoves.size());
        Move m = node.untriedMoves.remove(toBeRemoved);
        EvaluatorHelper.makeMove(this.field, m);

        Color nextColor = (node.colorToMove == Color.WHITE) ? Color.BLACK : Color.WHITE;
        List<Move> childMoves = EvaluatorHelper.getAllPossibleMoves(this.field, nextColor);
        Node child = new Node(m, node, nextColor, childMoves);
        node.children.add(child);
        node = child;
      }

      // Simulation (rollout) from nodes position
      double result = rollout(node.colorToMove);

      // Backpropagation and undo moves back to root
      Node cursor = node;
      while (cursor != root) {
        cursor.visits++;
        cursor.totalValue += (cursor.colorToMove == node.colorToMove) ? result : (1-result);
        EvaluatorHelper.reverseMove(this.field, cursor.move);
        cursor = cursor.parent;
      }
      root.visits++;
      root.totalValue += (root.colorToMove == node.colorToMove) ? result : (1-result);
      // report current best estimate for root's children
      for (Node child : root.children) {
          double value = child.totalValue / Math.max(child.visits, 1);
          value = this.playerColor == Color.WHITE ? 1 - value : value;
          this.updateEvaluation(
                  child.move, (int) (1000*value));}
      }
  }
}
