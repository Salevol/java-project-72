package hexlet.code;

import hexlet.code.controllers.RootController;

import io.javalin.Javalin;

public final class App {

    private static int getPort() {
        String port = System.getenv().getOrDefault("PORT", "5000");
        return Integer.valueOf(port);
    }

    public static Javalin getApp() {
        Javalin app = Javalin.create();
        addRoutes(app);
        app.before(ctx -> ctx.attribute("ctx", ctx));
        return app;
    }

    private static void addRoutes(Javalin app) {
        app.get("/", RootController.welcome);
    }

    public static void main(String[] args) {
        Javalin app = getApp();
        app.start(getPort());
    }
}
