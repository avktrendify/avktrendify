package com.github.hkzorman.avakinitemdb.controllers;

import com.github.hkzorman.avakinitemdb.models.db.AvakinItem;
import com.github.hkzorman.avakinitemdb.models.ui.PagedResponse;
import com.github.hkzorman.avakinitemdb.repositories.AvakinItemRepository;
import com.github.hkzorman.avakinitemdb.services.AvakinItemSyncService;
import com.github.hkzorman.avakinitemdb.services.AzureTranslatorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@CrossOrigin
@RequestMapping("/items")
public class AvakinItemController {

    private AvakinItemRepository repository;

    private Logger logger = LoggerFactory.getLogger(AvakinItemController.class);

    @Autowired
    public AvakinItemController(AvakinItemRepository repository, AzureTranslatorService translator) {
        this.repository = repository;
    }

    @GetMapping
    public PagedResponse<AvakinItem> get(
            @RequestParam(name = "title", required = false) String title,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "page", required = false, defaultValue = "0") int currentPage,
            @RequestParam(name = "lang", required = false, defaultValue = "en") String language // Supported: ISO 639 en and es
    ) throws Exception {
        Optional<Page<AvakinItem>> result;
        var pageRequest = PageRequest.of(currentPage, 24);

        logger.info("Received request: [title=" + title + ", category=" + category + ", page=" + currentPage);

        if (title != null && category != null) {
            switch (language) {
                case "es":
                    result = repository.findByTitleEsContainingAndType(title, category, pageRequest);
                    break;
                default:
                    result = repository.findByTitleContainingAndType(title, category, pageRequest);
            }

        }
        else if (title != null) {
            switch (language) {
                case "es":
                    result = repository.findByTitleEsContaining(title, pageRequest);
                    break;
                default:
                    result = repository.findByTitleContaining(title, pageRequest);
            }
        }
        else if (category != null) {
            result = repository.findByType(category, pageRequest);
        }
        else {
            result = Optional.of(repository.findAll(pageRequest));
        }

        if (result.isPresent()) {
            var page = result.get();
            return new PagedResponse<>(page.getNumber(), page.getTotalPages(), page.getContent());
        }

        throw new Exception("No items found.");
    }
}
