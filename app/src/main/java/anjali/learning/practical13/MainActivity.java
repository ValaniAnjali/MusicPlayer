package anjali.learning.practical13;

import android.animation.ObjectAnimator;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Bundle;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private ImageView music;
    private ImageButton forwardBtn, backwardBtn, pauseBtn, playBtn, prevBtn, nextBtn;
    private MediaPlayer mediaPlayer;
    private TextView songName, startTime, songTime;
    private SeekBar songProgress;
    private static int onTime = 0, playTime = 0, endTime = 0, forwardTime = 5000, backwardTime = 5000;
    private Handler handler = new Handler();
    private int[] songs = {R.raw.song, R.raw.song2, R.raw.song3, R.raw.song4}; // Replace with your song resources
    private String[] songNames = {"Song 1", "Song 2", "Song 3", "Song 4"};
    private int[] images={R.drawable.img1,R.drawable.img2,R.drawable.img3,R.drawable.img4};// Replace with your song names
    private int currentSongIndex = 0;

    private ObjectAnimator rotationAnimator; // For smooth rotation

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        backwardBtn = findViewById(R.id.btnBackward);
        forwardBtn = findViewById(R.id.btnForward);
        prevBtn = findViewById(R.id.btnPrev);
        nextBtn = findViewById(R.id.btnNext);
        playBtn = findViewById(R.id.btnPlay);
        pauseBtn = findViewById(R.id.btnPause);
        songName = findViewById(R.id.txtSongName);
        startTime = findViewById(R.id.txtStartTime);
        songTime = findViewById(R.id.txtSongTime);
        music = findViewById(R.id.music);

        songName.setText(songNames[0]);
        mediaPlayer = MediaPlayer.create(this, R.raw.song);
        songProgress = findViewById(R.id.seekBar);
        songProgress.setClickable(true);

        playBtn.setVisibility(View.VISIBLE);
        pauseBtn.setVisibility(View.GONE);

        // Initialize rotation animator
        setupRotationAnimator();

        playBtn.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Playing Audio", Toast.LENGTH_SHORT).show();
            mediaPlayer.start();
            endTime = mediaPlayer.getDuration();
            playTime = mediaPlayer.getCurrentPosition();

            if (onTime == 0) {
                songProgress.setMax(endTime);
                onTime = 1;
            }

            songTime.setText(formatTime(endTime));
            startTime.setText(formatTime(playTime));
            songProgress.setProgress(playTime);
            handler.postDelayed(UpdateSongTime, 100);

            pauseBtn.setVisibility(View.VISIBLE);
            playBtn.setVisibility(View.GONE);

            rotationAnimator.start(); // Start the smooth rotation
        });

        pauseBtn.setOnClickListener(v -> {
            mediaPlayer.pause();
            Toast.makeText(getApplicationContext(), "Pausing Audio", Toast.LENGTH_SHORT).show();
            pauseBtn.setVisibility(View.GONE);
            playBtn.setVisibility(View.VISIBLE);

            rotationAnimator.cancel(); // Stop the animation when music is paused
        });

        forwardBtn.setOnClickListener(v -> {
            if ((playTime + forwardTime) <= endTime) {
                playTime += forwardTime;
                mediaPlayer.seekTo(playTime);
            } else {
                Toast.makeText(getApplicationContext(), "Cannot jump forward 5 seconds", Toast.LENGTH_SHORT).show();
            }
        });

        backwardBtn.setOnClickListener(v -> {
            if ((playTime - backwardTime) > 0) {
                playTime -= backwardTime;
                mediaPlayer.seekTo(playTime);
            } else {
                Toast.makeText(getApplicationContext(), "Cannot jump backward 5 seconds", Toast.LENGTH_SHORT).show();
            }
        });

        prevBtn.setOnClickListener(view -> playPreviousSong());
        nextBtn.setOnClickListener(view -> playNextSong());
    }

    private void setupRotationAnimator() {
        rotationAnimator = ObjectAnimator.ofFloat(music, "rotation", 0f, 360f);
        rotationAnimator.setDuration(2000); // Duration for one full rotation
        rotationAnimator.setInterpolator(new LinearInterpolator()); // Ensures smooth rotation
        rotationAnimator.setRepeatCount(ObjectAnimator.INFINITE); // Rotate indefinitely
    }

    private void playPreviousSong() {
        if (currentSongIndex > 0) {
            currentSongIndex--;

            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
            }

            mediaPlayer = MediaPlayer.create(MainActivity.this, songs[currentSongIndex]);
            songName.setText(songNames[currentSongIndex]);
            music.setImageResource(images[currentSongIndex]);

            updateUIForNewSong();
        } else {
            Toast.makeText(MainActivity.this, "No previous song", Toast.LENGTH_SHORT).show();
        }
    }

    private void playNextSong() {
        if (currentSongIndex < songs.length - 1) {
            currentSongIndex++;

            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
            }

            mediaPlayer = MediaPlayer.create(MainActivity.this, songs[currentSongIndex]);
            songName.setText(songNames[currentSongIndex]);
            music.setImageResource(images[currentSongIndex]);
            updateUIForNewSong();
        } else {
            Toast.makeText(MainActivity.this, "No next song", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUIForNewSong() {
        endTime = mediaPlayer.getDuration();
        songTime.setText(formatTime(endTime));
        startTime.setText("0:00");
        songProgress.setMax(endTime);
        songProgress.setProgress(0);

        mediaPlayer.start();
        handler.postDelayed(UpdateSongTime, 100);

        pauseBtn.setVisibility(View.VISIBLE);
        playBtn.setVisibility(View.GONE);

        rotationAnimator.start(); // Start rotating for the new song
    }

    private String formatTime(int time) {
        return String.format("%d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(time),
                TimeUnit.MILLISECONDS.toSeconds(time) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time)));
    }

    private Runnable UpdateSongTime = new Runnable() {
        @Override
        public void run() {
            playTime = mediaPlayer.getCurrentPosition();
            startTime.setText(formatTime(playTime));
            songProgress.setProgress(playTime);
            handler.postDelayed(this, 100);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        rotationAnimator.cancel(); // Stop animation when activity is destroyed
    }
}
