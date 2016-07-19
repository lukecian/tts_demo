package com.example.tts_test;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;

import java.util.ArrayList;
import java.util.List;

/**
 * 语音播报组件
 */
public class TtsManager implements SynthesizerListener,InitListener{
	public interface TtsListener
	{
		void onInit(boolean success);
		void onSpeakStart(String text);
		void onSpeakFinish(String text);
		void onSpeakError(String error);
	}
	
    public static TtsManager ttsManager;
    boolean canSpeak = true; // 当前是否可以播放语音
    private Context mContext;
    // 合成对象.
    private SpeechSynthesizer mSpeechSynthesizer;
    List<String> pendingWords = new ArrayList<String>();
    List<Runnable> postSpeakingActionList = new ArrayList<Runnable>();

    TtsManager(Context context) {
        mContext = context;
    }

    public static TtsManager getInstance(Context context) {
        if (ttsManager == null) {
            ttsManager = new TtsManager(context.getApplicationContext());
        }
        return ttsManager;
    }

    public void init() {
		try {
			String speechAppId = mContext.getPackageManager()
					.getApplicationInfo(mContext.getPackageName(), PackageManager.GET_META_DATA)
					.metaData.getString("speech_appid");
			SpeechUtility.createUtility(mContext, "appid="+speechAppId);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			SpeechUtility.createUtility(mContext, "appid="+"570b4e3f");
		}
		mSpeechSynthesizer = SpeechSynthesizer.createSynthesizer(mContext, this);
        initSpeechSynthesizer();
    }

    public void playText(String what,Runnable action)
    {
        postSpeakingActionList.add(action);
        playText(what);
    }

    /**
     * 抢占式语音播报，会把前面未说完的强行停止
     * @param playText
     */
    public void playText(String playText) {
        if (null == mSpeechSynthesizer) {
            // 创建合成对象.
            mSpeechSynthesizer = SpeechSynthesizer.createSynthesizer(mContext,this);
            initSpeechSynthesizer();
        }
        // 进行语音合成.
        if (mSpeechSynthesizer != null)
        {
            pendingWords.clear();
        	mSpeechSynthesizer.stopSpeaking();
        	mSpeechSynthesizer.startSpeaking(playText, this);
        }
    }

    /**
     * 非抢占式播报，会等待当前的语音播报完毕再继续播报
     * @param playText
     */
    public void playTextAndWait(String playText) {
        if (!canSpeak) {
            pendingWords.add(playText);
            return;
        }
        if (null == mSpeechSynthesizer) {
            mSpeechSynthesizer = SpeechSynthesizer.createSynthesizer(mContext,null);
            initSpeechSynthesizer();
        }
        // 进行语音合成.
        mSpeechSynthesizer.startSpeaking(playText, this);
    }
    
    public void stopSpeaking() {
        if (mSpeechSynthesizer != null)
            mSpeechSynthesizer.stopSpeaking();
    }

    public void startSpeaking() {
        canSpeak = true;
    }

    private void initSpeechSynthesizer() {
		// 清空参数
		mSpeechSynthesizer.setParameter(SpeechConstant.PARAMS, null);
		// 根据合成引擎设置相应参数
		mSpeechSynthesizer.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
		// 设置在线合成发音人
		mSpeechSynthesizer.setParameter(SpeechConstant.VOICE_NAME, "xiaoyu");//vixr xiaoyan xiaoyu
		// 设置合成语速
		mSpeechSynthesizer.setParameter(SpeechConstant.SPEED, "50");
		// 设置合成音调
		mSpeechSynthesizer.setParameter(SpeechConstant.PITCH, "50");
		// 设置合成音量
		mSpeechSynthesizer.setParameter(SpeechConstant.VOLUME, "50");
		// 设置播放器音频流类型
		mSpeechSynthesizer.setParameter(SpeechConstant.STREAM_TYPE, "3");
		// 设置播放合成音频打断音乐播放，默认为true
		mSpeechSynthesizer.setParameter(SpeechConstant.KEY_REQUEST_FOCUS,"true");
    }

    @Override
    public void onBufferProgress(int arg0, int arg1, int arg2, String arg3) {
    }


    @Override
    public void onSpeakBegin() {
        canSpeak = false;
    }

    @Override
    public void onSpeakPaused() {
    }

    @Override
    public void onSpeakProgress(int arg0, int arg1, int arg2) {
    }

    @Override
    public void onSpeakResumed() {
    }

    public void destroy() {
        if (mSpeechSynthesizer != null) {
            mSpeechSynthesizer.stopSpeaking();
        }
    }

	@Override
	public void onCompleted(SpeechError arg0) {
		canSpeak = true;
        if(pendingWords.size()>0)
        {
            String words = pendingWords.get(0);
            mSpeechSynthesizer.startSpeaking(words, this);
            pendingWords.remove(0);
        }
        if(postSpeakingActionList.size()>0)
        {
            Runnable action = postSpeakingActionList.get(0);
            action.run();
            postSpeakingActionList.remove(0);
        }
	}

	@Override
	public void onInit(int arg0) {
		if(arg0 == ErrorCode.SUCCESS)
		{
			Log.e("TTS", "TTS初始化成功~");
		}else
		{
			Log.e("TTS", "TTS初始化失败!");
		}
	}

	@Override
	public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {
	}
}
