package ru.timoxa0.GABot.models;

import java.io.InputStream;

public record Cape(InputStream stream) {
    public Cape(InputStream stream) {
        this.stream = stream;
    }

    @Override
    public InputStream stream() {
        return stream;
    }
}
