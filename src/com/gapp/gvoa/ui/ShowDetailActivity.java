package com.gapp.gvoa.ui;

import java.io.IOException;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gapp.gvoa.R;
import com.gapp.gvoa.datatype.RssItem;
import com.gapp.gvoa.db.DbRssItem;
import com.gapp.gvoa.parser.ItemHtmlParser;
import com.gapp.gvoa.util.GPreference;
import com.gapp.gvoa.util.GvoaUtil;
import com.gapp.gvoa.util.MsgCenter;
import com.gapp.gvoa.util.MsgCenter.GSubscriber;
import com.gapp.gvoa.util.NetworkUtil;

public class ShowDetailActivity extends Activity implements GSubscriber
{
	private static final String TAG = "ShowDetailActivity";
	public static final int MSG_SUCCESS = 0;
	public static final int MSG_FAILURE = 1;
	public static final int MSG_MP3_PROGRESS = 2;
	public static final int MSG_MP3 = 3;
	
	
	private static final int EXTRA_SHOW_NONE=0;
	private static final int EXTRA_SHOW_DOWNLOADING=1;
	private static final int EXTRA_SHOW_PLAYCONTROL=2;
    
	private RelativeLayout mPlayView=null; 
	private View mDownloadView=null;
	
    private ImageButton buttonPlayStop; 
    private SeekBar seekBar;
    private ProgressBar mProgressBar;
    
    private MediaPlayer mediaPlayer=null;
    
    
	private RssItem rssItem; 
	
	private Thread mThread;  
	
    public void onCreate(Bundle icicle) 
    {
        super.onCreate(icicle);
        
        
        MsgCenter.instance().register(this);        
        setContentView(R.layout.rss_detail);
        
        rssItem = getIntent().getParcelableExtra(RssItem.class.getName()); 
        Log.i(TAG, "load rssitem url="+rssItem.getLink());
        
        TextView title= (TextView) findViewById(R.id.title);
        title.setText(Html.fromHtml(rssItem.getTitle()));          
        
        if(rssItem.getStatus()<RssItem.E_PARSE_TXT_OK)
        {
        	if(mThread == null) {  
                mThread = new Thread(runnable);  
                mThread.start();
            }  
            else {  
                Toast.makeText(getApplication(), getApplication().getString(R.string.thread_started), Toast.LENGTH_LONG).show();  
            }  
      	
        } 
        else
        {
       	     TextView detail= (TextView) findViewById(R.id.detail);
       	     detail.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, GPreference.getPreferredTextSize());
             detail.setText(Html.fromHtml(rssItem.getFullText()));
             if(rssItem.getStatus()<RssItem.E_DOWN_MP3_OK||!GvoaUtil.isFileExists(rssItem.getLocalmp3()))
             {
            	 if(null==rssItem.getMp3url())
            	 {
            		 Toast.makeText(getApplication(), getApplication().getString(R.string.no_mp3_content), Toast.LENGTH_LONG).show(); 
            	 }else 
            	 {
            		 checkAndDownMp3();
            	 }
             }
        } 
        
