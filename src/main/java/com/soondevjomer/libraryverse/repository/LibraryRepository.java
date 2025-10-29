package com.soondevjomer.libraryverse.repository;

import com.soondevjomer.libraryverse.model.Library;
import com.soondevjomer.libraryverse.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface LibraryRepository extends JpaRepository<Library, Long>, JpaSpecificationExecutor<Library> {

    Optional<Library> findByOwnerUsername(String username);
}

