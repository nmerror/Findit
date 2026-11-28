package com.bubt.findit.db.dao;

import com.bubt.findit.core.model.ItemSummary;
import com.bubt.findit.core.model.ItemType;
import com.bubt.findit.core.model.LocationCount;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The in-memory implementation of {@link ItemDao}.
 *
 * <p>It is used by the demo mode ({@code bash run.sh}) and by the self-test,
 * so the whole flow - post, close, remove - can be tried without a database
 * server. The data is real in the sense that it behaves exactly like the
 * MySQL version, but it disappears when the program ends.
 */
public class InMemoryItemDao implements ItemDao {

    private record Row(long id, long reportedBy, String itemType, String category, String title,
                       String description, String brand, String color, String locationName,
                       String status, String imagePath, LocalDateTime createdAt) {
    }

    private final Map<Long, Row> items = new LinkedHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1);

    @Override
    public long countByType(String itemType) {
        return items.values().stream()
                .filter(row -> itemType == null || row.itemType().equals(itemType))
                .count();
    }

    @Override
    public long countByCategory(String category) {
        return items.values().stream()
                .filter(row -> category == null || row.category().equals(category))
                .count();
    }

    @Override
    public long countOpen() {
        return items.values().stream().filter(row -> "OPEN".equals(row.status())).count();
    }

    @Override
    public long countReportedBy(long userId, String itemType) {
        return items.values().stream()
                .filter(row -> row.reportedBy() == userId)
                .filter(row -> itemType == null || row.itemType().equals(itemType))
                .count();
    }

    @Override
    public List<ItemSummary> recent(int limit) {
        return findByType(null, null, limit);
    }

    @Override
    public List<LocationCount> topLocations(int limit) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (Row row : items.values()) {
            String place = row.locationName() == null ? "Unknown" : row.locationName();
            counts.merge(place, 1, Integer::sum);
        }
        List<LocationCount> result = new ArrayList<>();
        counts.forEach((place, total) -> result.add(new LocationCount(place, total)));
        result.sort((left, right) -> Integer.compare(right.getCount(), left.getCount()));
        return result.subList(0, Math.min(limit, result.size()));
    }

    @Override
    public long insert(ItemRecord item) {
        long id = sequence.getAndIncrement();
        items.put(id, new Row(id, item.getReportedBy() == null ? 0 : item.getReportedBy(),
                item.getItemType(), item.getCategory(), item.getTitle(), item.getDescription(),
                item.getBrand(), item.getColor(), item.getLocationName(),
                item.getStatus() == null ? "OPEN" : item.getStatus(), null, LocalDateTime.now()));
        return id;
    }

    @Override
    public List<ItemSummary> findByType(String itemType, String status, int limit) {
        List<ItemSummary> result = new ArrayList<>();
        List<Row> rows = new ArrayList<>(items.values());
        rows.sort((left, right) -> right.createdAt().compareTo(left.createdAt()));
        for (Row row : rows) {
            if (itemType != null && !itemType.equals(row.itemType())) {
                continue;
            }
            if (status != null && !status.equals(row.status())) {
                continue;
            }
            result.add(toSummary(row));
            if (result.size() >= limit) {
                break;
            }
        }
        return result;
    }

    @Override
    public List<ItemSummary> findByReporter(long userId, int limit) {
        List<ItemSummary> result = new ArrayList<>();
        for (Row row : items.values()) {
            if (row.reportedBy() == userId) {
                result.add(toSummary(row));
            }
        }
        result.sort((left, right) -> right.getCreatedAt().compareTo(left.getCreatedAt()));
        return result.subList(0, Math.min(limit, result.size()));
    }

    @Override
    public ItemSummary findById(long itemId) {
        Row row = items.get(itemId);
        return row == null ? null : toSummary(row);
    }

    @Override
    public boolean updateStatus(long itemId, String status) {
        Row row = items.get(itemId);
        if (row == null) {
            return false;
        }
        items.put(itemId, new Row(row.id(), row.reportedBy(), row.itemType(), row.category(),
                row.title(), row.description(), row.brand(), row.color(), row.locationName(),
                status, row.imagePath(), row.createdAt()));
        return true;
    }

    @Override
    public boolean deleteById(long itemId) {
        return items.remove(itemId) != null;
    }

    @Override
    public boolean insertImage(long itemId, long userId, String filePath) {
        Row row = items.get(itemId);
        if (row == null) {
            return false;
        }
        items.put(itemId, new Row(row.id(), row.reportedBy(), row.itemType(), row.category(),
                row.title(), row.description(), row.brand(), row.color(), row.locationName(),
                row.status(), filePath, row.createdAt()));
        return true;
    }

    @Override
    public String findImagePath(long itemId) {
        Row row = items.get(itemId);
        return row == null ? null : row.imagePath();
    }

    @Override
    public List<String> listLocations() {
        return List.of("Central Library", "Main Auditorium", "Cafeteria", "Exam Hall",
                "Hostel Gate", "Sports Complex");
    }

    private ItemSummary toSummary(Row row) {
        ItemSummary summary = new ItemSummary(row.id(), row.title(), row.category(),
                ItemType.fromString(row.itemType()), row.locationName(), "User " + row.reportedBy(),
                row.reportedBy(), row.status(), row.createdAt());
        summary.setImagePath(row.imagePath());
        return summary;
    }
}
