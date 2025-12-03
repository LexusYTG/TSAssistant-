package com.TSgames.TSassistant;

import android.*;
import android.animation.*;
import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.content.res.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.graphics.drawable.shapes.*;
import android.net.*;
import android.os.*;
import android.provider.*;
import android.util.*;
import android.view.*;
import android.view.animation.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity implements TTSManager.TTSCallback {
		private TTSManager ttsManager;
		private CommandProcessor commandProcessor;
		private VoiceRecorder voiceRecorder;
		private TextView statusText;
		private TextView dialogText;
		private AudioVisualizerView visualizerView;
		private Handler mainHandler;
		private boolean isDestroyed = false;
		private boolean isListening = false;
		private boolean isSpeaking = false;
		private boolean isPermissionCheckComplete = false;

		// Código de solicitud de permisos
		private static final int PERMISSION_REQUEST_CODE = 1001;
		private static final int SETTINGS_PERMISSION_REQUEST_CODE = 1002;

		// Lista completa de permisos peligrosos que requieren solicitud en tiempo de ejecución
		private static final String[] DANGEROUS_PERMISSIONS = {
				Manifest.permission.RECORD_AUDIO,
				Manifest.permission.READ_CONTACTS,
				Manifest.permission.CALL_PHONE,
				Manifest.permission.CAMERA,
				Manifest.permission.ACCESS_FINE_LOCATION,
				Manifest.permission.ACCESS_COARSE_LOCATION,
				Manifest.permission.SEND_SMS,
				Manifest.permission.READ_SMS,
				Manifest.permission.READ_EXTERNAL_STORAGE,
				Manifest.permission.WRITE_EXTERNAL_STORAGE
			};

		// Permisos especiales que requieren configuración manual del usuario
		private static final String[] SPECIAL_PERMISSIONS = {
				Manifest.permission.WRITE_SETTINGS,
				Manifest.permission.WRITE_SECURE_SETTINGS,
				Manifest.permission.CHANGE_WIFI_STATE,
				Manifest.permission.CHANGE_CONFIGURATION
			};

		@Override
		protected void onCreate(Bundle savedInstanceState) {
				super.onCreate(savedInstanceState);
				requestWindowFeature(Window.FEATURE_NO_TITLE);

				// Configuración de ventana adaptable
				getWindow().setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
				// Eliminamos setLayout fijo para permitir que el contenido defina el tamaño

				mainHandler = new Handler();

				// Layout principal con scroll para pantallas pequeñas
				ScrollView scrollView = new ScrollView(this);
				scrollView.setFillViewport(true);

				// Contenedor relativo para superposición de elementos
				RelativeLayout container = new RelativeLayout(this);
				container.setGravity(Gravity.CENTER);

				Resources resources = getResources();
				final float density = resources.getDisplayMetrics().density;
				final int paddingHorizontal = (int) (24 * density);
				final int paddingVertical = (int) (16 * density);

				// Layout principal con fondo gradiente
				final LinearLayout root = new LinearLayout(this);
				root.setOrientation(LinearLayout.VERTICAL);
				root.setGravity(Gravity.CENTER);
				root.setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical);

				// Fondo gradiente con bordes redondeados y sombra
				GradientDrawable background = new GradientDrawable(
					GradientDrawable.Orientation.TOP_BOTTOM,
					new int[]{Color.parseColor("#8E44AD"), Color.parseColor("#3498DB")}
				);
				background.setCornerRadius(48 * density);

				GradientDrawable shadow = new GradientDrawable();
				shadow.setColor(Color.parseColor("#40000000"));
				shadow.setCornerRadius(48 * density);

				LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{shadow, background});
				layerDrawable.setLayerInset(1, (int) (12 * density), (int) (12 * density), (int) (12 * density), (int) (20 * density));
				root.setBackgroundDrawable(layerDrawable);

				// Icono de micrófono central
				ImageView micIcon = new ImageView(this);
				ShapeDrawable micBg = new ShapeDrawable(new OvalShape());
				micBg.getPaint().setColor(Color.parseColor("#ECF0F1"));
				micBg.getPaint().setShadowLayer(8 * density, 0, 4 * density, Color.parseColor("#80000000"));
				micIcon.setBackgroundDrawable(micBg);
				micIcon.setImageResource(android.R.drawable.ic_btn_speak_now);
				micIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
				LinearLayout.LayoutParams micParams = new LinearLayout.LayoutParams(
					(int) (80 * density),
					(int) (80 * density)
				);
				micParams.gravity = Gravity.CENTER;
				micParams.bottomMargin = (int) (8 * density);
				root.addView(micIcon, micParams);

				// Visualizador de audio
				visualizerView = new AudioVisualizerView(this);
				LinearLayout.LayoutParams vizParams = new LinearLayout.LayoutParams(
					(int) (220 * density),
					(int) (220 * density)
				);
				vizParams.gravity = Gravity.CENTER;
				vizParams.topMargin = (int) (-140 * density);
				root.addView(visualizerView, vizParams);

				// Texto de status
				statusText = new TextView(this);
				statusText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
				statusText.setTextColor(Color.WHITE);
				statusText.setGravity(Gravity.CENTER);
				statusText.setText("Verificando permisos...");
				LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.WRAP_CONTENT
				);
				statusParams.topMargin = (int) (16 * density);
				root.addView(statusText, statusParams);

				// Cuadro de diálogo
				dialogText = new TextView(this);
				dialogText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
				dialogText.setTextColor(Color.parseColor("#2C3E50"));
				dialogText.setGravity(Gravity.CENTER);
				dialogText.setPadding((int) (24 * density), (int) (12 * density), (int) (24 * density), (int) (12 * density));
				dialogText.setMinHeight((int) (48 * density));

				GradientDrawable dialogBg = new GradientDrawable();
				dialogBg.setColor(Color.WHITE);
				dialogBg.setCornerRadii(new float[]{24 * density, 24 * density, 24 * density, 24 * density, 0, 0, 24 * density, 24 * density});
				dialogBg.setStroke((int) (2 * density), Color.parseColor("#3498DB"));
				dialogText.setBackgroundDrawable(dialogBg);

				LinearLayout.LayoutParams dialogParams = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.WRAP_CONTENT
				);
				dialogParams.topMargin = (int) (12 * density);
				root.addView(dialogText, dialogParams);

				// Botón de cierre
				Button closeBtn = new Button(this);
				closeBtn.setText("✕");
				closeBtn.setTextColor(Color.WHITE);
				closeBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
				closeBtn.setBackgroundColor(Color.TRANSPARENT);

				RelativeLayout.LayoutParams rootParams = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.MATCH_PARENT,
					RelativeLayout.LayoutParams.WRAP_CONTENT
				);
				rootParams.addRule(RelativeLayout.CENTER_IN_PARENT);
				container.addView(root, rootParams);

				// Posicionar botón de cierre en la esquina superior derecha
				RelativeLayout.LayoutParams closeParams = new RelativeLayout.LayoutParams(
					(int) (48 * density),
					(int) (48 * density)
				);
				closeParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
				closeParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
				closeParams.setMargins(0, (int) (8 * density), (int) (8 * density), 0);
				container.addView(closeBtn, closeParams);

				scrollView.addView(container);
				setContentView(scrollView);

				// Animación de entrada
				root.setAlpha(0f);
				root.setScaleX(0.9f);
				root.setScaleY(0.9f);
				AnimatorSet entrySet = new AnimatorSet();
				entrySet.playTogether(
					ObjectAnimator.ofFloat(root, "alpha", 0f, 1f),
					ObjectAnimator.ofFloat(root, "scaleX", 0.9f, 1f),
					ObjectAnimator.ofFloat(root, "scaleY", 0.9f, 1f)
				);
				entrySet.setDuration(500);
				entrySet.setInterpolator(new BounceInterpolator());
				entrySet.start();

				// Listener del botón de cerrar
				closeBtn.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
									AnimatorSet set = new AnimatorSet();
									set.playTogether(
										ObjectAnimator.ofFloat(root, "alpha", 1f, 0f),
										ObjectAnimator.ofFloat(root, "scaleX", 1f, 0.8f),
										ObjectAnimator.ofFloat(root, "scaleY", 1f, 0.8f)
									);
									set.setDuration(300);
									set.setInterpolator(new AccelerateDecelerateInterpolator());
									set.addListener(new Animator.AnimatorListener() {
												@Override
												public void onAnimationEnd(Animator animation) {
														finish();
													}

												@Override
												public void onAnimationStart(Animator animation) {
													}

												@Override
												public void onAnimationCancel(Animator animation) {
													}

												@Override
												public void onAnimationRepeat(Animator animation) {
													}
											});
									set.start();
								}
						});

				// Iniciar verificación de permisos
				checkAllPermissions();
			}

		/**
		 * Verifica y solicita todos los permisos necesarios
		 */
		private void checkAllPermissions() {
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
						// En versiones anteriores a Android 6.0, los permisos se conceden al instalar
						initApp();
						return;
					}

				// Verificar permisos peligrosos
				final List<String> permissionsToRequest = new ArrayList<String>();
				for (String permission : DANGEROUS_PERMISSIONS) {
						if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
								permissionsToRequest.add(permission);
							}
					}

				// Verificar permisos especiales
				boolean needSpecialPermissions = false;
				for (String permission : SPECIAL_PERMISSIONS) {
						if (permission.equals(Manifest.permission.WRITE_SETTINGS)) {
								if (!Settings.System.canWrite(this)) {
										needSpecialPermissions = true;
										break;
									}
							} else if (permission.equals(Manifest.permission.WRITE_SECURE_SETTINGS)) {
								// Este permiso solo se puede conceder a apps de sistema
								statusText.setText("⚠️ Permiso de sistema no disponible");
							}
					}

				if (permissionsToRequest.isEmpty() && !needSpecialPermissions) {
						// Todos los permisos concedidos
						initApp();
						return;
					}

				// Solicitar permisos peligrosos
				if (!permissionsToRequest.isEmpty()) {
						statusText.setText("Solicitando permisos necesarios...");
						requestPermissions(permissionsToRequest.toArray(new String[0]), PERMISSION_REQUEST_CODE);
					}

				// Solicitar permisos especiales
				if (needSpecialPermissions) {
						showSpecialPermissionDialog();
					}
			}

		/**
		 * Muestra un diálogo para permisos especiales que requieren configuración manual
		 */
		private void showSpecialPermissionDialog() {
				new android.app.AlertDialog.Builder(this)
					.setTitle("Permisos especiales requeridos")
					.setMessage("La app necesita permisos adicionales para modificar configuraciones del sistema. Por favor, concédelos manualmente.")
					.setPositiveButton("Ir a Configuración", new android.content.DialogInterface.OnClickListener() {
							@Override
							public void onClick(android.content.DialogInterface dialog, int which) {
									Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
									intent.setData(Uri.parse("package:" + getPackageName()));
									startActivityForResult(intent, SETTINGS_PERMISSION_REQUEST_CODE);
								}
						})
					.setNegativeButton("Cancelar", new android.content.DialogInterface.OnClickListener() {
							@Override
							public void onClick(android.content.DialogInterface dialog, int which) {
									statusText.setText("⚠️ Algunos permisos especiales no fueron concedidos");
									// Continuamos con los permisos básicos
									initApp();
								}
						})
					.setCancelable(false)
					.show();
			}

		@Override
		public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
				super.onRequestPermissionsResult(requestCode, permissions, grantResults);

				if (requestCode == PERMISSION_REQUEST_CODE) {
						boolean allGranted = true;
						String deniedPermission = null;

						for (int i = 0; i < grantResults.length; i++) {
								if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
										allGranted = false;
										deniedPermission = permissions[i];
										break;
									}
							}

						if (allGranted) {
								statusText.setText("✅ Permisos concedidos. Iniciando...");
								initApp();
							} else {
								statusText.setText("❌ Permiso denegado: " + (deniedPermission != null ? deniedPermission : "Desconocido"));
								Toast.makeText(this, "Necesitas conceder todos los permisos para usar la app", Toast.LENGTH_LONG).show();

								// Redirigir a configuración después de 3 segundos
								mainHandler.postDelayed(new Runnable() {
											@Override
											public void run() {
													if (!isDestroyed) {
															Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
															intent.setData(Uri.parse("package:" + getPackageName()));
															startActivity(intent);
															finish();
														}
												}
										}, 3000);
							}
					}
			}

		@Override
		protected void onActivityResult(int requestCode, int resultCode, Intent data) {
				super.onActivityResult(requestCode, resultCode, data);
				if (requestCode == SETTINGS_PERMISSION_REQUEST_CODE) {
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
								if (Settings.System.canWrite(this)) {
										statusText.setText("✅ Permisos especiales concedidos");
									} else {
										statusText.setText("⚠️ Permisos especiales denegados");
									}
								initApp();
							}
					}
			}

		/**
		 * Inicializa la aplicación después de obtener permisos
		 */
		private void initApp() {
				// Evitar múltiples inicializaciones
				if (isPermissionCheckComplete) return;
				isPermissionCheckComplete = true;

				ttsManager = TTSManager.getInstance(this);
				ttsManager.setCallback(this);
				commandProcessor = new CommandProcessor(this, ttsManager);

				if (!VoiceRecorder.isRecognitionAvailable(this)) {
						statusText.setText("❌ Reconocimiento de voz no disponible");
						Toast.makeText(this, "Instala Google App o servicio de voz", Toast.LENGTH_LONG).show();
						autoFinish(3000);
						return;
					}

				statusText.setText("🎤 Escuchando...");
				startVoiceRecognition();
			}

		private void startVoiceRecognition() {
				if (isDestroyed || isSpeaking) return;

				voiceRecorder = new VoiceRecorder(this, new VoiceRecorder.RecognitionCallback() {
							@Override
							public void onResult(String text) {
									if (isDestroyed || isSpeaking) return;

									stopVoiceRecognition();
									visualizerView.stopAnimation();

									statusText.setText("✅ " + text);
									commandProcessor.processCommand(text);
								}

							@Override
							public void onError(String msg) {
									if (isDestroyed || isSpeaking) return;

									statusText.setText("❌ " + msg);

									mainHandler.postDelayed(new Runnable() {
												@Override
												public void run() {
														if (!isDestroyed && !isSpeaking) {
																statusText.setText("🎤 Escuchando...");
																startVoiceRecognition();
															}
													}
											}, 2000);
								}
						});

				if (voiceRecorder.isReady()) {
						voiceRecorder.startListening();
						isListening = true;
						visualizerView.startAnimation();
					} else {
						statusText.setText("❌ Error inicializando reconocimiento de voz");
						autoFinish(2000);
					}
			}

		private void stopVoiceRecognition() {
				if (voiceRecorder != null) {
						voiceRecorder.stopListening();
						isListening = false;
						visualizerView.stopAnimation();
					}
			}

		// Implementación del callback del TTS
		@Override
		public void onSpeechStart() {
				isSpeaking = true;
				statusText.setText("🗣️ Hablando...");
				stopVoiceRecognition();
				visualizerView.stopAnimation();
			}

		@Override
		public void onSpeechDone() {
				isSpeaking = false;

				mainHandler.postDelayed(new Runnable() {
							@Override
							public void run() {
									if (!isDestroyed && !isListening) {
											statusText.setText("🎤 Escuchando...");
											startVoiceRecognition();
										}
								}
						}, 1000);
			}

		// Método para actualizar el texto del diálogo
		public void updateDialogText(String text) {
				dialogText.setAlpha(0f);
				dialogText.setText(text);
				ObjectAnimator.ofFloat(dialogText, "alpha", 0f, 1f).setDuration(300).start();
			}

		private void autoFinish(long delay) {
				mainHandler.postDelayed(new Runnable() {
							@Override
							public void run() {
									if (!isDestroyed) finish();
								}
						}, delay);
			}

		@Override
		protected void onDestroy() {
				isDestroyed = true;
				if (voiceRecorder != null) voiceRecorder.destroy();
				if (ttsManager != null) ttsManager.shutdown();
				super.onDestroy();
			}

		// Clase personalizada para el visualizador de audio
		public static class AudioVisualizerView extends View {
				private Paint paint;
				private Paint wavePaint;
				private float centerX, centerY;
				private float baseRadius;
				private float currentRadius;
				private ValueAnimator animator;
				private float density;

				public AudioVisualizerView(Context context) {
						super(context);
						init(context);
					}

				public AudioVisualizerView(Context context, AttributeSet attrs) {
						super(context, attrs);
						init(context);
					}

				private void init(Context context) {
						density = context.getResources().getDisplayMetrics().density;
						baseRadius = 60 * density;
						currentRadius = baseRadius;

						paint = new Paint();
						paint.setColor(Color.parseColor("#FFFFFF"));
						paint.setStyle(Paint.Style.FILL);
						paint.setAntiAlias(true);
						paint.setShadowLayer(20 * density, 0, 0, Color.parseColor("#8E44AD"));

						wavePaint = new Paint(paint);
						wavePaint.setStyle(Paint.Style.STROKE);
						wavePaint.setStrokeWidth(6 * density);
						wavePaint.setAlpha(160);
					}

				@Override
				protected void onSizeChanged(int w, int h, int oldw, int oldh) {
						super.onSizeChanged(w, h, oldw, oldh);
						centerX = w / 2f;
						centerY = h / 2f;
					}

				@Override
				protected void onDraw(Canvas canvas) {
						super.onDraw(canvas);
						canvas.drawCircle(centerX, centerY, currentRadius, paint);

						// Dibujar ondas concéntricas
						wavePaint.setShader(new android.graphics.RadialGradient(
												centerX, centerY, currentRadius + 50 * density,
												Color.parseColor("#8E44AD"), Color.TRANSPARENT,
												android.graphics.Shader.TileMode.CLAMP
											));
						canvas.drawCircle(centerX, centerY, currentRadius + 20 * density, wavePaint);
						canvas.drawCircle(centerX, centerY, currentRadius + 40 * density, wavePaint);
						canvas.drawCircle(centerX, centerY, currentRadius + 60 * density, wavePaint);
					}

				public void startAnimation() {
						if (animator != null && animator.isRunning()) return;
						animator = ValueAnimator.ofFloat(baseRadius, baseRadius * 1.4f);
						animator.setDuration(600);
						animator.setRepeatCount(ValueAnimator.INFINITE);
						animator.setRepeatMode(ValueAnimator.REVERSE);
						animator.setInterpolator(new AccelerateDecelerateInterpolator());
						animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
									@Override
									public void onAnimationUpdate(ValueAnimator animation) {
											currentRadius = (Float) animation.getAnimatedValue();
											invalidate();
										}
								});
						animator.start();
					}

				public void stopAnimation() {
						if (animator != null) {
								animator.cancel();
								animator = null;
							}
						currentRadius = baseRadius;
						invalidate();
					}
			}
	}
