package amazinginsidestudios.audiolab;

/**
 * Created by Sangeeth Nandakumar on 11-01-2018.
 */

import android.content.Context;
import android.media.MediaPlayer;

import java.io.IOException;

public class StreamPlayer
{
    private MediaPlayer mediaPlayer;
    private int playbackPosition=0;
    private Context context;

    public StreamPlayer(Context context) {
        this.context = context;
    }

    public void pauseAudio()
    {
        if(mediaPlayer != null && mediaPlayer.isPlaying())
        {
            playbackPosition = mediaPlayer.getCurrentPosition();
            mediaPlayer.pause();
        }
    }

    public void restartAudio()
    {
        if(mediaPlayer != null && !mediaPlayer.isPlaying())
        {
            mediaPlayer.seekTo(playbackPosition);
            mediaPlayer.start();
        }
    }

    public void resetAudio()
    {
        if(mediaPlayer != null)
        {
            mediaPlayer.stop();
            playbackPosition = 0;
        }
    }

    public void playAudio(String url)
    {
        killMediaPlayer();
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void playLocalAudio(int resId) throws Exception
    {
        mediaPlayer = MediaPlayer.create(context,resId);
        mediaPlayer.start();
    }

    public void killMediaPlayer()
    {
        if(mediaPlayer!=null)
        {
            try
            {
                mediaPlayer.release();
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}

