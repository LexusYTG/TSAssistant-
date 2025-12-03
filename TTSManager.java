package com.TSgames.TSassistant;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.Locale;

public class TTSManager implements TextToSpeech.OnInitListener {
    private static final String TAG = "TTSManager";
    private static TTSManager instance;
    private TextToSpeech tts;
    private boolean isLoaded = false;
    private Context context;
    private TTSCallback callback;

    public interface TTSCallback {
        void onSpeechStart();
        void onSpeechDone();
    }

    private TTSManager(Context context) {
        this.context = context;
        tts = new TextToSpeech(context, this);
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
				@Override
				public void onStart(String utteranceId) {
					Log.d(TAG, "TTS started: " + utteranceId);
					if (callback != null) {
						callback.onSpeechStart();
					}
				}

				@Override
				public void onDone(String utteranceId) {
					Log.d(TAG, "TTS done: " + utteranceId);
					if (callback != null) {
						callback.onSpeechDone();
					}
				}

				@Override
				public void onError(String utteranceId) {
					Log.e(TAG, "TTS error on utterance: " + utteranceId);
					if (callback != null) {
						callback.onSpeechDone(); // Aun así, notificamos para continuar
					}
				}
			});
    }

    public static synchronized TTSManager getInstance(Context context) {
        if (instance == null) {
            instance = new TTSManager(context);
        }
        return instance;
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            isLoaded = true;
            // Configurar español como idioma predeterminado
            Locale spanish = new Locale("es", "ES");
            int result = tts.setLanguage(spanish);

            if (result == TextToSpeech.LANG_MISSING_DATA || 
                result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "Idioma español no soportado");
                // Intentar con español neutro
                tts.setLanguage(Locale.getDefault());
            }

            tts.setSpeechRate(1.0f);
            tts.setPitch(1.0f);
        } else {
            Log.e(TAG, "TTS init failed with status: " + status);
        }
    }

    public void speak(String text) {
        if (isLoaded && text != null && !text.trim().isEmpty()) {
            // Generar un utterance ID único para poder rastrear
            String utteranceId = String.valueOf(System.currentTimeMillis());
            tts.speak(text, TextToSpeech.QUEUE_ADD, null, utteranceId);
        }
    }

    public void setCallback(TTSCallback callback) {
        this.callback = callback;
    }

    public void shutdown() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            instance = null;
        }
    }
}

