package hexlet.code.controllers;

import io.javalin.http.Handler;

public final class RootController {

    public static Handler welcome = ctx -> ctx.html("<p>Hello World</p>");
}
