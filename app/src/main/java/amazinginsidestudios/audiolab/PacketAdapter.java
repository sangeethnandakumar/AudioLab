package amazinginsidestudios.audiolab;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.CardView;
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

import com.squareup.picasso.Picasso;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import bullyfox.sangeeth.testube.managers.AppSettings;
import bullyfox.sangeeth.testube.network.FileDownloader;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

/**
 * Created by Sangeeth Nandakumar on 07-01-2018.
 */

public class PacketAdapter extends BaseAdapter {
    Activity activity;
    Context context;
    List<Packet> packetList;
    AppSettings settings;

    ImageView image;
    ImageButton menu_popup;
    TextView actor_movie,duration_size;
    CardView sheet;
    View v;

    public PacketAdapter(Activity activity, Context context, List<Packet> packetList) {
        this.activity = activity;
        this.context = context;
        this.packetList = packetList;
        settings=new AppSettings(context);
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

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup)
    {
        final LinearLayout transit;
            if (packetList.get(i).Type==Type.Sound)
            {
                //Initi
                v=View.inflate(context,R.layout.sound,null);
                image=v.findViewById(R.id.sound_image);
                TextView name=v.findViewById(R.id.sound_name_year);
                actor_movie=v.findViewById(R.id.sound_actor_movie);
                duration_size=v.findViewById(R.id.sound_duration_size);
                final ImageButton play=v.findViewById(R.id.sound_play);
                menu_popup=v.findViewById(R.id.sound_popup);
                sheet=v.findViewById(R.id.sound_sheet);
                final SmoothProgressBar progressBar=v.findViewById(R.id.progress);
                //Swipe Gesture
                v.setOnTouchListener(new QuickShare(activity)
                {
                    public void onSwipeTop() {
                        Toast.makeText(context, "top", Toast.LENGTH_SHORT).show();
                    }
                    public void onSwipeRight()
                    {
                        //QuickShare to WhatsApp
                        quickShare("com.whatsapp");
                    }
                    public void onSwipeLeft() {
                        // shareLink("https://play.google.com/store/apps/details?id=amazinginsidestudios.audiolab");
                    }
                    public void onSwipeBottom() {
                        Toast.makeText(context, "bottom", Toast.LENGTH_SHORT).show();
                    }

                });
                //Menu
                final PopupMenu popup = new PopupMenu(context, menu_popup);
                popup.getMenuInflater().inflate(R.menu.menu_sound_popup, popup.getMenu());
                //PopupMenu
                menu_popup.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                File file = new File("/storage/emulated/0/temp.mp3");
                                Uri uri = Uri.fromFile(file);
                                Intent share = new Intent(Intent.ACTION_SEND);
                                share.setType("audio/mp3");
                                share.putExtra(Intent.EXTRA_STREAM, uri);
                                activity.startActivity(Intent.createChooser(share, "Share Audio File"));
                                return false;
                            }
                        });
                        popup.show();
                    }
                });
                //Setup
                String urlStr = "http://amazinginside.esy.es/amazinginsidestudios/AudioLab/Actors/"+packetList.get(i).PosterUrl;
                URI uri = null;
                try
                {
                    URL url = new URL(urlStr);
                    uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
                    url = uri.toURL();
                    Picasso.with(context).load(url.toString()).into(image);
                }
                catch (Exception e)
                {}
                name.setText(packetList.get(i).Name);
                actor_movie.setText(packetList.get(i).Actor+" | "+packetList.get(i).Movie);
                duration_size.setText(packetList.get(i).Duration+"Sec | "+packetList.get(i).Filesize+"KB");
                //Download & Play
                play.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        URL url = null;
                        try
                        {
                            String urlStr = "http://amazinginside.esy.es/amazinginsidestudios/AudioLab/Sounds/"+packetList.get(i).Movie+"/"+packetList.get(i).Url;
                            URI uri = null;
                            try
                            {
                                url = new URL(urlStr);
                                uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
                                url = uri.toURL();
                            }
                            catch (MalformedURLException e)
                            {
                                e.printStackTrace();
                            }
                            catch (URISyntaxException e)
                            {
                                e.printStackTrace();
                            }
                        }
                        catch (Exception e)
                        {}
                        //Download
                        FileDownloader downloader=new FileDownloader(context,url.toString());
                        downloader.setOnDownloadStatusListner(new FileDownloader.OnDownloadStatusListner() {
                            @Override
                            public void onStarted()
                            {
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
                            public void onConnected(long l, boolean b) {

                            }

                            @Override
                            public void onDownloading(long l, long l1, int i) {
                                progressBar.setSmoothProgressDrawableSpeed(15);
                            }

                            @Override
                            public void onCompleted()
                            {
                                progressBar.setVisibility(View.GONE);
                                play.setImageDrawable(activity.getResources().getDrawable( R.drawable.playing ));
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
                                }
                                catch (Exception e)
                                {}
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
                });
            }
            else if (packetList.get(i).Type==Type.Movie)
            {
                //Initi
                v=View.inflate(context,R.layout.movie,null);
                transit=(LinearLayout)v.findViewById(R.id.transit);
                image=v.findViewById(R.id.movie_image);
                TextView name=v.findViewById(R.id.movie_name_year);
                menu_popup=v.findViewById(R.id.movie_popup);
                //On CLick
                v.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        Intent intent = new Intent(activity, FilterActivity.class);
                        intent.putExtra("type", "Movie");
                        intent.putExtra("name", packetList.get(i).Name);
                        intent.putExtra("image", packetList.get(i).PosterUrl);
                        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, transit, "transit");
                        activity.startActivity(intent, options.toBundle());
                    }
                });
                //Setup
                String urlStr = "http://amazinginside.esy.es/amazinginsidestudios/AudioLab/Movies/"+packetList.get(i).PosterUrl;
                URI uri = null;
                try
                {
                    URL url = new URL(urlStr);
                    uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
                    url = uri.toURL();
                    Picasso.with(context).load(url.toString()).into(image);
                }
                catch (Exception e)
                {}
                name.setText(packetList.get(i).Name + " ("+String.valueOf(packetList.get(i).Year)+")");
            }
            else if (packetList.get(i).Type==Type.Actor)
            {
                //Init
                v=View.inflate(context,R.layout.actor,null);
                transit=(LinearLayout)v.findViewById(R.id.transit);
                image=v.findViewById(R.id.actor_image);
                TextView name=v.findViewById(R.id.actor_name);
                menu_popup=v.findViewById(R.id.actor_popup);
                //Actor Filter
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(activity, FilterActivity.class);
                        intent.putExtra("type", "Actor");
                        intent.putExtra("name", packetList.get(i).Name);
                        intent.putExtra("image", packetList.get(i).PosterUrl);
                        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, transit, "transit");
                        activity.startActivity(intent, options.toBundle());
                    }
                });
                //Setup
                String urlStr = "http://amazinginside.esy.es/amazinginsidestudios/AudioLab/Actors/"+packetList.get(i).PosterUrl;
                URI uri = null;
                try
                {
                    URL url = new URL(urlStr);
                    uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
                    url = uri.toURL();
                    Picasso.with(context).load(url.toString()).into(image);
                }
                catch (Exception e)
                {}
                name.setText(packetList.get(i).Name);
            }
        return v;
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

    public void shareLink(String link)
    {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, link+"\nChat with Malayalam comedy dialogues with just a swipe");
        sendIntent.setType("text/plain");
        activity.startActivity(sendIntent);
    }

    private boolean isAuthenticated()
    {
      if (settings.retriveSettings("isAuthenticated").equals("true"))
      {
          return true;
      }
      else
      {
          return false;
      }
    }

}
