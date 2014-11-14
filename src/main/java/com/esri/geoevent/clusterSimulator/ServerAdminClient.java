/*
  Copyright 1995-2014 Esri

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

  For additional information, contact:
  Environmental Systems Research Institute, Inc.
  Attn: Contracts Dept
  380 New York Street
  Redlands, California, USA 92373

  email: contracts@esri.com
 */
package com.esri.geoevent.clusterSimulator;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

public class ServerAdminClient
{
	private String															hostname;
	private String															token;
	private long																expiration;
	private String															username;
	private String															password;
	private ConcurrentHashMap<String, Machine>	machines	= new ConcurrentHashMap<>();
	private Timer																timer;
	private Simulator														simulator;
	private EventHandler<ActionEvent>						clusterListener;
	private boolean															connected;
	private CertificateChecker									certChecker;

	public ServerAdminClient(String hostname, String username, String password, Simulator simulator, CertificateChecker certChecker) throws Exception
	{
		this.username = username;
		this.password = password;
		InetAddress addr = InetAddress.getByName(hostname);
		this.hostname = addr.getCanonicalHostName();
		this.simulator = simulator;
		this.certChecker = certChecker;
		refreshToken();
		timer = new Timer();
		timer.scheduleAtFixedRate(new MachineListUpdateTask(certChecker), 0, 1000);
		connected = true;

	}

	public ConcurrentHashMap<String, Machine> getMachines()
	{
		return machines;
	}
	
	private CloseableHttpClient createHttpClient()
	{
		return HttpClients.custom().setSSLSocketFactory(getSSLSocketFactory(certChecker)).build();
	}

	private String getTokenURL(CloseableHttpClient httpClient, String hostname) throws Exception
	{
		HttpUriRequest getRequest = RequestBuilder.get().setUri(new URI("http://" + hostname + ":6080/arcgis/rest/info")).addParameter("f", "json").build();

		CloseableHttpResponse response;
		try
		{
			response = httpClient.execute(getRequest);
			ObjectMapper mapper = new ObjectMapper();
			JsonNode infoObject = mapper.readTree(response.getEntity().getContent());
			final String authInfoAttribute = "authInfo";
			if (infoObject.has(authInfoAttribute))
			{
				JsonNode authInfoObject = infoObject.get(authInfoAttribute);
				final String tokenUrlAttribute = "tokenServicesUrl";
				if (authInfoObject.has(tokenUrlAttribute))
				{
					return authInfoObject.get(tokenUrlAttribute).asText();
				}
			}
		}
		catch (IOException ioe)
		{
			throw ioe;
		}
		throw new Exception("Could not find token URL.");
	}

	private void refreshToken() throws IOException
	{
		long now = System.currentTimeMillis();
		if (now < expiration)
			return;

		
		

		try(CloseableHttpClient httpClient = createHttpClient();)
		{
			String serverTokenUrl = getTokenURL(httpClient, hostname);
			HttpUriRequest postRequest = RequestBuilder.post().setUri(new URI(serverTokenUrl)).addParameter("username", username).addParameter("password", password).addParameter("client", "requestip").addParameter("f", "json").build();
			HttpResponse tokenGenerationResponse = httpClient.execute(postRequest);
			ObjectMapper mapper = new ObjectMapper();
			JsonNode schema = mapper.readTree(tokenGenerationResponse.getEntity().getContent());

			if (!schema.has("token"))
				throw new IOException("No token granted.");
			token = schema.get("token").asText();
			expiration = schema.get("expires").asLong();
		}
		catch (Exception e)
		{
			throw new IOException(e.getMessage());
		}		
	}

	class MachineListUpdateTask extends TimerTask
	{
		private CertificateChecker	certChecker;

		MachineListUpdateTask(CertificateChecker certChecker)
		{
			this.certChecker = certChecker;
		}

		public void run()
		{
			try
			{
				refreshToken();
				// String url = "https://"+hostname+":6143/geoevent/admin/clusters/mine/.json";
				String url = "http://" + hostname + ":6080/arcgis/admin/clusters/default/machines";
				CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(getSSLSocketFactory(certChecker)).build();
				HttpUriRequest request = RequestBuilder.get().setUri(url).addParameter("token", token).addParameter("f", "json").build();
				try
				{

					HttpResponse response = httpClient.execute(request);
					HttpEntity entity = response.getEntity();
					ObjectMapper mapper = new ObjectMapper();
					JsonNode schema = mapper.readTree(entity.getContent());

					if (!schema.has("machines"))
						throw new IOException("No machines available for this service.");
					JsonNode machineList = schema.get("machines");
					for (JsonNode machine : machineList)
					{
						String machineName = machine.get("machineName").asText();
						String realTimeState = machine.get("realTimeState").asText();
						if (machines.containsKey(machineName))
						{
							Machine currentMachine = machines.get(machineName);
							if (!currentMachine.getRealTimeState().equals(realTimeState))
							{
								currentMachine.setRealTimeState(realTimeState);
							}
						}
						else
						{
							System.out.println("Connected to " + machineName);
							machines.put(machineName, new Machine(machineName, realTimeState, simulator));
							clusterListener.handle(new ActionEvent());
						}
					}
				}
				catch (UnsupportedEncodingException e)
				{
					throw new IOException(e.getMessage());
				}
				catch (ClientProtocolException e)
				{
					throw new IOException(e.getMessage());
				}
				finally
				{
					try
					{
						httpClient.close();
					}
					catch (Throwable t)
					{
						throw new IOException(t.getMessage());
					}
				}
			}
			catch (Exception ex)
			{
				System.err.println("Error while updating the cluster's machine list. : " + ex.getMessage());
			}
		}
	}

	private SSLConnectionSocketFactory getSSLSocketFactory(final CertificateChecker certChecker)
	{
		KeyStore trustStore;
		try
		{
			trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			trustStore.load(null, null);
			TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			trustManagerFactory.init((KeyStore) null);
			X509TrustManager x509TrustManager = null;
			for (TrustManager trustManager : trustManagerFactory.getTrustManagers())
			{
				if (trustManager instanceof X509TrustManager)
				{
					x509TrustManager = (X509TrustManager) trustManager;
					break;
				}
			}

			X509Certificate[] acceptedIssuers = x509TrustManager.getAcceptedIssuers();
			if (acceptedIssuers != null)
			{
				// If this is null, something is really wrong...
				int issuerNum = 1;
				for (X509Certificate cert : acceptedIssuers)
				{
					trustStore.setCertificateEntry("issuer" + issuerNum, cert);
					issuerNum++;
				}
			}
			TrustStrategy trustStrategy = new TrustStrategy()
				{
					@Override
					public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException
					{
						if (certChecker == null)
							return false;

						return certChecker.allowConnection(chain);
					}

				};

			SSLContextBuilder sslContextBuilder = new SSLContextBuilder();
			sslContextBuilder.loadTrustMaterial(trustStore, trustStrategy);
			sslContextBuilder.useTLS();
			SSLContext sslContext = sslContextBuilder.build();
			SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext);
			return sslSocketFactory;
		}
		catch (GeneralSecurityException | IOException e)
		{
			System.err.println("SSL Error : " + e.getMessage());
		}
		return null;
	}

	public void setClusterListener(EventHandler<ActionEvent> actionListener)
	{
		clusterListener = actionListener;
	}

	public boolean isConnected()
	{
		return connected;
	}

	public void disconnect()
	{
		connected = false;
		timer.cancel();
		for (Machine machine : machines.values())
		{
			machine.disconnect();
		}
	}
}
