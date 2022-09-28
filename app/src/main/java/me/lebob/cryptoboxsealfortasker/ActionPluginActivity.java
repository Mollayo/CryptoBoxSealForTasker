package me.lebob.cryptoboxsealfortasker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;


import me.lebob.cryptoboxsealfortasker.utils.BundleScrubber;
import me.lebob.cryptoboxsealfortasker.utils.Constants;
import me.lebob.cryptoboxsealfortasker.utils.ActionBundleManager;
import me.lebob.cryptoboxsealfortasker.utils.TaskerPlugin;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;


public class ActionPluginActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(Constants.LOG_TAG, "ActionPluginActivity::onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_action);

        final String previousBlurb = getIntent().getStringExtra(com.twofortyfouram.locale.api.Intent.EXTRA_STRING_BLURB);

        final Bundle previousBundle = getIntent().getBundleExtra(com.twofortyfouram.locale.api.Intent.EXTRA_BUNDLE);
        BundleScrubber.scrub(previousBundle);
        if (isBundleValid(previousBundle) && previousBlurb!=null)
            onPostCreateWithPreviousResult(previousBundle,previousBlurb);

        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.doEncode);
        radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                updateWidgetVisibility(checkedId);
            }
        });
    }

    private void updateWidgetVisibility(int checkedId)
    {
        EditText msgToEncodeOrDecode=findViewById(R.id.msgToEncodeOrDecode);
        EditText privateKey=findViewById(R.id.privateKey);
        // checkedId is the RadioButton selected
        if (checkedId==R.id.cryptoBoxSeal) {
            // Show the widgets specific to sending a message
            privateKey.setVisibility(INVISIBLE);
        }
        else {
            // Hide the widgets
            privateKey.setVisibility(VISIBLE);
        }
    }

    // Method that gets called once request to enable bluetooth completes
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v(Constants.LOG_TAG, "ActionPluginActivity::onActivityResult");
    }

    // Method that checks if bundle is valid
    public boolean isBundleValid(Bundle bundle) {
        Log.v(Constants.LOG_TAG, "ActionPluginActivity::isBundleValid");
        return ActionBundleManager.isBundleValid(bundle);
    }

    // Method that uses previously saved bundle
    public void onPostCreateWithPreviousResult(Bundle bundle,String blurb) {
        Log.v(Constants.LOG_TAG, "ActionPluginActivity::onPostCreateWithPreviousResult");

        final int doEncode = ActionBundleManager.getDoEncode(bundle);
        if (doEncode==1) {
            ((RadioGroup) findViewById(R.id.doEncode)).check(R.id.cryptoBoxSeal);
            updateWidgetVisibility(R.id.cryptoBoxSeal);
        }
        else if (doEncode==0) {
            ((RadioGroup) findViewById(R.id.doEncode)).check(R.id.cryptoBoxSealOpen);
            updateWidgetVisibility(R.id.cryptoBoxSealOpen);
        }

        final String msgToEncodeOrDecode = ActionBundleManager.getMsgToEncodeOrDecode(bundle);
        ((EditText) findViewById(R.id.msgToEncodeOrDecode)).setText(msgToEncodeOrDecode);
        final String publicKey = ActionBundleManager.getPublicKey(bundle);
        ((EditText) findViewById(R.id.publicKey)).setText(publicKey);
        final String privateKey = ActionBundleManager.getPrivateKey(bundle);
        ((EditText) findViewById(R.id.privateKey)).setText(privateKey);
        final String resultVarName = ActionBundleManager.getResultVarName(bundle);
        ((EditText) findViewById(R.id.resultVarName)).setText(resultVarName);
    }

    // Method that returns the bundle to be saved
    public Bundle getResultBundle() {
        Log.v(Constants.LOG_TAG, "ActionPluginActivity::getResultBundle");
        String msgToEncodeOrDecode = ((EditText) findViewById(R.id.msgToEncodeOrDecode)).getText().toString();
        String publicKey = ((EditText) findViewById(R.id.publicKey)).getText().toString();
        String privateKey = ((EditText) findViewById(R.id.privateKey)).getText().toString();
        String resultVarName = ((EditText) findViewById(R.id.resultVarName)).getText().toString();

        int val = ((RadioGroup)findViewById(R.id.doEncode)).getCheckedRadioButtonId();
        int doEncode=1;
        if (val==R.id.cryptoBoxSeal)
            doEncode=1;
        else if (val==R.id.cryptoBoxSealOpen)
            doEncode=0;

        Bundle bundle = ActionBundleManager.generateBundle(doEncode, msgToEncodeOrDecode, publicKey, privateKey, resultVarName);

        if (bundle == null) {
            Context context = getApplicationContext();
            String error = ActionBundleManager.getErrorMessage(context, doEncode, msgToEncodeOrDecode, publicKey, privateKey, resultVarName);
            if (error != null) {
                Toast.makeText(context, error, Toast.LENGTH_LONG).show();
            } else {
                Log.e(Constants.LOG_TAG, "Null bundle, but no error");
            }
            return null;
        }

        // This is to specify that the some of the data can be Tasker variables
        if (TaskerPlugin.Setting.hostSupportsOnFireVariableReplacement(this)) {
            TaskerPlugin.Setting.setVariableReplaceKeys(bundle, new String[]{
                    Constants.BUNDLE_MSG_TO_ENCODE_OR_DECODE,
                    Constants.BUNDLE_PUBLIC_KEY,
                    Constants.BUNDLE_PRIVATE_KEY,
                    Constants.BUNDLE_RESULT_VAR_NAME});
        }
        return bundle;
    }

    // Method that creates summary of bundle
    public String getResultBlurb(Bundle bundle) {
        Log.v(Constants.LOG_TAG, "ActionPluginActivity::getResultBlurb");
        return ActionBundleManager.getBundleBlurb(bundle);
    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    public void onBackPressed() {

        // The bundle to save the user parameters
        final Bundle resultBundle = getResultBundle();
        if (!isBundleValid(resultBundle))
            return;

        super.onStop();
        Intent intent = new Intent();
        intent.putExtra(com.twofortyfouram.locale.api.Intent.EXTRA_BUNDLE, resultBundle);

        // For the explanation text of the plugin
        String blurbStr=getResultBlurb(resultBundle);
        intent.putExtra(com.twofortyfouram.locale.api.Intent.EXTRA_STRING_BLURB, blurbStr);

        // For the synchronous execution
        if (TaskerPlugin.Setting.hostSupportsSynchronousExecution( getIntent().getExtras()))
            TaskerPlugin.Setting.requestTimeoutMS( intent, 7000 );

        // Show variables to the user in Tasker
        if ( TaskerPlugin.hostSupportsRelevantVariables( getIntent().getExtras() ) ) {
            if (ActionBundleManager.getDoEncode(resultBundle)==1)
                TaskerPlugin.addRelevantVariableList(intent, new String[]{
                        ActionBundleManager.getResultVarName(resultBundle) + "\nEncoded msg\nVariable containing the result of the encoding",
                        "%error\nError\nThe error if any"});
            else
                TaskerPlugin.addRelevantVariableList(intent, new String[]{
                        ActionBundleManager.getResultVarName(resultBundle) + "\nDecoded msg\nVariable containing the result of the decoding",
                        "%error\nError\nThe error if any"});
        }

        setResult(RESULT_OK, intent);
        finish();
    }
}

