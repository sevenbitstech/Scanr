package com.pancard.android.core;

import android.util.Log;

import com.pancard.android.activity.otheracivity.CommonScan;
import com.pancard.android.utility.DataAttributes;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;

/**
 * Created by elitebook on 13/9/17.
 */

public class XmlStringParsing {

    public void processScannedData(String scanData) {
        Log.d("Rajdeol", scanData);
        XmlPullParserFactory pullParserFactory;
        try {
            // init the parserfactory
            pullParserFactory = XmlPullParserFactory.newInstance();
            // get the parser
            XmlPullParser parser = pullParserFactory.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(new StringReader(scanData));
            // parse the XML
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_DOCUMENT) {
                    Log.d("Rajdeol", parser.toString());
                } else if (eventType == XmlPullParser.START_TAG && DataAttributes.AADHAAR_DATA_TAG.equals(parser.getName())) {
                    // extract data from tag
                    //uid
                    CommonScan.CARD_UNIQE_NO = parser.getAttributeValue(null, DataAttributes.AADHAR_UID_ATTR);
                    //name
                    CommonScan.CARD_HOLDER_NAME = parser.getAttributeValue(null, DataAttributes.AADHAR_NAME_ATTR);
                    //gender
                    CommonScan.CARD_HOLDER_NAME += " (" + parser.getAttributeValue(null, DataAttributes.AADHAR_GENDER_ATTR) + ") ";
                    // year of birth
                    CommonScan.CARD_HOLDER_DOB = parser.getAttributeValue(null, DataAttributes.AADHAR_YOB_ATTR);
                    // care of
                    if (parser.getAttributeValue(null, DataAttributes.AADHAR_CO_ATTR) != null)
                        CommonScan.CARD_ISSUE_ADDRESS = parser.getAttributeValue(null, DataAttributes.AADHAR_CO_ATTR) + ",";
                    // village Tehsil
                    if (parser.getAttributeValue(null, DataAttributes.AADHAR_VTC_ATTR) != null)
                        CommonScan.CARD_ISSUE_ADDRESS += parser.getAttributeValue(null, DataAttributes.AADHAR_VTC_ATTR) + ",";

                    CommonScan.CARD_BIRTH_PLACE = parser.getAttributeValue(null, DataAttributes.AADHAR_VTC_ATTR);
                    // Post Office
                    CommonScan.CARD_ISSUE_ADDRESS += parser.getAttributeValue(null, DataAttributes.AADHAR_PO_ATTR) + ",";
                    // district
                    CommonScan.CARD_ISSUE_ADDRESS += parser.getAttributeValue(null, DataAttributes.AADHAR_DIST_ATTR) + ",";
                    // state
                    CommonScan.CARD_ISSUE_ADDRESS += parser.getAttributeValue(null, DataAttributes.AADHAR_STATE_ATTR) + ",";
                    // Post Code
                    CommonScan.CARD_ISSUE_ADDRESS += parser.getAttributeValue(null, DataAttributes.AADHAR_PC_ATTR);

                } else if (eventType == XmlPullParser.END_TAG) {
                    Log.d("Rajdeol", "End tag " + parser.getName());
                } else if (eventType == XmlPullParser.TEXT) {
                    Log.d("Rajdeol", "Text " + parser.getText());
                }
                // update eventType
                eventType = parser.next();
            }
            // display the data on screen
        } catch (XmlPullParserException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();

        }

    }// EO function

}
