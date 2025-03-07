package Farm;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.*;

public class FarmController {
    @FXML
    private Label moneyLabel;
    @FXML
    private ComboBox<String> cultureChoice;
    @FXML
    private ComboBox<String> animalChoice;
    @FXML
    private GridPane fieldGrid;
    @FXML
    private ListView<String> inventoryListView;
    @FXML
    private ListView<String> sellInventoryListView;
    @FXML
    private TextField quantityField;
    @FXML
    private ListView<String> marketPriceListView;

    private double money = 1500.0;
    private ObservableList<String> inventoryItems = FXCollections.observableArrayList();
    private Map<String, Integer> inventory = new HashMap<>();
    private List<Crop> crops = new ArrayList<>();
    private List<Animal> animals = new ArrayList<>();
    private Map<String, Double> marketPrices = new HashMap<>();
    private int fieldSize = 5;

    @FXML
    public void initialize() {
        cultureChoice.setItems(FXCollections.observableArrayList("Ble", "Mais", "Riz"));
        animalChoice.setItems(FXCollections.observableArrayList("Vache", "Poule"));
        inventoryListView.setItems(inventoryItems);
        sellInventoryListView.setItems(inventoryItems);
        initializeFieldGrid();

        marketPrices.put("Ble", 90.0);
        marketPrices.put("Mais", 100.0);
        marketPrices.put("Riz", 110.0);
        marketPrices.put("Lait", 70.0);
        marketPrices.put("Œufs", 90.0);

        updateMarketPrices();
    }

    private void initializeFieldGrid() {
        fieldGrid.getChildren().clear();
        for (int row = 0; row < fieldSize; row++) {
            for (int col = 0; col < fieldSize; col++) {
                Label cell = new Label();
                cell.setMinSize(50, 50);
                cell.setStyle("-fx-border-color: grey; -fx-background-color: lightgreen;");
                fieldGrid.add(cell, col, row);
            }
        }
    }

    public void buyCulture() {
        String selectedCulture = cultureChoice.getValue();
        if (selectedCulture == null) return;

        int price = new Random().nextInt(41) + 60;
        if (money >= price) {
            money -= price;
            moneyLabel.setText(String.format("%.2f$", money));

            Crop newCrop = new Crop(selectedCulture);
            crops.add(newCrop);
            newCrop.startGrowth(() -> updateFieldForGrowth(newCrop));

            addCropToField(newCrop);
        } else {
            showAlert("Pas assez d'argent", "Vous n'avez pas assez d'argent pour acheter " + selectedCulture);
        }
    }

    public void buyAnimals() {
        String selectedAnimal = animalChoice.getValue();
        if (selectedAnimal == null) return;

        int price = new Random().nextInt(41) + 100;
        if (money >= price) {
            money -= price;
            moneyLabel.setText(String.format("%.2f$", money));

            Animal newAnimal = new Animal(selectedAnimal);
            animals.add(newAnimal);
            addAnimalToField(newAnimal);
        } else {
            showAlert("Pas assez d'argent", "Vous n'avez pas assez d'argent pour acheter " + selectedAnimal);
        }
    }

    private void addCropToField(Crop crop) {
        for (int row = 0; row < fieldSize; row++) {
            for (int col = 0; col < fieldSize; col++) {
                Label cell = (Label) getNodeFromGridPane(fieldGrid, col, row);
                if (cell != null && cell.getText().isEmpty()) {
                    cell.setText(crop.getName().substring(0, 1));
                    cell.setStyle("-fx-border-color: grey; -fx-background-color: brown;");
                    return;
                }
            }
        }
    }

    private void addAnimalToField(Animal animal) {
        for (int row = 0; row < fieldSize; row++) {
            for (int col = 0; col < fieldSize; col++) {
                Label cell = (Label) getNodeFromGridPane(fieldGrid, col, row);
                if (cell != null && cell.getText().isEmpty()) {
                    cell.setText(animal.getName().substring(0, 1));
                    cell.setStyle("-fx-border-color: grey; -fx-background-color: lightblue;");
                    int finalCol = col;
                    int finalRow = row;
                    cell.setOnMouseClicked(event -> animal.feed(inventory, cell, finalRow, finalCol));
                    return;
                }
            }
        }
    }

    private void updateFieldForGrowth(Crop crop) {
        for (Node node : fieldGrid.getChildren()) {
            Label cell = (Label) node;
            if (cell.getText().equals(crop.getName().substring(0, 1))) {
                cell.setStyle("-fx-border-color: grey; -fx-background-color: yellow;");
            }
        }
    }

    public void harvestCrops() {
        List<Crop> harvestedCrops = new ArrayList<>();

        for (Crop crop : crops) {
            if (crop.isReady()) {
                harvestedCrops.add(crop);
                inventory.put(crop.getName(), inventory.getOrDefault(crop.getName(), 0) + 1);
            }
        }

        if (harvestedCrops.isEmpty()) {
            showAlert("Récolte", "Aucune culture prête à être récoltée !");
            return;
        }

        crops.removeAll(harvestedCrops);
        updateInventory();
        initializeFieldGrid();

        crops.forEach(this::addCropToField);
    }

    public void sellCrops() {
        String selectedItem = sellInventoryListView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showAlert("Erreur", "Sélectionnez une récolte à vendre.");
            return;
        }

        String cropName = selectedItem.split(":")[0].trim();
        int quantityToSell = Integer.parseInt(quantityField.getText());

        if (!inventory.containsKey(cropName) || inventory.get(cropName) < quantityToSell) {
            showAlert("Erreur", "Pas assez de récolte à vendre.");
            return;
        }

        double earnings = marketPrices.getOrDefault(cropName, 0.0) * quantityToSell;
        money += earnings;
        moneyLabel.setText(String.format("%.2f$", money));

        inventory.put(cropName, inventory.get(cropName) - quantityToSell);
        if (inventory.get(cropName) == 0) {
            inventory.remove(cropName);
        }

        updateInventory();
        showAlert("Vente réussie", "Vous avez vendu " + quantityToSell + " " + cropName + " pour " + earnings + "$.");
    }

    private void updateMarketPrices() {
        ObservableList<String> pricesList = FXCollections.observableArrayList();
        for (Map.Entry<String, Double> entry : marketPrices.entrySet()) {
            pricesList.add(entry.getKey() + ": " + entry.getValue() + "$");
        }
        marketPriceListView.setItems(pricesList);
    }

    private void updateInventory() {
        inventoryItems.clear();
        inventory.forEach((key, value) -> inventoryItems.add(key + ": " + value));
    }

    private Node getNodeFromGridPane(GridPane gridPane, int col, int row) {
        return gridPane.getChildren().stream()
                .filter(node -> GridPane.getColumnIndex(node) != null &&
                        GridPane.getRowIndex(node) != null &&
                        GridPane.getColumnIndex(node) == col &&
                        GridPane.getRowIndex(node) == row)
                .findFirst()
                .orElse(null);
    }

    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}