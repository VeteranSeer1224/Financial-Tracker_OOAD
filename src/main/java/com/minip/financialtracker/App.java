package com.minip.financialtracker;

import com.minip.financialtracker.model.Category;
import com.minip.financialtracker.service.FinancialTracker;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Scanner;

public class App {

    public static void main(String[] args) {
        FinancialTracker tracker = new FinancialTracker();
        Scanner scanner = new Scanner(System.in);

        seedDemoData(tracker);

        System.out.println("Financial Tracker");
        System.out.println("Type the menu number and press Enter.");

        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> addIncome(scanner, tracker);
                case "2" -> addExpense(scanner, tracker);
                case "3" -> listTransactions(tracker);
                case "4" -> showBalance(tracker);
                case "5" -> showMonthlySummary(scanner, tracker);
                case "6" -> addBudget(scanner, tracker);
                case "7" -> showBudgetStatus(tracker);
                case "0" -> running = false;
                default -> System.out.println("Unknown option. Try again.");
            }
        }

        System.out.println("Goodbye.");
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("1. Add income");
        System.out.println("2. Add expense");
        System.out.println("3. List transactions");
        System.out.println("4. Show balance");
        System.out.println("5. Show monthly summary");
        System.out.println("6. Set budget");
        System.out.println("7. Show budget status");
        System.out.println("0. Exit");
        System.out.print("> ");
    }

    private static void addIncome(Scanner scanner, FinancialTracker tracker) {
        System.out.print("Description: ");
        String description = scanner.nextLine();
        BigDecimal amount = readAmount(scanner);
        Category category = readCategory(scanner);
        LocalDate date = readDate(scanner);
        tracker.addIncome(description, amount, category, date);
        System.out.println("Income recorded.");
    }

    private static void addExpense(Scanner scanner, FinancialTracker tracker) {
        System.out.print("Description: ");
        String description = scanner.nextLine();
        BigDecimal amount = readAmount(scanner);
        Category category = readCategory(scanner);
        LocalDate date = readDate(scanner);
        tracker.addExpense(description, amount, category, date);
        System.out.println("Expense recorded.");
    }

    private static void listTransactions(FinancialTracker tracker) {
        if (tracker.getTransactions().isEmpty()) {
            System.out.println("No transactions yet.");
            return;
        }

        tracker.getTransactions().forEach(transaction -> System.out.println(transaction.toDisplayString()));
    }

    private static void showBalance(FinancialTracker tracker) {
        System.out.println("Current balance: " + tracker.getBalance().toPlainString());
    }

    private static void showMonthlySummary(Scanner scanner, FinancialTracker tracker) {
        System.out.print("Month (YYYY-MM, blank for current month): ");
        String input = scanner.nextLine().trim();
        System.out.println(tracker.getMonthlySummary(input.isEmpty() ? null : input));
    }

    private static void addBudget(Scanner scanner, FinancialTracker tracker) {
        Category category = readCategory(scanner);
        BigDecimal amount = readAmount(scanner);
        tracker.setBudget(category, amount);
        System.out.println("Budget saved.");
    }

    private static void showBudgetStatus(FinancialTracker tracker) {
        String status = tracker.getBudgetStatus();
        System.out.println(status.isBlank() ? "No budgets defined." : status);
    }

    private static BigDecimal readAmount(Scanner scanner) {
        while (true) {
            System.out.print("Amount: ");
            String value = scanner.nextLine().trim();
            try {
                BigDecimal amount = new BigDecimal(value);
                if (amount.signum() <= 0) {
                    System.out.println("Amount must be greater than zero.");
                    continue;
                }
                return amount;
            } catch (NumberFormatException ex) {
                System.out.println("Enter a valid numeric amount.");
            }
        }
    }

    private static Category readCategory(Scanner scanner) {
        Category[] categories = Category.values();
        while (true) {
            System.out.println("Choose category:");
            for (int index = 0; index < categories.length; index++) {
                System.out.println((index + 1) + ". " + categories[index]);
            }
            System.out.print("> ");

            String value = scanner.nextLine().trim();
            try {
                int index = Integer.parseInt(value);
                if (index >= 1 && index <= categories.length) {
                    return categories[index - 1];
                }
            } catch (NumberFormatException ignored) {
                // fall through and try enum lookup
            }

            try {
                return Category.valueOf(value.toUpperCase());
            } catch (IllegalArgumentException ex) {
                System.out.println("Invalid category.");
            }
        }
    }

    private static LocalDate readDate(Scanner scanner) {
        System.out.print("Date (YYYY-MM-DD, blank for today): ");
        String value = scanner.nextLine().trim();
        if (value.isEmpty()) {
            return LocalDate.now();
        }
        return LocalDate.parse(value);
    }

    private static void seedDemoData(FinancialTracker tracker) {
        tracker.addIncome("Salary", new BigDecimal("5000"), Category.SALARY, LocalDate.now().withDayOfMonth(1));
        tracker.addExpense("Groceries", new BigDecimal("120.50"), Category.FOOD, LocalDate.now());
        tracker.addExpense("Bus pass", new BigDecimal("45.00"), Category.TRANSPORT, LocalDate.now());
        tracker.setBudget(Category.FOOD, new BigDecimal("300"));
    }
}
