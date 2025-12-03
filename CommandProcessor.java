package com.TSgames.TSassistant;



import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.database.*;
import android.icu.text.*;
import android.icu.util.*;
import android.media.*;
import android.net.*;
import android.os.*;
import android.provider.*;
import android.support.v4.content.*;
import android.util.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;
import org.json.*;

import android.icu.util.Calendar;

public class CommandProcessor
 {
		private static final String TAG = "CommandProcessor";
		private Context context;
		private TTSManager tts;
		private Map<String, AppInfo> installedApps;
		private List<String> installedAppNames;
		private List<ContactInfo> contactsList;
		private List<SongInfo> songsList;
		private String currentState = "";
		private Random random = new Random();
		private SharedPreferences prefs;
		private static final String PREFS_NAME = "app_prefs";
		private static final String APPS_CACHED_KEY = "apps_cached";
		private static final String CONTACTS_LOADED_KEY = "contacts_loaded";
		private static final String SONGS_LOADED_KEY = "songs_loaded";

		private static final Map<String, String> COMMAND_MAP = new HashMap<String, String>();

		static {
				// Comandos en español solamente
				COMMAND_MAP.put("abre", "abrir");
				COMMAND_MAP.put("abrir", "abrir");
				COMMAND_MAP.put("abreme", "abrir");
				COMMAND_MAP.put("inicia", "abrir");
				COMMAND_MAP.put("ejecuta", "abrir");
				COMMAND_MAP.put("lanza", "abrir");
				COMMAND_MAP.put("corre", "abrir");
				COMMAND_MAP.put("lanzar", "abrir");
				COMMAND_MAP.put("iniciar", "abrir");

				COMMAND_MAP.put("llama", "llamar");
				COMMAND_MAP.put("llamar", "llamar");
				COMMAND_MAP.put("telefonear", "llamar");
				COMMAND_MAP.put("contactar", "llamar");
				COMMAND_MAP.put("marca", "llamar");
				COMMAND_MAP.put("marcar", "llamar");
				COMMAND_MAP.put("llámame", "llamar");

				COMMAND_MAP.put("busca", "buscar");
				COMMAND_MAP.put("buscar", "buscar");
				COMMAND_MAP.put("busquemos", "buscar");
				COMMAND_MAP.put("encuentra", "buscar");
				COMMAND_MAP.put("google", "buscar");
				COMMAND_MAP.put("investiga", "buscar");
				COMMAND_MAP.put("investigar", "buscar");
				COMMAND_MAP.put("búsqueda", "buscar");

				COMMAND_MAP.put("alarma", "alarma");
				COMMAND_MAP.put("despertador", "alarma");
				COMMAND_MAP.put("alarmas", "alarma");
				COMMAND_MAP.put("despierta", "alarma");
				COMMAND_MAP.put("despiértame", "alarma");
				COMMAND_MAP.put("pon alarma", "alarma");

				COMMAND_MAP.put("volumen", "volumen");
				COMMAND_MAP.put("sonido", "volumen");
				COMMAND_MAP.put("audio", "volumen");
				COMMAND_MAP.put("ruido", "volumen");
				COMMAND_MAP.put("sube volumen", "volumen");
				COMMAND_MAP.put("baja volumen", "volumen");

				COMMAND_MAP.put("brillo", "brillo");
				COMMAND_MAP.put("luz", "brillo");
				COMMAND_MAP.put("iluminacion", "brillo");
				COMMAND_MAP.put("pantalla", "brillo");
				COMMAND_MAP.put("aumenta brillo", "brillo");
				COMMAND_MAP.put("disminuye brillo", "brillo");

				COMMAND_MAP.put("apaga", "apagar");
				COMMAND_MAP.put("apagar", "apagar");
				COMMAND_MAP.put("cierra", "apagar");
				COMMAND_MAP.put("cerrar", "apagar");
				COMMAND_MAP.put("termina", "apagar");
				COMMAND_MAP.put("finaliza", "apagar");
				COMMAND_MAP.put("salir", "apagar");
				COMMAND_MAP.put("adiós", "apagar");

				COMMAND_MAP.put("hora", "hora");
				COMMAND_MAP.put("tiempo", "hora");
				COMMAND_MAP.put("reloj", "hora");
				COMMAND_MAP.put("fecha", "hora");
				COMMAND_MAP.put("día", "hora");
				COMMAND_MAP.put("qué hora es", "hora");
				COMMAND_MAP.put("qué día es", "hora");

				COMMAND_MAP.put("calcula", "calcular");
				COMMAND_MAP.put("calcular", "calcular");
				COMMAND_MAP.put("matematica", "calcular");
				COMMAND_MAP.put("cuanto es", "calcular");
				COMMAND_MAP.put("suma", "calcular");
				COMMAND_MAP.put("multiplica", "calcular");
				COMMAND_MAP.put("resta", "calcular");
				COMMAND_MAP.put("divide", "calcular");
				COMMAND_MAP.put("raíz", "calcular");
				COMMAND_MAP.put("potencia", "calcular");

				COMMAND_MAP.put("mensaje", "mensaje");
				COMMAND_MAP.put("mensajear", "mensaje");
				COMMAND_MAP.put("texto", "mensaje");
				COMMAND_MAP.put("sms", "mensaje");
				COMMAND_MAP.put("whatsapp", "mensaje");
				COMMAND_MAP.put("envia", "mensaje");
				COMMAND_MAP.put("enviar", "mensaje");
				COMMAND_MAP.put("manda", "mensaje");

				COMMAND_MAP.put("navega", "navegar");
				COMMAND_MAP.put("navegar", "navegar");
				COMMAND_MAP.put("mapa", "navegar");
				COMMAND_MAP.put("direcciones", "navegar");
				COMMAND_MAP.put("ruta", "navegar");
				COMMAND_MAP.put("gps", "navegar");

				COMMAND_MAP.put("reproduce", "musica");
				COMMAND_MAP.put("reproducir", "musica");
				COMMAND_MAP.put("música", "musica");
				COMMAND_MAP.put("pon música", "musica");
				COMMAND_MAP.put("pausa", "musica");
				COMMAND_MAP.put("detén", "musica");

				COMMAND_MAP.put("temporizador", "temporizador");
				COMMAND_MAP.put("timer", "temporizador");
				COMMAND_MAP.put("cuenta atrás", "temporizador");
				COMMAND_MAP.put("pon temporizador", "temporizador");

				COMMAND_MAP.put("recordatorio", "recordatorio");
				COMMAND_MAP.put("recuérdame", "recordatorio");
				COMMAND_MAP.put("nota", "recordatorio");
				COMMAND_MAP.put("recordar", "recordatorio");

				COMMAND_MAP.put("clima", "clima");
				COMMAND_MAP.put("tiempo", "clima");
				COMMAND_MAP.put("pronóstico", "clima");
				COMMAND_MAP.put("qué tiempo hace", "clima");

				COMMAND_MAP.put("noticias", "noticias");
				COMMAND_MAP.put("novedades", "noticias");
				COMMAND_MAP.put("actualidad", "noticias");

				COMMAND_MAP.put("traduce", "traducir");
				COMMAND_MAP.put("traducir", "traducir");

				COMMAND_MAP.put("wifi", "wifi");
				COMMAND_MAP.put("wi-fi", "wifi");
				COMMAND_MAP.put("conexión", "wifi");
				COMMAND_MAP.put("activa wifi", "wifi");
				COMMAND_MAP.put("desactiva wifi", "wifi");

				COMMAND_MAP.put("bluetooth", "bluetooth");
				COMMAND_MAP.put("activa bluetooth", "bluetooth");
				COMMAND_MAP.put("desactiva bluetooth", "bluetooth");

				COMMAND_MAP.put("modo avión", "modo_avion");
				COMMAND_MAP.put("avión", "modo_avion");
				COMMAND_MAP.put("activa modo avión", "modo_avion");

				COMMAND_MAP.put("foto", "camara");
				COMMAND_MAP.put("toma foto", "camara");
				COMMAND_MAP.put("cámara", "camara");
				COMMAND_MAP.put("abre cámara", "camara");

				COMMAND_MAP.put("email", "email");
				COMMAND_MAP.put("correo", "email");
				COMMAND_MAP.put("envía email", "email");

				COMMAND_MAP.put("batería", "bateria");
				COMMAND_MAP.put("carga", "bateria");
				COMMAND_MAP.put("nivel batería", "bateria");

				COMMAND_MAP.put("chiste", "chiste");
				COMMAND_MAP.put("cuenta un chiste", "chiste");
				COMMAND_MAP.put("contame un chiste", "chiste");

				COMMAND_MAP.put("pon dinamita", "dinamita");

				COMMAND_MAP.put("hola", "saludo");
				COMMAND_MAP.put("buenos dias", "saludo");
				COMMAND_MAP.put("buenas tardes", "saludo");
				COMMAND_MAP.put("buenas noches", "saludo");
				COMMAND_MAP.put("buen dia", "saludo");
				COMMAND_MAP.put("buena tarde", "saludo");
				COMMAND_MAP.put("buena noche", "saludo");
				COMMAND_MAP.put("hey", "saludo");
				COMMAND_MAP.put("saludos", "saludo");

				COMMAND_MAP.put("guia", "guia");
				COMMAND_MAP.put("ayuda", "guia");
				COMMAND_MAP.put("dame una guia de uso", "guia");
				COMMAND_MAP.put("comandos", "guia");

				COMMAND_MAP.put("escondete", "esconder");
				COMMAND_MAP.put("esconderé", "esconder");
				COMMAND_MAP.put("rapido", "esconder");

				COMMAND_MAP.put("quien eres", "info_dev");
				COMMAND_MAP.put("desarrollador", "info_dev");
				COMMAND_MAP.put("quien te hizo", "info_dev");

				COMMAND_MAP.put("que es", "que_es");
				COMMAND_MAP.put("qué es", "que_es");
				COMMAND_MAP.put("gracias", "gracias");
				COMMAND_MAP.put("de nada", "de_nada");

				COMMAND_MAP.put("lista", "lista");
				COMMAND_MAP.put("listar", "lista");
				COMMAND_MAP.put("listame", "lista");
				COMMAND_MAP.put("muestrame", "lista");
			}

		public CommandProcessor(Context context, TTSManager tts) {
				this.context = context;
				this.tts = tts;
				this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
				this.contactsList = new ArrayList<ContactInfo>();
				this.songsList = new ArrayList<SongInfo>();
				loadInstalledApps();
				loadContacts();
				loadSongs();
				
			}
		

		private void loadInstalledApps() {
				installedApps = new HashMap<String, AppInfo>();
				installedAppNames = new ArrayList<String>();
				PackageManager pm = context.getPackageManager();

				Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
				mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
				List<ResolveInfo> apps = pm.queryIntentActivities(mainIntent, 0);

				for (ResolveInfo ri : apps) {
						String appName = ri.loadLabel(pm).toString().toLowerCase();
						String packageName = ri.activityInfo.packageName;
						AppInfo appInfo = new AppInfo(appName, packageName, "Aplicación instalada en el dispositivo.");
						installedApps.put(appName, appInfo);
						installedAppNames.add(appName);

						saveAppToCache(appName, packageName);
					}

				loadCachedApps();
				loadAppDescriptionsFromJson();
			}

		private void saveAppToCache(String appName, String packageName) {
				String cachedApps = prefs.getString(APPS_CACHED_KEY, "");
				String entry = appName + ":" + packageName + ";";
				if (!cachedApps.contains(entry)) {
						cachedApps += entry;
						prefs.edit().putString(APPS_CACHED_KEY, cachedApps).apply();
					}
			}

		private void loadCachedApps() {
				String cachedApps = prefs.getString(APPS_CACHED_KEY, "");
				if (!cachedApps.isEmpty()) {
						String[] entries = cachedApps.split(";");
						for (String entry : entries) {
								if (entry.contains(":")) {
										String[] parts = entry.split(":");
										if (parts.length == 2) {
												String appName = parts[0].toLowerCase();
												String packageName = parts[1];
												if (!installedApps.containsKey(appName)) {
														AppInfo appInfo = new AppInfo(appName, packageName, "Aplicación previamente instalada.");
														installedApps.put(appName, appInfo);
														installedAppNames.add(appName);
													}
											}
									}
							}
					}
			}

		private void loadAppDescriptionsFromJson() {
				try {
						InputStream is = context.getAssets().open("apps.json");
						byte[] buffer = new byte[is.available()];
						is.read(buffer);
						is.close();
						String json = new String(buffer, "UTF-8");
						JSONObject obj = new JSONObject(json);
						JSONArray appsArray = obj.getJSONArray("apps");
						for (int i = 0; i < appsArray.length(); i++) {
								JSONObject appJson = appsArray.getJSONObject(i);
								String name = appJson.getString("name").toLowerCase();
								String description = appJson.optString("description", "Sin descripción disponible.");
								if (installedApps.containsKey(name)) {
										installedApps.get(name).description = description;
									}
							}
					} catch (Exception e) {
						Log.e(TAG, "Error loading apps.json", e);
					}
			}

		private void loadContacts() {
				boolean contactsLoaded = prefs.getBoolean(CONTACTS_LOADED_KEY, false);
				File contactsFile = new File(context.getFilesDir(), "contacts.json");

				if (contactsLoaded && contactsFile.exists()) {
						loadContactsFromJson();
					} else {
						readContactsFromSystem();
						saveContactsToJson();
						prefs.edit().putBoolean(CONTACTS_LOADED_KEY, true).apply();
					}
			}

		private void readContactsFromSystem() {
				contactsList.clear();
				Cursor cursor = null;
				try {
						cursor = context.getContentResolver().query(
							ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
							new String[]{
									ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
									ContactsContract.CommonDataKinds.Phone.NUMBER
								},
							null,
							null,
							ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
						);

						if (cursor != null) {
								while (cursor.moveToNext()) {
										String name = cursor.getString(cursor.getColumnIndex(
																		   ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
										String number = cursor.getString(cursor.getColumnIndex(
																			 ContactsContract.CommonDataKinds.Phone.NUMBER));

										if (name != null && number != null) {
												number = number.replaceAll("[^0-9+]", "");
												contactsList.add(new ContactInfo(name, number));
											}
									}
							}
					} catch (Exception e) {
						Log.e(TAG, "Error reading contacts", e);
					} finally {
						if (cursor != null) {
								cursor.close();
							}
					}
			}

		private void saveContactsToJson() {
				try {
						JSONArray jsonArray = new JSONArray();
						for (ContactInfo contact : contactsList) {
								JSONObject obj = new JSONObject();
								obj.put("name", contact.name);
								obj.put("number", contact.number);
								jsonArray.put(obj);
							}

						FileOutputStream fos = context.openFileOutput("contacts.json", Context.MODE_PRIVATE);
						fos.write(jsonArray.toString().getBytes());
						fos.close();
					} catch (Exception e) {
						Log.e(TAG, "Error saving contacts to JSON", e);
					}
			}

		private void loadContactsFromJson() {
				contactsList.clear();
				try {
						FileInputStream fis = context.openFileInput("contacts.json");
						BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
						StringBuilder stringBuilder = new StringBuilder();
						String line;
						while ((line = reader.readLine()) != null) {
								stringBuilder.append(line);
							}
						fis.close();

						JSONArray jsonArray = new JSONArray(stringBuilder.toString());
						for (int i = 0; i < jsonArray.length(); i++) {
								JSONObject obj = jsonArray.getJSONObject(i);
								String name = obj.getString("name");
								String number = obj.getString("number");
								contactsList.add(new ContactInfo(name, number));
							}
					} catch (Exception e) {
						Log.e(TAG, "Error loading contacts from JSON", e);
						readContactsFromSystem();
						saveContactsToJson();
					}
			}

		private void loadSongs() {
				boolean songsLoaded = prefs.getBoolean(SONGS_LOADED_KEY, false);
				File songsFile = new File(context.getFilesDir(), "songs.json");

				if (songsLoaded && songsFile.exists()) {
						loadSongsFromJson();
					} else {
						scanMusicFiles();
						saveSongsToJson();
						prefs.edit().putBoolean(SONGS_LOADED_KEY, true).apply();
					}
			}

		private void scanMusicFiles() {
				songsList.clear();
				Cursor cursor = null;
				try {
						cursor = context.getContentResolver().query(
							MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
							new String[] {
									MediaStore.Audio.Media.TITLE,
									MediaStore.Audio.Media.DATA
								},
							MediaStore.Audio.Media.IS_MUSIC + " != 0",
							null,
							MediaStore.Audio.Media.TITLE + " ASC"
						);

						if (cursor != null) {
								while (cursor.moveToNext()) {
										String name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
										String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
										if (name != null && path != null) {
												name = cleanSongName(name);
												songsList.add(new SongInfo(name, path));
											}
									}
							}
					} catch (Exception e) {
						Log.e(TAG, "Error scanning music files", e);
					} finally {
						if (cursor != null) {
								cursor.close();
							}
					}
			}

		private String cleanSongName(String songName) {
				// Remover caracteres especiales y números al inicio
				songName = songName.replaceAll("^[0-9\\-\\.\\s]+", "");
				// Remover información común entre corchetes o paréntesis
				songName = songName.replaceAll("\\[.*?\\]", "");
				songName = songName.replaceAll("\\(.*?\\)", "");
				// Remover guiones bajos y múltiples espacios
				songName = songName.replaceAll("_", " ");
				songName = songName.replaceAll("\\s+", " ").trim();
				return songName;
			}

		private void saveSongsToJson() {
				try {
						JSONArray jsonArray = new JSONArray();
						for (SongInfo song : songsList) {
								JSONObject obj = new JSONObject();
								obj.put("name", song.name);
								obj.put("path", song.path);
								jsonArray.put(obj);
							}

						FileOutputStream fos = context.openFileOutput("songs.json", Context.MODE_PRIVATE);
						fos.write(jsonArray.toString().getBytes());
						fos.close();
					} catch (Exception e) {
						Log.e(TAG, "Error saving songs to JSON", e);
					}
			}

		private void loadSongsFromJson() {
				songsList.clear();
				try {
						FileInputStream fis = context.openFileInput("songs.json");
						BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
						StringBuilder stringBuilder = new StringBuilder();
						String line;
						while ((line = reader.readLine()) != null) {
								stringBuilder.append(line);
							}
						fis.close();

						JSONArray jsonArray = new JSONArray(stringBuilder.toString());
						for (int i = 0; i < jsonArray.length(); i++) {
								JSONObject obj = jsonArray.getJSONObject(i);
								String name = obj.getString("name");
								String path = obj.getString("path");
								songsList.add(new SongInfo(name, path));
							}
					} catch (Exception e) {
						Log.e(TAG, "Error loading songs from JSON", e);
						scanMusicFiles();
						saveSongsToJson();
					}
			}

		private ContactInfo findContactByName(String contactName) {
				contactName = contactName.toLowerCase().trim();

				if (contactName.matches("\\d+")) {
						return new ContactInfo("Número", contactName);
					}

				List<ContactMatch> matches = new ArrayList<ContactMatch>();

				for (ContactInfo contact : contactsList) {
						int similarity = calculateSimilarity(contactName, contact.name.toLowerCase());
						matches.add(new ContactMatch(contact, similarity));
					}

				Collections.sort(matches, new Comparator<ContactMatch>() {
							@Override
							public int compare(ContactMatch m1, ContactMatch m2) {
									return Integer.compare(m2.similarity, m1.similarity);
								}
						});

				if (!matches.isEmpty() && matches.get(0).similarity > 50) {
						return matches.get(0).contactInfo;
					}

				return null;
			}

		private SongInfo findSongByName(String songName) {
				songName = songName.toLowerCase().trim();

				List<SongMatch> matches = new ArrayList<SongMatch>();

				for (SongInfo song : songsList) {
						int similarity = calculateSimilarity(songName, song.name.toLowerCase());
						matches.add(new SongMatch(song, similarity));
					}

				Collections.sort(matches, new Comparator<SongMatch>() {
							@Override
							public int compare(SongMatch m1, SongMatch m2) {
									return Integer.compare(m2.similarity, m1.similarity);
								}
						});

				if (!matches.isEmpty() && matches.get(0).similarity > 50) {
						return matches.get(0).songInfo;
					}

				return null;
			}

		private void handleCallCommand(String contactName) {
				String[] askResponses = {"¿A quién deseas llamar?", "¿Cuál contacto marcar?", "¿A qué número llamar?"};
				if (contactName == null || contactName.isEmpty()) {
						tts.speak(askResponses[random.nextInt(askResponses.length)]);
						currentState = "waiting_call_name";
						return;
					}

				contactName = contactName.replaceFirst("(?i)a ", "").trim();

				if (contactName.matches("\\d+")) {
						Intent intent = new Intent(Intent.ACTION_CALL);
						intent.setData(Uri.parse("tel:" + contactName));
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						context.startActivity(intent);
						String[] numResponses = {"Llamando al número " + contactName + ".", "Marcando el " + contactName + ".", "Llamada al " + contactName + "."};
						tts.speak(numResponses[random.nextInt(numResponses.length)]);
						return;
					}

				ContactInfo contact = findContactByName(contactName);
				if (contact != null) {
						Intent intent = new Intent(Intent.ACTION_CALL);
						intent.setData(Uri.parse("tel:" + contact.number));
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						context.startActivity(intent);
						String[] callResponses = {"Llamando a " + contact.name + ".", "Marcando a " + contact.name + ".", "Iniciando llamada a " + contact.name + "."};
						tts.speak(callResponses[random.nextInt(callResponses.length)]);
					} else {
						String[] notFoundResponses = {"Contacto no encontrado: " + contactName + ".", "No encontré el contacto " + contactName + ".", "El contacto " + contactName + " no existe."};
						tts.speak(notFoundResponses[random.nextInt(notFoundResponses.length)]);
					}
			}

		public void processCommand(String command) {
				command = command.trim().toLowerCase();
				if (command.isEmpty()) return;


				if (!currentState.isEmpty()) {
						if (currentState.equals("waiting_alarm_time")) {
								handleAlarmCommand(command);
							} else if (currentState.equals("waiting_call_name")) {
								handleCallCommand(command);
							} else if (currentState.equals("waiting_search_term")) {
								handleSearchCommand(command);
							} else if (currentState.equals("waiting_calculate_expression")) {
								handleCalculateCommand(command);
							} else if (currentState.equals("waiting_message_param")) {
								handleMessageCommand(command);
							} else if (currentState.equals("waiting_navigate_destination")) {
								handleNavigateCommand(command);
							} else if (currentState.equals("waiting_timer_time")) {
								handleTimerCommand(command);
							} else if (currentState.equals("waiting_reminder_content")) {
								handleReminderCommand(command);
							} else if (currentState.equals("waiting_translate_text")) {
								handleTranslateCommand(command);
							} else if (currentState.equals("waiting_email_param")) {
								handleEmailCommand(command);
							} else if (currentState.equals("waiting_open_app")) {
								handleOpenCommand(command);
							} else if (currentState.equals("waiting_weather_location")) {
								handleWeatherCommand(command);
							} else if (currentState.equals("waiting_news_topic")) {
								handleNewsCommand(command);
							} else if (currentState.equals("waiting_wifi_action")) {
								handleWifiCommand(command);
							} else if (currentState.equals("waiting_bluetooth_action")) {
								handleBluetoothCommand(command);
							} else if (currentState.equals("waiting_airplane_action")) {
								handleAirplaneModeCommand(command);
							} else if (currentState.equals("waiting_volume_action")) {
								handleVolumeCommand(command);
							} else if (currentState.equals("waiting_brightness_action")) {
								handleBrightnessCommand(command);
							} else if (currentState.equals("waiting_music_param")) {
								handleMusicCommand(command);
							} else if (currentState.equals("waiting_que_es")) {
								handleWhatIsCommand(command);
							} else if (currentState.equals("waiting_list_param")) {
								handleListCommand(command);
							}
						currentState = "";
						return;
					}

				String[] parts = command.split("\\s+", 3);
				String key = parts.length > 0 ? parts[0] : "";
				String param = parts.length > 1 ? (parts.length > 2 ? parts[1] + " " + parts[2] : parts[1]) : "";

				String commandType = COMMAND_MAP.get(key);
				if (commandType == null) {
						commandType = COMMAND_MAP.get(command);
					}
				if (commandType == null) {
						for (Map.Entry<String, String> entry : COMMAND_MAP.entrySet()) {
								if (command.contains(entry.getKey())) {
										commandType = entry.getValue();
										param = command.replace(entry.getKey(), "").trim();
										break;
									}
							}
					}
				if (commandType == null) commandType = "desconocido";

				if (commandType.equals("abrir")) {
						handleOpenCommand(param);
					} else if (commandType.equals("llamar")) {
						handleCallCommand(param);
					} else if (commandType.equals("buscar")) {
						handleSearchCommand(param);
					} else if (commandType.equals("alarma")) {
						handleAlarmCommand(param);
					} else if (commandType.equals("volumen")) {
						handleVolumeCommand(param);
					} else if (commandType.equals("brillo")) {
						handleBrightnessCommand(param);
					} else if (commandType.equals("apagar")) {
						handleShutdownCommand();
					} else if (commandType.equals("hora")) {
						handleTimeCommand();
					} else if (commandType.equals("calcular")) {
						handleCalculateCommand(param);
					} else if (commandType.equals("mensaje")) {
						handleMessageCommand(param);
					} else if (commandType.equals("navegar")) {
						handleNavigateCommand(param);
					} else if (commandType.equals("musica")) {
						handleMusicCommand(param);
					} else if (commandType.equals("temporizador")) {
						handleTimerCommand(param);
					} else if (commandType.equals("recordatorio")) {
						handleReminderCommand(param);
					} else if (commandType.equals("clima")) {
						handleWeatherCommand(param);
					} else if (commandType.equals("noticias")) {
						handleNewsCommand(param);
					} else if (commandType.equals("traducir")) {
						handleTranslateCommand(param);
					} else if (commandType.equals("wifi")) {
						handleWifiCommand(param);
					} else if (commandType.equals("bluetooth")) {
						handleBluetoothCommand(param);
					} else if (commandType.equals("modo_avion")) {
						handleAirplaneModeCommand(param);
					} else if (commandType.equals("camara")) {
						handleCameraCommand(param);
					} else if (commandType.equals("email")) {
						handleEmailCommand(param);
					} else if (commandType.equals("bateria")) {
						handleBatteryCommand();
					} else if (commandType.equals("chiste")) {
						handleJokeCommand();
					} else if (commandType.equals("dinamita")) {
						handleDynamiteCommand();
					} else if (commandType.equals("saludo")) {
						handleGreetingCommand();
					} else if (commandType.equals("guia")) {
						handleGuideCommand();
					} else if (commandType.equals("esconder")) {
						handleEsconderCommand();
					} else if (commandType.equals("info_dev")) {
						handleInfoDevCommand();
					} else if (commandType.equals("que_es")) {
						handleWhatIsCommand(param);
					} else if (commandType.equals("gracias")) {
						handleThanksCommand();
					} else if (commandType.equals("de_nada")) {
						handleYoureWelcomeCommand();
					} else if (commandType.equals("lista")) {
						handleListCommand(param);
					} else {
						handleUnknownCommand(command);
					}
			}

		private void handleOpenCommand(String appName) {
				String[] askResponses = {"¿Qué aplicación deseas abrir?", "¿Cuál app quieres lanzar?", "¿Qué app debo iniciar?"};
				if (appName == null || appName.isEmpty()) {
						tts.speak(askResponses[random.nextInt(askResponses.length)]);
						currentState = "waiting_open_app";
						return;
					}

				AppInfo app = findAppBySimilarity(appName);
				if (app != null) {
						Intent intent = context.getPackageManager().getLaunchIntentForPackage(app.packageName);
						if (intent != null) {
								intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								context.startActivity(intent);
								String[] openResponses = {"Abriendo " + app.name + ".", "Iniciando " + app.name + ".", "Lanzando " + app.name + "."};
								tts.speak(openResponses[random.nextInt(openResponses.length)]);
							} else {
								String[] failResponses = {"No se puede abrir " + appName + ".", "Error al lanzar " + appName + ".", "No pude iniciar " + appName + "."};
								tts.speak(failResponses[random.nextInt(failResponses.length)]);
							}
					} else {
						String[] notFoundResponses = {"App no encontrada: " + appName + ".", "No encontré la app " + appName + ".", "La aplicación " + appName + " no está instalada."};
						tts.speak(notFoundResponses[random.nextInt(notFoundResponses.length)]);
					}
			}

		private AppInfo findAppBySimilarity(String searchTerm) {
				searchTerm = searchTerm.toLowerCase().trim();

				if (installedApps.containsKey(searchTerm)) {
						return installedApps.get(searchTerm);
					}

				List<AppMatch> matches = new ArrayList<AppMatch>();

				for (Map.Entry<String, AppInfo> entry : installedApps.entrySet()) {
						String appName = entry.getKey();
						AppInfo appInfo = entry.getValue();

						int similarity = calculateSimilarity(searchTerm, appName);
						matches.add(new AppMatch(appInfo, similarity));
					}

				Collections.sort(matches, new Comparator<AppMatch>() {
							@Override
							public int compare(AppMatch m1, AppMatch m2) {
									return Integer.compare(m2.similarity, m1.similarity);
								}
						});

				if (!matches.isEmpty() && matches.get(0).similarity > 0) {
						return matches.get(0).appInfo;
					}

				return null;
			}

		private int calculateSimilarity(String s1, String s2) {
				s1 = s1.toLowerCase();
				s2 = s2.toLowerCase();

				if (s1.equals(s2)) return 100;

				if (s2.contains(s1) || s1.contains(s2)) return 80;

				String[] words1 = s1.split("\\s+");
				String[] words2 = s2.split("\\s+");

				int wordMatches = 0;
				for (String w1 : words1) {
						for (String w2 : words2) {
								if (w1.equals(w2) || w2.contains(w1) || w1.contains(w2)) {
										wordMatches++;
										break;
									}
							}
					}

				int maxWords = Math.max(words1.length, words2.length);
				if (maxWords > 0) {
						return (wordMatches * 100) / maxWords;
					}

				return 0;
			}

		private void handleSearchCommand(String term) {
				String[] askResponses = {"¿Qué deseas buscar?", "¿Cuál término investigar?", "¿Qué quieres encontrar?"};
				if (term == null || term.isEmpty()) {
						tts.speak(askResponses[random.nextInt(askResponses.length)]);
						currentState = "waiting_search_term";
						return;
					}
				String url = "https://www.google.com/search?q=" + Uri.encode(term);
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
				String[] searchResponses = {"Buscando " + term + ".", "Investigando " + term + ".", "Encontrando información sobre " + term + "."};
				tts.speak(searchResponses[random.nextInt(searchResponses.length)]);
			}

		private void handleAlarmCommand(String timeStr) {
				String[] askResponses = {"¿Para qué hora la alarma?", "¿A qué hora despertarte?", "¿Hora para la alarma?"};
				if (timeStr == null || timeStr.isEmpty()) {
						tts.speak(askResponses[random.nextInt(askResponses.length)]);
						currentState = "waiting_alarm_time";
						return;
					}
				try {
						Date time = parseTime(timeStr);
						if (time != null) {
								Calendar cal = Calendar.getInstance();
								cal.setTime(time);
								Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM);
								intent.putExtra(AlarmClock.EXTRA_HOUR, cal.get(Calendar.HOUR_OF_DAY));
								intent.putExtra(AlarmClock.EXTRA_MINUTES, cal.get(Calendar.MINUTE));
								intent.putExtra(AlarmClock.EXTRA_SKIP_UI, true);
								intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								context.startActivity(intent);
								String formattedTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(time);
								String[] setResponses = {"Alarma puesta para las " + formattedTime + ".", "Despertador configurado a las " + formattedTime + ".", "Alarma activada para " + formattedTime + "."};
								tts.speak(setResponses[random.nextInt(setResponses.length)]);
							} else {
								String[] failResponses = {"Hora no entendida: " + timeStr + ".", "No pude parsear la hora " + timeStr + ".", "Formato de hora inválido: " + timeStr + "."};
								tts.speak(failResponses[random.nextInt(failResponses.length)]);
							}
					} catch (Exception e) {
						String[] errorResponses = {"Error poniendo alarma.", "Fallo al configurar la alarma.", "Problema con la alarma."};
						tts.speak(errorResponses[random.nextInt(errorResponses.length)]);
						Log.e(TAG, "Error alarma", e);
					}
			}

		private Date parseTime(String timeStr) {
				timeStr = timeStr.trim().toLowerCase();
				timeStr = timeStr.replaceAll("(?i)a las|para las|las|la", "").trim();
				boolean isPM = timeStr.matches(".*(?i)(pm|tarde|noche).*");
				boolean isAM = timeStr.matches(".*(?i)(am|mañana).*");
				timeStr = timeStr.replaceAll("(?i) de la mañana|de la tarde|de la noche|am|pm", "").trim();
				timeStr = timeStr.replace(' ', ':').replaceAll(":+", ":");
				String[] parts = timeStr.split(":");
				int hour = 0, min = 0;
				try {
						if (parts.length == 1) {
								hour = Integer.parseInt(parts[0]);
								min = 0;
							} else if (parts.length == 2) {
								hour = Integer.parseInt(parts[0]);
								min = Integer.parseInt(parts[1]);
							} else {
								return null;
							}
						if (hour >= 1 && hour <= 12) {
								if (isPM) hour += 12;
								if (hour == 12 && isAM) hour = 0;
							}
						if (hour < 0 || hour > 23 || min < 0 || min > 59) {
								return null;
							}
						Calendar cal = Calendar.getInstance();
						cal.set(Calendar.HOUR_OF_DAY, hour);
						cal.set(Calendar.MINUTE, min);
						cal.set(Calendar.SECOND, 0);
						cal.set(Calendar.MILLISECOND, 0);
						if (cal.getTime().before(new Date())) {
								cal.add(Calendar.DAY_OF_MONTH, 1);
							}
						return cal.getTime();
					} catch (NumberFormatException e) {
						return null;
					}
			}

		private void handleVolumeCommand(String action) {
				String[] askResponses = {"¿Subir, bajar o nivel de volumen?", "¿Ajustar volumen cómo?", "¿Qué hacer con el volumen?"};
				if (action == null || action.isEmpty()) {
						currentState = "waiting_volume_action";
						tts.speak(askResponses[random.nextInt(askResponses.length)]);
						return;
					}
				AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
				if (action.contains("sube") || action.contains("aumenta") || action.contains("alto")) {
						am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
						String[] raiseResponses = {"Volumen subido.", "Aumentando el volumen.", "Volumen más alto."};
						tts.speak(raiseResponses[random.nextInt(raiseResponses.length)]);
					} else if (action.contains("baja") || action.contains("disminuye") || action.contains("bajo")) {
						am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
						String[] lowerResponses = {"Volumen bajado.", "Disminuyendo el volumen.", "Volumen más bajo."};
						tts.speak(lowerResponses[random.nextInt(lowerResponses.length)]);
					} else if (action.contains("silencio") || action.contains("mute")) {
						am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_SHOW_UI);
						String[] muteResponses = {"Volumen en silencio.", "Modo silencio activado.", "Silenciando el volumen."};
						tts.speak(muteResponses[random.nextInt(muteResponses.length)]);
					} else if (action.matches("\\d+")) {
						int level = Integer.parseInt(action);
						int max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
						level = Math.min(max, Math.max(0, level));
						am.setStreamVolume(AudioManager.STREAM_MUSIC, level, AudioManager.FLAG_SHOW_UI);
						String[] setResponses = {"Volumen ajustado a " + level + ".", "Nivel de volumen en " + level + ".", "Volumen establecido en " + level + "."};
						tts.speak(setResponses[random.nextInt(setResponses.length)]);
					} else {
						int current = am.getStreamVolume(AudioManager.STREAM_MUSIC);
						String[] currentResponses = {"Volumen actual es " + current + ".", "El volumen está en " + current + ".", "Nivel actual: " + current + "."};
						tts.speak(currentResponses[random.nextInt(currentResponses.length)]);
					}
			}

		private void handleBrightnessCommand(String action) {
				String[] askResponses = {"¿Aumentar, disminuir o nivel de brillo?", "¿Ajustar brillo cómo?", "¿Qué hacer con el brillo?"};
				if (action == null || action.isEmpty()) {
						currentState = "waiting_brightness_action";
						tts.speak(askResponses[random.nextInt(askResponses.length)]);
						return;
					}
				try {
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
								if (!Settings.System.canWrite(context)) {
										Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
										intent.setData(Uri.parse("package:" + context.getPackageName()));
										intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
										context.startActivity(intent);
										String[] permResponses = {"Por favor, concede permiso para cambiar el brillo.", "Necesito permiso para ajustar brillo."};
										tts.speak(permResponses[random.nextInt(permResponses.length)]);
										return;
									}
							}
						Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
						int current = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
						if (action.contains("auto")) {
								Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
								String[] autoResponses = {"Brillo automático activado.", "Modo automático de brillo on.", "Brillo en auto."};
								tts.speak(autoResponses[random.nextInt(autoResponses.length)]);
							} else if (action.contains("aumenta") || action.contains("sube") || action.contains("alto")) {
								int newLevel = Math.min(255, current + 50);
								Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, newLevel);
								String[] raiseResponses = {"Brillo aumentado.", "Subiendo el brillo.", "Brillo más alto."};
								tts.speak(raiseResponses[random.nextInt(raiseResponses.length)]);
							} else if (action.contains("disminuye") || action.contains("baja") || action.contains("bajo")) {
								int newLevel = Math.max(0, current - 50);
								Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, newLevel);
								String[] lowerResponses = {"Brillo disminuido.", "Bajando el brillo.", "Brillo más bajo."};
								tts.speak(lowerResponses[random.nextInt(lowerResponses.length)]);
							} else if (action.matches("\\d+")) {
								int level = Integer.parseInt(action);
								level = Math.min(255, Math.max(0, level));
								Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, level);
								String[] setResponses = {"Brillo ajustado a " + level + ".", "Nivel de brillo en " + level + ".", "Brillo establecido en " + level + "."};
								tts.speak(setResponses[random.nextInt(setResponses.length)]);
							} else {
								String[] currentResponses = {"Brillo actual es " + current + ".", "El brillo está en " + current + ".", "Nivel de brillo: " + current + "."};
								tts.speak(currentResponses[random.nextInt(currentResponses.length)]);
							}
					} catch (Exception e) {
						String[] errorResponses = {"Error ajustando brillo.", "Fallo al cambiar brillo.", "Problema con el brillo."};
						tts.speak(errorResponses[random.nextInt(errorResponses.length)]);
						Log.e(TAG, "Error brillo", e);
					}
			}

		private void handleShutdownCommand() {
				String[] byeResponses = {"Adiós, hasta luego.", "Hasta la vista.", "Cerrando, chau."};
				tts.speak(byeResponses[random.nextInt(byeResponses.length)]);
				((Activity) context).finish();
			}

		private void handleTimeCommand() {
				SimpleDateFormat sdf = new SimpleDateFormat("HH:mm 'del' d 'de' MMMM 'de' yyyy", Locale.getDefault());
				String currentTime = sdf.format(new Date());
				String[] timeResponses = {"La hora actual es " + currentTime + ".", "Son las " + currentTime + ".", "Fecha y hora: " + currentTime + "."};
				tts.speak(timeResponses[random.nextInt(timeResponses.length)]);
			}

		private void handleCalculateCommand(String expression) {
				String[] askResponses = {"¿Qué operación calcular?", "¿Cuál expresión matemática?", "¿Qué cuenta hacer?"};
				if (expression == null || expression.isEmpty()) {
						tts.speak(askResponses[random.nextInt(askResponses.length)]);
						currentState = "waiting_calculate_expression";
						return;
					}
				try {
						double result = evaluateExpression(expression);
						String[] resultResponses = {"El resultado es " + result + ".", "La cuenta da " + result + ".", "Cálculo: " + result + "."};
						tts.speak(resultResponses[random.nextInt(resultResponses.length)]);
					} catch (Exception e) {
						String[] errorResponses = {"Error en el cálculo: " + e.getMessage() + ".", "Cálculo inválido.", "No pude calcular eso."};
						tts.speak(errorResponses[random.nextInt(errorResponses.length)]);
					}
			}

		private void handleMessageCommand(String param) {
				String[] askResponses = {"¿A quién y qué mensaje enviar?", "¿Destinatario y texto?", "¿Mensaje para quién?"};
				if (param == null || param.isEmpty()) {
						tts.speak(askResponses[random.nextInt(askResponses.length)]);
						currentState = "waiting_message_param";
						return;
					}
				String to = "";
				String message = param;
				Pattern pat = Pattern.compile("(?i)a (.+?) (que diga|diciendo|el mensaje|mensaje)? (.+)");
				Matcher mat = pat.matcher(param);
				if (mat.find()) {
						to = mat.group(1).trim();
						message = mat.group(3).trim();
					}
				if (to.isEmpty() || message.isEmpty()) {
						String[] formatResponses = {"Formato incorrecto para mensaje.", "No entendí el formato del mensaje.", "Por favor, especifica destinatario y texto."};
						tts.speak(formatResponses[random.nextInt(formatResponses.length)]);
						return;
					}

				ContactInfo contact = findContactByName(to);
				String number = contact != null ? contact.number : to.replaceAll("\\D+", "");

				if (number.isEmpty()) {
						String[] notFoundResponses = {"Número o contacto no encontrado.", "No encontré el destinatario.", "Contacto inválido."};
						tts.speak(notFoundResponses[random.nextInt(notFoundResponses.length)]);
						return;
					}
				Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + number));
				intent.putExtra("sms_body", message);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
				String[] sendResponses = {"Enviando mensaje a " + to + ".", "Mensaje enviado a " + to + ".", "Mandando texto a " + to + "."};
				tts.speak(sendResponses[random.nextInt(sendResponses.length)]);
			}

		private void handleNavigateCommand(String destination) {
				String[] askResponses = {"¿A qué destino navegar?", "¿Cuál dirección?", "¿Hacia dónde ir?"};
				if (destination == null || destination.isEmpty()) {
						tts.speak(askResponses[random.nextInt(askResponses.length)]);
						currentState = "waiting_navigate_destination";
						return;
					}
				Uri gmmIntentUri = Uri.parse("google.navigation:q=" + Uri.encode(destination));
				Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
				mapIntent.setPackage("com.google.android.apps.maps");
				mapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
						context.startActivity(mapIntent);
						String[] navResponses = {"Navegando a " + destination + ".", "Dirigiéndote a " + destination + ".", "Ruta hacia " + destination + "."};
						tts.speak(navResponses[random.nextInt(navResponses.length)]);
					} else {
						String[] noAppResponses = {"No hay app de mapas instalada.", "Instala Google Maps para navegar.", "Error: sin app de navegación."};
						tts.speak(noAppResponses[random.nextInt(noAppResponses.length)]);
					}
			}

		private void handleMusicCommand(String param) {
				String[] askResponses = {"¿Qué canción deseas reproducir?", "¿Qué música quieres escuchar?", "¿Cuál canción?"};
				if (param == null || param.isEmpty()) {
						tts.speak(askResponses[random.nextInt(askResponses.length)]);
						currentState = "waiting_music_param";
						return;
					}

				// Verificar si se especifica una aplicación con "con [app]" o "en [app]"
				String songName = param;
				String appName = null;

				Pattern pattern = Pattern.compile("(.+?)\\s+(?:con|en|usando)\\s+(.+)", Pattern.CASE_INSENSITIVE);
				Matcher matcher = pattern.matcher(param);
				if (matcher.find()) {
						songName = matcher.group(1).trim();
						appName = matcher.group(2).trim();
					}

				if (songName.contains("pausa") || songName.contains("detén") || songName.contains("para")) {
						AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
						am.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
						String[] pauseResponses = {"Música pausada.", "Deteniendo la música.", "Pausa en reproducción."};
						tts.speak(pauseResponses[random.nextInt(pauseResponses.length)]);
					} else {
						// Buscar la canción por similitud
						SongInfo song = findSongByName(songName);
						if (song != null) {
								// Reproducir la canción
								playSong(song, appName);
							} else {
								String[] notFoundResponses = {"Canción no encontrada: " + songName + ".", "No encontré la canción " + songName + ".", "La canción " + songName + " no está en tu lista."};
								tts.speak(notFoundResponses[random.nextInt(notFoundResponses.length)]);
							}
					}
			}

		private void playSong(SongInfo song, String appName) {
				File file = new File(song.path);
				if (!file.exists()) {
						String[] errorResponses = {"El archivo de la canción no existe.", "No pude encontrar el archivo de la canción.", "La ruta de la canción no es válida."};
						tts.speak(errorResponses[random.nextInt(errorResponses.length)]);
						return;
					}

				Uri uri = FileProvider.getUriForFile(context, "com.TSgames.TSassistant.fileprovider", file);
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(uri, "audio/*");
				intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

				// Si se especificó una aplicación
				if (appName != null) {
						AppInfo app = findAppBySimilarity(appName);
						if (app != null) {
								intent.setPackage(app.packageName);
								String[] appResponses = {"Abriendo " + song.name + " con " + app.name + ".", "Reproduciendo " + song.name + " en " + app.name + ".", "Iniciando " + song.name + " con " + app.name + "."};
								tts.speak(appResponses[random.nextInt(appResponses.length)]);
							} else {
								String[] appNotFoundResponses = {"No encontré la aplicación " + appName + ". Usando reproductor predeterminado.", "App no encontrada, usando reproductor por defecto."};
								tts.speak(appNotFoundResponses[random.nextInt(appNotFoundResponses.length)]);
							}
					} else {
						String[] playResponses = {"Reproduciendo " + song.name + ".", "Sonando " + song.name + ".", "Escuchando " + song.name + "."};
						tts.speak(playResponses[random.nextInt(playResponses.length)]);
					}

				// Verificar si hay alguna aplicación que pueda manejar el intent
				if (intent.resolveActivity(context.getPackageManager()) != null) {
						context.startActivity(intent);
					} else {
						// Si no hay aplicación específica, intentar con un intent genérico
						intent = new Intent(Intent.ACTION_VIEW);
						intent.setDataAndType(uri, "audio/*");
						intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

						if (intent.resolveActivity(context.getPackageManager()) != null) {
								context.startActivity(intent);
							} else {
								String[] noAppResponses = {"No hay aplicación para reproducir música.", "No se encontró un reproductor de música."};
								tts.speak(noAppResponses[random.nextInt(noAppResponses.length)]);
							}
					}
			}

		private void handleTimerCommand(String timeStr) {
				String[] askResponses = {"¿Para cuánto tiempo el temporizador?", "¿Duración del timer?", "¿Cuántos minutos/segundos?"};
				if (timeStr == null || timeStr.isEmpty()) {
						tts.speak(askResponses[random.nextInt(askResponses.length)]);
						currentState = "waiting_timer_time";
						return;
					}
				int seconds = 0;
				Pattern pat = Pattern.compile("(\\d+)\\s*(horas?|mins?|minutos?|segs?|segundos?)");
				Matcher mat = pat.matcher(timeStr.toLowerCase());
				while (mat.find()) {
						int val = Integer.parseInt(mat.group(1));
						String unit = mat.group(2);
						if (unit.startsWith("h")) seconds += val * 3600;
						else if (unit.startsWith("m")) seconds += val * 60;
						else seconds += val;
					}
				if (seconds > 0) {
						Intent intent = new Intent(AlarmClock.ACTION_SET_TIMER);
						intent.putExtra(AlarmClock.EXTRA_LENGTH, seconds);
						intent.putExtra(AlarmClock.EXTRA_SKIP_UI, true);
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						context.startActivity(intent);
						String[] setResponses = {"Temporizador puesto para " + seconds + " segundos.", "Timer iniciado en " + seconds + " seg.", "Cuenta atrás de " + seconds + " segundos."};
						tts.speak(setResponses[random.nextInt(setResponses.length)]);
					} else {
						String[] failResponses = {"Tiempo no entendido.", "No pude parsear el tiempo.", "Formato de tiempo inválido."};
						tts.speak(failResponses[random.nextInt(failResponses.length)]);
					}
			}

		private void handleReminderCommand(String param) {
				String[] askResponses = {"¿Qué quieres que te recuerde?", "¿Cuál es la nota?", "¿Recordatorio sobre qué?"};
				if (param == null || param.isEmpty()) {
						tts.speak(askResponses[random.nextInt(askResponses.length)]);
						currentState = "waiting_reminder_content";
						return;
					}
				Intent intent = new Intent(Intent.ACTION_INSERT);
				intent.setType("vnd.android.cursor.dir/event");
				intent.putExtra("title", param);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
				String[] setResponses = {"Recordatorio creado: " + param + ".", "Nota guardada: " + param + ".", "Te recordaré " + param + "."};
				tts.speak(setResponses[random.nextInt(setResponses.length)]);
			}

		private void handleWeatherCommand(String location) {
				String[] askResponses = {"¿Clima en qué ubicación?", "¿Dónde chequear el tiempo?", "¿Ciudad para el pronóstico?"};
				if (location == null || location.isEmpty()) {
						tts.speak(askResponses[random.nextInt(askResponses.length)]);
						currentState = "waiting_weather_location";
						return;
					}
				handleSearchCommand("clima en " + location);
				String[] showResponses = {"Mostrando clima en " + location + ".", "Pronóstico para " + location + ".", "Tiempo en " + location + "."};
				tts.speak(showResponses[random.nextInt(showResponses.length)]);
			}

		private void handleNewsCommand(String topic) {
				String[] askResponses = {"¿Noticias sobre qué tema?", "¿Cuál categoría de noticias?", "¿Tema para las novedades?"};
				if (topic == null || topic.isEmpty()) {
						tts.speak(askResponses[random.nextInt(askResponses.length)]);
						currentState = "waiting_news_topic";
						return;
					}
				handleSearchCommand("noticias " + topic);
				String[] showResponses = {"Mostrando noticias sobre " + topic + ".", "Novedades en " + topic + ".", "Headlines de " + topic + "."};
				tts.speak(showResponses[random.nextInt(showResponses.length)]);
			}

		private void handleTranslateCommand(String param) {
				String[] askResponses = {"¿Qué texto traducir y a qué idioma?", "¿Frase y lenguaje?", "¿Qué traducir?"};
				if (param == null || param.isEmpty()) {
						tts.speak(askResponses[random.nextInt(askResponses.length)]);
						currentState = "waiting_translate_text";
						return;
					}
				String text = param;
				String toLang = "inglés";
				Pattern pat = Pattern.compile("(.+)\\s+a\\s+(\\w+)");
				Matcher mat = pat.matcher(param);
				if (mat.find()) {
						text = mat.group(1).trim();
						toLang = mat.group(2).trim();
					}
				Uri uri = Uri.parse("https://translate.google.com/?sl=auto&tl=" + toLang + "&text=" + Uri.encode(text) + "&op=translate");
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
				String[] transResponses = {"Traduciendo '" + text + "' a " + toLang + ".", "Traducción de '" + text + "' al " + toLang + ".", "Convirtiendo '" + text + "' a " + toLang + "."};
				tts.speak(transResponses[random.nextInt(transResponses.length)]);
			}

		private void handleWifiCommand(String action) {
				String[] askResponses = {"¿Activar o desactivar WiFi?", "¿WiFi on u off?", "¿Qué con el WiFi?"};
				if (action == null || action.isEmpty()) {
						currentState = "waiting_wifi_action";
						tts.speak(askResponses[random.nextInt(askResponses.length)]);
						return;
					}
				// Para compatibilidad con Android 10+, abrir settings en lugar de cambiar directamente
				Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
				boolean enable = action.contains("activa") || action.contains("enciende") || action.contains("on");
				String[] statusResponses = {"Abriendo ajustes de WiFi para " + (enable ? "activar" : "desactivar") + ".", "Ve a los ajustes para cambiar WiFi."};
				tts.speak(statusResponses[random.nextInt(statusResponses.length)]);
			}

		private void handleBluetoothCommand(String action) {
				String[] askResponses = {"¿Activar o desactivar Bluetooth?", "¿Bluetooth on u off?", "¿Qué con el Bluetooth?"};
				if (action == null || action.isEmpty()) {
						currentState = "waiting_bluetooth_action";
						tts.speak(askResponses[random.nextInt(askResponses.length)]);
						return;
					}
				// Para compatibilidad, abrir settings
				Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
				boolean enable = action.contains("activa") || action.contains("enciende") || action.contains("on");
				String[] statusResponses = {"Abriendo ajustes de Bluetooth para " + (enable ? "activar" : "desactivar") + ".", "Ve a los ajustes para cambiar Bluetooth."};
				tts.speak(statusResponses[random.nextInt(statusResponses.length)]);
			}

		private void handleAirplaneModeCommand(String action) {
				String[] askResponses = {"¿Activar o desactivar modo avión?", "¿Modo avión on u off?", "¿Qué con el modo avión?"};
				if (action == null || action.isEmpty()) {
						currentState = "waiting_airplane_action";
						tts.speak(askResponses[random.nextInt(askResponses.length)]);
						return;
					}
				// Para compatibilidad, abrir settings (no se puede cambiar directamente desde API 17+)
				Intent intent = new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
				boolean enable = action.contains("activa") || action.contains("enciende") || action.contains("on");
				String[] statusResponses = {"Abriendo ajustes de modo avión para " + (enable ? "activar" : "desactivar") + ".", "Ve a los ajustes para cambiar modo avión."};
				tts.speak(statusResponses[random.nextInt(statusResponses.length)]);
			}

		private void handleCameraCommand(String param) {
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
				String[] openResponses = {"Abriendo cámara.", "Iniciando la cámara.", "Cámara lista."};
				tts.speak(openResponses[random.nextInt(openResponses.length)]);
			}

		private void handleEmailCommand(String param) {
				String[] askResponses = {"¿A quién, asunto y cuerpo del email?", "¿Destinatario, tema y mensaje?", "¿Email para quién?"};
				if (param == null || param.isEmpty()) {
						tts.speak(askResponses[random.nextInt(askResponses.length)]);
						currentState = "waiting_email_param";
						return;
					}
				String to = "";
				String subject = "";
				String body = "";
				Pattern pat = Pattern.compile("(?i)a (.+?) asunto (.+?) (cuerpo|mensaje|que diga)? (.+)");
				Matcher mat = pat.matcher(param);
				if (mat.find()) {
						to = mat.group(1).trim();
						subject = mat.group(2).trim();
						body = mat.group(4).trim();
					} else {
						String[] formatResponses = {"Formato de email no entendido.", "No capté el formato del correo.", "Especifica to, asunto y body."};
						tts.speak(formatResponses[random.nextInt(formatResponses.length)]);
						return;
					}
				Intent intent = new Intent(Intent.ACTION_SENDTO);
				intent.setData(Uri.parse("mailto:" + to));
				intent.putExtra(Intent.EXTRA_SUBJECT, subject);
				intent.putExtra(Intent.EXTRA_TEXT, body);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
				String[] sendResponses = {"Enviando email a " + to + ".", "Correo enviado a " + to + ".", "Mandando email a " + to + "."};
				tts.speak(sendResponses[random.nextInt(sendResponses.length)]);
			}

		private void handleBatteryCommand() {
				Intent batteryIntent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
				int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
				int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
				int percent = (level * 100) / scale;
				String[] levelResponses = {"Nivel de batería al " + percent + " por ciento.", "Batería en " + percent + "%.", "Carga actual: " + percent + "%."};
				tts.speak(levelResponses[random.nextInt(levelResponses.length)]);
			}

		private void handleJokeCommand() {
				String[] jokes = {
						"¿Por qué los pájaros no usan Facebook? Porque ya tienen Twitter.",
						"¿Qué le dice un jaguar a otro? Jaguar you.",
						"¿Por qué el libro de matemáticas estaba triste? Porque tenía muchos problemas.",
						"¿Cómo se despide un mago? Hablamos abracadabra.",
						"¿Por qué los tomates son rojos? Porque se sonrojan al ver la ensalada.",
						"¿Qué hace un vampiro en la computadora? Navega por la web.",
						"¿Por qué el ciclista llevaba una chaqueta? Porque iba en contra del viento.",
						"¿Qué le dice una pared a otra? Nos vemos en la esquina.",
						"¿Por qué los fantasmas son malos mintiendo? Porque se les ve el plumero.",
						"¿Qué le dice un imán a otro? Me atraes mucho.",
						"¿Por qué el mar no se seca? Porque tiene olas.",
						"¿Qué le dice un techo a otro? Techo de menos.",
						"¿Por qué los pájaros vuelan al sur? Porque es demasiado lejos para caminar.",
						"¿Qué es un pez en un cine? Un espectador.",
						"¿Por qué el computador fue al médico? Porque tenía un virus."
					};
				tts.speak(jokes[random.nextInt(jokes.length)]);
			}

		private void handleDynamiteCommand() {
				String[] items = {"netherite", "diamantes", "hierro", "cobblestone", "oro", "esmeraldas", "lapis", "redstone", "carbón", "cuarzo", "libros encantados"};
				int amount = random.nextInt(64) + 1;
				String item = items[random.nextInt(items.length)];
				String[] boomResponses = {"Colocando dinamita... pssst... ¡Boom! Encontré " + amount + " de " + item + ".", "¡Explosión! Hallé " + amount + " " + item + ".", "Dinamita activada... ¡Boom! " + amount + " " + item + "."};
				tts.speak(boomResponses[random.nextInt(boomResponses.length)]);
			}

		private void handleGreetingCommand() {
				Calendar cal = Calendar.getInstance();
				int hour = cal.get(Calendar.HOUR_OF_DAY);
				String greeting = hour < 12 ? "Buenos días" : hour < 18 ? "Buenas tardes" : "Buenas noches";
				String[] greetResponses = {greeting + ", ¿en qué puedo ayudarte?", greeting + ", ¿qué necesitas?", greeting + ", hola, ¿cómo te ayudo?"};
				tts.speak(greetResponses[random.nextInt(greetResponses.length)]);
			}

		private void handleGuideCommand() {
				String guide = "Soy TS Assistant, tu asistente de voz. Comandos disponibles: abre [app], llama a [contacto], busca [término], pon alarma para [hora], ajusta volumen [sube/baja/nivel], ajusta brillo [aumenta/disminuye/nivel], apagar, qué hora es, calcula [expresión], envía mensaje a [contacto] que diga [texto], navega a [destino], reproduce [canción], pon temporizador para [tiempo], recuérdame [nota], clima en [lugar], noticias [tema], traduce [texto] a [idioma], activa/desactiva wifi, activa/desactiva bluetooth, activa/desactiva modo avión, abre cámara, envía email a [correo] asunto [tema] cuerpo [texto], nivel de batería, cuenta un chiste, pon dinamita, hola, guía, escóndete, quién eres, qué es [cosa], gracias, lista mis contactos, lista mi música.";
				String[] guideResponses = {guide, "Guía de comandos: " + guide, "Aquí va la lista de comandos: " + guide};
				tts.speak(guideResponses[random.nextInt(guideResponses.length)]);
			}

		private void handleEsconderCommand() {
				AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
				int maxVol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
				am.setStreamVolume(AudioManager.STREAM_MUSIC, maxVol / 2, 0);
				try {
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
								if (!Settings.System.canWrite(context)) {
										Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
										intent.setData(Uri.parse("package:" + context.getPackageName()));
										intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
										context.startActivity(intent);
										String[] permResponses = {"Por favor, concede permiso para cambiar el brillo.", "Necesito permiso para ajustar brillo."};
										tts.speak(permResponses[random.nextInt(permResponses.length)]);
										return;
									}
							}
						Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 128);
					} catch (Exception e) {
						Log.e(TAG, "Error in handleEsconderCommand", e);
					}
				String[] hideResponses = {"Modo discreto activado.", "Escondiéndome...", "Modo bajo perfil on."};
				tts.speak(hideResponses[random.nextInt(hideResponses.length)]);
			}

		private void handleInfoDevCommand() {
				String[] infoResponses = {"Soy desarrollado por TuringSoftware, Leonardo S. Nivoida de Argentina.", "Creado por Leonardo S. Nivoida de TuringSoftware, Argentina.", "Desarrollador: Leonardo S. Nivoida, TuringSoftware."};
				tts.speak(infoResponses[random.nextInt(infoResponses.length)]);
			}

		private void handleWhatIsCommand(String param) {
				String[] askResponses = {"¿Qué cosa deseas saber qué es?", "¿Cuál es el tema?", "¿Qué definir?"};
				if (param == null || param.isEmpty()) {
						tts.speak(askResponses[random.nextInt(askResponses.length)]);
						currentState = "waiting_que_es";
						return;
					}
				AppInfo app = findAppBySimilarity(param);
				if (app != null && !app.description.equals("Sin descripción disponible.")) {
						String[] descResponses = {param + " es " + app.description + ".", "Definición: " + param + " es " + app.description + ".", app.description};
						tts.speak(descResponses[random.nextInt(descResponses.length)]);
					} else {
						String[] notKnownResponses = {"No sé qué es " + param + ".", "Sin información sobre " + param + ".", "No tengo datos de " + param + "."};
						tts.speak(notKnownResponses[random.nextInt(notKnownResponses.length)]);
					}
			}

		private void handleThanksCommand() {
				String[] thanksResponses = {"De nada.", "A tu servicio.", "Con gusto.", "No hay de qué.", "Encantado de ayudar."};
				tts.speak(thanksResponses[random.nextInt(thanksResponses.length)]);
			}

		private void handleYoureWelcomeCommand() {
				String[] welcomeResponses = {"Gracias a ti.", "Siempre a disposición.", "No hay problema."};
				tts.speak(welcomeResponses[random.nextInt(welcomeResponses.length)]);
			}

		private void handleListCommand(String param) {
				String[] askResponses = {"¿Qué deseas listar? Por ejemplo: 'mis contactos' o 'mi música'.", "¿Qué quieres ver en lista? Di 'mis contactos', 'aplicaciones' o 'mi música'.", "¿Listar contactos, aplicaciones o música?"};
				if (param == null || param.isEmpty()) {
						tts.speak(askResponses[random.nextInt(askResponses.length)]);
						currentState = "waiting_list_param";
						return;
					}

				param = param.toLowerCase().trim();

				if (param.contains("contacto") || param.contains("contactos")) {
						listContacts();
					} else if (param.contains("app") || param.contains("aplicacion") || param.contains("aplicaciones")) {
						listApps();
					} else if (param.contains("música") || param.contains("musica") || param.contains("cancion") || param.contains("canciones")) {
						listSongs();
					} else {
						String[] unknownResponses = {"No sé qué listar. Intenta decir 'lista mis contactos', 'lista aplicaciones' o 'lista mi música'.", "Comando de lista no reconocido. Prueba con 'lista contactos' o 'lista música'.", "Solo puedo listar contactos, aplicaciones o música."};
						tts.speak(unknownResponses[random.nextInt(unknownResponses.length)]);
					}
			}

		private void listContacts() {
				if (contactsList.isEmpty()) {
						String[] emptyResponses = {"No tienes contactos guardados.", "Tu lista de contactos está vacía.", "No hay contactos para mostrar."};
						tts.speak(emptyResponses[random.nextInt(emptyResponses.length)]);
						return;
					}

				StringBuilder contactNames = new StringBuilder();
				int count = 0;
				for (ContactInfo contact : contactsList) {
						count++;
						contactNames.append(contact.name);
						if (count < contactsList.size()) {
								contactNames.append(", ");
							}

						if (count % 5 == 0 && count < contactsList.size()) {
								contactNames.append("\n");
							}
					}

				String contactListStr = contactNames.toString();
				int totalContacts = contactsList.size();

				String[] introResponses = {"Tienes " + totalContacts + " contactos guardados. Los nombres son: ", "Lista de contactos: ", "Tus contactos son: "};
				tts.speak(introResponses[random.nextInt(introResponses.length)] + contactListStr);
			}

		private void listApps() {
				if (installedAppNames.isEmpty()) {
						String[] emptyResponses = {"No hay aplicaciones instaladas.", "No se encontraron aplicaciones.", "La lista de aplicaciones está vacía."};
						tts.speak(emptyResponses[random.nextInt(emptyResponses.length)]);
						return;
					}

				StringBuilder appNames = new StringBuilder();
				int count = 0;
				for (String appName : installedAppNames) {
						count++;
						appNames.append(appName);
						if (count < installedAppNames.size()) {
								appNames.append(", ");
							}

						if (count % 5 == 0 && count < installedAppNames.size()) {
								appNames.append("\n");
							}
					}

				String appListStr = appNames.toString();
				int totalApps = installedAppNames.size();

				String[] introResponses = {"Tienes " + totalApps + " aplicaciones instaladas. Algunas son: ", "Lista de aplicaciones: ", "Tus aplicaciones son: "};
				tts.speak(introResponses[random.nextInt(introResponses.length)] + appListStr);
			}

		private void listSongs() {
				if (songsList.isEmpty()) {
						String[] emptyResponses = {"No tienes canciones guardadas.", "No se encontraron canciones.", "Tu lista de música está vacía."};
						tts.speak(emptyResponses[random.nextInt(emptyResponses.length)]);
						return;
					}

				StringBuilder songNames = new StringBuilder();
				int count = 0;
				for (SongInfo song : songsList) {
						count++;
						songNames.append(song.name);
						if (count < songsList.size()) {
								songNames.append(", ");
							}

						if (count % 5 == 0 && count < songsList.size()) {
								songNames.append("\n");
							}
					}

				String songListStr = songNames.toString();
				int totalSongs = songsList.size();

				String[] introResponses = {"Tienes " + totalSongs + " canciones guardadas. Los nombres son: ", "Lista de canciones: ", "Tus canciones son: "};
				tts.speak(introResponses[random.nextInt(introResponses.length)] + songListStr);
			}

		private void handleUnknownCommand(String command) {
				String[] unknownResponses = {"Lo siento, no entendí el comando: " + command + ". Prueba con 'guía' para ayuda.", "Comando desconocido: " + command + ". Di 'guía' para más info.", "No capté eso: " + command + ". Usa 'guía'."};
				tts.speak(unknownResponses[random.nextInt(unknownResponses.length)]);
			}

		private double evaluateExpression(String expression) {
				try {
						expression = expression.replaceAll("\\s+", "");
						while (expression.contains("sqrt")) {
								Pattern p = Pattern.compile("sqrt\\((\\d+(\\.\\d+)?)\\)");
								Matcher m = p.matcher(expression);
								if (m.find()) {
										double val = Math.sqrt(Double.parseDouble(m.group(1)));
										expression = expression.replace(m.group(0), Double.toString(val));
									} else {
										throw new Exception("Sqrt inválido");
									}
							}
						Stack<Double> values = new Stack<Double>();
						Stack<Character> ops = new Stack<Character>();
						for (int i = 0; i < expression.length(); i++) {
								char c = expression.charAt(i);
								if (c == ' ') continue;
								if (Character.isDigit(c) || c == '.') {
										StringBuffer buf = new StringBuffer();
										while (i < expression.length() && (Character.isDigit(expression.charAt(i)) || expression.charAt(i) == '.')) {
												buf.append(expression.charAt(i++));
											}
										values.push(Double.parseDouble(buf.toString()));
										i--;
									} else if (c == '(') {
										ops.push(c);
									} else if (c == ')') {
										while (ops.peek() != '(') {
												values.push(applyOp(ops.pop(), values.pop(), values.pop()));
											}
										ops.pop();
									} else if (c == '+' || c == '-' || c == '*' || c == '/' || c == '^') {
										while (!ops.empty() && hasPrecedence(c, ops.peek())) {
												values.push(applyOp(ops.pop(), values.pop(), values.pop()));
											}
										ops.push(c);
									}
							}
						while (!ops.empty()) {
								values.push(applyOp(ops.pop(), values.pop(), values.pop()));
							}
						return values.pop();
					} catch (Exception e) {
						throw new RuntimeException("Expresión inválida");
					}
			}

		private static boolean hasPrecedence(char op1, char op2) {
				if (op2 == '(' || op2 == ')') return false;
				if ((op1 == '*' || op1 == '/') && (op2 == '+' || op2 == '-')) return false;
				if (op1 == '^' && (op2 == '+' || op2 == '-' || op2 == '*' || op2 == '/')) return false;
				return true;
			}

		private static double applyOp(char op, double b, double a) {
				switch (op) {
						case '+': return a + b;
						case '-': return a - b;
						case '*': return a * b;
						case '/': if (b == 0) throw new UnsupportedOperationException("División por cero"); return a / b;
						case '^': return Math.pow(a, b);
					}
				return 0;
			}

		private static class AppInfo {
				String name;
				String packageName;
				String description;

				AppInfo(String name, String packageName, String description) {
						this.name = name;
						this.packageName = packageName;
						this.description = description;
					}
			}

		private static class AppMatch {
				AppInfo appInfo;
				int similarity;

				AppMatch(AppInfo appInfo, int similarity) {
						this.appInfo = appInfo;
						this.similarity = similarity;
					}
			}

		private static class ContactInfo {
				String name;
				String number;

				ContactInfo(String name, String number) {
						this.name = name;
						this.number = number;
					}
			}

		private static class ContactMatch {
				ContactInfo contactInfo;
				int similarity;

				ContactMatch(ContactInfo contactInfo, int similarity) {
						this.contactInfo = contactInfo;
						this.similarity = similarity;
					}
			}

		private static class SongInfo {
				String name;
				String path;

				SongInfo(String name, String path) {
						this.name = name;
						this.path = path;
					}
			}

		private static class SongMatch {
				SongInfo songInfo;
				int similarity;

				SongMatch(SongInfo songInfo, int similarity) {
						this.songInfo = songInfo;
						this.similarity = similarity;
					}
			}
	}
