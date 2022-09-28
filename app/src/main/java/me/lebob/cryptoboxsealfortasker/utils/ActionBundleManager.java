package me.lebob.cryptoboxsealfortasker.utils;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;


public class ActionBundleManager {

    // only accept valid MAC addresses of form 00:11:22:AA:BB:CC, where colons can be dashes
    public static boolean isKeyValid(String key) {
        if (key == null) {
            return false;
        }

        // We allow variables from Tasker
        return TaskerPlugin.variableNameValid(key);
    }

    // Whether the bundle is valid. Strings must be non-null, and either variables
    // or valid format (correctly-formatted MAC, non-empty, proper hex if binary, etc.)
    public static boolean isBundleValid(final Bundle bundle) {
        if (bundle == null) {
            Log.w(Constants.LOG_TAG, "Null bundle");
            return false;
        }

        /*
        String[] keys = {Constants.BUNDLE_CON_DISCON_SEND, Constants.BUNDLE_PUBLIC_KEY,
                Constants.BUNDLE_PRIVATE_KEY, Constants.BUNDLE_MSG_TO_ENCODE, Constants.BUNDLE_MSG_TO_DECODE};
        for (String key: keys) {
            if (!bundle.containsKey(key)) {
                Log.w(Constants.LOG_TAG, "Bundle missing key " + key);
            }
        }


        // Check for the case of sending a message
        String msg = getMsgToDecode(bundle);
        if (msg == null) {
            Log.w(Constants.LOG_TAG, "Null message");
            return false;
        }

        // allow variable replacement, at the expense
        // of sanity checking hex
        // We allow variable MACs
        if (TaskerPlugin.variableNameValid(msg)) {
            return true;
        }
        */
        return true;
    }

    // method to get error message for the given values, or null if no error exists
    public static String getErrorMessage(Context context,int doEncode, final String msgToEncodeOrDecode,
                                         final String publicKey, final String privateKey, final String resultVarName) {
        Resources res = context.getResources();
        /*
        if (connDisconSend==0 || connDisconSend==1) {
            if (!isMacValid(mac)) {
                return res.getString(R.string.invalid_mac);
            }
        }*/
        return null;
    }

    // Method to create bundle from the individual values
    public static Bundle generateBundle(final int doEncode, final String msgToEncodeOrDecode,
                                        final String publicKey, final String privateKey, final String resultVarName) {
        if (msgToEncodeOrDecode == null) {
            return null;
        }

        final Bundle bundle = new Bundle();
        bundle.putInt(Constants.BUNDLE_DO_ENCODE, doEncode);
        bundle.putString(Constants.BUNDLE_MSG_TO_ENCODE_OR_DECODE, msgToEncodeOrDecode);
        bundle.putString(Constants.BUNDLE_PUBLIC_KEY, publicKey);
        bundle.putString(Constants.BUNDLE_PRIVATE_KEY, privateKey);
        bundle.putString(Constants.BUNDLE_RESULT_VAR_NAME, resultVarName);

        if (!isBundleValid(bundle)) {
            return null;
        } else {
            return bundle;
        }
    }

    // Method for getting short String description of bundle
    public static String getBundleBlurb(final Bundle bundle) {
        if (!isBundleValid(bundle)) {
            return null;
        }

        final int doEncode = getDoEncode(bundle);
        final String msgToEncodeOrDecode = getMsgToEncodeOrDecode(bundle);
        final String publicKey = getPublicKey(bundle);
        final String privateKey = getPrivateKey(bundle);
        final String resultVarName = getResultVarName(bundle);

        StringBuilder builder = new StringBuilder();
        if (doEncode==1)
        {
            builder.append("Encoding message ");
            builder.append(msgToEncodeOrDecode);
        }
        else
        {
            builder.append("Decoding message ");
            builder.append(msgToEncodeOrDecode);
        }
        return builder.toString();
    }

    // Method to get the data from the bundle
    public static int getDoEncode(final Bundle bundle) {
        return bundle.getInt(Constants.BUNDLE_DO_ENCODE, 0);
    }

    // Method to get the data from the bundle
    public static String getMsgToEncodeOrDecode(final Bundle bundle) {
        return bundle.getString(Constants.BUNDLE_MSG_TO_ENCODE_OR_DECODE, null);
    }


    // Method to get the data from the bundle
    public static String getPublicKey(final Bundle bundle) {
        return bundle.getString(Constants.BUNDLE_PUBLIC_KEY, null);
    }

    // Method to get the data from the bundle
    public static String getPrivateKey(final Bundle bundle) {
        return bundle.getString(Constants.BUNDLE_PRIVATE_KEY, null);
    }

    public static String getResultVarName(final Bundle bundle) {
        return bundle.getString(Constants.BUNDLE_RESULT_VAR_NAME, null);
    }

}
