package com.bubt.findit.ui.posts;

import com.bubt.findit.app.AppServices;
import com.bubt.findit.core.model.ItemCategory;
import com.bubt.findit.core.model.OperationResult;
import com.bubt.findit.core.model.User;
import com.bubt.findit.service.PostService;
import com.bubt.findit.ui.common.AppButton;
import com.bubt.findit.ui.common.FlatComboBox;
import com.bubt.findit.ui.common.Fonts;
import com.bubt.findit.ui.common.ModernTextField;
import com.bubt.findit.ui.common.RoundedPanel;
import com.bubt.findit.ui.common.UiTheme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.io.File;
import java.nio.file.Path;

/**
 * The form where a student reports a lost item.
 *
 * <p>The category is a drop-down - the first question is always "what kind of
 * thing was it?" - and a photo can be attached. The photo is what the office
 * looks at when it has to decide whether a claim is true.
 */
public class PostLostItemPanel extends JPanel {

    private final AppServices services;
    private final PostService postService;
    private final User user;

    private final ModernTextField titleField = new ModernTextField("for example: black wallet");
    private final FlatComboBox<ItemCategory> categoryBox =
            new FlatComboBox<>(ItemCategory.values());
    private final ModernTextField brandField = new ModernTextField("brand or model (optional)");
    private final ModernTextField colorField = new ModernTextField("colour (optional)");
    private final FlatComboBox<String> locationBox = new FlatComboBox<>(new String[0]);
    private final JTextArea descriptionArea = new JTextArea(4, 20);
    private final JLabel photoLabel = new JLabel("No photo attached");
    private final JLabel messageLabel = new JLabel(" ");
    private final JLabel previewLabel = new JLabel();

    private Path photo;

    public PostLostItemPanel(AppServices services, User user) {
        super(new BorderLayout());
        this.services = services;
        this.postService = services.postService();
        this.user = user;

        setBackground(UiTheme.WINDOW);
        setBorder(BorderFactory.createEmptyBorder(4, 26, 22, 26));

        for (String location : postService.locations()) {
            locationBox.addItem(location);
        }
        // The drop-down shows a friendly text - "Electronics", not "ELECTRONICS".
        categoryBox.setItemLabels(ItemCategory::label);

        add(buildForm(), BorderLayout.CENTER);
    }

    // --------------------------------------------------------------- layout

    private javax.swing.JComponent buildForm() {
        RoundedPanel card = new RoundedPanel(UiTheme.R_LG, UiTheme.SURFACE, UiTheme.STROKE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(24, 26, 22, 26));
        card.setAlignmentX(LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 760));

        JLabel heading = new JLabel("Report a lost item");
        heading.setFont(Fonts.heading(19));
        heading.setForeground(UiTheme.TEXT);
        heading.setAlignmentX(LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Tell the campus what you lost - the office and the other "
                + "students will see it on the board.");
        sub.setFont(Fonts.body(13));
        sub.setForeground(UiTheme.TEXT_MUTE);
        sub.setAlignmentX(LEFT_ALIGNMENT);

        card.add(heading);
        card.add(Box.createVerticalStrut(6));
        card.add(sub);
        card.add(Box.createVerticalStrut(20));

        card.add(label("What did you lose?"));
        card.add(Box.createVerticalStrut(6));
        card.add(fullWidth(titleField));
        card.add(Box.createVerticalStrut(14));

        card.add(label("What kind of thing was it?"));
        card.add(Box.createVerticalStrut(6));
        card.add(fullWidth(categoryBox));
        card.add(Box.createVerticalStrut(14));

        JPanel row = new JPanel(new java.awt.GridLayout(1, 2, 14, 0));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        JPanel brand = new JPanel();
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        brand.setOpaque(false);
        brand.add(label("Brand or model"));
        brand.add(Box.createVerticalStrut(6));
        brand.add(fullWidth(brandField));
        JPanel color = new JPanel();
        color.setLayout(new BoxLayout(color, BoxLayout.Y_AXIS));
        color.setOpaque(false);
        color.add(label("Colour"));
        color.add(Box.createVerticalStrut(6));
        color.add(fullWidth(colorField));
        row.add(brand);
        row.add(color);
        card.add(row);
        card.add(Box.createVerticalStrut(14));

        card.add(label("Where did you lose it?"));
        card.add(Box.createVerticalStrut(6));
        card.add(fullWidth(locationBox));
        card.add(Box.createVerticalStrut(14));

