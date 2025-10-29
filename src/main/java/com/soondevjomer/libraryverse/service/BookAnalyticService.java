package com.soondevjomer.libraryverse.service;

public interface BookAnalyticService {

    void incrementViewCount(Long bookId);

    double getPopularityScore(Long bookId);
}
