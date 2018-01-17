package amazinginsidestudios.audiolab;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v4.app.ActivityOptionsCompat;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.List;
import bullyfox.sangeeth.testube.network.FileDownloader;
import bullyfox.sangeeth.testube.network.WebServer;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

/**
 * Created by Sangeeth Nandakumar on 07-01-2018.
 */

public class PacketAdapter extends BaseAdapter {
    Activity activity;
    Context context;
    List<Packet> packetList;


    public PacketAdapter(Activity activity, Context context, List<Packet> packetList)
    {
        this.activity = activity;
        this.context = context;
        this.packetList = packetList;
    }

    @Override
    public int getCount() {
        return packetList.size();
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public Object getItem(int i) {
        return packetList.get(i);
    }



    //HANDLER ENGINES
    public View handleSound(int i)
    {
        //Inflate Sound View
        View v=View.inflate(context,R.layout.sound,null);
        //Components
        ImageView image=(ImageView)v.findViewById(R.id.sound_image);
        TextView name=(TextView)v.findViewById(R.id.sound_name_year);
        TextView actor_movie=(TextView)v.findViewById(R.id.sound_actor_movie);
        TextView duration_size=(TextView)v.findViewById(R.id.sound_duration_size);
        ImageButton play=(ImageButton)v.findViewById(R.id.sound_play);
        ImageButton menu_popup=(ImageButton)v.findViewById(R.id.sound_popup);
        SmoothProgressBar progressBar=(SmoothProgressBar)v.findViewById(R.id.progress);
        //Fill Info
        name.setText(packetList.get(i).Name);
        actor_movie.setText(packetList.get(i).Actor+" | "+packetList.get(i).Movie);
        duration_size.setText(packetList.get(i).Duration+"Sec | "+packetList.get(i).Filesize+"KB");
        //Identify Swipes
        identifySoundSwipes(v);
        //Identify MenuClicks
        inflateSoundMenu(i,menu_popup);
        //Render Poster
        renderActorPoster(image,actorImageResolver(i));
        //Handle PLayButton clicks
        playButtonClick(i,progressBar,play);
        //Return view
        return v;
    }

    public View handleActor(int i)
    {
        //Inflate Actor View
        View v=View.inflate(context,R.layout.actor,null);
        //Components
        ImageView image=(ImageView)v.findViewById(R.id.actor_image);
        TextView name=(TextView)v.findViewById(R.id.actor_name);
        ImageButton menu_popup=(ImageButton)v.findViewById(R.id.actor_popup);
        //Fill Info
        name.setText(packetList.get(i).Name);
        //Identify Clicks
        identifyActorClicks(v,i);
        //Identify MenuClicks
        inflateActorMenu(menu_popup);
        //Render Poster
        renderActorPoster(image,actorImageResolver(i));
        //Return view
        return v;
    }

    public View handleMovie(final int i)
    {
        //Inflate Movie View
        View v=View.inflate(context,R.layout.movie,null);
        //Components
        ImageView image=(ImageView)v.findViewById(R.id.movie_image);
        TextView name=(TextView)v.findViewById(R.id.movie_name);
        ImageButton menu_popup=(ImageButton)v.findViewById(R.id.movie_popup);
        //Fill Info
        name.setText(packetList.get(i).Name+" ("+packetList.get(i).Year+")");
        //Identify Clicks
        identifyMovieClicks(v,i);
        //Identify MenuClicks
        inflateMovieMenu(menu_popup);
        //Render Poster
        renderMoviePoster(image,movieImageResolver(i));
        //Return view
        return v;
    }


    //SWIPE MACHINES
    private void identifySoundSwipes(View view)
    {
        view.setOnTouchListener(new QuickShare(activity)
        {
            public void onSwipeTop()
            {
                Toast.makeText(context, "TOP", Toast.LENGTH_SHORT).show();
            }

            public void onSwipeRight()
            {
                quickShare("com.whatsapp");
            }

            public void onSwipeLeft() {

            }

            public void onSwipeBottom() {
                Toast.makeText(context, "BOTTOM", Toast.LENGTH_SHORT).show();
            }
        });
    }



    //CLICK MACHINES
    private void identifyMovieClicks(View view,final int i)
    {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LinearLayout transit=(LinearLayout)view.findViewById(R.id.transit);
                Intent intent = new Intent(activity, FilterActivity.class);
                intent.putExtra("type", "Movie");
                intent.putExtra("name", packetList.get(i).Name);
                intent.putExtra("image", packetList.get(i).PosterUrl);
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, transit, "transit");
                activity.startActivity(intent, options.toBundle());
            }
        });
    }

    private void identifyActorClicks(View view,final int i)
    {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LinearLayout transit=(LinearLayout)view.findViewById(R.id.transit);
                Intent intent = new Intent(activity, FilterActivity.class);
                intent.putExtra("type", "Actor");
                intent.putExtra("name", packetList.get(i).Name);
                intent.putExtra("image", packetList.get(i).PosterUrl);
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, transit, "transit");
                activity.startActivity(intent, options.toBundle());
            }
        });
    }





    //MENU MACHINES
    private void inflateSoundMenu(final int i,ImageButton menu_popup)
    {
        final PopupMenu popup = new PopupMenu(context, menu_popup);
        popup.getMenuInflater().inflate(R.menu.menu_sound_popup, popup.getMenu());
        menu_popup.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
                {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem)
                    {
                        switch (menuItem.getItemId())
                        {
                            case R.id.sendto:
                                openShareDIalogue();
                                return true;
                            case R.id.reportpoor:
                                poorQualityReporter(i);
                                return true;
                            case R.id.reportwrong:
                                wrongInfoReporter(i);
                                return true;
                            default:
                                return true;
                        }
                    }
                });
                popup.show();
            }
        });
    }


    private void inflateActorMenu(ImageButton menu_popup)
    {
        final PopupMenu popup = new PopupMenu(context, menu_popup);
        popup.getMenuInflater().inflate(R.menu.menu_actor_popup, popup.getMenu());
        menu_popup.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                popup.show();
            }
        });
    }

    private void inflateMovieMenu(ImageButton menu_popup)
    {
        final PopupMenu popup = new PopupMenu(context, menu_popup);
        popup.getMenuInflater().inflate(R.menu.menu_movie_popup, popup.getMenu());
        menu_popup.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                popup.show();
            }
        });
    }

    private void openShareDIalogue()
    {
        File file = new File("/storage/emulated/0/temp.mp3");
        Uri uri = Uri.fromFile(file);
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("audio/mp3");
        share.putExtra(Intent.EXTRA_STREAM, uri);
        activity.startActivity(Intent.createChooser(share, "Share Audio File"));
    }


    //REPORTERS
    private void poorQualityReporter(final int i)
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("Reports/"+packetList.get(i).Name+"/PoorQuality").setValue(0);
    }

    private void wrongInfoReporter(final int i)
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("Reports/"+packetList.get(i).Name+"/WrongInfo").setValue(0);
    }



    //URL RESOLVERS
    private String actorImageResolver(int i)
    {
        String urlStr = "http://amazinginside.esy.es/amazinginsidestudios/AudioLab/Actors/"+packetList.get(i).PosterUrl;
        URI uri = null;
        try
        {
            URL url = new URL(urlStr);
            uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
            url = uri.toURL();
        }
        catch (Exception e)
        {}
        return uri.toString();
    }

    private String movieImageResolver(int i)
    {
        String urlStr = "http://amazinginside.esy.es/amazinginsidestudios/AudioLab/Movies/"+packetList.get(i).PosterUrl;
        URI uri = null;
        try
        {
            URL url = new URL(urlStr);
            uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
            url = uri.toURL();
        }
        catch (Exception e)
        {}
        return uri.toString();
    }

    private String soundResolver(int i)
    {
        String urlStr = "http://amazinginside.esy.es/amazinginsidestudios/AudioLab/Sounds/"+packetList.get(i).Movie+"/"+packetList.get(i).Url;
        URI uri = null;
        try
        {
            URL url = new URL(urlStr);
            uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
            url = uri.toURL();
        }
        catch (Exception e)
        {}
        return uri.toString();
    }


    //ANALITICS
    private void reportDownload(final int i)
    {
        String urlStr = "http://amazinginside.esy.es/amazinginsidestudios/AudioLab/Scripts/sound_usage.php?q="+packetList.get(i).Name;
        URI uri = null;
        try
        {
            URL url = new URL(urlStr);
            uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
            url = uri.toURL();
        }
        catch (Exception e)
        {}
        WebServer server=new WebServer(context);
        server.setOnServerStatusListner(new WebServer.OnServerStatusListner() {
            @Override
            public void onServerResponded(String s) {}
            @Override
            public void onServerRevoked() {}
        });
        server.connectWithGET(uri.toString());
    }



    //RENDERS
    private void renderActorPoster(ImageView image,String url)
    {
        Picasso.with(context).load(url).into(image);
    }

    private void renderMoviePoster(ImageView image,String url)
    {
        Picasso.with(context).load(url).into(image);
    }




    //DOWNLOAD ENGINE
    private void downloadSound(final int i,final SmoothProgressBar progressBar,final ImageButton play)
    {
        FileDownloader downloader=new FileDownloader(context,soundResolver(i));
        downloader.setOnDownloadStatusListner(new FileDownloader.OnDownloadStatusListner()
        {
            @Override
            public void onStarted() {
                progressBar.setVisibility(View.VISIBLE);
                File file = new File("/storage/emulated/0" + File.separator + "temp.mp3");
                if (file.exists())
                {
                    file.delete();
                }
            }

            @Override
            public void onConnecting() {
                play.setImageDrawable(activity.getResources().getDrawable( R.drawable.downloading));
                progressBar.setSmoothProgressDrawableSpeed(10);
            }

            @Override
            public void onConnected(long l, boolean b) {}

            @Override
            public void onDownloading(long l, long l1, int i) {
                progressBar.setSmoothProgressDrawableSpeed(15);
            }

            @Override
            public void onCompleted() {
                reportDownload(i);
                progressBar.setVisibility(View.GONE);
                playAudio(play);
            }

            @Override
            public void onFailed(String s) {}

            @Override
            public void onPaused() {}

            @Override
            public void onCancelled() {}
        });
        downloader.downloadFile("/storage/emulated/0","temp.mp3");
    }


    //CONTENT CLICKS
    private void playButtonClick(final int i,final SmoothProgressBar progressBar,final ImageButton play)
    {
        play.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                //Download & Play
                downloadSound(i,progressBar,play);
            }
        });
    }


    //AUDIO PLAYER
    private void playAudio(final ImageButton play)
    {
        MediaPlayer mp = new MediaPlayer();
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer)
            {
                play.setImageDrawable(activity.getResources().getDrawable( R.drawable.play));
            }
        });
        try
        {
            mp.setDataSource("/storage/emulated/0" + File.separator + "temp.mp3");
            mp.prepare();
            mp.start();
            play.setImageDrawable(activity.getResources().getDrawable( R.drawable.playing ));
        }
        catch (Exception e)
        {}
    }


    @Override
    public View getView(final int i, View view, ViewGroup viewGroup)
    {
        switch (packetList.get(i).Type)
        {
            case Sound:
                return handleSound(i);
            case Actor:
                return handleActor(i);
            case Movie:
                return handleMovie(i);
            default:
                return view;
        }
    }


    public void quickShare(String package_name)
    {
        File file = new File("/storage/emulated/0/temp.mp3");
        Uri uri = Uri.fromFile(file);
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("audio/mp3");
        share.setPackage(package_name);
        share.putExtra(Intent.EXTRA_STREAM, uri);
        activity.startActivity(share);
    }

}
