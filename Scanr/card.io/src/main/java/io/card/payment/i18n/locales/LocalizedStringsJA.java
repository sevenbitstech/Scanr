package io.card.payment.i18n.locales;

import java.util.HashMap;
import java.util.Map;

import io.card.payment.i18n.StringKey;
import io.card.payment.i18n.SupportedLocale;

// Class autogenerated!  Do not modify.
// Generated on 2014-12-10 11:29:25.151634 via script:
// /Users/twhipple/Documents/buffalo/lib-i18n/i18n/script/generate_android_i18n.py -java_src_path src/ -java_gen_path gen/ -stringkey_path io/card/payment/i18n/ -strings_path ../strings/projects/card.io/strings/ --strict

public class LocalizedStringsJA implements SupportedLocale<StringKey> {

    private static Map<StringKey, String> mDisplay = new HashMap<>();
    private static Map<String, String> mAdapted = new HashMap<>();

    public LocalizedStringsJA() {
        mDisplay.put(StringKey.CANCEL, "キャンセル");
        mDisplay.put(StringKey.CARDTYPE_AMERICANEXPRESS, "American Express");
        mDisplay.put(StringKey.CARDTYPE_DISCOVER, "Discover");
        mDisplay.put(StringKey.CARDTYPE_JCB, "JCB");
        mDisplay.put(StringKey.CARDTYPE_MASTERCARD, "MasterCard");
        mDisplay.put(StringKey.CARDTYPE_VISA, "Visa");
        mDisplay.put(StringKey.DONE, "完了");
        mDisplay.put(StringKey.ENTRY_CVV, "カード確認コード");
        mDisplay.put(StringKey.ENTRY_POSTAL_CODE, "郵便番号");
        mDisplay.put(StringKey.ENTRY_CARDHOLDER_NAME, "カード保有者の名前");
        mDisplay.put(StringKey.ENTRY_EXPIRES, "有効期限");
        mDisplay.put(StringKey.EXPIRES_PLACEHOLDER, "MM/YY");
        mDisplay.put(StringKey.SCAN_GUIDE, "ここでカードをお持ちください。\n自動的にスキャンされます。");
        mDisplay.put(StringKey.KEYBOARD, "キーボード…");
        mDisplay.put(StringKey.ENTRY_CARD_NUMBER, "カード番号");
        mDisplay.put(StringKey.MANUAL_ENTRY_TITLE, "カードの詳細");
        mDisplay.put(StringKey.ERROR_NO_DEVICE_SUPPORT, "この端末ではカード番号の読込にカメラを使えません。");
        mDisplay.put(StringKey.ERROR_CAMERA_CONNECT_FAIL, "端末のカメラを使用できません。");
        mDisplay.put(StringKey.ERROR_CAMERA_UNEXPECTED_FAIL, "カメラを起動中に予期しないエラーが発生しました。");

        // no adapted_translations found
    }

    @Override
    public String getName() {
        return "ja";
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
