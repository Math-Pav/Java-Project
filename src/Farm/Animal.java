package Farm;

import javafx.application.Platform;
import javafx.scene.control.Label;
import java.util.*;

public class Animal {
    private String name;
    private int feedCount;
    private final int FEED_THRESHOLD = 2;
    private boolean isAdult;
    private Timer productionTimer;
    private String product;

    public Animal(String name) {
        this.name = name;
        this.feedCount = 0;
        this.isAdult = false;
        this.product = name.equals("Vache") ? "Lait" : "Œufs";
    }

    public void feed(Map<String, Integer> inventory, Label cell, int row, int col) {
        String requiredFood = name.equals("Vache") ? "Ble" : "Mais";

        if (!inventory.containsKey(requiredFood) || inventory.get(requiredFood) < 1) {
            showAlert("Erreur", "Pas assez de " + requiredFood + " pour nourrir " + name + " !");
            return;
        }

        inventory.put(requiredFood, inventory.get(requiredFood) - 1);
        if (inventory.get(requiredFood) == 0) inventory.remove(requiredFood);

        feedCount++;

        if (feedCount >= FEED_THRESHOLD) {
            showAlert("Évolution", name + " en (" + row + ", " + col + ") devient adulte...");
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        cell.setStyle("-fx-border-color: grey; -fx-background-color: orange;");
                        isAdult = true;
                        startProduction(cell);
                    });
                }
            }, 5000);
        } else {
            showAlert("Nourrissage", name + " en (" + row + ", " + col + ") a été nourri " + feedCount + " fois.");
        }
    }

    private void startProduction(Label cell) {
        if (!isAdult) return;

        productionTimer = new Timer();
        productionTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    cell.setStyle("-fx-border-color: grey; -fx-background-color: yellow;");

                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            Platform.runLater(() -> cell.setStyle("-fx-border-color: grey; -fx-background-color: orange;"));
                        }
                    }, 5000);
                });
            }
        }, 0, 10000);
    }

    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public String getName() {
        return name;
    }
}