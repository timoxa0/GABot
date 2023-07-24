package ru.timoxa0.GABot.handlers;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import io.undertow.util.Headers;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONObject;
import ru.timoxa0.GABot.models.MCUser;

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
            if (requestParams.containsKey("username")) {
                String username = requestParams.get("username").element();

                if (requestParams.containsKey("type")) {
                    String type = requestParams.get("type").element();
                    InputStream inputStream = null;
                    switch (type) {
                        case "skin" -> inputStream = TEXTURE_PROVIDER.getSkin(MCUser.getByName(username)).stream();
                        case "cape" -> inputStream = TEXTURE_PROVIDER.getCape(MCUser.getByName(username)).stream();
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
                    httpServerExchange.getResponseHeaders()
                            .put(Headers.CONTENT_TYPE, "application/json");
                    String jsonBuilder = new JSONObject()
                            .put("SKIN", new JSONObject()
                                    .put("url", getProxyRequestURIBuilder(httpServerExchange)
                                            .addParameter("username", username)
                                            .addParameter("type", "skin")
                                            .toString()
                                    )
                                    .put("digest", getHash(TEXTURE_PROVIDER.getSkin(MCUser.getByName(username)).stream()))
                                    .put("metadata", new JSONObject()
                                            .put("model", (TEXTURE_PROVIDER.getSkin(MCUser.getByName(username)).isSlim()) ? "slim" : "normal")
                                    )
                            )
                            .put("CAPE", new JSONObject()
                                    .put("url", getProxyRequestURIBuilder(httpServerExchange)
                                            .addParameter("username", username)
                                            .addParameter("type", "cape")
                                            .toString()
                                    )
                                    .put("digest", getHash(TEXTURE_PROVIDER.getCape(MCUser.getByName(username)).stream()))
                            )
                            .toString();
                    httpServerExchange.getResponseSender()
                            .send(jsonBuilder);
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
