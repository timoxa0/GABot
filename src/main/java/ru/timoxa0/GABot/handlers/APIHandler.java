package ru.timoxa0.GABot.handlers;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import io.undertow.util.Headers;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONObject;
import ru.timoxa0.GABot.models.Cape;
import ru.timoxa0.GABot.models.MCUser;
import ru.timoxa0.GABot.models.Skin;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.Deque;
import java.util.Map;

public class APIHandler implements HttpHandler {
    private final static TextureProvider TEXTURE_PROVIDER = TextureProvider.getTextureHandler();

    private static String getHash(InputStream inputStream) throws IOException {
        String md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(inputStream);
        return Base64.getEncoder().encodeToString(md5.getBytes());
    }

    @Override
    public void handleRequest(HttpServerExchange httpServerExchange) throws Exception {
        if (httpServerExchange.isInIoThread()) {
            httpServerExchange.dispatch(this);
            return;
        }
        if (httpServerExchange.getRequestURI().equals("/texture-provider")) {
            Map<String, Deque<String>> requestParams = httpServerExchange.getQueryParameters();
            if (requestParams.containsKey("username") | requestParams.containsKey("uuid")) {
                MCUser user = null;
                if (requestParams.containsKey("username")) {
                    user = MCUser.getByName(requestParams.get("username").element());
                } else if (requestParams.containsKey("uuid")) {
                    user = MCUser.getByUUID(requestParams.get("uuid").element());
                }

                if (user != null) {
                    if (requestParams.containsKey("type")) {
                        String type = requestParams.get("type").element();
                        InputStream inputStream = null;
                        switch (type) {
                            case "skin" -> inputStream = TEXTURE_PROVIDER.getSkin(user).stream();
                            case "cape" -> inputStream = TEXTURE_PROVIDER.getCape(user).stream();
                        }
                        if (inputStream != null) {
                            httpServerExchange.startBlocking();
                            httpServerExchange.getResponseHeaders()
                                    .put(Headers.CONTENT_TYPE, "image/png");
                            OutputStream outputStream = httpServerExchange.getOutputStream();
                            byte[] buf = new byte[8192];
                            int c;
                            while ((c = inputStream.read(buf, 0, buf.length)) > 0) {
                                outputStream.write(buf, 0, c);
                                outputStream.flush();
                            }
                            outputStream.close();
                        } else {
                            httpServerExchange.getResponseHeaders()
                                    .put(Headers.CONTENT_TYPE, "application/json");
                            httpServerExchange.getResponseSender()
                                    .send(new JSONObject().toString());
                        }
                    } else {
                        Skin skin = TEXTURE_PROVIDER.getSkin(user);
                        Cape cape = TEXTURE_PROVIDER.getCape(user);

                        httpServerExchange.getResponseHeaders()
                                .put(Headers.CONTENT_TYPE, "application/json");
                        JSONObject jsonBuilder = new JSONObject()
                                .put("SKIN", new JSONObject()
                                        .put("url", getProxyRequestURIBuilder(httpServerExchange)
                                                .addParameter("uuid", user.getUUID())
                                                .addParameter("type", "skin")
                                                .toString()
                                        )
                                        .put("digest", getHash(skin.stream()))
                                        .put("metadata", new JSONObject()
                                                .put("model", (skin.isSlim()) ? "slim" : "normal")
                                        )
                                )
                                .put("CAPE", new JSONObject()
                                        .put("url", getProxyRequestURIBuilder(httpServerExchange)
                                                .addParameter("uuid", user.getUUID())
                                                .addParameter("type", "cape")
                                                .toString()
                                        )
                                        .put("digest", getHash(cape.stream()))
                                );
                        httpServerExchange.getResponseSender()
                                .send(jsonBuilder.toString());
                    }
                } else {
                    httpServerExchange.getResponseHeaders()
                            .put(Headers.CONTENT_TYPE, "application/json");
                    httpServerExchange.getResponseSender()
                            .send(new JSONObject().toString());
                }
            } else {
                httpServerExchange.getResponseHeaders()
                        .put(Headers.CONTENT_TYPE, "application/json");
                httpServerExchange.getResponseSender()
                        .send(new JSONObject().toString());
            }
        }
    }

    private URIBuilder getProxyRequestURIBuilder(HttpServerExchange httpServerExchange) throws URISyntaxException {
        HeaderMap headerMap = httpServerExchange.getRequestHeaders();
        return new URIBuilder(String.format("%s://%s%s",
                headerMap.get("X-Forwarded-Proto").getFirst(),
                headerMap.get("Host").getFirst(),
                httpServerExchange.getRequestURI()
        ));
    }
}
