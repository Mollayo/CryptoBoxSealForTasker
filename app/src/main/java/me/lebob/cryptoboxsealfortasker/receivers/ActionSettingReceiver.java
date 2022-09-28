package me.lebob.cryptoboxsealfortasker.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.libsodium.jni.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;


import me.lebob.cryptoboxsealfortasker.utils.Constants;
import me.lebob.cryptoboxsealfortasker.utils.ActionBundleManager;
import me.lebob.cryptoboxsealfortasker.utils.TaskerPlugin;

public class ActionSettingReceiver  extends BroadcastReceiver {

    // This is mandatory for the initialisation of the library
    final Sodium sodium= NaCl.sodium();

    // Method that checks whether bundle is valid
    protected boolean isBundleValid(Bundle bundle) {
        Log.v(Constants.LOG_TAG, "ActionSettingReceiver::isBundleValid");
        return ActionBundleManager.isBundleValid(bundle);
    }

    /* s must be an even-length string. */
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    // Method responsible for the connection and data transmission.
    @Override
    //protected void firePluginSetting(Context context, Bundle bundle)
    public void onReceive(Context context, Intent intent)
    {
        Log.v(Constants.LOG_TAG, "ActionSettingReceiver::firePluginSetting");

        final Bundle localeBundle = intent.getBundleExtra(com.twofortyfouram.locale.api.Intent.EXTRA_BUNDLE);
        if (!isBundleValid(localeBundle)) {
            Log.v(Constants.LOG_TAG, "localBundle is invalid"); //$NON-NLS-1$
            return;
        }

        // Get the user parameters
        final int doEncode = ActionBundleManager.getDoEncode(localeBundle);
        final String msgToEncodeOrDecode = ActionBundleManager.getMsgToEncodeOrDecode(localeBundle);
        final String publicKeyStr = ActionBundleManager.getPublicKey(localeBundle);
        final String privateKeyStr = ActionBundleManager.getPrivateKey(localeBundle);
        final String resultVarName = ActionBundleManager.getResultVarName(localeBundle);

        byte[] publicKey =hexStringToByteArray(publicKeyStr);
        byte[] privateKey =hexStringToByteArray(privateKeyStr);
        Log.v(Constants.LOG_TAG, "Public key:" + publicKeyStr);


        int res;

        Boolean success=true;
        String resultStr="";
        try {
            if (doEncode==1)
            {
                // Decode message
                byte[] cipher=new byte[Sodium.crypto_box_sealbytes()+msgToEncodeOrDecode.getBytes(StandardCharsets.UTF_8).length];
                res=Sodium.crypto_box_seal(cipher,msgToEncodeOrDecode.getBytes(StandardCharsets.UTF_8),
                                           msgToEncodeOrDecode.getBytes(StandardCharsets.UTF_8).length,
                                           publicKey);
                if (res!=0)
                {
                    success=false;
                    resultStr="Failed to encode the message";
                }
                else
                {
                    resultStr=Base64.getUrlEncoder().encodeToString(cipher);

                    /*
                    // Try to decode the message for debugging
                    byte[] plaintext=new byte[msgToEncodeOrDecode.length()];
                    res=Sodium.crypto_box_seal_open(plaintext,cipher,cipher.length,publicKey,privateKey);
                    if (res!=0)
                        Log.v(Constants.LOG_TAG, "Failed to decode the encoded data");
                     else {
                        String tmp= new String(plaintext, StandardCharsets.UTF_8);
                        Log.v(Constants.LOG_TAG, "Decoding the encoded data: " + tmp);
                    }
                     */
                }
                Log.v(Constants.LOG_TAG, "Encoded data");
            }
            else if (doEncode==0)
            {
                // Decode message
                byte[] cipher = Base64.getUrlDecoder().decode(msgToEncodeOrDecode);
                byte[] message=new byte[cipher.length-Sodium.crypto_box_sealbytes()];
                res=Sodium.crypto_box_seal_open(message,cipher,cipher.length,publicKey,privateKey);
                if (res!=0)
                {
                    Log.v(Constants.LOG_TAG, "Failed to decode the encoded data");
                    success=false;
                    resultStr="Failed to decode the message";
                }
                else
                    resultStr = new String(message, StandardCharsets.UTF_8);
                Sodium.crypto_box_zerobytes();
            }
        } catch (Exception e) {
            success=false;
            resultStr=e.toString();
            Log.e(Constants.LOG_TAG, "Failed: " + resultStr);
            e.printStackTrace();
        }
        // Return the result
        if (isOrderedBroadcast())
        {
            if (TaskerPlugin.Setting.hostSupportsVariableReturn(intent.getExtras()))
            {
                Bundle vars = new Bundle();
                if (success) {
                    vars.putString(resultVarName, resultStr);
                    Log.i("Query success", "Returning "+resultVarName+" with value " + resultStr);
                }
                else
                    vars.putString("%error", resultStr);
                TaskerPlugin.addVariableBundle(getResultExtras(true), vars);
            }

            if (!success)
                setResultCode(TaskerPlugin.Setting.RESULT_CODE_FAILED);
            else
                setResultCode(TaskerPlugin.Setting.RESULT_CODE_OK);
        }
    }
}
