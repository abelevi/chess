package handler;

import dataaccess.DataAccessException;
import io.javalin.http.Context;

import java.util.Map;

public class ExceptionHandler {
    static void handleDataAccessException(Context ctx, DataAccessException e) {
        String message = e.getMessage();
        if (message.contains("bad request")) {
            ctx.status(400);
        } else if (message.contains("unauthorized")) {
            ctx.status(401);
        } else if (message.contains("already taken")) {
            ctx.status(403);
        } else {
            ctx.status(500);
        }
        ctx.json(Map.of("message", message));
    }
}
