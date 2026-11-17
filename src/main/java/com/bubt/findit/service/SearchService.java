package com.bubt.findit.service;

import com.bubt.findit.core.model.SearchHit;
import com.bubt.findit.core.model.User;
import com.bubt.findit.core.registry.ServiceRegistry;
import com.bubt.findit.core.spi.SearchContributor;
import com.bubt.findit.db.dao.UserDao;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * ============================================================================
 *  SEGMENT 1 - Global Search Service
 * ============================================================================
 *
 * <p>Searches the {@code users} table (Segment 1 data) and asks every other
 * segment that registered a {@link SearchContributor} for its own hits.
 *
 * <p>This is the "Search and Report Generation" feature of the project
 * proposal (Section 6). It works on day one with the users only, and it grows
 * automatically as the other segments register their contributors.
 */
public class SearchService {

    private final UserDao userDao;

    public SearchService(UserDao userDao) {
        this.userDao = userDao;
    }

    /** Searches the whole system. Never returns null. */
    public List<SearchHit> search(String query, int limit) {
        List<SearchHit> hits = new ArrayList<>();
        if (query == null || query.isBlank()) {
            return hits;
        }
        String needle = query.trim().toLowerCase();

        hits.addAll(searchUsers(needle, limit));

        for (SearchContributor contributor : ServiceRegistry.searchContributors()) {
            if (contributor == null || !contributor.isAvailable()) {
                continue;
            }
            try {
                List<SearchHit> part = contributor.search(needle, limit);
                if (part != null) {
                    hits.addAll(part);
                }
            } catch (RuntimeException ex) {
                // A broken contributor must never break the search.
                System.err.println("[search] contributor failed: " + ex.getMessage());
            }
        }

        hits.sort(Comparator.comparing(SearchHit::getSource).thenComparing(SearchHit::getTitle));
        return hits.size() > limit ? hits.subList(0, limit) : hits;
    }

    /** Segment 1 searches its own table: name, e-mail address and student ID. */
    private List<SearchHit> searchUsers(String needle, int limit) {
        List<SearchHit> hits = new ArrayList<>();
        for (User user : userDao.findAll(limit * 3, 0)) {
            String name = safe(user.getFullName());
            String email = safe(user.getEmail());
            String studentId = safe(user.getStudentId());
            if (name.toLowerCase().contains(needle)
                    || email.toLowerCase().contains(needle)
                    || studentId.toLowerCase().contains(needle)) {
                hits.add(new SearchHit("USERS", user.getFullName(),
                        email + "  ·  " + studentId, "PERSON", user.getId()));
            }
            if (hits.size() >= limit) {
                break;
            }
        }
        return hits;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
