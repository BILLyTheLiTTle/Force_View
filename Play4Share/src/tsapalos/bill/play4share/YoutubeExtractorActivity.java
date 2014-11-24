
package tsapalos.bill.play4share;

import tsapalos.bill.play4share.R;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class YoutubeExtractorActivity extends Activity {

    private TextView incomingURLTextView, youtubeVideoURLTextView;
    private Button play;

    private String htmlSource, link, videoUrl = null,
            exceptionLog = "Dear LiTTle,\n\nI have no bug to report but I want to thank you for your effort.";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube_extractor);

        incomingURLTextView = (TextView) findViewById(R.id.incoming_url_content_textview);
        youtubeVideoURLTextView = (TextView) findViewById(R.id.youtube_video_url_content_textview);
        play = (Button) findViewById(R.id.play_button);

        Intent intent = getIntent();
        link = intent.getDataString();
        if (link != null)
            incomingURLTextView.setText(link);

        // get the html source and export the video url
        stripUrl();
    }

    private void stripUrl() {
        final Handler handler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 3) {
                    String txt = getString(R.string.play) + " (3)";
                    play.setText(txt);
                }
                else if (msg.what == 2) {
                    youtubeVideoURLTextView.setText(R.string.searching);
                    String txt = getString(R.string.play) + " (2)";
                    play.setText(txt);
                }
                else if (msg.what == 1) {
                    String txt = getString(R.string.play) + " (1)";
                    play.setText(txt);
                }
                else if (msg.what == 0) {
                    youtubeVideoURLTextView.setText("https://www.youtube.com/watch?v=" + videoUrl);
                    String txt = getString(R.string.play);
                    play.setText(txt);
                    play.setEnabled(true);
                    String toast = String.format(getString(R.string.ok_toast),
                            getString(R.string.play));
                    Toast.makeText(getApplicationContext(), toast, Toast.LENGTH_LONG).show();
                }
                else if (msg.what == -1) {
                    youtubeVideoURLTextView.setText(R.string.not_found);
                    String txt = getString(R.string.play);
                    play.setText(txt);
                    String toast = String.format(getString(R.string.error_toast),
                            getString(R.string.bug_report));
                    Toast.makeText(getApplicationContext(), toast, Toast.LENGTH_LONG).show();
                }
            }
        };
        new Thread() {
            public void run() {
                try {
                    if (link != null) {
                        handler.sendMessage(handler.obtainMessage(3));
                        String raw = UrlUtils.getRawPageUrl(link);
                        // Log.e("RAW", raw);
                        handler.sendMessage(handler.obtainMessage(2));
                        htmlSource = UrlUtils.getHtmlSource(raw);
                        // Log.e("PAGE", htmlSource);
                        handler.sendMessage(handler.obtainMessage(1));
                        videoUrl = UrlUtils.exportVideoUrl(htmlSource);
                        // Log.e("VIDEO", videoUrl);
                        if (videoUrl == null) {
                            throw new NullPointerException("The video URL is null!");
                        }
                        handler.sendMessage(handler.obtainMessage(0));
                    }
                } catch (Exception ex) {
                    exceptionLog = "The URL (" + link
                            + ") throws an excpetion.\nThe esception log is:\n==========\n";
                    exceptionLog = exceptionLog + ex.getMessage() + "\n==========";
                    handler.sendMessage(handler.obtainMessage(-1));
                }
            };
        }.start();
    }

    public void play(View view) {
        String url = "https://www.youtube.com/watch?v=" + videoUrl;
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    public void exit(View view) {
        finish();
    }

    public void reportBug(View view) {
        final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        // Fill it with Data
        emailIntent.setType("plain/text");
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] {
            "littleprog@gmail.com"
        });
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "[BUG] YouForce video sniffing");
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, exceptionLog);
        // Send it off to the Activity-Chooser
        startActivity(Intent.createChooser(emailIntent, getString(R.string.email_chooser_title)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.youtube_extractor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}