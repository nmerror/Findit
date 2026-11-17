package com.bubt.findit.core.spi.stub;

import com.bubt.findit.core.model.ItemCategory;
import com.bubt.findit.core.model.ItemSummary;
import com.bubt.findit.core.model.ItemType;
import com.bubt.findit.core.model.LocationCount;
import com.bubt.findit.core.spi.ItemCatalogService;

import java.util.Collections;
import java.util.List;

/**
 * Placeholder for Segment 2 until the real implementation exists.
 * The dashboard detects this through {@code isAvailable() == false} and shows
 * "waiting for Segment 2" instead of empty numbers.
 */
public class StubItemCatalogService implements ItemCatalogService {

    @Override
    public List<ItemSummary> recentItems(int limit) {
        return Collections.emptyList();
    }

    @Override
    public int countByType(ItemType type) {
        return 0;
    }

    @Override
    public int countByCategory(ItemCategory category) {
        return 0;
    }

    @Override
    public List<LocationCount> topLocations(int limit) {
        return Collections.emptyList();
    }

    @Override
    public boolean isAvailable() {
        return false;
    }
}
