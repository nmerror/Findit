package com.bubt.findit.service;

import com.bubt.findit.core.model.ItemCategory;
import com.bubt.findit.core.model.ItemSummary;
import com.bubt.findit.core.model.OperationResult;
import com.bubt.findit.core.model.Role;
import com.bubt.findit.core.model.User;
import com.bubt.findit.core.registry.ServiceRegistry;
import com.bubt.findit.core.spi.AuditService;
import com.bubt.findit.core.util.Validation;
import com.bubt.findit.db.dao.ItemDao;
import com.bubt.findit.db.dao.ItemRecord;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;

/**
 * ============================================================================
 *  The lost-and-found flow: post a lost item, close it when it is found,
 *  remove it when the office says so.
 * ============================================================================
 *
 * <p>The three rules of the flow live here and nowhere else:
 * <ul>
 *   <li>a student posts a lost item with a photo,</li>
 *   <li>the owner clicks <em>Confirm found</em> - the post is closed and
 *       disappears from the open list,</li>
 *   <li>the administrator can remove a post at any time.</li>
 * </ul>
 */
public class PostService {

    /** The status of a post that is still looking for its owner. */
    public static final String STATUS_OPEN = "OPEN";
    /** The status of a post whose owner got the item back. */
    public static final String STATUS_CLOSED = "CLOSED";

    private static final String ERR_NO_ITEMS = "NO_ITEM_SERVICE";

    private final ItemDao itemDao;

    public PostService(ItemDao itemDao) {
        this.itemDao = itemDao;
    }

    // ------------------------------------------------------------- posting

    /**
     * Posts a lost item.
     *
     * @param photo the picture the user attached, or null
     * @return the id of the new post
     */
    public OperationResult<Long> postLostItem(User author, String title, ItemCategory category,
                                               String brand, String color, String description,
                                               String location, Path photo) {
        if (author == null) {
            return OperationResult.fail("NOT_LOGGED_IN", "Please log in first.");
        }
        if (itemDao == null) {
            return OperationResult.fail(ERR_NO_ITEMS, "The database is not available.");
        }
        String cleanTitle = title == null ? "" : title.trim();
        if (cleanTitle.isEmpty()) {
            return OperationResult.fail("TITLE_REQUIRED", "Give the item a short title.");
        }
        ItemCategory realCategory = category == null ? ItemCategory.OTHER : category;
        String cleanLocation = location == null ? "" : location.trim();

        ItemRecord record = new ItemRecord();
        record.setReportedBy(author.getId());
        record.setItemType("LOST");
        record.setCategory(realCategory.name());
        record.setTitle(cleanTitle);
        record.setDescription(description == null ? null : description.trim());
        record.setBrand(brand == null || brand.isBlank() ? null : brand.trim());
        record.setColor(color == null || color.isBlank() ? null : color.trim());
        record.setLocationName(cleanLocation.isEmpty() ? null : cleanLocation);
        record.setStatus(STATUS_OPEN);
        record.setLostDate(LocalDate.now().toString());

        long itemId = itemDao.insert(record);
        if (itemId <= 0) {
            return OperationResult.fail("INSERT_FAILED", "The post could not be saved.");
        }

        if (photo != null) {
            String stored = storePhoto(photo, itemId);
            if (stored != null) {
                itemDao.insertImage(itemId, author.getId(), stored);
            }
        }

        audit(author.getId(), "POST_ITEM", "Lost item posted: " + cleanTitle);
        return OperationResult.ok(itemId, "Your lost item is now on the board.");
    }

    // -------------------------------------------------------------- reading

    /** The open lost posts, newest first - what every user sees. */
    public List<ItemSummary> openLostPosts(int limit) {
        if (itemDao == null) {
            return List.of();
        }
        return itemDao.findByType("LOST", STATUS_OPEN, limit);
    }

    /** Every post of the campus, including the closed ones - the admin view. */
    public List<ItemSummary> allPosts(int limit) {
        if (itemDao == null) {
            return List.of();
        }
        return itemDao.findByType(null, null, limit);
    }

    /** The posts of one user, newest first. */
    public List<ItemSummary> myPosts(long userId, int limit) {
        if (itemDao == null) {
            return List.of();
        }
        return itemDao.findByReporter(userId, limit);
    }

    /** The places of the campus - for the drop-down of the form. */
    public List<String> locations() {
        if (itemDao == null) {
            return List.of();
        }
        return itemDao.listLocations();
    }

    // -------------------------------------------------------------- actions

    /**
     * The owner found the item: the post is closed and disappears from the
     * open list.
     */
    public OperationResult<String> confirmFound(User user, long itemId) {
        if (user == null || itemDao == null) {
            return OperationResult.fail("NOT_LOGGED_IN", "Please log in first.");
        }
        ItemSummary item = itemDao.findById(itemId);
        if (item == null) {
            return OperationResult.fail("NOT_FOUND", "That post does not exist any more.");
        }
        if (!user.isAdmin() && (item.getPostedByUserId() == null
                || item.getPostedByUserId() != user.getId())) {
            return OperationResult.fail("NOT_YOURS", "You can only close your own post.");
        }
        itemDao.updateStatus(itemId, STATUS_CLOSED);
        audit(user.getId(), "ITEM_FOUND", "Lost item found: " + item.getTitle());
        return OperationResult.ok(STATUS_CLOSED, "The post was closed - the item is back with its owner.");
    }

    /** The administrator removes a post - for example after the item was handed over. */
    public OperationResult<String> deletePost(User admin, long itemId) {
        if (admin == null || !admin.isAdmin()) {
            return OperationResult.fail("ACCESS_DENIED", "Only the administrator can remove a post.");
        }
        if (itemDao == null) {
            return OperationResult.fail(ERR_NO_ITEMS, "The database is not available.");
        }
        ItemSummary item = itemDao.findById(itemId);
        if (item == null) {
            return OperationResult.fail("NOT_FOUND", "That post does not exist any more.");
        }
        itemDao.deleteById(itemId);
        audit(admin.getId(), "ITEM_REMOVED", "Post removed by the office: " + item.getTitle());
        return OperationResult.ok(STATUS_CLOSED, "The post was removed.");
    }

    // --------------------------------------------------------------- photos

    /**
     * Copies the picture into the {@code uploads} folder of the project and
     * returns the path that is stored in the database.
     */
    private String storePhoto(Path photo, long itemId) {
        try {
            Path folder = Path.of("uploads");
            Files.createDirectories(folder);
            String name = itemId + "-" + System.currentTimeMillis() + "-"
                    + photo.getFileName().toString().replaceAll("[^a-zA-Z0-9._-]", "_");
            Path target = folder.resolve(name);
            Files.copy(photo, target, StandardCopyOption.REPLACE_EXISTING);
            return target.toAbsolutePath().toString();
        } catch (IOException ex) {
            // A missing photo must not stop the post - the text is what matters.
            return null;
        }
    }

    private void audit(Long actorUserId, String action, String details) {
        AuditService auditService = ServiceRegistry.get(AuditService.class);
        if (auditService != null) {
            auditService.record(action, actorUserId, details);
        }
    }
}
