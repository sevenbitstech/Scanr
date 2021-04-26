package io.card.payment;

/* CreditCard.java
 * See the file "LICENSE.md" for the full license governing this code.
 */

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;
import java.util.UUID;

/**
 * Describes a credit card.
 *
 * @version 2.0
 */
public class CreditCard implements Parcelable {

    /**
     * Number of years into the future that a card expiration date is considered to be valid.
     */
    public static final int EXPIRY_MAX_FUTURE_YEARS = 15;
    public static final Parcelable.Creator<CreditCard> CREATOR = new Parcelable.Creator<CreditCard>() {

        @Override
        public CreditCard createFromParcel(Parcel source) {
            return new CreditCard(source);
        }

        @Override
        public CreditCard[] newArray(int size) {
            return new CreditCard[size];
        }
    };
    private static final String TAG = CreditCard.class.getSimpleName();
    /**
     * 15 or 16 digit card number. All numbers, no spaces.
     */
    public String cardNumber;
    /**
     * Month in two digit natural form. {January=1, ..., December=12}
     */
    public int expiryMonth = 0;
    /**
     * Four digit year
     */
    public int expiryYear = 0;
    /**
     * Three or four character security code.
     */
    public String cvv;
    /**
     * Billing postal code for card.
     */
    public String postalCode;
    /**
     * Cardholder name.
     */
    public String cardholderName;
    // these should NOT be public
    String scanId;
    boolean flipped = false;
    int yoff;
    int[] xoff;

    // constructors
    public CreditCard() {
        xoff = new int[16];
        scanId = UUID.randomUUID().toString();
    }

    public CreditCard(String number, int month, int year, String code, String postalCode, String cardholderName) {
        this.cardNumber = number;
        this.expiryMonth = month;
        this.expiryYear = year;
        this.cvv = code;
        this.postalCode = postalCode;
        this.cardholderName = cardholderName;
    }

    // parcelable
    private CreditCard(Parcel src) {
        cardNumber = src.readString();
        expiryMonth = src.readInt();
        expiryYear = src.readInt();
        cvv = src.readString();
        postalCode = src.readString();
        cardholderName = src.readString();
        scanId = src.readString();
        yoff = src.readInt();
        xoff = src.createIntArray();
    }

    public static boolean isDateValid(int expiryMonth, int expiryYear) {
        if (expiryMonth < 1 || 12 < expiryMonth) {
            return false;
        }

        Calendar now = Calendar.getInstance();
        int thisYear = now.get(Calendar.YEAR);
        int thisMonth = now.get(Calendar.MONTH) + 1;

        if (expiryYear < thisYear) {
            return false;
        }
        if (expiryYear == thisYear && expiryMonth < thisMonth) {
            return false;
        }
        if (expiryYear > thisYear + CreditCard.EXPIRY_MAX_FUTURE_YEARS) {
            return false;
        }

        return true;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public final void writeToParcel(Parcel dest, int flags) {
        dest.writeString(cardNumber);
        dest.writeInt(expiryMonth);
        dest.writeInt(expiryYear);
        dest.writeString(cvv);
        dest.writeString(postalCode);
        dest.writeString(cardholderName);
        dest.writeString(scanId);
        dest.writeInt(yoff);
        dest.writeIntArray(xoff);
    }

    /**
     * @return The type of card, detected from the number
     */
    public CardType getCardType() {
        return CardType.fromCardNumber(cardNumber);
    }

    /**
     * @return <code>true</code> indicates a current, valid date.
     */
    public boolean isExpiryValid() {
        return isDateValid(expiryMonth, expiryYear);
    }

    /**
     * @return a string suitable for writing to a log. Should not be displayed to the user.
     */
    @Override
    public String toString() {
        String s = "{" + getCardType() + ": " + cardNumber;
        if (expiryMonth > 0 || expiryYear > 0) {
            s += "  expiry:" + expiryMonth + "/" + expiryYear;
        }
        if (postalCode != null) {
            s += "  postalCode:" + postalCode;
        }
        if (cardholderName != null) {
            s += "  cardholderName:" + cardholderName;
        }
        if (cvv != null) {
            s += "  cvvLength:" + ((cvv != null) ? cvv.length() : 0);
        }
        s += "}";
        return s;
    }
}
