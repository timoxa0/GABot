package ru.timoxa0.GABot.models;

import java.io.InputStream;

public record Skin(InputStream stream, boolean isSlim) {

    @Override
    public InputStream stream() {
        return stream;
    }

    @Override
    public boolean isSlim() {
        return isSlim;
    }
}
