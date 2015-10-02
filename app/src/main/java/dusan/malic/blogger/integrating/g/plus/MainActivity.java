package dusan.malic.blogger.integrating.g.plus;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{
    /* TAG string for logcat output */
    private static final String TAG = "MainActivity";

    /* Client used to interact with Google APIs. */
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Build GoogleApiClient with access to basic profile */
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                /* We want to access basic profile information */
                .addScope(new Scope(Scopes.PROFILE))
                /* We also want to access Google account email address */
                .addScope(new Scope(Scopes.EMAIL))
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        /* connect to GoogleApiClient */
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        /* disconnect from GoogleApiClient */
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        // onConnected indicates that an account was selected on the device, that the selected
        // account has granted any requested permissions to our app and that we were able to
        // establish a service connection to Google Play services.
        Log.d(TAG, "onConnected:" + bundle);

        setUserDetails();
    }

    // This method will be called only after GoogleApiClient connection.
    // In this method it is safe to use GoogleApiClient.
    private void setUserDetails(){
        // get selected google profile
        Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);

        // just to be sure that everything went well
        if (currentPerson == null)
            return;

        // set user name into TextView
        ((TextView) findViewById(R.id.google_name)).setText(currentPerson.getDisplayName());

        // set user email into TextView
        ((TextView) findViewById(R.id.google_email))
                .setText(Plus.AccountApi.getAccountName(mGoogleApiClient));

        // if user has profile picture load it into ImageView
        if (currentPerson.getImage() != null){
            Picasso.with(this)
                    .load(currentPerson.getImage().getUrl())
                    .into((ImageView) findViewById(R.id.google_profile));
        }

        // if user has cover image load it into ImageView
        if(currentPerson.getCover() != null){
            Picasso.with(this)
                    .load(currentPerson.getCover().getCoverPhoto().getUrl())
                    .into((ImageView) findViewById(R.id.google_cover));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended");
        // no need to implement this method
        // GoogleApiClient will automatically try to reconnect
        // stackoverflow.com/questions/26056148
    }

    /* Request code used to invoke sign in user interactions. */
    private static final int RC_SIGN_IN = 123;

    /* Is there a ConnectionResult resolution in progress? */
    private boolean mIsResolving = false;

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        // Could not connect to Google Play Services.  The user needs to select an account,
        // grant permissions or resolve an error in order to sign in.

        // if we aren't resolving already and if it has a resolution
        if (!mIsResolving && connectionResult.hasResolution()) {
            try {
                // start resolution
                connectionResult.startResolutionForResult(this, RC_SIGN_IN);
                // set flag to true, since we are resolving at the moment
                mIsResolving = true;
            } catch (IntentSender.SendIntentException e) {
                Log.e(TAG, "Could not resolve ConnectionResult.", e);
                // if some error occurred reset resolving flag
                mIsResolving = false;
                // try to connect again
                mGoogleApiClient.connect();
            }
        } else {
            // Could not resolve the connection result, show the user an
            // error dialog.
            // Note: I am using Toast here but it is always god practice to display a good error
            // message (show exactly what went wrong)
            Toast.makeText(getApplicationContext(), "Could not resolve result: " + connectionResult,
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult:" + requestCode + ":" + resultCode + ":" + data);

        // If it is result for our request code
        if (requestCode == RC_SIGN_IN) {
            // we aren't resolving anymore
            mIsResolving = false;

            // If the error resolution was successful, continue connection
            if (resultCode == RESULT_OK) {
                mGoogleApiClient.connect();
            } else {
                // If the error resolution was not successful show Toast
                // Note: I am using Toast here but it is always god practice to display a good error
                // message (show exactly what went wrong)
                Toast.makeText(getApplicationContext(), "Could not resolve",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}
