// Task.java
package com.example.collection_board_games;

import javafx.beans.property.*;
import java.time.LocalDate;

public class BoardGame {
    private final IntegerProperty id;
    private final StringProperty title;
    private final StringProperty description;
    private final StringProperty category;
    private final StringProperty priority;
    private final StringProperty status;
    private final ObjectProperty<LocalDate> dueDate;

    public BoardGame(int id, String title, String description, String category,
                     String priority, String status, LocalDate dueDate) {
        this.id = new SimpleIntegerProperty(id);
        this.title = new SimpleStringProperty(title);
        this.description = new SimpleStringProperty(description);
        this.category = new SimpleStringProperty(category);
        this.priority = new SimpleStringProperty(priority);
        this.status = new SimpleStringProperty(status);
        this.dueDate = new SimpleObjectProperty<>(dueDate);
    }

    // Геттеры
    public int getId() { return id.get(); }
    public String getTitle() { return title.get(); }
    public String getDescription() { return description.get(); }
    public String getCategory() { return category.get(); }
    public String getPriority() { return priority.get(); }
    public String getStatus() { return status.get(); }
    public LocalDate getDueDate() { return dueDate.get(); }

    // Сеттеры
    public void setId(int id) { this.id.set(id); }
    public void setTitle(String title) { this.title.set(title); }
    public void setDescription(String description) { this.description.set(description); }
    public void setCategory(String category) { this.category.set(category); }
    public void setPriority(String priority) { this.priority.set(priority); }
    public void setStatus(String status) { this.status.set(status); }
    public void setDueDate(LocalDate dueDate) { this.dueDate.set(dueDate); }

    // Property методы
    public IntegerProperty idProperty() { return id; }
    public StringProperty titleProperty() { return title; }
    public StringProperty descriptionProperty() { return description; }
    public StringProperty categoryProperty() { return category; }
    public StringProperty priorityProperty() { return priority; }
    public StringProperty statusProperty() { return status; }
    public ObjectProperty<LocalDate> dueDateProperty() { return dueDate; }
}