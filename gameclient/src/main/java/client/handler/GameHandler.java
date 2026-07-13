package client.handler;

import com.sun.net.httpserver.HttpExchange;

import ai.engine.KIClient;
import ai.engine.KIClient.Difficulty;
import client.dto.ServerGameMove;
import client.dto.ServerGameState;
import client.exception.InvalidDataException;
import client.game.Content;
import client.game.Move;
import client.game.Player;
import client.util.NetworkUtil;
import java.io.IOException;
import java.util.concurrent.ExecutionException;



/**
 * HTTP handler for game-related endpoints. Routes incoming requests to the matching
 * action handler and forwards them to the gameserver or downstream services.
 */
public class GameHandler extends Handler {

    private record Route(String method, String[] path) {}
    private KIClient ai = new KIClient(Difficulty.HARD);

    private volatile int calculationTimeMs = 5000;
    private volatile int calculationTimeBufferMs = 1000;

    /**
     * Routes the incoming HTTP request based on method and URI path to the matching
     * action handler. Any exception raised by an action handler is caught here and
     * answered with HTTP 500.
     *
     * @param req The HTTP exchange to handle
     */
    @Override
    public void handle(HttpExchange req) {
        try {
            String method = req.getRequestMethod();
            String[] path = req.getRequestURI()
                .getPath()
                .substring("/game/".length())
                .split("/");

            Route route = new Route(method, path);

            switch (route) {

                // (POST, [echo])
                case Route r when route.method.equals("POST")   && route.path.length == 1   && "echo".equals(route.path[0])
                    -> handlePOSTEcho(req);

                // (POST, [staticai])
                case Route r when route.method.equals("POST")   && route.path.length == 1   && "staticai".equals(route.path[0])
                    -> handlePOSTGameState(req);

                default -> handleInvalidRequest(req, route);
            }
        }
        catch (Exception e) {

            logger.warn("An exception occured: method = {}; targetURI = {}; exception = {}:{}:{}",
                req.getRequestMethod(), req.getRequestURI(), e.getClass().getName(), e.getMessage(), e.getStackTrace());

            try {
                NetworkUtil.respond(req, 500, "Internal Server Error");
            }
            catch (IOException io) {
                logger.warn("Failed responding with Internal Server Error: exception = {}", io);
            }
        }
        finally {
            req.close();
        }
    }


    private void handlePOSTEcho(HttpExchange req)
        throws IOException, InterruptedException, ExecutionException {

        try {
            String rec = NetworkUtil.deserializeBody(req, String.class);
            NetworkUtil.respond(req, 200, rec);
            logger.debug("Received data: data = {}", rec);
        }
        catch (Exception e) {
            logger.warn("Exception: {}:{}:{}", e, e.getMessage(), e.getStackTrace());
        }
    }


    /**
     * Handles a POST /game/opponent request: deserializes the current game state
     * and forwards it to the frontend endpoint.
     *
     * @param req The HTTP exchange to handle
     * @throws IOException          if reading the request body or sending the outgoing request fails
     * @throws InterruptedException if the outgoing request is interrupted
     */
    private void handlePOSTGameState(HttpExchange req)
        throws IOException, InterruptedException, ExecutionException {

        try {
            ServerGameState recGameState = NetworkUtil.deserializeBody(req, ServerGameState.class);
            byte[][] updatedBoard = recGameState.board();
            Player.Color playerColor = Player.Color.parseByte(recGameState.player());

            logger.trace("Received game state from server: playerToMove = {}, board = {}",
                          playerColor, updatedBoard);

            Content[][] board = new Content[updatedBoard.length][updatedBoard.length];
            logger.trace("Deserializing board.");
            for (byte i = 0; i < updatedBoard.length; i++) {
                for (byte j = 0; j < updatedBoard.length; j++) {
                    byte value = updatedBoard[i][j];
                    switch (value) {
                        case 0 -> board[i][j] = Content.WHITE_AMAZONE;
                        case 1 -> board[i][j] = Content.BLACK_AMAZONE;
                        case -2 -> board[i][j] = Content.ARROW;
                        case -1 -> board[i][j] = Content.EMPTY;
                        default -> {logger.warn("Error while deserializing board!");}
            }}}
            logger.trace("Updated board.");

            ai.startCalculating(board, 5, playerColor);
            Thread.sleep(calculationTimeMs - calculationTimeBufferMs);
            Move move = ai.getBestMove();
            String playerMoveJson = NetworkUtil.serializeObject(
                new ServerGameMove(
                    playerColor.code(),
                    new int[]{ move.start().x(), move.start().y() },
                    new int[]{ move.to().x(),    move.to().y()    },
                    new int[]{ move.arrow().x(), move.arrow().y() }
                ));

            NetworkUtil.respond(req, 200, playerMoveJson);
            ai.stopCalculating();
        }
        catch (InvalidDataException ide) {
            logger.info("Invalid data!");
            NetworkUtil.respond(req, 400, "Invalid data!");
        }
        catch (Exception e) { logger.warn("Exception: {}:{}:{}", e, e.getMessage(), e.getStackTrace()); }
    }

    /**
     * Responds with HTTP 404 for an unsupported request route.
     *
     * @param req   The HTTP exchange to respond to
     * @param route The unsupported route identifier (method + path)
     * @throws IOException if sending the response fails
     */
    private void handleInvalidRequest(HttpExchange req, Route route) throws IOException {
        logger.info("Invalid request route received: route = {} {}", route.method, route.path);
        NetworkUtil.respond(req, 404, "Request route not defined!");
    }

    public void setCalculationTimeMs(int calculationTimeMs) {
        this.calculationTimeMs = calculationTimeMs;
    }

    public void setCalculationTimeBufferMs(int calculationTimeBufferMs) {
        this.calculationTimeBufferMs = calculationTimeBufferMs;
    }
}
