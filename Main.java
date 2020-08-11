package banking;

import java.sql.*;
import java.util.Scanner;

public class Main {
    static boolean programWorking = true;

    public static String getUrl(String[] args) {
        String fileName = "bankdb.db";
        if (args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("-fileName")) fileName = args[i + 1];
            }
        }
        return fileName;
    }

    public static void createDBandTable(String fileName) {


        String url = "jdbc:sqlite:" + fileName;
        String sql = "CREATE TABLE IF NOT EXISTS card(\n" +
                "    id INTEGER PRIMARY KEY, \n" +
                "    number TEXT, \n" +
                "    pin TEXT, \n" +
                "    balance INTEGER DEFAULT 0\n" +
                " );";

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void addBankAccount(String fileName) {
        BankAccount account = new BankAccount();

        String url = "jdbc:sqlite:" + fileName;
        String sql = "INSERT INTO card(number,pin) VALUES(?,?)";

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement prst = conn.prepareStatement(sql)) {

            prst.setString(1, account.getCardNumber());
            prst.setString(2, account.getPinCode());

            prst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean findAccountAndLogin(String fileName, String cardNumber, String pin) {

        String url = "jdbc:sqlite:" + fileName;
        String sql = "SELECT number, pin FROM card WHERE number = ? AND pin = ?";

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement prstm = conn.prepareStatement(sql)) {
            prstm.setString(1, cardNumber);
            prstm.setString(2, pin);

            ResultSet rs = prstm.executeQuery();
            boolean empty = true;
            while (rs.next()) {
                empty = false;
            }
            if (empty) {
                System.out.println("Wrong card number or PIN!");
                return false;
            } else {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

    }

    public static boolean cardContainsNumber(String fileName, String cardNumber) {
        String url = "jdbc:sqlite:" + fileName;
        String sql = "SELECT number FROM card WHERE number = ?";

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement prstm = conn.prepareStatement(sql)) {
            prstm.setString(1, cardNumber);

            ResultSet rs = prstm.executeQuery();
            boolean empty = true;
            while (rs.next()) {
                empty = false;
            }
            if (empty) {
                System.out.println("Such a card does not exist.");
                return false;
            } else {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void accountMenu(String dataBaseFileName, String cardNumber, String pinNumber, Scanner in) {


        if (!findAccountAndLogin(dataBaseFileName, cardNumber, pinNumber)) {
            return;
        } else {


            System.out.println("\nYou have successfully logged in!\n");

            while (true) {

                int balance = getBalance(dataBaseFileName, cardNumber);

                System.out.println(
                                "1. Balance\n" +
                                "2. Add income\n" +
                                "3. Do transfer\n" +
                                "4. Close account\n" +
                                "5. Log out\n" +
                                "0. Exit");
                String choise = in.nextLine();
                if (Integer.parseInt(choise) == 0) {
                    programWorking = false;
                    break;
                }
                if (Integer.parseInt(choise) == 1) {
                    System.out.println("\nBalance: " + balance + "\n");
                    continue;
                }

                if (Integer.parseInt(choise) == 2) {
                    System.out.println("Enter income:");
                    int deposit = Integer.parseInt(in.nextLine());

                    addIncome(dataBaseFileName, cardNumber, balance, deposit);

                    System.out.println("Income was added!");
                    continue;
                }

                if (Integer.parseInt(choise) == 3 ) {
                    System.out.println("\nTransfer\nEnter card number:");
                    String numberToTransfer = in.nextLine();

                    if (numberToTransfer.equals(cardNumber)) {
                        System.out.println("You can't transfer money to the same account!");
                        continue;
                    }

                    if (!BankAccount.checkLuhnAlg(numberToTransfer)) {
                        System.out.println("Probably you made mistake in the card number. Please try again!");
                        continue;
                    } else {
                        if (cardContainsNumber(dataBaseFileName, numberToTransfer)) {
                            System.out.println("Enter how much money you want to transfer:");
                            int transfer = Integer.parseInt(in.nextLine());
                            int balanceOfNumberToTransfer = getBalance(dataBaseFileName, numberToTransfer);
                            if (transfer <= balance) {
                                addIncome(dataBaseFileName,numberToTransfer,balanceOfNumberToTransfer, transfer);
                                removeMoney(dataBaseFileName, cardNumber, balance, transfer);
                                System.out.println("Success!");
                                continue;


                            } else {
                                System.out.println("Not enough money!");
                                continue;
                            }

                        }
                    }
                }


                if (Integer.parseInt(choise) == 4) {
                    removeAccount(dataBaseFileName, cardNumber);
                    break;
                }

                if (Integer.parseInt(choise) == 5 ) break;

            }
        }


    }

    public static int getBalance(String fileName, String cardNumber) {
        String url = "jdbc:sqlite:" + fileName;
        String sql = "SELECT balance FROM card WHERE number = ?";
        int balance = -1;

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement prst = conn.prepareStatement(sql)) {
            prst.setString(1, cardNumber);
            ResultSet rs = prst.executeQuery();
            while(rs.next()) {
                balance = rs.getInt("balance");
                return balance;
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }
        return balance;
    }

    public static void addIncome(String fileName, String cardNumer, int oldBlance, int deposit) {

        String url = "jdbc:sqlite:" + fileName;
        String sql = "UPDATE card SET balance = ? WHERE number = ?";

        try (Connection conn = DriverManager.getConnection(url);
                PreparedStatement prst = conn.prepareStatement(sql)) {
            prst.setInt(1, oldBlance + deposit);
            prst.setString(2, cardNumer);
            prst.executeUpdate();


        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static void removeMoney(String fileName, String cardNumber,int oldBalance, int deposit) {
        String url = "jdbc:sqlite:" + fileName;
        String sql = "UPDATE card SET balance = ? WHERE number = ?";

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement prst = conn.prepareStatement(sql)) {
            prst.setInt(1, oldBalance - deposit);
            prst.setString(2, cardNumber);
            prst.executeUpdate();


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void removeAccount(String fileName, String cardNumber) {
        String url = "jdbc:sqlite:" + fileName;
        String sql = "DELETE FROM card WHERE number = ?";

        try (Connection conn = DriverManager.getConnection(url);
            PreparedStatement prst = conn.prepareStatement(sql)) {
            prst.setString(1, cardNumber);
            prst.executeUpdate();
            System.out.println("The account has been closed!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void selectAll(String fileName) {
        String url = "jdbc:sqlite:" + fileName;
        String sql = "SELECT number, pin FROM card";
        try (Connection conn = DriverManager.getConnection(url);
            Statement stm = conn.createStatement();
            ResultSet rs = stm.executeQuery(sql)) {
            while (rs.next()) {
                System.out.println(rs.getString("number") +"\t" + rs.getString("pin"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        String dataBaseFileName = getUrl(args);

        createDBandTable(dataBaseFileName);


        Scanner in = new Scanner(System.in);


        while (programWorking) {
            System.out.println(
                    "1. Create an account\n" +
                            "2. Log into account\n" +
                            "0. Exit");

            String choise = in.nextLine();
            if (Integer.parseInt(choise) == 0) programWorking = false;
            if (Integer.parseInt(choise) == 1) addBankAccount(dataBaseFileName);
            if (Integer.parseInt(choise) == 2) {
                System.out.println("Enter your card number:");
                String cardNumber = in.nextLine();
                System.out.println("Enter your PIN:");
                String pinNumber = in.nextLine();

                accountMenu(dataBaseFileName, cardNumber, pinNumber, in);



            }

        }






    }


}
