package com.soondevjomer.libraryverse.initializer;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MasterInitializer {

    private final GenreInitializer genreInitializer;
    private final UserInitializer userInitializer;

    @EventListener(ApplicationReadyEvent.class)
    public void initAll() {
        genreInitializer.initGenres();
    }
}
