package com.example.tts_test;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;


public class MainActivity extends Activity {
	Button speakBtn;
	Button stopspeakBtn;
    TtsManager tts;
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        tts = TtsManager.getInstance(getApplicationContext());
        tts.init();
        tts.startSpeaking();
        
        th.start();
        
        speakBtn = (Button)findViewById(R.id.speakBtn);
        speakBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				tts.playText("前方100米有施工,请减速慢行");
			}
		});
        
        stopspeakBtn = (Button)findViewById(R.id.stopSpeakBtn);
        stopspeakBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				tts.stopSpeaking();
			}
		});

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
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
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	tts.destroy();
    	th.interrupt();
    };
    
    Thread th = new Thread(new Runnable() {
		int i = 0;
		@Override
		public void run() {
			while(i < 20 && !Thread.interrupted())
			{
				tts.playTextAndWait(i+"公里");
				i++;
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	});
}
