package io.card.payment;

/* CardType.java
 * See the file "LICENSE.md" for the full license governing this code.
 */

import android.text.TextUtils;
import android.util.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

/**
 * Enumerates each supported card type. see http://en.wikipedia.org/wiki/Bank_card_number for more
 * details.
 *
 * @version 1.0
 */
public enum CardType {
    /**
     * American Express cards start in 34 or 37
     */
    AMEX("AmEx"),
    /**
     * Diners Club
     */
    DINERSCLUB("DinersClub"),
    /**
     * Discover starts with 6x for some values of x.
     */
    DISCOVER("Discover"),
    /**
     * JCB (see http://www.jcbusa.com/) cards start with 35
     */
    JCB("JCB"),
    /**
     * Mastercard starts with 51-55
     */
    MASTERCARD("MasterCard"),
    /**
     * Visa starts with 4
     */
    VISA("Visa"),
    /**
     * Maestro
     */
    MAESTRO("Maestro"),
    /**
     * Unknown card type.
     */
    UNKNOWN("Unknown"),
    /**
     * Not enough information given.
     * <br><br>
     * More digits are required to know the card type. (e.g. all we have is a 3, so we don't know if
     * it's JCB or AmEx)
     */
    INSUFFICIENT_DIGITS("More digits required");

    private static int minDigits = 1;
    private static HashMap<Pair<String, String>, CardType> intervalLookup;

    static {
        // initialize
        intervalLookup = new HashMap<Pair<String, String>, CardType>();
        intervalLookup.put(getNewPair("2221", "2720"), CardType.MASTERCARD);    // MasterCard 2-series
        intervalLookup.put(getNewPair("300", "305"), CardType.DINERSCLUB);      // Diners Club (Discover)
        intervalLookup.put(getNewPair("309", null), CardType.DINERSCLUB);       // Diners Club (Discover)
        intervalLookup.put(getNewPair("34", null), CardType.AMEX);              // AmEx
        intervalLookup.put(getNewPair("3528", "3589"), CardType.JCB);           // JCB
        intervalLookup.put(getNewPair("36", null), CardType.DINERSCLUB);        // Diners Club (Discover)
        intervalLookup.put(getNewPair("37", null), CardType.AMEX);              // AmEx
        intervalLookup.put(getNewPair("38", "39"), CardType.DINERSCLUB);        // Diners Club (Discover)
        intervalLookup.put(getNewPair("4", null), CardType.VISA);               // Visa
        intervalLookup.put(getNewPair("50", null), CardType.MAESTRO);           // Maestro
        intervalLookup.put(getNewPair("51", "55"), CardType.MASTERCARD);        // MasterCard
        intervalLookup.put(getNewPair("56", "59"), CardType.MAESTRO);           // Maestro
        intervalLookup.put(getNewPair("6011", null), CardType.DISCOVER);        // Discover
        intervalLookup.put(getNewPair("61", null), CardType.MAESTRO);           // Maestro
        intervalLookup.put(getNewPair("62", null), CardType.DISCOVER);          // China UnionPay (Discover)
        intervalLookup.put(getNewPair("63", null), CardType.MAESTRO);           // Maestro
        intervalLookup.put(getNewPair("644", "649"), CardType.DISCOVER);        // Discover
        intervalLookup.put(getNewPair("65", null), CardType.DISCOVER);          // Discover
        intervalLookup.put(getNewPair("66", "69"), CardType.MAESTRO);           // Maestro
        intervalLookup.put(getNewPair("88", null), CardType.DISCOVER);          // China UnionPay (Discover)

        for (Entry<Pair<String, String>, CardType> entry : getIntervalLookup().entrySet()) {
            minDigits = Math.max(minDigits, entry.getKey().first.length());
            if (entry.getKey().second != null) {
                minDigits = Math.max(minDigits, entry.getKey().second.length());
            }
        }
    }

    public final String name;

    private CardType(String name) {
        this.name = name;
    }

    /**
     * Determine if a number matches a prefix interval
     *
     * @param number        credit card number
     * @param intervalStart prefix (e.g. "4") or prefix interval start (e.g. "51")
     * @param intervalEnd   prefix interval end (e.g. "55") or null for non-intervals
     * @return -1 for insufficient digits, 0 for no, 1 for yes.
     */
    private static boolean isNumberInInterval(String number, String intervalStart,
                                              String intervalEnd) {
        // Log.d("CardType", "numberInInterval(number:" + number + ",intervalStart:" + intervalStart
        // + ",intervalEnd:" + intervalEnd + ")");

        int numCompareStart = Math.min(number.length(), intervalStart.length());
        int numCompareEnd = Math.min(number.length(), intervalEnd.length());

        if (Integer.parseInt(number.substring(0, numCompareStart)) < Integer.parseInt(intervalStart
                .substring(0, numCompareStart))) {
            // number is too low
            return false;
        } else if (Integer.parseInt(number.substring(0, numCompareEnd)) > Integer
                .parseInt(intervalEnd.substring(0, numCompareEnd))) {
            // number is too high
            return false;
        }

        return true;
    }

    private static HashMap<Pair<String, String>, CardType> getIntervalLookup() {
        return intervalLookup;
    }

    private static Pair<String, String> getNewPair(String intervalStart, String intervalEnd) {
        if (intervalEnd == null) {
            // set intervalEnd to intervalStart before creating the Pair object, because apparently
            // Pair.hashCode() can't handle nulls on some devices/versions. WTF.
            intervalEnd = intervalStart;
        }
        return new Pair<String, String>(intervalStart, intervalEnd);
    }

    /**
     * Infer the card type from a string.
     *
     * @param typeStr The String value of this enum
     * @return the matched real type
     */
    public static CardType fromString(String typeStr) {
        if (typeStr == null) {
            return CardType.UNKNOWN;
        }

        for (CardType type : CardType.values()) {
            if (type == CardType.UNKNOWN || type == CardType.INSUFFICIENT_DIGITS) {
                continue;
            }

            if (typeStr.equalsIgnoreCase(type.toString())) {
                return type;
            }
        }
        return CardType.UNKNOWN;
    }

    /**
     * Infer the CardType from the number string. See http://en.wikipedia.org/wiki/Bank_card_number
     * for these ranges (last checked: 19 Feb 2013)
     *
     * @param numStr A string containing only the card number.
     * @return the inferred card type
     */
    public static CardType fromCardNumber(String numStr) {
        if (TextUtils.isEmpty(numStr)) {
            return CardType.UNKNOWN;
        }

        HashSet<CardType> possibleCardTypes = new HashSet<CardType>();
        for (Entry<Pair<String, String>, CardType> entry : getIntervalLookup().entrySet()) {
            boolean isPossibleCard = isNumberInInterval(numStr, entry.getKey().first,
                    entry.getKey().second);
            if (isPossibleCard) {
                possibleCardTypes.add(entry.getValue());
            }
        }

        if (possibleCardTypes.size() > 1) {
            return CardType.INSUFFICIENT_DIGITS;
        } else if (possibleCardTypes.size() == 1) {
            return possibleCardTypes.iterator().next();
        } else {
            return CardType.UNKNOWN;
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
