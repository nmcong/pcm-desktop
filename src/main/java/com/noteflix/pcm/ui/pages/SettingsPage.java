package com.noteflix.pcm.ui.pages;

import com.noteflix.pcm.core.i18n.I18n;
import com.noteflix.pcm.ui.base.BaseView;
import com.noteflix.pcm.ui.styles.LayoutConstants;
import com.noteflix.pcm.ui.styles.StyleConstants;
import com.noteflix.pcm.ui.utils.LayoutHelper;
import com.noteflix.pcm.ui.utils.UIFactory;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.octicons.Octicons;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Settings page - Placeholder Implementation
 * 
 * Simple placeholder page showing basic information about the Settings functionality.
 * This serves as a template for future implementation of application configuration features.
 */
@Slf4j
public class SettingsPage extends BaseView {

  public SettingsPage() {
    super(
        I18n.get("page.settings.title"),
        I18n.get("page.settings.subtitle"),
        new FontIcon(Octicons.GEAR_24)
    );
    log.debug("SettingsPage placeholder initialized");
  }

  @Override
  protected Node createMainContent() {
    VBox content = LayoutHelper.createVBox(LayoutConstants.SPACING_XL);
    content.getStyleClass().add(StyleConstants.PAGE_CONTAINER);
    content.setAlignment(Pos.CENTER);
    LayoutHelper.setVGrow(content);

    // Create placeholder content
    VBox placeholderCard = UIFactory.createCard();
    placeholderCard.setAlignment(Pos.CENTER);
    placeholderCard.setPrefHeight(400);
    placeholderCard.getStyleClass().add("placeholder-card");

    FontIcon icon = new FontIcon(Octicons.GEAR_24);
    icon.setIconSize(64);
    icon.getStyleClass().add("placeholder-icon");

    Label title = UIFactory.createSectionTitle("Application Settings");
    title.getStyleClass().add("placeholder-title");

    Label description = UIFactory.createMutedLabel(
        "This page will contain application configuration functionality including:\n\n" +
        "• Theme and appearance settings\n" +
        "• Database connection configuration\n" +
        "• User preferences and profiles\n" +
        "• Notification and alert settings\n" +
        "• System and performance options\n\n" +
        "Coming soon..."
    );
    description.setWrapText(true);
    description.getStyleClass().add("placeholder-description");

    placeholderCard.getChildren().addAll(icon, title, description);
    content.getChildren().add(placeholderCard);

    return content;
  }

  @Override
  public void onActivate() {
    super.onActivate();
    log.debug("SettingsPage placeholder activated");
  }
}