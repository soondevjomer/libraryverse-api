package com.soondevjomer.libraryverse.service;

import com.soondevjomer.libraryverse.dto.PopularityDto;

public interface PopularityService {

    PopularityDto calcBookPopulariyScore(Long bookid);

    PopularityDto calcLibraryPopularityScore(Long libraryId);
}
