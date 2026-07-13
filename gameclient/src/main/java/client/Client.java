package client;
import com.sun.net.httpserver.HttpServer;

import client.exception.ServerCommunicationException;
import client.handler.GameHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Scanner;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Entry point for the gameclient: validates URIs, verifies connectivity to the gameserver,
 * and starts a local HTTP server with request handlers.
 */
public class Client {

    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    /**
     * Starts the gameclient: validates both URIs, verifies the gameserver is reachable,
     * creates a local HTTP server and binds the game and debug handlers.
     *
     * @param clientURI The local URI under which this gameclient will be reachable
     * @param serverURI The URI of the gameserver to communicate with
     * @throws IllegalArgumentException if either URI fails validation
     * @throws IOException              if the local HTTP server cannot be created
     * @throws ServerCommunicationException if the gameserver is not reachable
     */
    public static void main() throws IOException {

        logger.trace("Creating Gameclient HttpServer.");
        HttpServer server = HttpServer.create(
                new InetSocketAddress("0.0.0.0", 8080), 0
        );
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());



        /* Handlers */
        logger.trace("Binding Handlers to Gameclient HttpServer.");

        // Game
        GameHandler gameHandler = new GameHandler();
        server.createContext( "/game/", gameHandler );

        logger.trace("Starting Gameclient HttpServer.");
        server.start();

        try (Scanner scanner = new Scanner(System.in);) {

            System.out.println("Format: <variable_name> <new_value>");

            while (scanner.hasNextLine()) {
                String input = scanner.nextLine();
                String[] parts = input.split(" ");
            
                if (parts.length == 1 && parts[0].equals("exit")) { break; }
                if (parts.length == 2) {
                    String variableName = parts[0];
                    String newValue = parts[1];
                
                    if (variableName.equalsIgnoreCase("calculationTimeBufferMs")) {
                        try {
                            int calculationTimeBufferMs = Integer.parseInt(newValue);
                            gameHandler.setCalculationTimeBufferMs(calculationTimeBufferMs);
                            System.out.println("-> Rechenzeitbuffer geändert auf: " + calculationTimeBufferMs + "ms");
                        } catch (NumberFormatException e) {
                            System.out.println("-> Fehler: Keine gültige Zahl.");
                        }
                    }

                    else if (variableName.equalsIgnoreCase("calculationTimeMs")) {
                        try {
                            int calculationTimeMs = Integer.parseInt(newValue);
                            gameHandler.setCalculationTimeMs(calculationTimeMs);
                            System.out.println("-> Rechenzeit geändert auf: " + calculationTimeMs + "ms");
                        } catch (NumberFormatException e) {
                            System.out.println("-> Fehler: Keine gültige Zahl.");
                        }
                    }
                }
                else {
                    System.out.println("Invalid input!");
                }
            }
        }
    }



    // Getter

    // Setter
}
