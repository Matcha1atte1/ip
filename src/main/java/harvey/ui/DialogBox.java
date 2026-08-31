package harvey.ui;

import java.io.IOException;
import java.util.Collections;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
/**
 * One message in the conversation: a picture of the speaker beside what they said.
 * <p>
 * The two static factory methods are the way to make one. A constructor could not do the
 * job alone, because the user's box and Harvey's differ after construction: Harvey's is
 * mirrored so the two speakers face each other from opposite sides.
 */
public class DialogBox extends HBox {
    @FXML
    private Label dialog;
    @FXML
    private ImageView displayPicture;

    /**
     * Builds a box in the user's layout, with the picture on the right.
     *
     * @param text the message to show.
     * @param img  the speaker's picture.
     */
    private DialogBox(String text, Image img) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainWindow.class.getResource("/view/DialogBox.fxml"));
            fxmlLoader.setController(this);
            fxmlLoader.setRoot(this);
            fxmlLoader.load();
        } catch (IOException e) {
            // Packaged with the program, so a failure here means a broken build.
            e.printStackTrace();
        }

        dialog.setText(text);
        displayPicture.setImage(img);
        clipDisplayPictureToCircle();
    }

    /** Clips the display picture into a circle, so that profile pictures appear round. */
    private void clipDisplayPictureToCircle() {
        double radius = displayPicture.getFitWidth() / 2;
        Circle clip = new Circle(radius, radius, radius);
        displayPicture.setClip(clip);
    }

    /** Flips the box so the picture is on the left and the text on the right. */
    private void flip() {
        ObservableList<Node> tmp = FXCollections.observableArrayList(this.getChildren());
        Collections.reverse(tmp);
        getChildren().setAll(tmp);
        setAlignment(Pos.TOP_LEFT);
        dialog.getStyleClass().add("reply-label");
    }

    /**
     * Colors the box by the kind of command that produced it, so that an addition, a
     * deletion and a refusal can be told apart at a glance.
     *
     * @param commandType the simple class name of the command, or null to leave the
     *                    default styling alone.
     */
    private void changeDialogStyle(String commandType) {
        if (commandType == null) {
            return;
        }

        switch (commandType) {
            case "AddCommand":
                dialog.getStyleClass().add("add-label");
                break;
            case "MarkCommand":
            case "UnmarkCommand":
                dialog.getStyleClass().add("marked-label");
                break;
            case "DeleteCommand":
                dialog.getStyleClass().add("delete-label");
                break;
            case "ErrorCommand":
                dialog.getStyleClass().add("error-label");
                break;
            default:
                // list, find and bye keep the ordinary reply styling.
        }
    }

    /**
     * Returns a box showing something the user said.
     *
     * @param text the message.
     * @param img  the user's picture.
     * @return the new box.
     */
    public static DialogBox getUserDialog(String text, Image img) {
        return new DialogBox(text, img);
    }

    /**
     * Returns a box showing something Harvey said, mirrored and colored by command.
     *
     * @param text        the message.
     * @param img         Harvey's picture.
     * @param commandType the simple class name of the command that produced the message.
     * @return the new box.
     */
    public static DialogBox getHarveyDialog(String text, Image img, String commandType) {
        DialogBox db = new DialogBox(text, img);
        db.flip();
        db.changeDialogStyle(commandType);
        return db;
    }
}
