package hexlet.code.controllers;


import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;
import io.ebean.PagedList;
import io.javalin.http.Handler;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Objects;

public class UrlController {

    private static final int URLS_ON_PAGE = 10;

    public static final Handler LIST_URLS = ctx -> {
        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
        int offset = (page - 1) * URLS_ON_PAGE;
        PagedList<Url> pagedUrls = new QUrl()
                .setFirstRow(offset)
                .setMaxRows(URLS_ON_PAGE)
                .orderBy().id.asc()
                .findPagedList();

        List<Url> urls = pagedUrls.getList();

        ctx.attribute("urls", urls);
        ctx.attribute("page", page);
        ctx.render("urls/index.html");
    };

    public static final Handler ADD_URL = ctx -> {
        String inputUrl = ctx.formParam("url");
        URL url;
        try {
            url = new URL(inputUrl);
        } catch (MalformedURLException e) {
            ctx.sessionAttribute("flash", "Invalid URL");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.redirect("/");
            return;
        }

        Url preparedUrl = new Url(url);

        Url check = new QUrl()
                .name.equalTo(preparedUrl.getName())
                .findOne();

        if (Objects.nonNull(check)) {
            ctx.sessionAttribute("flash", "Page already registered");
            ctx.sessionAttribute("flash-type", "info");
            ctx.redirect("/urls");
            return;
        }

        preparedUrl.save();

        ctx.sessionAttribute("flash", "Page successfully added");
        ctx.sessionAttribute("flash-type", "success");
        ctx.redirect("/urls");
    };

    public static final Handler SHOW_URL = ctx -> {
        long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);
        Url url = new QUrl()
                .id.equalTo(id)
                .findOne();

        ctx.attribute("url", url);
        ctx.render("urls/show.html");
    };
}
