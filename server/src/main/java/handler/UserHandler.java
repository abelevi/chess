package handler;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import io.javalin.http.Context;
import model.AuthData;
import model.UserData;
import service.UserService;

import java.util.Map;

public class UserHandler {
    private final UserService userService;
    private final Gson gson = new Gson();

    public UserHandler(UserService userService) {
        this.userService = userService;
    }

    public void register(Context ctx) {
       UserData user = gson.fromJson(ctx.body(), UserData.class);
        try {
            AuthData auth = userService.register(user);
            ctx.status(200);
            ctx.json(auth);
        }
        catch (DataAccessException e) {
            if (e.getMessage().contains("bad request")) {
                ctx.status(400);
            } else if (e.getMessage().contains("unauthorized")) {
                ctx.status(401);
            } else if (e.getMessage().contains("already taken")) {
                ctx.status(403);
            } else {
                ctx.status(500);
            }
            ctx.json(Map.of("message", e.getMessage()));
        }

    }

    public void login(Context ctx) {
        UserData user = gson.fromJson(ctx.body(), UserData.class);
        try {
            AuthData auth = userService.login(user);
            ctx.status(200);
            ctx.json(auth);
        }
        catch (DataAccessException e) {
            if (e.getMessage().contains("bad request")) {
                ctx.status(400);
            } else if (e.getMessage().contains("unauthorized")) {
                ctx.status(401);
            } else {
                ctx.status(500);
            }
            ctx.json(Map.of("message", e.getMessage()));
        }
    }

    public void logout(Context ctx) {
        String authToken = ctx.header("authorization");
        try {
            userService.logout(authToken);
            ctx.status(200);
            ctx.json(new Object());
        }
        catch (DataAccessException e) {
            if (e.getMessage().contains("unauthorized")) {
                ctx.status(401);
            } else {
                ctx.status(500);
            }
            ctx.json(Map.of("message", e.getMessage()));
        }

    }
}
