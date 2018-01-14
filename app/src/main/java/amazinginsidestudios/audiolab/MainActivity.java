package amazinginsidestudios.audiolab;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.provider.Settings.Secure;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import bullyfox.sangeeth.testube.managers.AppSettings;
import bullyfox.sangeeth.testube.network.WebServer;
import bullyfox.sangeeth.testube.permission.Permit;

public class MainActivity extends AppCompatActivity {
    private FirebaseAnalytics mFirebaseAnalytics;
    private FirebaseUser mFirebaseUser;
    String mPhotoUrl;
    String mUsername;
    ConstraintLayout signin;
    private FirebaseAuth mAuth;
    private static final int RC_SIGN_IN = 123;
    Button login;
    TextView username,logout;
    ImageView profile;
    AppSettings settings;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        signin= findViewById(R.id.signin);
        login= findViewById(R.id.login);
        logout= findViewById(R.id.signout);
        username=findViewById(R.id.username);
        profile=findViewById(R.id.profile);
        settings=new AppSettings(getApplicationContext());
        settings.saveSettings("isAuthenticated","false");

        checkAuth();

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                authenticateUser();
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });

        Permit permit=new Permit(MainActivity.this);
        permit.setOnPermitStatusListner(new Permit.OnPermitStatusListner() {
            @Override
            public void onAllPermitsGranded() {
                query("");
            }

            @Override
            public void onSomePermitsDenied(ArrayList<String> arrayList) {

            }

            @Override
            public void onAllPermitsDenied() {

            }
        });
        permit.askPermitsFor(Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE);

        final EditText search=(EditText)findViewById(R.id.query);

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search.setText("");
                query("");
            }
        });

        search.setOnKeyListener(new View.OnKeyListener()
        {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) && (i == KeyEvent.KEYCODE_ENTER))
                {
                    query(search.getText().toString());
                    return true;
                }
                return false;
            }
        });


    }



    private void registerUser(FirebaseUser user)
    {
        String hardware = Secure.getString(getApplicationContext().getContentResolver(), Secure.ANDROID_ID);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        String email=user.getEmail();
        email=email.replace("@","_").replace(".","_");
        database.getReference(email+"/Name").setValue(user.getDisplayName());
        database.getReference(email+"/Photo").setValue(user.getPhotoUrl().toString());
        database.getReference(email+"/"+hardware+"/Brand").setValue(Build.BRAND);
        database.getReference(email+"/"+hardware+"/Model").setValue(Build.MODEL);
        database.getReference(email+"/"+hardware+"/Manufacturer").setValue(Build.MANUFACTURER);

        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        double availableMegs = mi.availMem / 0x100000L;
        double totalMegs = mi.totalMem / 0x100000L;
        double percentAvail = mi.availMem / (double)mi.totalMem * 100.0;

        database.getReference(email+"/"+hardware+"/TotalRAM").setValue(String.valueOf(availableMegs+"MB"));
        database.getReference(email+"/"+hardware+"/AvailRAM").setValue(String.valueOf(totalMegs+"MB"));
        database.getReference(email+"/"+hardware+"/CompRAM").setValue(String.valueOf(percentAvail).substring(0,3)+"Per");

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        database.getReference(email+"/"+hardware+"/ScreenWidth").setValue(String.valueOf(width));
        database.getReference(email+"/"+hardware+"/ScreenHeight").setValue(String.valueOf(height));

        double wi=(double)width/(double)displayMetrics.xdpi;
        double hi=(double)height/(double)displayMetrics.ydpi;
        double x = Math.pow(wi,2);
        double y = Math.pow(hi,2);
        double screenInches = Math.sqrt(x+y);

        database.getReference(email+"/"+hardware+"/ScreenInches").setValue(String.valueOf(screenInches).substring(0,4)+"in");
    }





    public void query(String query)
    {
        WebServer server=new WebServer(getApplicationContext());
        server.setOnServerStatusListner(new WebServer.OnServerStatusListner() {
            @Override
            public void onServerResponded(String s) {
                deserialise(s);
            }

            @Override
            public void onServerRevoked() {
                Toast.makeText(MainActivity.this, "Can't connect to server", Toast.LENGTH_SHORT).show();
            }
        });
        String uri = Uri.parse("http://amazinginside.esy.es/amazinginsidestudios/AudioLab/Scripts/query.php")
                .buildUpon()
                .appendQueryParameter("q", query)
                .build().toString();
        server.connectWithGET(uri);
    }

    public void deserialise(String json)
    {
        Type listType = new TypeToken<ArrayList<Packet>>(){}.getType();
        List<Packet> packetList = new Gson().fromJson(json, listType);
        if (packetList==null)
        {
            packetList=new ArrayList<>();
        }
        render(packetList);
    }

    public void render(final List<Packet> packetList)
    {
        PacketAdapter adapter=new PacketAdapter(MainActivity.this,getApplicationContext(),packetList);
        ListView searchList=findViewById(R.id.search_list);
        searchList.setAdapter(adapter);
    }

    public void checkAuth()
    {
        mAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mAuth.getCurrentUser();
        if (mFirebaseUser == null)
        {
            signin.setVisibility(View.VISIBLE);
        }
        else
        {
            signin.setVisibility(View.INVISIBLE);
            mUsername = mFirebaseUser.getDisplayName();
            openConnector(mFirebaseUser);
            registerUser(mFirebaseUser);
        }
    }

    public void authenticateUser()
    {
        List<AuthUI.IdpConfig> providers = Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build());
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN)
        {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK)
            {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                signin.setVisibility(View.GONE);
                openConnector(user);
                registerUser(user);
            }
            else
            {
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void openConnector(FirebaseUser user)
    {
        settings.saveSettings("isAuthenticated","true");
        username.setText(user.getDisplayName());
        if (user.getPhotoUrl() != null)
        {
            Picasso.with(this).load(user.getPhotoUrl().toString()).into(profile);
        }
        else
        {
            Picasso.with(this).load(R.drawable.google).into(profile);
        }
    }

    private void signOut()
    {
        AuthUI.getInstance()
                .signOut(MainActivity.this)
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        checkAuth();
                    }
                });
    }


}
