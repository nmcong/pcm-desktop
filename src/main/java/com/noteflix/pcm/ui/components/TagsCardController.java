package com.noteflix.pcm.ui.components;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for tags card
 */
@Slf4j
public class TagsCardController implements Initializable {

    @FXML
    private HBox tagsContainer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        log.debug("Initializing TagsCard component");
    }

    @FXML
    private void handleAddTag() {
        log.info("🏷️ Adding new tag");
        
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Tag");
        dialog.setHeaderText("Add New Tag");
        dialog.setContentText("Enter tag name:");
        
        dialog.showAndWait().ifPresent(tagName -> {
            if (!tagName.trim().isEmpty()) {
                log.info("Adding tag: {}", tagName);
                addTag(tagName, "tag-blue");
            }
        });
    }

    public void addTag(String tagName, String styleClass) {
        if (tagsContainer != null) {
            Label tag = new Label(tagName);
            tag.getStyleClass().addAll("tag", styleClass);
            
            // Add before the "+ Add" button
            int insertIndex = tagsContainer.getChildren().size() - 1;
            tagsContainer.getChildren().add(insertIndex, tag);
            
            log.info("Tag added: {}", tagName);
        }
    }

    public void removeTag(String tagName) {
        if (tagsContainer != null) {
            tagsContainer.getChildren().removeIf(node -> {
                if (node instanceof Label) {
                    Label label = (Label) node;
                    return label.getText().equals(tagName) && label.getStyleClass().contains("tag");
                }
                return false;
            });
            log.info("Tag removed: {}", tagName);
        }
    }

    public void clearTags() {
        if (tagsContainer != null) {
            // Remove all tags but keep the "+ Add" button
            tagsContainer.getChildren().removeIf(node -> 
                node instanceof Label && ((Label) node).getStyleClass().contains("tag")
            );
            log.info("All tags cleared");
        }
    }
}

