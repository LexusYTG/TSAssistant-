package com.TSgames.TSassistant;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.ArrayList;

public class VoiceRecorder {
    private static final String TAG = "VoiceRecorder";
    private static final int TIMEOUT_MS = 8000;
    private SpeechRecognizer speechRecognizer;
    private Intent recognizerIntent;
    private RecognitionCallback callback;
    private boolean isListening = false;
    private boolean isReady = false;
    private Handler timeoutHandler;
    private Runnable timeoutRunnable;
    private Context context;

    public interface RecognitionCallback {
        void onResult(String text);
        void onError(String message);
    }

    public static boolean isRecognitionAvailable(Context context) {
        return SpeechRecognizer.isRecognitionAvailable(context);
    }

    public VoiceRecorder(Context context, final RecognitionCallback callback) {
        this.context = context;
        this.callback = callback;

        if (!isRecognitionAvailable(context)) {
            Log.e(TAG, "Speech recognition not available");
            return;
        }

        // Siempre usar español
        String language = "es-ES";

        timeoutHandler = new Handler();
        timeoutRunnable = new Runnable() {
            @Override public void run() {
                if (isListening) {
                    stopListening();
                    callback.onError("Tiempo de espera agotado");
                }
            }
        };

        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
            if (speechRecognizer == null) {
                Log.e(TAG, "SpeechRecognizer creation failed");
                return;
            }

            recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language);
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.getPackageName());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
            }

            setupListener();
            isReady = true;
        } catch (Exception e) {
            Log.e(TAG, "VoiceRecorder init error", e);
        }
    }

    private void setupListener() {
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
				@Override public void onReadyForSpeech(Bundle params) {
					isListening = true;
					startTimeout();
				}

				@Override public void onBeginningOfSpeech() {
					resetTimeout();
				}

				@Override public void onRmsChanged(float rmsdB) {}
				@Override public void onBufferReceived(byte[] buffer) {}
				@Override public void onEndOfSpeech() {
					stopTimeout();
					// No marcamos isListening = false aquí porque el reconocimiento sigue procesando
				}

				@Override public void onError(int error) {
					stopTimeout();
					isListening = false;

					// IGNORAR ERRORES DE RED - NO MOSTRAR NADA
					if (error == SpeechRecognizer.ERROR_NETWORK || 
						error == SpeechRecognizer.ERROR_NETWORK_TIMEOUT ||
						error == SpeechRecognizer.ERROR_SERVER) {
						// Solo loguear y no hacer nada - la app seguirá funcionando
						Log.d(TAG, "Error de red ignorado (modo offline activo)");
						return; // ← ESTA ES LA LÍNEA CLAVE: NO LLAMAR AL CALLBACK
					}

					// Solo reportar errores NO relacionados con red
					callback.onError(getErrorText(error));
				}

				@Override public void onResults(Bundle results) {
					stopTimeout();
					isListening = false;
					ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
					if (matches != null && !matches.isEmpty()) {
						callback.onResult(matches.get(0));
					}
				}

				@Override public void onPartialResults(Bundle partialResults) {}
				@Override public void onEvent(int eventType, Bundle params) {}
			});
    }

    private void startTimeout() {
        if (timeoutHandler != null) {
            timeoutHandler.postDelayed(timeoutRunnable, TIMEOUT_MS);
        }
    }

    private void resetTimeout() {
        stopTimeout();
        startTimeout();
    }

    private void stopTimeout() {
        if (timeoutHandler != null && timeoutRunnable != null) {
            timeoutHandler.removeCallbacks(timeoutRunnable);
        }
    }

    public void startListening() {
        if (!isReady || speechRecognizer == null) {
            if (callback != null) {
                callback.onError("No está listo");
            }
            return;
        }

        if (isListening) {
            Log.w(TAG, "Ya está escuchando");
            return;
        }

        try {
            speechRecognizer.startListening(recognizerIntent);
            isListening = true;
        } catch (Exception e) {
            Log.e(TAG, "startListening error", e);
            if (callback != null) {
                callback.onError("Error al iniciar");
            }
        }
    }

    // NUEVO MÉTODO: Detener la escucha
    public void stopListening() {
        if (speechRecognizer != null && isListening) {
            try {
                speechRecognizer.stopListening();
                isListening = false;
                stopTimeout();
                Log.d(TAG, "Escucha detenida");
            } catch (Exception e) {
                Log.e(TAG, "stopListening error", e);
            }
        }
    }

    public void destroy() {
        stopTimeout();
        if (speechRecognizer != null) {
            try {
                speechRecognizer.destroy();
            } catch (Exception e) {
                Log.e(TAG, "destroy error", e);
            }
            speechRecognizer = null;
        }
        isReady = false;
    }

    public boolean isReady() {
        return isReady;
    }

    public boolean isListening() {
        return isListening;
    }

    private String getErrorText(int error) {
        switch (error) {
            case SpeechRecognizer.ERROR_AUDIO: return "Error de audio";
            case SpeechRecognizer.ERROR_CLIENT: return "Error cliente";
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS: return "Faltan permisos";
            case SpeechRecognizer.ERROR_NETWORK: return "Error de red";
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT: return "Tiempo de red agotado";
            case SpeechRecognizer.ERROR_NO_MATCH: return "No reconocido";
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY: return "Reconocedor ocupado";
            case SpeechRecognizer.ERROR_SERVER: return "Error del servidor";
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT: return "No se detectó voz";
            default: return "Error " + error;
        }
    }
}