        card.add(label("Description"));
        card.add(Box.createVerticalStrut(6));
        descriptionArea.setFont(Fonts.body(13));
        descriptionArea.setForeground(UiTheme.TEXT);
        descriptionArea.setBackground(UiTheme.SURFACE);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        JScrollPane descriptionScroll = new JScrollPane(descriptionArea);
        descriptionScroll.setBorder(BorderFactory.createLineBorder(UiTheme.STROKE));
        descriptionScroll.setAlignmentX(LEFT_ALIGNMENT);
        descriptionScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        card.add(descriptionScroll);
        card.add(Box.createVerticalStrut(14));

        card.add(label("Photo of the item (optional, but it helps a lot)"));
        card.add(Box.createVerticalStrut(6));
        card.add(buildPhotoRow());
        card.add(Box.createVerticalStrut(14));

        messageLabel.setFont(Fonts.body(12));
        messageLabel.setForeground(UiTheme.ROSE);
        messageLabel.setAlignmentX(LEFT_ALIGNMENT);
        card.add(messageLabel);
        card.add(Box.createVerticalStrut(8));

        JPanel actions = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);
        actions.setAlignmentX(LEFT_ALIGNMENT);

        AppButton post = new AppButton("Post the lost item", AppButton.Kind.PRIMARY);
        post.addActionListener(event -> post());

        AppButton clear = new AppButton("Clear the form", AppButton.Kind.SECONDARY);
        clear.addActionListener(event -> clearForm());

        actions.add(post);
        actions.add(clear);
        card.add(actions);

        JScrollPane scroll = new JScrollPane(card);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        return scroll;
    }

    private JPanel buildPhotoRow() {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);
        AppButton choose = new AppButton("Choose a photo...", AppButton.Kind.SECONDARY);
        choose.addActionListener(event -> choosePhoto());
        left.add(choose);
        left.add(Box.createVerticalStrut(8));
        photoLabel.setFont(Fonts.caption(11));
        photoLabel.setForeground(UiTheme.TEXT_MUTE);
        photoLabel.setAlignmentX(LEFT_ALIGNMENT);
        left.add(photoLabel);

        previewLabel.setBorder(BorderFactory.createLineBorder(UiTheme.STROKE));
        previewLabel.setPreferredSize(new Dimension(110, 110));
        previewLabel.setBackground(UiTheme.SURFACE_2);

        row.add(left, BorderLayout.CENTER);
        row.add(previewLabel, BorderLayout.EAST);
        return row;
    }

    private JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Fonts.medium(12));
        label.setForeground(UiTheme.TEXT_DIM);
        label.setAlignmentX(LEFT_ALIGNMENT);
        return label;
    }

    private javax.swing.JComponent fullWidth(javax.swing.JComponent component) {
        component.setAlignmentX(LEFT_ALIGNMENT);
        component.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        return component;
    }

    // -------------------------------------------------------------- actions

    private void choosePhoto() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose a photo of the item");
        chooser.setFileFilter(new FileNameExtensionFilter("Pictures", "jpg", "jpeg", "png", "gif"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            photo = file.toPath();
            photoLabel.setText(file.getName());
            showPreview(file);
        }
    }

    private void showPreview(File file) {
        try {
            Image image = javax.imageio.ImageIO.read(file);
            if (image == null) {
                previewLabel.setIcon(null);
                previewLabel.setText("No preview");
                return;
            }
            double factor = Math.min(100.0 / image.getWidth(null), 100.0 / image.getHeight(null));
            Image scaled = image.getScaledInstance(
                    (int) (image.getWidth(null) * factor), (int) (image.getHeight(null) * factor),
                    Image.SCALE_SMOOTH);
            previewLabel.setIcon(new javax.swing.ImageIcon(scaled));
            previewLabel.setText("");
        } catch (Exception ex) {
            previewLabel.setIcon(null);
            previewLabel.setText("No preview");
        }
    }

    private void post() {
        OperationResult<Long> result = postService.postLostItem(
                user,
                titleField.getText(),
                (ItemCategory) categoryBox.getSelectedItem(),
                brandField.getText(),
                colorField.getText(),
                descriptionArea.getText(),
                (String) locationBox.getSelectedItem(),
                photo);

        if (result.isSuccess()) {
            messageLabel.setForeground(UiTheme.MINT);
            messageLabel.setText(result.getMessage());
            clearForm();
        } else {
            messageLabel.setForeground(UiTheme.ROSE);
            messageLabel.setText(result.getMessage());
        }
    }

    private void clearForm() {
        titleField.setText("");
        brandField.setText("");
        colorField.setText("");
        descriptionArea.setText("");
        photo = null;
        photoLabel.setText("No photo attached");
        previewLabel.setIcon(null);
        previewLabel.setText("");
    }
}
