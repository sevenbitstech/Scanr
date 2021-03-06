package io.card.payment.i18n.locales;

import java.util.HashMap;
import java.util.Map;

import io.card.payment.i18n.StringKey;
import io.card.payment.i18n.SupportedLocale;

// Class autogenerated!  Do not modify.
// Generated on 2014-12-10 11:29:25.145036 via script:
// /Users/twhipple/Documents/buffalo/lib-i18n/i18n/script/generate_android_i18n.py -java_src_path src/ -java_gen_path gen/ -stringkey_path io/card/payment/i18n/ -strings_path ../strings/projects/card.io/strings/ --strict

public class LocalizedStringsHE implements SupportedLocale<StringKey> {

    private static Map<StringKey, String> mDisplay = new HashMap<>();
    private static Map<String, String> mAdapted = new HashMap<>();

    public LocalizedStringsHE() {
        mDisplay.put(StringKey.CANCEL, "ביטול");
        mDisplay.put(StringKey.CARDTYPE_AMERICANEXPRESS, "אמריקן אקספרס");
        mDisplay.put(StringKey.CARDTYPE_DISCOVER, "Discover‏");
        mDisplay.put(StringKey.CARDTYPE_JCB, "JCB‏");
        mDisplay.put(StringKey.CARDTYPE_MASTERCARD, "מאסטרקארד");
        mDisplay.put(StringKey.CARDTYPE_VISA, "ויזה");
        mDisplay.put(StringKey.DONE, "בוצע");
        mDisplay.put(StringKey.ENTRY_CVV, "קוד אימות כרטיס");
        mDisplay.put(StringKey.ENTRY_POSTAL_CODE, "מיקוד");
        mDisplay.put(StringKey.ENTRY_CARDHOLDER_NAME, "שם בעל הכרטיס");
        mDisplay.put(StringKey.ENTRY_EXPIRES, "תאריך תפוגה");
        mDisplay.put(StringKey.EXPIRES_PLACEHOLDER, "MM/YY‏");
        mDisplay.put(StringKey.SCAN_GUIDE, "החזק את הכרטיס כאן.\nהסריקה תתבצע באופן אוטומטי.");
        mDisplay.put(StringKey.KEYBOARD, "מקלדת…");
        mDisplay.put(StringKey.ENTRY_CARD_NUMBER, "מספר כרטיס");
        mDisplay.put(StringKey.MANUAL_ENTRY_TITLE, "פרטי כרטיס");
        mDisplay.put(StringKey.ERROR_NO_DEVICE_SUPPORT, "המכשיר אינו מסוגל להשתמש במצלמה לקריאת מספרי כרטיס.");
        mDisplay.put(StringKey.ERROR_CAMERA_CONNECT_FAIL, "מצלמת המכשיר אינה זמינה.");
        mDisplay.put(StringKey.ERROR_CAMERA_UNEXPECTED_FAIL, "המכשיר נתקל בשגיאה בלתי צפויה בזמן הפעלת המצלמה.");

        // no adapted_translations found
    }

    @Override
    public String getName() {
        return "he";
    }

    @Override
    public String getAdaptedDisplay(StringKey key, String country) {
        String adaptedKey = key.toString() + "|" + country;
        if (mAdapted.containsKey(adaptedKey)) {
            return mAdapted.get(adaptedKey);
        } else {
            return mDisplay.get(key);
        }
    }
}
