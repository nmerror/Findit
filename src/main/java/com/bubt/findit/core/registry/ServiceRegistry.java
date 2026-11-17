package com.bubt.findit.core.registry;

import com.bubt.findit.core.spi.AdminWorkloadService;
import com.bubt.findit.core.spi.ClaimQueryService;
import com.bubt.findit.core.spi.ItemCatalogService;
import com.bubt.findit.core.spi.MatchQueryService;
import com.bubt.findit.core.spi.NotificationService;
import com.bubt.findit.core.spi.SearchContributor;
import com.bubt.findit.core.spi.stub.StubAdminWorkloadService;
import com.bubt.findit.core.spi.stub.StubClaimQueryService;
import com.bubt.findit.core.spi.stub.StubItemCatalogService;
import com.bubt.findit.core.spi.stub.StubMatchQueryService;
import com.bubt.findit.core.spi.stub.StubNotificationService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ============================================================================
 *  THE WIRING HUB - this is how Segment 1 connects to Segments 2, 3 and 4.
 * ============================================================================
 *
 * <p>At start-up every service contract ({@code core.spi}) is registered here.
 * Segment 1 first registers <b>stub</b> implementations (the project runs even
 * if nobody else has written a single line yet). When a teammate finishes
 * their segment they replace the stub with the real one:
 *
 * <pre>{@code
 * ServiceRegistry.register(ItemCatalogService.class, new ItemCatalogServiceImpl());
 * }</pre>
 *
 * <p>Nobody else in the code base ever calls {@code new ItemCatalogServiceImpl()}
 * directly - that is what keeps the segments decoupled. The dashboard asks for
 * the <i>interface</i> and always gets whatever is currently registered.
 */
public final class ServiceRegistry {

    private static final Map<Class<?>, Object> SERVICES = new ConcurrentHashMap<>();

    /** Optional contributors that join the global search. */
    private static final java.util.List<SearchContributor> SEARCH_CONTRIBUTORS =
            java.util.Collections.synchronizedList(new java.util.ArrayList<>());

    private ServiceRegistry() {
    }

    /** Register (or replace) the implementation of a contract. */
    public static <T> void register(Class<T> contract, T implementation) {
        if (contract == null || implementation == null) {
            throw new IllegalArgumentException("contract and implementation must not be null");
        }
        SERVICES.put(contract, implementation);
        System.out.println("[ServiceRegistry] registered " + contract.getSimpleName()
                + " -> " + implementation.getClass().getSimpleName());
    }

    /** The registered implementation, or null when nobody registered one. */
    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> contract) {
        return (T) SERVICES.get(contract);
    }

    /** The registered implementation, or the given fallback when there is none. */
    public static <T> T getOrDefault(Class<T> contract, T fallback) {
        T found = get(contract);
        return found == null ? fallback : found;
    }

    public static boolean isRegistered(Class<?> contract) {
        return SERVICES.containsKey(contract);
    }

    /**
     * Installs the stub implementations. Call this first, at start-up, so the
     * application is always runnable.
     */
    public static void registerDefaults() {
        register(ItemCatalogService.class, new StubItemCatalogService());
        register(ClaimQueryService.class, new StubClaimQueryService());
        register(MatchQueryService.class, new StubMatchQueryService());
        register(AdminWorkloadService.class, new StubAdminWorkloadService());
        register(NotificationService.class, new StubNotificationService());
    }

    /**
     * Registers an optional search contributor. Any segment may call this to
     * join the global search in the top bar.
     */
    public static void registerSearchContributor(SearchContributor contributor) {
        if (contributor != null && !SEARCH_CONTRIBUTORS.contains(contributor)) {
            SEARCH_CONTRIBUTORS.add(contributor);
            System.out.println("[ServiceRegistry] search contributor -> "
                    + contributor.getClass().getSimpleName());
        }
    }

    /** All registered search contributors. */
    public static java.util.List<SearchContributor> searchContributors() {
        synchronized (SEARCH_CONTRIBUTORS) {
            return new java.util.ArrayList<>(SEARCH_CONTRIBUTORS);
        }
    }

    /** Only used by tests. */
    public static void reset() {
        SERVICES.clear();
        SEARCH_CONTRIBUTORS.clear();
    }
}
