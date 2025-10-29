package com.soondevjomer.libraryverse.service;

import com.soondevjomer.libraryverse.dto.PopularityDto;
import com.soondevjomer.libraryverse.model.BookAnalytic;

public interface PopularityService {

    PopularityDto calcBookPopulariyScore(Long bookid);

    PopularityDto calcLibraryPopularityScore(Long libraryId);
}
