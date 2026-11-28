package com.bubt.findit.tools;

import com.bubt.findit.app.AppServices;
import com.bubt.findit.config.AppConfig;
import com.bubt.findit.core.model.ItemCategory;
import com.bubt.findit.core.model.OperationResult;
import com.bubt.findit.core.model.User;
import com.bubt.findit.db.DaoFactory;
import com.bubt.findit.db.Database;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Posts one lost item from the command line — through the **real** service.
 *
 * <p>This is a small helper for the build server: it produces the screenshot of
 * the board with a post on it. The row it writes is a normal row in the
 * {@code items} table, created by the same {@code PostService} that the form of
 * the application uses — there is no fake data anywhere in FindIt.
 *
 * <pre>
 *   java -cp "target/classes:lib/*" com.bubt.findit.tools.PostTool \
 *        ayesha@buet.edu Student@123 "Wireless Mouse" ELECTRONICS "Exam Hall" photo.png
 * </pre>
 */
public final class PostTool {

    private PostTool() {
    }

    public static void main(String[] args) {
        if (args.length < 5) {
            System.out.println("usage: PostTool <e-mail> <password> <title> <category> <place> [photo]");
            return;
        }
        try {
            AppConfig config = AppConfig.load();
            Database.initialize(config);
            DaoFactory.reset();
            AppServices services = new AppServices(config);

            OperationResult<?> login = services.authService().login(args[0], args[1]);
            if (!login.isSuccess()) {
                System.out.println("login failed: " + login.getMessage());
                return;
            }
            User user = services.userDao().findByEmail(args[0]).orElse(null);
            if (user == null) {
                System.out.println("no such account: " + args[0]);
                return;
            }

            Path photo = null;
            if (args.length > 5 && Files.exists(Path.of(args[5]))) {
                photo = Path.of(args[5]);
            }

            OperationResult<Long> post = services.postService().postLostItem(user,
                    args[2], ItemCategory.fromString(args[3]), null, null,
                    "Posted through the application.", args[4], photo);
            System.out.println(post.isSuccess()
                    ? "posted: " + post.getData()
                    : "post failed: " + post.getMessage());
        } catch (Exception ex) {
            System.out.println("error: " + ex.getMessage());
        }
    }
}