        if(null==rssItem.getMp3url())
        {
        	showExtra(EXTRA_SHOW_NONE);
        }
        else if(null==rssItem.getLocalmp3()||!GvoaUtil.isFileExists(rssItem.getLocalmp3()))
        {
        	Log.i(TAG, "localmp3 is null");
        	showExtra(EXTRA_SHOW_DOWNLOADING);
        }
        else
        {
        	showExtra(EXTRA_SHOW_PLAYCONTROL);
            initMp3Player();
        }
    }
    
    
    void showExtra(int showType)
    {    	
    	LinearLayout detail_linear_layout_view = (LinearLayout) findViewById(R.id.detail_linear_layout); 
    	switch( showType)
    	{
    		case EXTRA_SHOW_NONE:
    			break;
    		case EXTRA_SHOW_DOWNLOADING:
        		if(null!=mPlayView)
        		{
        		    detail_linear_layout_view.removeView(mPlayView);
        		    mPlayView.setVisibility(View.GONE);
        		}
        		mDownloadView = (View) LayoutInflater.from(this).inflate(R.layout.download_mp3, null);     	
            	detail_linear_layout_view.addView(mDownloadView);
            	mProgressBar = (ProgressBar)  findViewById(R.id.downprogressloadbar); 
    			break;
    		case EXTRA_SHOW_PLAYCONTROL:           	
            	if(null!=mDownloadView)
            	{
        		    Log.i(TAG,"remove mDownloadView");
            		detail_linear_layout_view.removeView(mDownloadView);
        		    mDownloadView.setVisibility(View.GONE);
            	}
        		mPlayView =(RelativeLayout) LayoutInflater.from(this).inflate(R.layout.audio_play, null);      		       	
            	detail_linear_layout_view.addView(mPlayView);
            	buttonPlayStop = (ImageButton) findViewById(R.id.play_pause);
            	seekBar = (SeekBar) findViewById(R.id.SeekBar01);  
    			break;
    			
    	}
  	
    }
    
    private void initMp3Player() {  	
    	
    	try {

    		mediaPlayer = new MediaPlayer();
			mediaPlayer.setDataSource(rssItem.getLocalmp3());
			mediaPlayer.prepare();
        	buttonPlayStop.setOnClickListener(new OnClickListener() {@Override public void onClick(View v) {buttonClick();}});
        	seekBar.setMax(mediaPlayer.getDuration());
        	seekBar.setOnTouchListener(new OnTouchListener() {@Override public boolean onTouch(View v, MotionEvent event) {
             	        seekChange(v);
        	            return false; }
                	});
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();

		}
    }
    
    // This is event handler thumb moving event
    private void seekChange(View v){
    	if(mediaPlayer.isPlaying()){
	    	SeekBar sb = (SeekBar)v;
			mediaPlayer.seekTo(sb.getProgress());
		}
    }
 
    // This is event handler for buttonClick event
    private void buttonClick(){
    	if (mediaPlayer.isPlaying())
    	{
    		buttonPlayStop.setImageResource(R.drawable.ic_media_play);
    		mediaPlayer.pause();
    	}
    	else
    	{
    		buttonPlayStop.setImageResource(R.drawable.ic_media_pause);
    		 try{
             	 mediaPlayer.start();
                 startPlayProgressUpdater(); 
             }catch (IllegalStateException e) {
             	mediaPlayer.pause();
             }
    	}
    }
    
    
    public void startPlayProgressUpdater() {
    	seekBar.setProgress(mediaPlayer.getCurrentPosition());
    	
		if (mediaPlayer.isPlaying()) {
			Runnable notification = new Runnable() {
		        public void run() {
		        	startPlayProgressUpdater();
				}
		    };		    
		    mHandler.postDelayed(notification,1000);
    	}else{
    		mediaPlayer.pause();
    		buttonPlayStop.setImageResource(R.drawable.ic_media_play);
    	}
    } 
    
    public void onMessage(final Message msg){
    	Log.i(TAG, "Get registered message from MsgCenter");
	
    	this.runOnUiThread(new Runnable() {
    		  public void run() {
    			  mHandler.handleMessage(msg);
    		  }
    		});
    }
    
    
    private Handler mHandler = new Handler() {  
        public void handleMessage (Message msg) {
            switch(msg.what) {  
            case MSG_SUCCESS:  
                //reload date from db
            	Log.i(TAG, "Parse rssItem SUCCESS");
            	TextView detail= (TextView) findViewById(R.id.detail);
            	detail.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, GPreference.getPreferredTextSize());
                detail.setText(Html.fromHtml(rssItem.getFullText())); 
                DbRssItem.updateItem(rssItem);                 
                
                if(null!=rssItem.getMp3url())
                {
                	showExtra(EXTRA_SHOW_DOWNLOADING);
                }               
                
                checkAndDownMp3();  
            	
                break;    
            case MSG_FAILURE:  
                Toast.makeText(getApplication(), getApplication().getString(R.string.get_rss_failure), Toast.LENGTH_LONG).show(); 
               
                mThread = null;
                break;  
             
                
            case MSG_MP3_PROGRESS:
    			Integer  downloadedSize =msg.arg1;
    			Integer  totalSize =msg.arg2;
    			RssItem item = (RssItem) msg.obj;
    		    if(item == rssItem  && null!=mProgressBar)
    		    {
    		    	mProgressBar.setProgress(downloadedSize*100/totalSize);
    		    }           	
            	
            	break;
            case MSG_MP3:

            	RssItem downItem = (RssItem) msg.obj;
            	if (rssItem!=downItem)
            	{
            		Log.i(TAG, "downloaded item is not the same as current one");
            		break;
            	}
            	
                if(rssItem.getStatus()==RssItem.E_DOWN_MP3_OK)
                {
                	Toast.makeText(getApplication(), getApplication().getString(R.string.get_mp3_success), Toast.LENGTH_LONG).show();
                   	showExtra(EXTRA_SHOW_PLAYCONTROL);               	
                	initMp3Player();
                }
                else
                {
                	Toast.makeText(getApplication(), getApplication().getString(R.string.get_mp3_failure), Toast.LENGTH_LONG).show(); 
                }           
                mThread = null;
                break;  
            } 
        }  
    };    
    
    

    private void checkAndDownMp3()
    {
    	int netStatus = GPreference.getNetWork();
    	
    	if(0xFFFF==netStatus)
    	{
    		Toast.makeText(getApplication(), getApplication().getString(R.string.no_network_connected), Toast.LENGTH_LONG).show(); 
    		return; 
    	}
    	
    	//WIFI_ONLY,  WIFI_AND_3G, NONE  	
    	String mp3Pref = GPreference.downloadMp3Pref();    	
    	
    	
    	if(netStatus == ConnectivityManager.TYPE_WIFI)
    	{
    		if(mp3Pref.equals("WIFI_ONLY")||mp3Pref.equals("WIFI_AND_3G"))
    		{
                mThread = new Thread(runnableMp3);  
                mThread.start();
    		}
    		else
    		{
    			Toast.makeText(getApplication(), getApplication().getString(R.string.audio_is_not_downloaded), Toast.LENGTH_LONG).show();
    		}    		
    	}
    	else
    	{
    		if(mp3Pref.equals("WIFI_AND_3G"))
    		{
                mThread = new Thread(runnableMp3);  
                mThread.start();
    		}
    		else
    		{
    			Toast.makeText(getApplication(), getApplication().getString(R.string.audio_is_not_downloaded), Toast.LENGTH_LONG).show();
    		} 
    	}
    }
	
    
     Runnable runnable = new Runnable() {  
        
        @Override  
        public void run() {  
            try {
            	ItemHtmlParser.parseItemDetail(rssItem);
            } catch (Exception e) {  
            	 Log.e(TAG, "Connect or parse Error", e);
            	 rssItem.setStatus(RssItem.E_PARSE_TXT_FAIL);
                mHandler.obtainMessage(MSG_FAILURE).sendToTarget();
                return;  
            }  
            mHandler.obtainMessage(MSG_SUCCESS).sendToTarget();
        }  
    }; 
    
     Runnable runnableMp3 = new Runnable() {  
          
        @Override  
        public void run() {   
        	Log.i(TAG, "Start download mp3");
            NetworkUtil.downloadMp3 (rssItem, mHandler);
            
        }  
    }; 
    
    
    
    
    
    @Override  
    protected void onStart() {  
        super.onStart();  
        Log.i(TAG, "onStart");  
    } 
    
    @Override  
    protected void onResume() {  
        super.onResume();  
        
        Log.i(TAG, "onResume");  
    }  
      
    @Override  
    protected void onPause() {  	
    	
        super.onPause();  
        Log.i(TAG, "onPause");  
    }  
 
    @Override  
    protected void onStop() {      	
        super.onStop();  
        Log.i(TAG, "onStop");  
    }  
    @Override  
    protected void onDestroy() {  
    	if(mediaPlayer!=null && mediaPlayer.isPlaying())
    	{
    		mediaPlayer.stop();
    	} 
    	
    	MsgCenter.instance().unRegister(this); 
        super.onDestroy();  
        Log.i(TAG, "onDestroy");  
    }  
}
