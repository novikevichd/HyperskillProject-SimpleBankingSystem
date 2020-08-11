package banking;

import java.util.*;

public class BankAccount {
    private String cardNumber;
    private String pinCode;
    private int balance;


    public BankAccount() {
        setCardNumber();
        setPinCode();
        balance = 0;
        System.out.println("\nYour card has been created\nYour card number:");
        System.out.println(this.getCardNumber());
        System.out.println("Your card PIN:");
        System.out.println(this.getPinCode() +"\n");

    }



    private void setCardNumber() {
        StringBuilder stringBuilder = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 9; i++) {
            stringBuilder.append(random.nextInt(10));
        }
        String tempNumber = "400000" + stringBuilder.toString();

        int sum = 0;
        for (int i = 0; i < tempNumber.length(); i++) {
            int temp = Character.getNumericValue(tempNumber.charAt(i));
            if ((i + 1) % 2 != 0 ) temp *= 2;
            if (temp > 9) temp -= 9;
            sum += temp;

        }
        int checkSum = 10 - (sum % 10);
        tempNumber += checkSum;
        cardNumber = tempNumber;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    private void setPinCode() {
        StringBuilder stringBuilder = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 4; i++) {
            stringBuilder.append(random.nextInt(10));
        }
        pinCode = stringBuilder.toString();
    }

    public String getPinCode() {
        return pinCode;
    }

    public int getBalance() {
        return balance;
    }

    public static boolean checkLuhnAlg(String cardNumber) {
        int sum = 0;
        for (int i = 0; i < cardNumber.length() - 1; i++) {
            int temp = Character.getNumericValue(cardNumber.charAt(i));
            if ((i + 1) % 2 != 0) temp *= 2;
            if (temp > 9) temp -= 9;
            sum += temp;
        }
        int checkSum = 10 - (sum % 10);

        if (checkSum == Character.getNumericValue(cardNumber.charAt(cardNumber.length() - 1))) return true;
        else return false;

    }

}
