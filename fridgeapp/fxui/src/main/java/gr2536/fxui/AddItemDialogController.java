package gr2536.fxui;

import java.time.LocalDate;

import gr2536.core.item.Item;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;

/**
 * Controller for the “Add Item” dialog.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Wire injected FXML controls</li>
 *   <li>Perform lightweight client-side validation</li>
 *   <li>Expose a factory method to build an immutable {@link Item}</li>
 * </ul>
 */
public class AddItemDialogController {

    // ========== Action buttons ==========
    /** Item name input field. */
    @FXML
    private TextField nameField;
    /** Quantity spinner for add operations. */
    @FXML
    private Spinner<Integer> quantitySpinner;
    /** Expiration date picker. */
    @FXML
    private DatePicker expirationPicker;
    //* DialogPane Root */
    @FXML
    private DialogPane dialogPane;

    //* CSS PseudoClass*/
    private static PseudoClass ERROR = PseudoClass.getPseudoClass("error");

    //Shared validation binding
    private BooleanBinding nameBlank;


    /**
    * Initializes control state, validation styling, and OK-button enabling.
    * Called automatically by the {@code FXMLLoader} after FXML injection.
    */
    @FXML
    private void initialize() {
        setupSpinner();

        //Binding reused by Validation and OkButtonBinding
        nameBlank = Bindings.createBooleanBinding(
            () -> nameField.getText() == null || nameField.getText().trim().isEmpty(),
            nameField.textProperty()
        );

        setupValidation();
        //Loads CSS
        dialogPane.getStyleClass().add("add-item-dialog");
        setupOkButtonBinding();
    }

    /** Sets up the quantity spinner for add operations. */
    private void setupSpinner() {
        quantitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 99, 1));
    }

     /**
     * Keeps the UI’s validation state in sync with user input.
     * Bind PseudoClass Error to blank name.
     */
    private void setupValidation() {
        // Toggle CSS error
        nameBlank.addListener((obs, wasBlank, isBlank) ->
            nameField.pseudoClassStateChanged(ERROR, isBlank)
        );
        // Set initial state
        nameField.pseudoClassStateChanged(ERROR, nameBlank.get()); 
    }

    /**
     * Disables the dialog’s OK button while the name field is blank.
     * Looks up the OK button from the injected {@link DialogPane} and binds its
     * {@code disableProperty()} to {@code nameBlank}.
     */
    private void setupOkButtonBinding() {
        javafx.application.Platform.runLater(() -> {

            ButtonType okType = dialogPane.getButtonTypes().stream()
                .filter(bt -> bt.getButtonData() == ButtonBar.ButtonData.OK_DONE)
                .findFirst()
                .orElse(null);

            if (okType != null) {
                Button ok = (Button) dialogPane.lookupButton(okType);
                if (ok != null) {
                    ok.disableProperty().bind(nameBlank);
                    ok.getStyleClass().add("primary");
                }
            }

            ButtonType cancelType = dialogPane.getButtonTypes().stream()
                .filter(bt -> bt.getButtonData() == ButtonBar.ButtonData.CANCEL_CLOSE)
                .findFirst()
                .orElse(null);

            if (cancelType != null) {
                Button cancel = (Button) dialogPane.lookupButton(cancelType);
                if (cancel != null) {
                    cancel.getStyleClass().add("secondary");
                }
            }
        });
    }


    /** Builds an Item from the current UI values.
     *  @return immutable {@link Item}
     *  @throws IllegalArgumentException if name is blank or quantity ≤ 0 (as enforced by {@link Item})
     */
    Item buildItemOrThrow() {
        String name = nameField.getText() == null ? "" : nameField.getText().trim();
        Integer qty = quantitySpinner.getValue() == null ? 1 : quantitySpinner.getValue();
        LocalDate exp = expirationPicker.getValue();

        return new Item(name, qty, exp);
    }
}
