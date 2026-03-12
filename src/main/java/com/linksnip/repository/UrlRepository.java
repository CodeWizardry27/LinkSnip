package com.linksnip.repository;

import com.linksnip.model.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UrlRepository extends JpaRepository<Url, Long> {

    Optional<Url> findByShortCode(String shortCode);

    Optional<Url> findByCustomAlias(String customAlias);

    boolean existsByShortCode(String shortCode);

    boolean existsByCustomAlias(String customAlias);

    List<Url> findAllByOrderByCreatedAtDesc();

    List<Url> findByExpiresAtBeforeAndExpiresAtIsNotNull(LocalDateTime now);
}
