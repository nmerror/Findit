package com.bubt.findit.service.data;

import com.bubt.findit.core.model.ItemCategory;
import com.bubt.findit.core.model.ItemSummary;
import com.bubt.findit.core.model.ItemType;
import com.bubt.findit.core.model.LocationCount;
import com.bubt.findit.core.spi.ItemCatalogService;
import com.bubt.findit.db.dao.ItemDao;

import java.util.List;

/**
 * Reads the item counters of Segment 2 straight from the database.
 *
 * <p>This is the reference implementation of the {@code ItemCatalogService}
 * contract. The Segment 2 team can either keep it (it already works with the
 * real tables) or replace it with their own class - the application does not
 * care which one is registered.
 */
public class ItemCatalogServiceImpl implements ItemCatalogService {

    private final ItemDao itemDao;

    public ItemCatalogServiceImpl(ItemDao itemDao) {
        this.itemDao = itemDao;
    }

    @Override
    public List<ItemSummary> recentItems(int limit) {
        return itemDao.recent(limit);
    }

    @Override
    public int countByType(ItemType type) {
        // name() is the value stored in the database (LOST / FOUND);
        // label() is the text shown on screen ("Lost Item").
        return type == null ? 0 : (int) itemDao.countByType(type.name());
    }

    @Override
    public int countByCategory(ItemCategory category) {
        // Same here: the column stores the enum name (ELECTRONICS).
        return category == null ? 0 : (int) itemDao.countByCategory(category.name());
    }

    @Override
    public List<LocationCount> topLocations(int limit) {
        return itemDao.topLocations(limit);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
