package client.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public abstract class Handler implements HttpHandler {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Override
    public abstract void handle(HttpExchange req);
}