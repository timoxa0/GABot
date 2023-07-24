package ru.timoxa0.GABot.models;

import java.io.InputStream;

public record Skin(InputStream stream, boolean isSlim) {
    public Skin(InputStream stream, boolean isSlim) {
        this.isSlim = isSlim;
        this.stream = stream;
    }

    @Override
    public InputStream stream() {
        return stream;
    }

    @Override
    public boolean isSlim() {
        return isSlim;
    }
}
