package com.github.hkzorman.avakinitemdb.controllers;

import com.github.hkzorman.avakinitemdb.models.db.AvakinItemChangeProposal;
import com.github.hkzorman.avakinitemdb.models.ui.PagedResponse;
import com.github.hkzorman.avakinitemdb.repositories.AvakinItemChangeRepository;
import com.github.hkzorman.avakinitemdb.repositories.AvakinItemRepository;
import com.github.hkzorman.avakinitemdb.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;

@RestController
@CrossOrigin("http://localhost:4200")
@RequestMapping("/itemChanges")
public class AvakinItemChangeController {

    private Logger logger = LoggerFactory.getLogger(AvakinItemChangeController.class);
    private AvakinItemChangeRepository repository;
    private AvakinItemRepository itemRepository;
    private UserRepository userRepository;

    @Autowired
    public AvakinItemChangeController(AvakinItemChangeRepository repository, AvakinItemRepository itemRepository, UserRepository userRepository) {
        this.repository = repository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<AvakinItemChangeProposal> get(@PathVariable String id) {
        var result = this.repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Unable to find change proposal with ID '" + id + "'."));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/active")
    public PagedResponse<AvakinItemChangeProposal> getActive(
            @RequestParam(name="page", required = false, defaultValue = "0") int currentPage
    ) throws Exception {
        var pageRequest = PageRequest.of(currentPage, 24);
        var result = this.repository.findByIsActive(true, pageRequest);
        if (result.isPresent()) {
            var page = result.get();
            return new PagedResponse<>(page.getNumber(), page.getTotalPages(), page.getContent());
        }
        else {
            return new PagedResponse<>(0, 0, new ArrayList<>());
        }
    }

    @GetMapping("/new")
    public ResponseEntity<AvakinItemChangeProposal> create(
            @RequestParam(name="itemId", required = true) String itemId,
            @RequestParam(name="newTitle", required = true) String newTitle
    ) {
        var username = ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        var item = this.itemRepository.findByItemId(itemId)
                .orElseThrow(() -> new IllegalAccessError("Item with ID " + itemId + " doesn't exists"));
        var user = this.userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalAccessError("User " + username + " doesn't exists"));

        var changeProposal = new AvakinItemChangeProposal();
        changeProposal.setNewTitle(newTitle);
        changeProposal.setItem(item);
        changeProposal.setCreatedBy(user);
        changeProposal.setCreatedOn(LocalDateTime.now());
        changeProposal.setActive(true);

        var result = this.repository.save(changeProposal);
        return ResponseEntity.ok(result);
    }

    // TODO: Implement approve
    @GetMapping("/approve")
    public ResponseEntity<AvakinItemChangeProposal> approve(
            @RequestParam(name="id") String proposalId,
            @RequestParam(name="approve") Boolean approved
    ) {
        var username = ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        var proposal = this.repository.findById(proposalId)
                .orElseThrow(() -> new IllegalArgumentException("No item change proposal exists with ID '" + proposalId + "'."));
        var item = this.itemRepository.findByItemId(proposal.getItem().getItemId())
                .orElseThrow(() -> new IllegalAccessError("Item with ID " + proposal.getItem().getItemId() + " doesn't exists"));
        var user = this.userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalAccessError("User " + username + " doesn't exists"));

        logger.info("User '" + "' has " + (approved ? "approved" : "rejected") + " proposal '" + proposalId + "' to change item '" + item.getItemId() + "' title from '" + item.getTitleEs() + "' to '" + proposal.getNewTitle() + "'");

        if (approved) {
            logger.info("Changing title of item " + item.getItemId() + " from '" + item.getTitleEs() + "' to '" + proposal.getNewTitle() + "'.");
            item.setTitleEs(proposal.getNewTitle());
            this.itemRepository.save(item);
        }

        proposal.setApproved(approved);
        proposal.setActionedBy(user);
        proposal.setActionedOn(LocalDateTime.now());
        var result = this.repository.save(proposal);

        return ResponseEntity.ok(result);
    }
}
