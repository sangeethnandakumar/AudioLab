package amazinginsidestudios.audiolab;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import bullyfox.sangeeth.testube.network.WebServer;

public class FilterActivity extends AppCompatActivity {
    ListView filterList;
    ImageView filterImage;
    TextView filterName;
    LinearLayout topsheet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
        filterList = (ListView)findViewById(R.id.filter_list);
        filterImage = (ImageView)findViewById(R.id.filter_image);
        filterName = (TextView)findViewById(R.id.filter_name);
        topsheet = (LinearLayout)findViewById(R.id.topsheet);

        Intent i = getIntent();
        Bundle b = i.getExtras();
        String type = b.getString("type");
        String name = b.getString("name");
        String image = b.getString("image");
        filterName.setText(name);

        switch (type) {
            case "Movie":
                queryMovie(name);
                fetchMovieImage(image);
                break;
            case "Actor":
                filterImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                queryActor(name);
                fetchActorImage(image);
                break;
        }



    }

    public void queryMovie(String query)
    {
        WebServer server=new WebServer(getApplicationContext());
        server.setOnServerStatusListner(new WebServer.OnServerStatusListner() {
            @Override
            public void onServerResponded(String s) {
                deserialise(s);
            }

            @Override
            public void onServerRevoked() {
                Toast.makeText(FilterActivity.this, "Can't connect to server", Toast.LENGTH_SHORT).show();
            }
        });
        String uri = Uri.parse("http://amazinginside.esy.es/amazinginsidestudios/AudioLab/Scripts/movie.php")
                .buildUpon()
                .appendQueryParameter("q", query)
                .build().toString();
        server.connectWithGET(uri);
    }

    public void queryActor(String query)
    {
        WebServer server=new WebServer(getApplicationContext());
        server.setOnServerStatusListner(new WebServer.OnServerStatusListner() {
            @Override
            public void onServerResponded(String s) {
                deserialise(s);
            }

            @Override
            public void onServerRevoked() {
                Toast.makeText(FilterActivity.this, "Can't connect to server", Toast.LENGTH_SHORT).show();
            }
        });
        String uri = Uri.parse("http://amazinginside.esy.es/amazinginsidestudios/AudioLab/Scripts/actor.php")
                .buildUpon()
                .appendQueryParameter("q", query)
                .build().toString();
        server.connectWithGET(uri);
    }

    public void deserialise(String json)
    {
        java.lang.reflect.Type listType = new TypeToken<ArrayList<Packet>>(){}.getType();
        List<Packet> packetList = new Gson().fromJson(json, listType);
        render(packetList);
    }

    public void render(List<Packet> packetList)
    {
        if (packetList==null)
        {
            packetList=new ArrayList<>();
        }
        PacketAdapter adapter=new PacketAdapter(FilterActivity.this,getApplicationContext(),packetList);
        filterList.setAdapter(adapter);
    }

    public void fetchMovieImage(String key)
    {
        String urlStr = "http://amazinginside.esy.es/amazinginsidestudios/AudioLab/Movies/"+key;
        URI uri = null;
        try
        {
            URL url = new URL(urlStr);
            uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
            url = uri.toURL();
            Picasso.with(getApplicationContext()).load(url.toString()).into(filterImage);
        }
        catch (Exception e)
        {}
    }

    public void fetchActorImage(String key)
    {
        String urlStr = "http://amazinginside.esy.es/amazinginsidestudios/AudioLab/Actors/"+key;
        URI uri = null;
        try
        {
            URL url = new URL(urlStr);
            uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
            url = uri.toURL();
            Picasso.with(getApplicationContext()).load(url.toString()).into(filterImage);
        }
        catch (Exception e)
        {}
    }
}
