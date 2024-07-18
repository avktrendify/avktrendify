package com.github.hkzorman.avakinitemdb.controllers;

import com.github.hkzorman.avakinitemdb.models.db.ItemMatchup;
import com.github.hkzorman.avakinitemdb.models.db.ItemMatchupProposal;
import com.github.hkzorman.avakinitemdb.repositories.AvakinItemRepository;
import com.github.hkzorman.avakinitemdb.repositories.ItemMatchupProposalRepository;
import com.github.hkzorman.avakinitemdb.repositories.ItemMatchupRepository;
import com.github.hkzorman.avakinitemdb.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/proposals")
public class ItemMatchupProposalController {
    private final AvakinItemRepository itemRepository;

    private final ItemMatchupProposalRepository repository;
    private final ItemMatchupRepository itemMatchupRepository;
    private final UserRepository userRepository;

    @Autowired
    public ItemMatchupProposalController(AvakinItemRepository itemRepository, ItemMatchupProposalRepository repository, ItemMatchupRepository itemMatchupRepository, UserRepository userRepository) {
        this.repository = repository;
        this.itemRepository = itemRepository;
        this.itemMatchupRepository = itemMatchupRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemMatchupProposal> get(@PathVariable String id) {
        var result = this.repository.findById(id);
        return result.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.badRequest().body(null));
    }

    @GetMapping("/active")
    public List<ItemMatchupProposal> getActive() {
        var result = this.repository.findByIsActive(true);
        return result.get();
    }

    @GetMapping("/vote")
    public ItemMatchupProposal vote(@RequestParam String id, @RequestParam String itemId) throws Exception {
        var proposal = this.repository.findById(id).orElse(null);
        if (proposal != null) {
            // Find the item
            var matchup = Arrays.stream(proposal.getMatchups())
                    .filter(x -> x.getItem().getItemId().equals(itemId))
                    .findFirst()
                    .orElseThrow(IllegalArgumentException::new); // ("Unable to find item with ID " + itemId + " in proposal " + id)

            // Add or remove user depending
            var username = ((User)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
            var user = userRepository.findByUsername(username).orElse(null);
            if (user != null) {
                // TODO: Improve the throw
                var item = this.itemRepository.findByItemId(itemId).orElseThrow();
                var users = matchup.getUsers();
                if (users.contains(user)) {
                    users.remove(user);
                    itemMatchupRepository.save(matchup);
                    user.getOwnedItems().remove(item);
                    userRepository.save(user);
                }
                else {
                    users.add(user);
                    itemMatchupRepository.save(matchup);
                    user.getOwnedItems().add(item);
                    userRepository.save(user);
                }
            }
            else {
                throw new IllegalAccessException("User with name " + username + " is not a valid user");
            }



            proposal = this.repository.save(proposal);
            return proposal;
        }
        else {
            throw new IllegalArgumentException("Unable to find proposal with ID " + id);
        }
    }

    @PostMapping("/save")
    public ResponseEntity<ItemMatchupProposal> save(@RequestBody ItemMatchupProposal proposal) throws Exception {
        var principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal == null) {
            return ResponseEntity.badRequest().body(null);
        }

        var username = ((User)principal).getUsername();
        System.out.println(SecurityContextHolder.getContext().getAuthentication().toString());
        var user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            throw new IllegalAccessException("User with name " + username + " is not a valid user");
        }

        if (proposal.getId() != null && !proposal.getId().isEmpty()) {
            var existingProposal = this.repository.findById(proposal.getId()).orElse(null);
            if (existingProposal != null) {
                existingProposal.setName(proposal.getName());
                existingProposal.setActive(proposal.isActive());
                existingProposal.setUpdatedOn(LocalDateTime.now());

                // Save matchups
                var matchupsWithId = Arrays.stream(proposal.getMatchups()).filter(x -> x.getId() > -1).collect(Collectors.toList());
                var newMatchups = Arrays.stream(proposal.getMatchups()).filter(x -> x.getId() == -1).collect(Collectors.toList());
                if (!newMatchups.isEmpty()) {
                    for (var matchup : newMatchups) {
                        matchup.setProposal(existingProposal);
                        matchupsWithId.add(itemMatchupRepository.save(matchup));
                    }
                }
                existingProposal.setMatchups(matchupsWithId.toArray(new ItemMatchup[0]));

                // TODO: Delete unused matchups

                return ResponseEntity.ok(repository.save(existingProposal));
            }
        }
        else {
            // new proposal
            proposal.setActive(true);
            proposal.setCreatedBy(user);
            proposal.setCreatedOn(LocalDateTime.now());
            // We need to save first in order to be able to create matchups with a proposal id
            proposal = repository.save(proposal);

            // Create item matchups
            var newMatchups = new ArrayList<ItemMatchup>(proposal.getMatchups().length);
            for (var matchup : proposal.getMatchups()) {
                matchup.setProposal(proposal);
                newMatchups.add(itemMatchupRepository.save(matchup));
            }
            proposal.setMatchups(newMatchups.toArray(new ItemMatchup[0]));

            return ResponseEntity.ok(repository.save(proposal));
        }

        return null;
    }
}
