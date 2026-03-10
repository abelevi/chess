package handler;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import io.javalin.http.Context;
import model.AuthData;
import model.UserData;
import service.UserService;

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
        } catch (DataAccessException e) {
            ExceptionHandler.handleDataAccessException(ctx, e);
        }
    }

    public void login(Context ctx) {
        UserData user = gson.fromJson(ctx.body(), UserData.class);
        try {
            AuthData auth = userService.login(user);
            ctx.status(200);
            ctx.json(auth);
        } catch (DataAccessException e) {
            ExceptionHandler.handleDataAccessException(ctx, e);
        }
    }

    public void logout(Context ctx) {
        String authToken = ctx.header("authorization");
        try {
            userService.logout(authToken);
            ctx.status(200);
            ctx.json(new Object());
        } catch (DataAccessException e) {
            ExceptionHandler.handleDataAccessException(ctx, e);
        }
    }
}
