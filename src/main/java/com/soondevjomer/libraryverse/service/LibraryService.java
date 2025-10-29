package com.soondevjomer.libraryverse.service;

import com.soondevjomer.libraryverse.dto.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface LibraryService {

    LibraryDto createLibrary(LibraryDto libraryDto);

    LibraryStatDto getLibraryStat();

    PageModel<List<LibraryDto>> getLibraryByPage(FilterDto filterDto);

    LibraryDto getLibraryById(Long libraryId);

    LibraryDto updateLibraryById(Long libraryId, LibraryRequestDto libraryRequestDto, MultipartFile file);
}
