package com.finance.tracker.frontend;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.File;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class FinanceTrackerDesktopApp extends Application {
    private final ApiClient apiClient = new ApiClient("http://localhost:8080");
    private final Label statusLabel = new Label("Ready");
    private final Label stepTitle = new Label();
    private final Label stepHint = new Label();
    private final Button backButton = new Button("Back");
    private final Button nextButton = new Button("Next");
    private final List<ScrollPane> steps = new ArrayList<>();
    private final boolean[] completed = new boolean[] {false, false, false, false, false};
    private int currentStep = 0;
    private UUID activeUserId;
    private String primaryPaymentMethodId;
    private Stage primaryStage;
    private DashboardData latestDashboardData;
    private byte[] latestExportBytes;

    // Dashboard UI
    private final Label monthlyExpensesValue = new Label("-");
    private final Label monthlySubscriptionsValue = new Label("-");
    private final Label annualTotalValue = new Label("-");
    private final Label budgetHealthValue = new Label("-");
    private final PieChart categoryPieChart = new PieChart();
    private final BarChart<String, Number> annualBarChart = new BarChart<>(new CategoryAxis(), new NumberAxis());
    private final ListView<String> recommendationsList = new ListView<>();
    private final ListView<String> notificationPreviewList = new ListView<>();

    // Step 1: identity
    private final TextField nameField = new TextField();
    private final TextField emailField = new TextField();
    private final TextField currencyField = new TextField("INR");
    private final PasswordField passwordField = new PasswordField();

    // Step 2: wallet setup
    private final ComboBox<String> paymentTypeField = combo("CREDIT_CARD", "BANK_ACCOUNT", "DIGITAL_WALLET");
    private final TextField holderNameField = new TextField();
    private final TextField paymentPrimaryField = new TextField();
    private final TextField paymentSecondaryField = new TextField();
    private final TextField defaultBudgetLimitField = new TextField("5000");

    // Step 3a: subscription
    private final TextField subNameField = new TextField();
    private final TextField subCostField = new TextField();
    private final ComboBox<String> subCategoryField =
            combo("ENTERTAINMENT", "HEALTH_FITNESS", "UTILITIES", "FOOD_DINING", "TRANSPORTATION", "OTHER");
    private final ComboBox<String> subFrequencyField = combo("WEEKLY", "MONTHLY", "QUARTERLY", "ANNUALLY", "CUSTOM");
    private final TextField subCustomDaysField = new TextField("30");
    private final DatePicker subStartDateField = new DatePicker(LocalDate.now());

    // Step 3b: expense
    private final TextField expenseAmountField = new TextField();
    private final TextField expenseDescriptionField = new TextField();
    private final ComboBox<String> expenseCategoryField =
            combo("ENTERTAINMENT", "HEALTH_FITNESS", "UTILITIES", "FOOD_DINING", "TRANSPORTATION", "OTHER");
    private final DatePicker expenseDateField = new DatePicker(LocalDate.now());

    private static final String[] STEP_TITLES = {
        "Step 1 of 5 - Create Your Profile",
        "Step 2 of 5 - Add Payment Setup",
        "Step 3 of 5 - Add First Subscription",
        "Step 4 of 5 - Log First Expense",
        "Step 5 of 5 - Insights Dashboard"
    };

    private static final String[] STEP_HINTS = {
        "Start with your identity details so the app can securely initialize your workspace.",
        "Add one payment method. The app will automatically set safe defaults in the background.",
        "Track a recurring service once and let the system manage renewals and reminders.",
        "Capture a daily spend and let budgets/categories/notifications refresh automatically.",
        "See your financial picture instantly with monthly, annual, and optimization views."
    };

    @Override
    public void start(Stage stage) {
        stage.setTitle("LedgerShield - Personal Finance Tracker");
        this.primaryStage = stage;
        initializeSteps();

        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-root");
        root.setTop(buildTop());
        root.setCenter(buildMain());
        root.setBottom(buildFooter());

        Scene scene = new Scene(root, 1200, 820);
        scene.getStylesheets().add(getClass().getResource("/ui/styles/dark-theme.css").toExternalForm());

        stage.setMinWidth(980);
        stage.setMinHeight(680);
        stage.setScene(scene);
        stage.show();
        renderStep();
    }

    private void initializeSteps() {
        steps.clear();
        steps.add(wrapStep(buildIdentityStep()));
        steps.add(wrapStep(buildWalletStep()));
        steps.add(wrapStep(buildSubscriptionStep()));
        steps.add(wrapStep(buildExpenseStep()));
        steps.add(wrapStep(buildInsightsStep()));
    }

    private VBox buildTop() {
        Label title = new Label("LedgerShield");
        title.getStyleClass().add("app-title");
        Label subtitle = new Label("A guided finance assistant - one task at a time.");
        subtitle.getStyleClass().add("app-subtitle");
        stepTitle.getStyleClass().add("card-title");
        stepHint.getStyleClass().add("app-subtitle");
        stepHint.setWrapText(true);

        VBox box = new VBox(8, title, subtitle, stepTitle, stepHint);
        box.setPadding(new Insets(14));
        return box;
    }

    private BorderPane buildMain() {
        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(12, 14, 6, 14));
        pane.setCenter(buildWorkflow());
        return pane;
    }

    private StackPane buildWorkflow() {
        StackPane stack = new StackPane();
        stack.setPrefWidth(1050);
        stack.getChildren().addAll(steps);
        return stack;
    }

    private VBox buildIdentityStep() {
        nameField.setPromptText("Full name");
        emailField.setPromptText("Email");
        currencyField.setPromptText("Currency");
        passwordField.setPromptText("Password");

        Button register = new Button("Create Profile");
        register.getStyleClass().add("led-button");
        register.setOnAction(event -> {
            Map<String, Object> payload = new HashMap<>();
            payload.put("name", nameField.getText());
            payload.put("email", emailField.getText());
            payload.put("currencySetting", currencyField.getText());
            payload.put(
                    "notificationPreferences",
                    Map.of("BUDGET_WARNING", true, "BUDGET_EXCEEDED", true, "RENEWAL_REMINDER", true));
            runRequest("Create profile", () -> apiClient.post("/api/v1/auth/register", payload), response -> {
                if (response.has("userId")) {
                    activeUserId = UUID.fromString(response.get("userId").asText());
                    completed[0] = true;
                    setStatus("Profile ready. You can continue.", false);
                    showInfoDialog("Welcome", "Your profile has been created successfully.");
                }
            });
        });

        Button login = new Button("Login");
        login.setOnAction(event -> runRequest(
                "Login",
                () -> {
                    JsonNode loginResponse = apiClient.post(
                            "/api/v1/auth/login", Map.of("email", emailField.getText(), "password", passwordField.getText()));
                    JsonNode users = apiClient.get("/api/v1/users");
                    String email = emailField.getText().trim();
                    if (users.isArray()) {
                        for (JsonNode user : users) {
                            if (email.equalsIgnoreCase(user.path("email").asText())) {
                                activeUserId = UUID.fromString(user.path("userId").asText());
                                break;
                            }
                        }
                    }
                    if (activeUserId == null) {
                        throw new IllegalArgumentException("Login succeeded, but user profile could not be loaded.");
                    }
                    return loginResponse;
                },
                result -> {
                    completed[0] = true;
                    setStatus("Login successful. Moving to payment setup...", false);
                    showInfoDialog("Welcome Back", "Signed in successfully.");
                    autoAdvanceStep();
                }));

        bindRequired(register, nameField, emailField, currencyField, passwordField);

        GridPane grid = grid();
        addRow(grid, 0, "Name", nameField, "Email", emailField);
        addRow(grid, 1, "Currency", currencyField, "Password", passwordField);

        VBox step = card("Identity", grid, row(register, login));
        return step;
    }

    private VBox buildWalletStep() {
        holderNameField.setPromptText("Account holder");
        paymentPrimaryField.setPromptText("Card/Routing/Provider");
        paymentSecondaryField.setPromptText("Expiry/Account/Email");
        defaultBudgetLimitField.setPromptText("Default category budget");

        Button saveWallet = new Button("Save Wallet");
        saveWallet.getStyleClass().add("led-button");
        saveWallet.setOnAction(event -> runRequest("Save wallet", () -> {
            UUID userId = requireUserId();
            JsonNode payment = apiClient.post("/api/v1/users/" + userId + "/payment-methods", paymentPayload());
            primaryPaymentMethodId = payment.get("paymentMethodId").asText();
            String category = "OTHER";
            Map<String, Object> budget = Map.of(
                    "name", category,
                    "spendingLimit", parseDouble(defaultBudgetLimitField.getText(), "default budget"),
                    "period", "MONTHLY",
                    "currentSpending", 0);
            apiClient.post("/api/v1/users/" + userId + "/budgets", budget);
            apiClient.post("/api/v1/notifications/scheduler/run", null);
            return payment;
        }, result -> {
            completed[1] = true;
            setStatus("Wallet ready. Continue to subscriptions.", false);
            showInfoDialog("Payment Setup Complete", "Your wallet is linked and smart budget defaults are active.");
        }));

        bindRequired(saveWallet, holderNameField, paymentPrimaryField, paymentSecondaryField, defaultBudgetLimitField);

        GridPane grid = grid();
        addRow(grid, 0, "Type", paymentTypeField, "Holder", holderNameField);
        addRow(grid, 1, "Primary", paymentPrimaryField, "Secondary", paymentSecondaryField);
        addRow(grid, 2, "Default Budget", defaultBudgetLimitField, null, null);

        return card("Payment Setup", grid, saveWallet);
    }

    private VBox buildSubscriptionStep() {
        subNameField.setPromptText("Service (Netflix, Spotify...)");
        subCostField.setPromptText("Subscription cost");
        subCustomDaysField.setPromptText("Custom days");

        Button addSub = new Button("Add Subscription");
        addSub.getStyleClass().add("led-button");
        addSub.setOnAction(event -> runRequest("Add subscription", () -> {
            UUID userId = requireUserId();
            String paymentMethodId = latestPaymentMethodId(userId);
            Map<String, Object> body = new HashMap<>();
            body.put("serviceName", subNameField.getText());
            body.put("cost", parseDouble(subCostField.getText(), "subscription cost"));
            body.put("paymentMethodId", paymentMethodId);
            body.put("categoryType", subCategoryField.getValue());
            body.put("categoryName", subCategoryField.getValue());
            body.put("status", "ACTIVE");
            body.put("lastAccessDate", LocalDate.now().toString());
            body.put("frequency", subFrequencyField.getValue());
            body.put("customIntervalDays", parseInt(subCustomDaysField.getText(), "custom days"));
            body.put("startDate", subStartDateField.getValue().toString());
            JsonNode response = apiClient.post("/api/v1/users/" + userId + "/subscriptions", body);
            apiClient.get("/api/v1/users/" + userId + "/subscriptions");
            apiClient.get("/api/v1/users/" + userId + "/notifications");
            return response;
        }, result -> {
            completed[2] = true;
            setStatus("Subscription saved. Continue to expense logging.", false);
            showInfoDialog("Subscription Added", "Great! Your recurring subscription is now tracked.");
        }));

        bindRequired(addSub, subNameField, subCostField, subCustomDaysField);

        GridPane subGrid = grid();
        addRow(subGrid, 0, "Service", subNameField, "Cost", subCostField);
        addRow(subGrid, 1, "Category", subCategoryField, "Frequency", subFrequencyField);
        addRow(subGrid, 2, "Custom Days", subCustomDaysField, "Start", subStartDateField);

        return card("First Subscription", subGrid, addSub);
    }

    private VBox buildExpenseStep() {
        expenseAmountField.setPromptText("Expense amount");
        expenseDescriptionField.setPromptText("Expense details");

        Button addExpense = new Button("Log Expense");
        addExpense.getStyleClass().add("led-button");
        addExpense.setOnAction(event -> runRequest("Log expense", () -> {
            UUID userId = requireUserId();
            String paymentMethodId = latestPaymentMethodId(userId);
            Map<String, Object> body = new HashMap<>();
            body.put("amount", parseDouble(expenseAmountField.getText(), "expense amount"));
            body.put("date", expenseDateField.getValue().toString());
            body.put("description", expenseDescriptionField.getText());
            body.put("paymentMethodId", paymentMethodId);
            body.put("categoryType", expenseCategoryField.getValue());
            body.put("categoryName", expenseCategoryField.getValue());
            body.put("budgetLimit", parseDouble(defaultBudgetLimitField.getText(), "budget limit"));
            JsonNode response = apiClient.post("/api/v1/users/" + userId + "/expenses", body);
            apiClient.get("/api/v1/users/" + userId + "/expenses");
            apiClient.get("/api/v1/users/" + userId + "/budgets");
            apiClient.get("/api/v1/users/" + userId + "/categories");
            apiClient.get("/api/v1/users/" + userId + "/notifications");
            return response;
        }, result -> {
            completed[3] = true;
            setStatus("Expense logged. Dashboard is now ready.", false);
            showInfoDialog("Expense Logged", "Expense saved. Your budgets and categories were refreshed automatically.");
        }));

        bindRequired(addExpense, expenseAmountField, expenseDescriptionField);

        GridPane expenseGrid = grid();
        addRow(expenseGrid, 0, "Amount", expenseAmountField, "Date", expenseDateField);
        addRow(expenseGrid, 1, "Category", expenseCategoryField, "Details", expenseDescriptionField);

        return card("First Expense", expenseGrid, addExpense);
    }

    private VBox buildInsightsStep() {
        Button dashboard = new Button("Refresh Dashboard");
        dashboard.getStyleClass().add("led-button");
        dashboard.setOnAction(event -> runRequest("Refresh dashboard", this::refreshDashboardData, result -> {
            if (latestDashboardData != null) {
                updateDashboardWidgets(
                        latestDashboardData.monthly(),
                        latestDashboardData.annual(),
                        latestDashboardData.optimization(),
                        latestDashboardData.notifications());
            }
            completed[4] = true;
            showInfoDialog("Dashboard Updated", "Your insights and reports are up to date.");
        }));

        Button monthlyReport = new Button("Generate Monthly Report");
        monthlyReport.setOnAction(event -> runRequest("Monthly report", () -> apiClient.get(apiClient.withQuery(
                        "/api/v1/users/" + requireUserId() + "/reports/monthly",
                        Map.of("month", String.valueOf(LocalDate.now().getMonthValue()), "year", String.valueOf(LocalDate.now().getYear())))),
                result -> showInfoDialog(
                        "Monthly Report Ready",
                        "Expenses: " + formatAmount(result.path("totalExpenseCost").asDouble())
                                + "\nSubscriptions: " + formatAmount(result.path("totalSubscriptionCost").asDouble()))));

        Button annualReport = new Button("Generate Annual Report");
        annualReport.setOnAction(event -> runRequest(
                "Annual report",
                () -> apiClient.get(apiClient.withQuery(
                        "/api/v1/users/" + requireUserId() + "/reports/annual", Map.of("year", String.valueOf(LocalDate.now().getYear())))),
                result -> showInfoDialog(
                        "Annual Report Ready",
                        "Yearly total: " + formatAmount(result.path("summary").path("grandTotal").asDouble()))));

        Button optimizationReport = new Button("Optimization Suggestions");
        optimizationReport.setOnAction(event -> runRequest(
                "Optimization report",
                () -> apiClient.get("/api/v1/users/" + requireUserId() + "/reports/optimization"),
                result -> showInfoDialog(
                        "Optimization Summary",
                        "Top expenses found: " + result.path("topExpenses").size() + "\nPotential cancellations: "
                                + result.path("unusedSubscriptions").size())));

        Button exportCsv = new Button("Export CSV");
        exportCsv.setOnAction(event -> exportReport("csv"));
        Button exportPdf = new Button("Export PDF");
        exportPdf.setOnAction(event -> exportReport("pdf"));

        monthlyExpensesValue.getStyleClass().add("metric-value");
        monthlySubscriptionsValue.getStyleClass().add("metric-value");
        annualTotalValue.getStyleClass().add("metric-value");
        budgetHealthValue.getStyleClass().add("metric-value");

        categoryPieChart.setLegendVisible(true);
        categoryPieChart.setLabelsVisible(true);
        categoryPieChart.setMinHeight(250);
        categoryPieChart.getStyleClass().add("dashboard-chart");

        annualBarChart.setAnimated(false);
        annualBarChart.setLegendVisible(false);
        annualBarChart.setTitle("Monthly Spending Trend");
        annualBarChart.setMinHeight(270);
        annualBarChart.getStyleClass().add("dashboard-chart");

        recommendationsList.setMinHeight(140);
        notificationPreviewList.setMinHeight(140);
        recommendationsList.getStyleClass().add("dashboard-list");
        notificationPreviewList.getStyleClass().add("dashboard-list");

        GridPane metrics = grid();
        addRow(metrics, 0, "Monthly Expenses", monthlyExpensesValue, "Monthly Subscriptions", monthlySubscriptionsValue);
        addRow(metrics, 1, "Annual Total", annualTotalValue, "Budget Health", budgetHealthValue);

        VBox reports = card(
                "Reports",
                row(monthlyReport, annualReport, optimizationReport),
                row(exportCsv, exportPdf));
        VBox charts = card("Visual Insights", categoryPieChart, annualBarChart);
        VBox smartCards = card("Smart Suggestions", new Label("Optimization Highlights"), recommendationsList, new Label("Recent Alerts"), notificationPreviewList);
        VBox controls = card("Dashboard Actions", row(dashboard), metrics);

        return card("Insights Dashboard", controls, reports, charts, smartCards);
    }

    private Map<String, Object> paymentPayload() {
        Map<String, Object> body = new HashMap<>();
        body.put("type", paymentTypeField.getValue());
        body.put("holderName", holderNameField.getText());
        body.put("default", true);
        switch (paymentTypeField.getValue()) {
            case "CREDIT_CARD" -> {
                body.put("maskedCardNumber", paymentPrimaryField.getText());
                body.put("expiryDate", paymentSecondaryField.getText());
            }
            case "BANK_ACCOUNT" -> {
                body.put("routingNumber", paymentPrimaryField.getText());
                body.put("maskedAccountNumber", paymentSecondaryField.getText());
            }
            default -> {
                body.put("walletProvider", paymentPrimaryField.getText());
                body.put("linkedEmail", paymentSecondaryField.getText());
            }
        }
        return body;
    }

    private String latestPaymentMethodId(UUID userId) throws Exception {
        if (primaryPaymentMethodId != null && !primaryPaymentMethodId.isBlank()) {
            return primaryPaymentMethodId;
        }
        JsonNode methods = apiClient.get("/api/v1/users/" + userId + "/payment-methods");
        if (!methods.isArray() || methods.isEmpty()) {
            throw new IllegalArgumentException("No payment method found. Complete Step 2 first.");
        }
        primaryPaymentMethodId = methods.get(methods.size() - 1).get("paymentMethodId").asText();
        return primaryPaymentMethodId;
    }

    private HBox buildFooter() {
        backButton.setOnAction(event -> {
            if (currentStep > 0) {
                currentStep--;
                renderStep();
            }
        });
        nextButton.getStyleClass().add("led-button");
        nextButton.setOnAction(event -> {
            if (canMoveNext() && currentStep < steps.size() - 1) {
                currentStep++;
                renderStep();
            }
        });

        statusLabel.getStyleClass().add("status-ok");
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox box = new HBox(10, backButton, nextButton, spacer, statusLabel);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(8, 14, 12, 14));
        return box;
    }

    private VBox card(String title, javafx.scene.Node... nodes) {
        Label label = new Label(title);
        label.getStyleClass().add("card-title");
        VBox box = new VBox(10);
        box.getStyleClass().add("card");
        box.setPadding(new Insets(14));
        box.getChildren().add(label);
        box.getChildren().addAll(nodes);
        return box;
    }

    private GridPane grid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        return grid;
    }

    private void addRow(GridPane grid, int row, String labelA, javafx.scene.Node fieldA, String labelB, javafx.scene.Node fieldB) {
        if (labelA != null && fieldA != null) {
            grid.add(new Label(labelA), 0, row);
            grid.add(fieldA, 1, row);
        }
        if (labelB != null && fieldB != null) {
            grid.add(new Label(labelB), 2, row);
            grid.add(fieldB, 3, row);
        }
    }

    private HBox row(javafx.scene.Node... nodes) {
        HBox row = new HBox(10, nodes);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private UUID requireUserId() {
        if (activeUserId == null) {
            throw new IllegalArgumentException("Complete Step 1 to create your profile first.");
        }
        return activeUserId;
    }

    private ComboBox<String> combo(String... values) {
        ComboBox<String> combo = new ComboBox<>();
        combo.getItems().addAll(values);
        if (values.length > 0) {
            combo.setValue(values[0]);
        }
        combo.setMaxWidth(Double.MAX_VALUE);
        return combo;
    }

    private double parseDouble(String value, String label) {
        try {
            return Double.parseDouble(value);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid " + label + ": " + value);
        }
    }

    private int parseInt(String value, String label) {
        try {
            return Integer.parseInt(value);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid " + label + ": " + value);
        }
    }

    private void bindRequired(Button button, TextInputControl... fields) {
        BooleanBinding anyBlank = Bindings.createBooleanBinding(
                () -> {
                    for (TextInputControl field : fields) {
                        if (field.getText() == null || field.getText().isBlank()) {
                            return true;
                        }
                    }
                    return false;
                },
                collectTextProperties(fields));
        button.disableProperty().bind(anyBlank);
    }

    private javafx.beans.Observable[] collectTextProperties(TextInputControl[] controls) {
        javafx.beans.Observable[] observables = new javafx.beans.Observable[controls.length];
        for (int i = 0; i < controls.length; i++) {
            observables[i] = controls[i].textProperty();
        }
        return observables;
    }

    private ScrollPane wrapStep(VBox content) {
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPadding(new Insets(0));
        scrollPane.getStyleClass().add("transparent-scroll");
        return scrollPane;
    }

    private void renderStep() {
        for (int i = 0; i < steps.size(); i++) {
            steps.get(i).setVisible(i == currentStep);
            steps.get(i).setManaged(i == currentStep);
        }
        stepTitle.setText(STEP_TITLES[currentStep]);
        stepHint.setText(STEP_HINTS[currentStep]);
        backButton.setDisable(currentStep == 0);
        nextButton.setDisable(!canMoveNext());
    }

    private boolean canMoveNext() {
        if (currentStep >= steps.size() - 1) {
            return false;
        }
        return completed[currentStep];
    }

    private void autoAdvanceStep() {
        if (canMoveNext() && currentStep < steps.size() - 1) {
            currentStep++;
            renderStep();
        }
    }

    private void runRequest(String action, RequestSupplier supplier, ResponseConsumer consumer) {
        setStatus(action + " in progress...", true);
        CompletableFuture.supplyAsync(() -> {
                    try {
                        return supplier.get();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .whenComplete((result, throwable) -> Platform.runLater(() -> {
                    if (throwable != null) {
                        setStatus(action + " failed: " + unwrap(throwable).getMessage(), true);
                        showErrorDialog("Action Failed", humanizeError(unwrap(throwable).getMessage()));
                        return;
                    }
                    try {
                        if (consumer != null) {
                            consumer.accept(result);
                        }
                        setStatus(action + " completed", false);
                        renderStep();
                    } catch (Exception ex) {
                        setStatus(action + " failed: " + ex.getMessage(), true);
                        showErrorDialog("Action Failed", humanizeError(ex.getMessage()));
                    }
                }));
    }

    private void runAsync(String action, RequestSupplier supplier) {
        runRequest(action, supplier, null);
    }

    private Throwable unwrap(Throwable throwable) {
        return throwable.getCause() == null ? throwable : throwable.getCause();
    }

    private void setStatus(String message, boolean pendingOrError) {
        statusLabel.setText(message);
        statusLabel.getStyleClass().removeAll("status-ok", "status-warn");
        statusLabel.getStyleClass().add(pendingOrError ? "status-warn" : "status-ok");
    }

    private JsonNode refreshDashboardData() throws Exception {
        UUID userId = requireUserId();
        int month = LocalDate.now().getMonthValue();
        int year = LocalDate.now().getYear();
        JsonNode monthly = apiClient.get(apiClient.withQuery(
                "/api/v1/users/" + userId + "/reports/monthly",
                Map.of("month", String.valueOf(month), "year", String.valueOf(year))));
        JsonNode annual = apiClient.get(
                apiClient.withQuery("/api/v1/users/" + userId + "/reports/annual", Map.of("year", String.valueOf(year))));
        JsonNode optimization = apiClient.get("/api/v1/users/" + userId + "/reports/optimization");
        JsonNode notifications = apiClient.get("/api/v1/users/" + userId + "/notifications");
        latestDashboardData = new DashboardData(monthly, annual, optimization, notifications);
        return monthly;
    }

    private void updateDashboardWidgets(JsonNode monthly, JsonNode annual, JsonNode optimization, JsonNode notifications) {
        monthlyExpensesValue.setText(formatAmount(monthly.path("totalExpenseCost").asDouble()));
        monthlySubscriptionsValue.setText(formatAmount(monthly.path("totalSubscriptionCost").asDouble()));
        annualTotalValue.setText(formatAmount(annual.path("summary").path("grandTotal").asDouble()));
        double expenses = monthly.path("totalExpenseCost").asDouble();
        double subs = monthly.path("totalSubscriptionCost").asDouble();
        budgetHealthValue.setText(expenses + subs > 0 ? "Active monitoring" : "No spends recorded");

        List<PieChart.Data> pieData = new ArrayList<>();
        JsonNode breakdown = monthly.path("categoryBreakdown");
        if (breakdown.isObject()) {
            breakdown.fields().forEachRemaining(entry -> pieData.add(new PieChart.Data(entry.getKey(), entry.getValue().asDouble())));
        }
        if (pieData.isEmpty()) {
            pieData.add(new PieChart.Data("No Data", 1));
        }
        categoryPieChart.setData(FXCollections.observableArrayList(pieData));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        JsonNode monthlyReports = annual.path("monthlyReports");
        if (monthlyReports.isArray()) {
            for (int i = 0; i < monthlyReports.size(); i++) {
                JsonNode report = monthlyReports.get(i);
                double total = report.path("totalExpenseCost").asDouble() + report.path("totalSubscriptionCost").asDouble();
                series.getData().add(new XYChart.Data<>(String.valueOf(i + 1), total));
            }
        }
        annualBarChart.getData().setAll(series);

        recommendationsList.getItems().clear();
        JsonNode topExpenses = optimization.path("topExpenses");
        int topCount = Math.min(5, topExpenses.size());
        for (int i = 0; i < topCount; i++) {
            JsonNode expense = topExpenses.get(i);
            recommendationsList
                    .getItems()
                    .add((i + 1) + ". " + expense.path("description").asText("Expense") + " - " + formatAmount(expense.path("amount").asDouble()));
        }
        JsonNode unusedSubs = optimization.path("unusedSubscriptions");
        for (int i = 0; i < Math.min(3, unusedSubs.size()); i++) {
            JsonNode sub = unusedSubs.get(i).path("subscription");
            recommendationsList.getItems().add("Consider reviewing " + sub.path("serviceName").asText("subscription"));
        }
        if (recommendationsList.getItems().isEmpty()) {
            recommendationsList.getItems().add("No optimization suggestions at the moment.");
        }

        notificationPreviewList.getItems().clear();
        for (int i = 0; i < Math.min(6, notifications.size()); i++) {
            JsonNode note = notifications.get(i);
            notificationPreviewList.getItems().add(note.path("message").asText("Notification"));
        }
        if (notificationPreviewList.getItems().isEmpty()) {
            notificationPreviewList.getItems().add("No recent notifications.");
        }
    }

    private void exportReport(String format) {
        runRequest("Export " + format.toUpperCase(), () -> {
            UUID userId = requireUserId();
            String path = apiClient.withQuery(
                    "/api/v1/users/" + userId + "/reports/export",
                    Map.of(
                            "type", "monthly",
                            "month", String.valueOf(LocalDate.now().getMonthValue()),
                            "year", String.valueOf(LocalDate.now().getYear()),
                            "format", format));
            latestExportBytes = apiClient.getBytes(path);
            return null;
        }, ignored -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Save " + format.toUpperCase() + " Report");
            chooser.setInitialFileName("ledger-report." + format);
            File file = chooser.showSaveDialog(primaryStage);
            if (file == null) {
                setStatus("Export cancelled", false);
                return;
            }
            Files.write(file.toPath(), latestExportBytes);
            showInfoDialog("Export Complete", "Your report has been downloaded successfully.");
        });
    }

    private String formatAmount(double amount) {
        return String.format("Rs. %,.2f", amount);
    }

    private String humanizeError(String message) {
        if (message == null || message.isBlank()) {
            return "Something went wrong. Please try again.";
        }
        return message.replace("HTTP 500", "server issue").replace("HTTP 400", "input issue");
    }

    private void showInfoDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.setTitle(title);
        alert.getDialogPane().getStyleClass().add("app-dialog");
        alert.showAndWait();
    }

    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.setTitle(title);
        alert.getDialogPane().getStyleClass().add("app-dialog");
        alert.showAndWait();
    }

    private record DashboardData(JsonNode monthly, JsonNode annual, JsonNode optimization, JsonNode notifications) {}

    public static void main(String[] args) {
        launch(args);
    }

    @FunctionalInterface
    private interface RequestSupplier {
        JsonNode get() throws Exception;
    }

    @FunctionalInterface
    private interface ResponseConsumer {
        void accept(JsonNode response) throws Exception;
    }
}
