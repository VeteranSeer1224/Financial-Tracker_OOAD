package com.minip.financialtracker.ui;

import com.minip.financialtracker.model.Budget;
import com.minip.financialtracker.model.Category;
import com.minip.financialtracker.model.Subscription;
import com.minip.financialtracker.model.Transaction;
import com.minip.financialtracker.model.TransactionType;
import com.minip.financialtracker.service.FinancialTracker;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Map;

public class FinancialTrackerController {
    private final FinancialTracker tracker = new FinancialTracker();

    private final ObservableList<Transaction> transactionRows = FXCollections.observableArrayList();
    private final ObservableList<BudgetRow> budgetRows = FXCollections.observableArrayList();
    private final ObservableList<Subscription> subscriptionRows = FXCollections.observableArrayList();

    private Label balanceValue;
    private Label monthIncomeValue;
    private Label monthExpenseValue;
    private Label monthNetValue;
    private Label infoMessage;

    public FinancialTrackerController() {
        seedDemoData();
        refreshAll();
    }

    public Parent buildRoot() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-root");
        root.setTop(buildHeader());
        root.setCenter(buildTabs());
        root.setBottom(buildInfoBar());
        return root;
    }

    private VBox buildHeader() {
        Label title = new Label("Personal Finance Tracker");
        title.getStyleClass().add("title-label");
        Label subtitle = new Label("Monitor cash flow, budgets, and recurring commitments with confidence.");
        subtitle.getStyleClass().add("subtitle-label");

        HBox metrics = new HBox(16,
                metricCard("Current Balance", balanceValue = new Label()),
                metricCard("Month Income", monthIncomeValue = new Label()),
                metricCard("Month Expense", monthExpenseValue = new Label()),
                metricCard("Month Net", monthNetValue = new Label()));
        metrics.setAlignment(Pos.CENTER_LEFT);

        VBox header = new VBox(10, title, subtitle, metrics);
        header.setPadding(new Insets(20, 24, 16, 24));
        return header;
    }

    private VBox metricCard(String heading, Label value) {
        value.getStyleClass().add("metric-value");
        Label label = new Label(heading);
        label.getStyleClass().add("metric-heading");

        VBox box = new VBox(8, label, value);
        box.getStyleClass().add("metric-card");
        box.setMinWidth(200);
        return box;
    }

    private TabPane buildTabs() {
        TabPane tabs = new TabPane(
                buildTransactionsTab(),
                buildBudgetsTab(),
                buildSubscriptionsTab(),
                buildSummaryTab()
        );
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.setPadding(new Insets(0, 16, 12, 16));
        return tabs;
    }

    private Tab buildTransactionsTab() {
        Tab tab = new Tab("Transactions");

        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Description");
        TextField amountField = new TextField();
        amountField.setPromptText("Amount");
        ComboBox<Category> categoryBox = createCategoryCombo();
        ComboBox<TransactionType> typeBox = new ComboBox<>(FXCollections.observableArrayList(TransactionType.values()));
        typeBox.getSelectionModel().select(TransactionType.EXPENSE);
        DatePicker datePicker = new DatePicker(LocalDate.now());

        Button addButton = new Button("Add Transaction");
        addButton.getStyleClass().add("primary-btn");
        addButton.setOnAction(event -> {
            try {
                String description = descriptionField.getText();
                BigDecimal amount = new BigDecimal(amountField.getText().trim());
                Category category = categoryBox.getValue();
                LocalDate date = datePicker.getValue() == null ? LocalDate.now() : datePicker.getValue();
                TransactionType type = typeBox.getValue();
                if (type == TransactionType.INCOME) {
                    tracker.addIncome(description, amount, category, date);
                } else {
                    tracker.addExpense(description, amount, category, date);
                }
                descriptionField.clear();
                amountField.clear();
                refreshAll();
                setMessage("Transaction recorded successfully.");
            } catch (Exception ex) {
                setMessage("Could not add transaction: " + ex.getMessage());
            }
        });

        GridPane form = new GridPane(12, 12);
        form.getStyleClass().add("content-card");
        form.setPadding(new Insets(18));
        form.add(new Label("Type"), 0, 0);
        form.add(typeBox, 1, 0);
        form.add(new Label("Description"), 2, 0);
        form.add(descriptionField, 3, 0);
        form.add(new Label("Amount"), 0, 1);
        form.add(amountField, 1, 1);
        form.add(new Label("Category"), 2, 1);
        form.add(categoryBox, 3, 1);
        form.add(new Label("Date"), 0, 2);
        form.add(datePicker, 1, 2);
        form.add(addButton, 3, 2);
        GridPane.setHgrow(descriptionField, Priority.ALWAYS);
        GridPane.setHgrow(amountField, Priority.ALWAYS);
        GridPane.setHgrow(categoryBox, Priority.ALWAYS);

        TableView<Transaction> table = new TableView<>(transactionRows);
        table.getStyleClass().add("content-card");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getColumns().add(column("Date", tx -> tx.getDate().toString()));
        table.getColumns().add(column("Type", tx -> tx.getType().name()));
        table.getColumns().add(column("Category", tx -> friendlyCategory(tx.getCategory())));
        table.getColumns().add(column("Amount", tx -> tx.getAmount().toPlainString()));
        table.getColumns().add(column("Description", Transaction::getDescription));

        VBox layout = new VBox(14, form, table);
        layout.setPadding(new Insets(8));
        VBox.setVgrow(table, Priority.ALWAYS);
        tab.setContent(layout);
        return tab;
    }

    private Tab buildBudgetsTab() {
        Tab tab = new Tab("Budgets");
        ComboBox<Category> categoryBox = createCategoryCombo();
        TextField limitField = new TextField();
        limitField.setPromptText("Monthly limit");

        Button saveButton = new Button("Save Budget");
        saveButton.getStyleClass().add("primary-btn");
        saveButton.setOnAction(event -> {
            try {
                tracker.setBudget(categoryBox.getValue(), new BigDecimal(limitField.getText().trim()));
                limitField.clear();
                refreshAll();
                setMessage("Budget saved.");
            } catch (Exception ex) {
                setMessage("Could not save budget: " + ex.getMessage());
            }
        });

        HBox form = new HBox(12,
                new Label("Category"), categoryBox,
                new Label("Limit"), limitField,
                saveButton);
        form.getStyleClass().add("content-card");
        form.setPadding(new Insets(18));
        form.setAlignment(Pos.CENTER_LEFT);

        TableView<BudgetRow> table = new TableView<>(budgetRows);
        table.getStyleClass().add("content-card");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getColumns().add(column("Category", BudgetRow::category));
        table.getColumns().add(column("Limit", BudgetRow::limit));
        table.getColumns().add(column("Spent This Month", BudgetRow::spent));
        table.getColumns().add(column("Status", BudgetRow::status));

        VBox layout = new VBox(14, form, table);
        layout.setPadding(new Insets(8));
        VBox.setVgrow(table, Priority.ALWAYS);
        tab.setContent(layout);
        return tab;
    }

    private Tab buildSubscriptionsTab() {
        Tab tab = new Tab("Subscriptions");

        TextField nameField = new TextField();
        nameField.setPromptText("Subscription name");
        TextField amountField = new TextField();
        amountField.setPromptText("Amount");
        ComboBox<Category> categoryBox = createCategoryCombo();
        TextField dayField = new TextField();
        dayField.setPromptText("Billing day (1-31)");

        Button addButton = new Button("Add Subscription");
        addButton.getStyleClass().add("primary-btn");
        addButton.setOnAction(event -> {
            try {
                tracker.addSubscription(
                        nameField.getText(),
                        new BigDecimal(amountField.getText().trim()),
                        categoryBox.getValue(),
                        Integer.parseInt(dayField.getText().trim()));
                nameField.clear();
                amountField.clear();
                dayField.clear();
                refreshAll();
                setMessage("Subscription added.");
            } catch (Exception ex) {
                setMessage("Could not add subscription: " + ex.getMessage());
            }
        });

        Button postDueButton = new Button("Post Due For This Month");
        postDueButton.setOnAction(event -> {
            int posted = tracker.postDueSubscriptions(YearMonth.now()).size();
            refreshAll();
            setMessage("Created " + posted + " due subscription transaction(s).");
        });

        Button toggleButton = new Button("Toggle Active");
        toggleButton.setOnAction(event -> {
            Subscription selected = null;
            if (!subscriptionRows.isEmpty()) {
                selected = subscriptionRows.get(0);
            }
            TableView<Subscription> table = (TableView<Subscription>) ((VBox) tab.getContent()).getChildren().get(1);
            if (table.getSelectionModel().getSelectedItem() != null) {
                selected = table.getSelectionModel().getSelectedItem();
            }
            if (selected != null) {
                tracker.setSubscriptionActive(selected.getId(), !selected.isActive());
                refreshAll();
                setMessage("Subscription status updated.");
            }
        });

        GridPane form = new GridPane(12, 12);
        form.getStyleClass().add("content-card");
        form.setPadding(new Insets(18));
        form.add(new Label("Name"), 0, 0);
        form.add(nameField, 1, 0);
        form.add(new Label("Amount"), 2, 0);
        form.add(amountField, 3, 0);
        form.add(new Label("Category"), 0, 1);
        form.add(categoryBox, 1, 1);
        form.add(new Label("Billing Day"), 2, 1);
        form.add(dayField, 3, 1);
        form.add(addButton, 0, 2);
        form.add(postDueButton, 1, 2);
        form.add(toggleButton, 2, 2);
        GridPane.setHgrow(nameField, Priority.ALWAYS);
        GridPane.setHgrow(amountField, Priority.ALWAYS);
        GridPane.setHgrow(categoryBox, Priority.ALWAYS);

        TableView<Subscription> table = new TableView<>(subscriptionRows);
        table.getStyleClass().add("content-card");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getColumns().add(column("Name", Subscription::getName));
        table.getColumns().add(column("Amount", s -> s.getAmount().toPlainString()));
        table.getColumns().add(column("Category", s -> friendlyCategory(s.getCategory())));
        table.getColumns().add(column("Billing Day", s -> Integer.toString(s.getBillingDay())));
        table.getColumns().add(column("Active", s -> s.isActive() ? "Yes" : "No"));

        VBox layout = new VBox(14, form, table);
        layout.setPadding(new Insets(8));
        VBox.setVgrow(table, Priority.ALWAYS);
        tab.setContent(layout);
        return tab;
    }

    private Tab buildSummaryTab() {
        Tab tab = new Tab("Monthly Summary");
        TextField monthField = new TextField();
        monthField.setPromptText("YYYY-MM (leave blank for current)");
        TextArea summaryArea = new TextArea();
        summaryArea.setEditable(false);
        summaryArea.getStyleClass().add("summary-area");

        Button loadButton = new Button("Generate Summary");
        loadButton.getStyleClass().add("primary-btn");
        loadButton.setOnAction(event -> {
            try {
                String input = monthField.getText().isBlank() ? null : monthField.getText().trim();
                summaryArea.setText(tracker.getMonthlySummary(input));
                setMessage("Summary generated.");
            } catch (Exception ex) {
                setMessage("Could not generate summary: " + ex.getMessage());
            }
        });

        VBox pane = new VBox(14,
                new HBox(12, new Label("Month"), monthField, loadButton),
                summaryArea);
        pane.setPadding(new Insets(16));
        VBox.setVgrow(summaryArea, Priority.ALWAYS);
        tab.setContent(pane);
        return tab;
    }

    private HBox buildInfoBar() {
        infoMessage = new Label("Ready.");
        HBox bar = new HBox(infoMessage);
        bar.getStyleClass().add("info-bar");
        bar.setPadding(new Insets(10, 18, 12, 18));
        return bar;
    }

    private ComboBox<Category> createCategoryCombo() {
        ComboBox<Category> box = new ComboBox<>(FXCollections.observableArrayList(Category.values()));
        box.setConverter(new StringConverter<>() {
            @Override
            public String toString(Category category) {
                return category == null ? "" : friendlyCategory(category);
            }

            @Override
            public Category fromString(String s) {
                return s == null || s.isBlank() ? null : Category.valueOf(s.toUpperCase());
            }
        });
        box.getSelectionModel().select(Category.OTHER);
        return box;
    }

    private static <T> TableColumn<T, String> column(String title, java.util.function.Function<T, String> mapper) {
        TableColumn<T, String> column = new TableColumn<>(title);
        column.setCellValueFactory(data -> new SimpleStringProperty(mapper.apply(data.getValue())));
        return column;
    }

    private void refreshAll() {
        transactionRows.setAll(tracker.getTransactions());
        subscriptionRows.setAll(tracker.getSubscriptions());
        budgetRows.setAll(buildBudgetRows());

        YearMonth month = YearMonth.now();
        BigDecimal income = tracker.getTransactions().stream()
                .filter(tx -> tx.getType() == TransactionType.INCOME)
                .filter(tx -> YearMonth.from(tx.getDate()).equals(month))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal expense = tracker.getTransactions().stream()
                .filter(tx -> tx.getType() == TransactionType.EXPENSE)
                .filter(tx -> YearMonth.from(tx.getDate()).equals(month))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (balanceValue != null) {
            balanceValue.setText(tracker.getBalance().toPlainString());
            monthIncomeValue.setText(income.toPlainString());
            monthExpenseValue.setText(expense.toPlainString());
            monthNetValue.setText(income.subtract(expense).toPlainString());
        }
    }

    private ObservableList<BudgetRow> buildBudgetRows() {
        Map<Category, BigDecimal> spentByCategory = new EnumMap<>(Category.class);
        YearMonth month = YearMonth.now();
        for (Transaction transaction : tracker.getTransactions()) {
            if (transaction.getType() == TransactionType.EXPENSE && YearMonth.from(transaction.getDate()).equals(month)) {
                spentByCategory.merge(transaction.getCategory(), transaction.getAmount(), BigDecimal::add);
            }
        }

        ObservableList<BudgetRow> rows = FXCollections.observableArrayList();
        for (Budget budget : tracker.getBudgets()) {
            BigDecimal spent = spentByCategory.getOrDefault(budget.getCategory(), BigDecimal.ZERO);
            BigDecimal remaining = budget.getLimit().subtract(spent);
            String state = remaining.signum() < 0
                    ? "over by " + remaining.abs().toPlainString()
                    : "remaining " + remaining.toPlainString();
            rows.add(new BudgetRow(
                    friendlyCategory(budget.getCategory()),
                    budget.getLimit().toPlainString(),
                    spent.toPlainString(),
                    state));
        }

        rows.sort(Comparator.comparing(BudgetRow::category));
        return rows;
    }

    private void seedDemoData() {
        LocalDate now = LocalDate.now();
        tracker.addIncome("Primary Salary", new BigDecimal("6500"), Category.SALARY, now.withDayOfMonth(1));
        tracker.addExpense("Groceries", new BigDecimal("280.45"), Category.FOOD, now.minusDays(4));
        tracker.addExpense("Netflix", new BigDecimal("15.99"), Category.ENTERTAINMENT, now.minusDays(2));
        tracker.addExpense("Bus Pass", new BigDecimal("55.00"), Category.TRANSPORT, now.minusDays(1));
        tracker.setBudget(Category.FOOD, new BigDecimal("500"));
        tracker.setBudget(Category.ENTERTAINMENT, new BigDecimal("180"));
        tracker.setBudget(Category.TRANSPORT, new BigDecimal("120"));
        tracker.addSubscription("Netflix", new BigDecimal("15.99"), Category.ENTERTAINMENT, 7);
        tracker.addSubscription("Cloud Storage", new BigDecimal("9.99"), Category.UTILITIES, 12);
    }

    private void setMessage(String message) {
        if (infoMessage != null) {
            infoMessage.setText(message);
        }
    }

    private static String friendlyCategory(Category category) {
        return switch (category) {
            case SALARY -> "Salary";
            case FOOD -> "Food";
            case TRANSPORT -> "Transport";
            case RENT -> "Rent";
            case UTILITIES -> "Utilities";
            case ENTERTAINMENT -> "Entertainment";
            case HEALTH -> "Health";
            case EDUCATION -> "Education";
            case OTHER -> "Other";
        };
    }

    private record BudgetRow(String category, String limit, String spent, String status) {
    }
}
