package io.card.payment.i18n.locales;

import java.util.HashMap;
import java.util.Map;

import io.card.payment.i18n.StringKey;
import io.card.payment.i18n.SupportedLocale;

// Class autogenerated!  Do not modify.
// Generated on 2014-12-10 11:29:25.156058 via script:
// /Users/twhipple/Documents/buffalo/lib-i18n/i18n/script/generate_android_i18n.py -java_src_path src/ -java_gen_path gen/ -stringkey_path io/card/payment/i18n/ -strings_path ../strings/projects/card.io/strings/ --strict

public class LocalizedStringsMS implements SupportedLocale<StringKey> {

    private static Map<StringKey, String> mDisplay = new HashMap<>();
    private static Map<String, String> mAdapted = new HashMap<>();

    public LocalizedStringsMS() {
        mDisplay.put(StringKey.CANCEL, "Batal");
        mDisplay.put(StringKey.CARDTYPE_AMERICANEXPRESS, "American Express");
        mDisplay.put(StringKey.CARDTYPE_DISCOVER, "Discover");
        mDisplay.put(StringKey.CARDTYPE_JCB, "JCB");
        mDisplay.put(StringKey.CARDTYPE_MASTERCARD, "MasterCard");
        mDisplay.put(StringKey.CARDTYPE_VISA, "Visa");
        mDisplay.put(StringKey.DONE, "Selesai");
        mDisplay.put(StringKey.ENTRY_CVV, "CVV");
        mDisplay.put(StringKey.ENTRY_POSTAL_CODE, "Poskod");
        mDisplay.put(StringKey.ENTRY_CARDHOLDER_NAME, "Nama Pemegang Kad");
        mDisplay.put(StringKey.ENTRY_EXPIRES, "Luput");
        mDisplay.put(StringKey.EXPIRES_PLACEHOLDER, "BB/TT");
        mDisplay.put(StringKey.SCAN_GUIDE, "Pegang kad di sini.\nIa akan mengimbas secara automatik.");
        mDisplay.put(StringKey.KEYBOARD, "Papan Kekunci…");
        mDisplay.put(StringKey.ENTRY_CARD_NUMBER, "Nombor Kad");
        mDisplay.put(StringKey.MANUAL_ENTRY_TITLE, "Butiran Kad");
        mDisplay.put(StringKey.ERROR_NO_DEVICE_SUPPORT, "Peranti ini tidak dapat menggunakan kamera untuk membaca nombor kad.");
        mDisplay.put(StringKey.ERROR_CAMERA_CONNECT_FAIL, "Kamera peranti tidak tersedia.");
        mDisplay.put(StringKey.ERROR_CAMERA_UNEXPECTED_FAIL, "Peranti mengalami ralat tidak dijangka semasa membuka kamera.");

        // no adapted_translations found
    }

    @Override
    public String getName() {
        return "ms";
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
