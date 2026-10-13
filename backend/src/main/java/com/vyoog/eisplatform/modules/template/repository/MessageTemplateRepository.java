package com.vyoog.eisplatform.modules.template.repository;

import com.vyoog.eisplatform.modules.template.model.MessageTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MessageTemplateRepository extends JpaRepository<MessageTemplate, Long> {

    Optional<MessageTemplate> findByCodeAndLanguage(String code, String language);

    List<MessageTemplate> findByCode(String code);
}
