/* Erik Wenkel
 * Weather Alarm App
 * 10/26/2013
 * 
 * Time Set Panel - Accressed with a location parameter and get the 
 * 					current weather at that location from weather.com/gov
 * 				  - Parses the html and writes the relevant data in json format.
 */

package edu.vt.ece4564.alarmserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CountDownLatch;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

@SuppressWarnings("serial")
public class AlarmServer extends HttpServlet {
	// Networking variables
	int startOfSpan;
	String subHtml;
	String line;
	String html;
	StringBuilder htmlBuilder = new StringBuilder();
	CountDownLatch latch = new CountDownLatch(2);
	String extractedData1 = "";
	String extractedData2 = "";
	String loc;

	public static void main(String[] args) throws Exception {
		// Creates the server
		Server server = new Server(8080);

		WebAppContext context = new WebAppContext();
		context.setWar("war");
		context.setContextPath("/");
		server.setHandler(context);

		server.start();
		server.join();
	}

	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		try {
			// Get the current zip code from the app
			loc = req.getParameter("location");

			if (loc != null) {
				// Weather.com Thread
				new Thread(new Runnable() {
					public void run() {
						try {
							// Setup connection to weather.com
							URL url1 = new URL(
									"http://www.weather.com/weather/today/"
											+ loc);
							HttpURLConnection connection1 = (HttpURLConnection) url1
									.openConnection();
							connection1.setRequestMethod("GET");
							BufferedReader readIn1 = new BufferedReader(
									new InputStreamReader(connection1
											.getInputStream()));

							// Create HTML document
							while ((line = readIn1.readLine()) != null) {
								htmlBuilder.append(line);
							}

							html = htmlBuilder.toString();
							readIn1.close();

							// Extract relevant data
							while ((startOfSpan = html.indexOf("It's ")) != -1) {
								subHtml = html.substring(startOfSpan
										+ ("It's ").length());
								extractedData1 = subHtml.substring(0,
										subHtml.indexOf("\">"));
								break;
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						latch.countDown();
					}
				}).start();

				// Weather.gov Thread
				new Thread(new Runnable() {
					public void run() {
						try {
							// Setup connection to weather.gov
							URL url2 = new URL("http://www.weather.gov/" + loc);
							HttpURLConnection connection2 = (HttpURLConnection) url2
									.openConnection();
							connection2.setRequestMethod("GET");
							BufferedReader readIn2 = new BufferedReader(
									new InputStreamReader(connection2
											.getInputStream()));

							// Create HTML document
							while ((line = readIn2.readLine()) != null) {
								htmlBuilder.append(line);
							}

							html = htmlBuilder.toString();
							readIn2.close();

							// Extract relevant data
							while ((startOfSpan = html
									.indexOf("\"myforecast-current\">")) != -1) {
								subHtml = html.substring(startOfSpan
										+ ("\"myforecast-current\">").length());
								extractedData2 = subHtml.substring(0,
										subHtml.indexOf("&deg"));
								break;
							}

						} catch (Exception e) {
							e.printStackTrace();
						}
						latch.countDown();
					}
				}).start();

				// Wait for threads to finish
				latch.await();

				// Extract only the weather and temperature
				String Temp1 = extractedData1.substring(0,
						extractedData1.indexOf("&"))
						+ " deg F";
				String Cond1 = extractedData1.substring(extractedData1
						.indexOf(" ") + 1);
				String Temp2 = extractedData2.substring(extractedData2
						.indexOf("\">") + 2) + " deg F";
				String Cond2 = extractedData2.substring(0,
						extractedData2.indexOf("<"));

				extractedData1 = "www.weather.com: " + Temp1 + ", " + Cond1;
				extractedData2 = "www.weather.gov: " + Temp2 + ", " + Cond2;

				// Set Json format
				resp.getWriter().write(
						"{\"extractedData1\":\"" + extractedData1 + "\",");
				resp.getWriter().write(
						"\"extractedData2\":\"" + extractedData2 + "\"}");
			} else {
				// If the url doesn't contain a location
				resp.getWriter().write("{\"Invalid\"=\"No Location\"}");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
