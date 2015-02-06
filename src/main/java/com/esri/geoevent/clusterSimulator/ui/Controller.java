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
package com.esri.geoevent.clusterSimulator.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import com.esri.geoevent.clusterSimulator.AcceptAlwaysCertChecker;
import com.esri.geoevent.clusterSimulator.CertificateChecker;
import com.esri.geoevent.clusterSimulator.Machine;
import com.esri.geoevent.clusterSimulator.ServerAdminClient;
import com.esri.geoevent.clusterSimulator.simulator.DefaultSimulator;
import com.esri.geoevent.clusterSimulator.simulator.Simulator;

public class Controller implements Initializable
{
	private static final String LAST_DIRECTORY = "LAST_DIRECTORY";
	private static final String LAST_FILENAME = "LAST_FILENAME";
	private static final String HOST_NAME = "HOST_NAME";
	private static final String USER_NAME = "USER_NAME";
	private static final String TRUST_ALL = "TRUST_ALL";
	private static final String PORT = "SERVER_PORT";
	private static final String SKIP_LINES = "SKIP_LINES";
	private static final String TIME_FIELD = "TIME_FIELD";


	@FXML
	TextField									hostnameField;
	@FXML
	TextField									portField;
	@FXML
	TextField									usernameField;
	@FXML
	TextField									passwordField;
	@FXML
	Button										connectButton;
	@FXML
	Label										clusterLabel;
	@FXML
	TextField									filenameField;
	@FXML
	TextField									eventCountField;
	@FXML
	TextField									messageRateField;
	@FXML
	CheckBox									continuousLoopCheckbox;
	@FXML
	CheckBox									trustAllSSLCertificates;
	@FXML
	Button										playButton;
	@FXML
	Button										stepButton;
	@FXML
	Label										totalEventsLabel;
	@FXML
	Label										activeSimulationFileLabel;
	@FXML
	Label										statusLabel;
	@FXML
	Label										sentEventsLabel;
	@FXML
	TextField									realRateField;
	@FXML
	ToggleGroup									playModeGroup;
	@FXML
	Label										statusMessageField;

	private Simulator					simulator;
	private ServerAdminClient	serverAdminClient;
	private Timeline					statisticsUpdater;
	private FileChooser				fileChooser	= new FileChooser();
	private String propertiesFilePath = System.getProperty("user.home") + File.separatorChar + ".geoevent-simulator"+ File.separatorChar + "user-interface.properties";
	private Properties preferences;
	private Stage fileLoaderStage;

	@FXML
	private void onConnectClicked(ActionEvent event)
	{
		if (serverAdminClient != null && serverAdminClient.isConnected())
		{
			serverAdminClient.disconnect();
			statusLabel.textProperty().setValue("Disconnected");
			statusLabel.setStyle("-fx-background-color: #ff7373;");
			connectButton.textProperty().setValue("Connect");
			if( simulator != null && simulator.isRunning() )
				this.onPlayPauseClicked(event);
		}
		else
		{
			try
			{
				CertificateChecker certChecker = (trustAllSSLCertificates.isSelected()) ? new AcceptAlwaysCertChecker() : new CertificateCheckerDialog();
				serverAdminClient = new ServerAdminClient(hostnameField.textProperty().getValue(), usernameField.textProperty().getValue(), passwordField.textProperty().getValue(), simulator, certChecker);
				preferences.setProperty( HOST_NAME, hostnameField.textProperty().getValue() );
				preferences.setProperty( PORT, portField.textProperty().getValue() );
				preferences.setProperty( USER_NAME, usernameField.textProperty().getValue() );
				preferences.setProperty( TRUST_ALL, Boolean.toString(trustAllSSLCertificates.isSelected()) );
				savePreferences();
				status("Connected to " + hostnameField.textProperty().get() );
				statusLabel.textProperty().setValue("Connected");
				statusLabel.setStyle("-fx-background-color: #ffffff;");
				connectButton.textProperty().setValue("Disconnect");
				serverAdminClient.setClusterListener(new EventHandler<ActionEvent>()
						{

					@Override
					public void handle(ActionEvent e)
					{
						Platform.runLater(new Runnable()
						{

							@Override
							public void run()
							{
								ConcurrentHashMap<String, Machine> machineList = serverAdminClient.getMachines();
								String statusString = "Cluster of " + machineList.size() + " : ";
								for (String machineName : machineList.keySet())
								{
									statusString += machineName;
									statusString += ", ";
								}
								statusString = statusString.substring(0, statusString.length() - 2);
								clusterLabel.textProperty().setValue(statusString);
							}
						});

					}
						});

				statisticsUpdater = new Timeline(new KeyFrame(Duration.millis(100), new StatisticsUpdater()));
				statisticsUpdater.setCycleCount(Timeline.INDEFINITE);
				statisticsUpdater.play();

			}
			catch (Exception ex)
			{
				status("Could not connect to " + hostnameField.textProperty().get());
			}
		}
	}
	
	private void status( String text )
	{
		this.statusMessageField.textProperty().set(text);
	}

	private class StatisticsUpdater implements EventHandler<ActionEvent>
	{
		public void handle(ActionEvent evt)
		{
			if (simulator.isReady())
			{
				sentEventsLabel.textProperty().setValue("Sent : " + simulator.getSentEventCount());
			}
		}
	}

	@FXML
	private void onSimulationFileTextFieldEdited(ActionEvent event)
	{
		loadSimulationFile(new File(filenameField.textProperty().getValue()), Integer.parseInt(preferences.getProperty(SKIP_LINES, "0")), Integer.parseInt(preferences.getProperty( TIME_FIELD, "0")) );
	}

	@FXML
	private void onFixedRateSelected(ActionEvent event)
	{
		messageRateField.setEditable(true);
		messageRateField.setDisable(false);
		eventCountField.setEditable(true);
		eventCountField.setDisable(false);
		realRateField.setEditable(false);
		realRateField.setDisable(true);
		updateFixedRate();
	}

	@FXML
	private void onRealRateSelected(ActionEvent event)
	{
		messageRateField.setEditable(false);
		messageRateField.setDisable(true);
		eventCountField.setEditable(false);
		eventCountField.setDisable(true);
		realRateField.setEditable(true);
		realRateField.setDisable(false);
		updateRealRate();
	}

	@FXML
	private void onSimulationFileChooserButtonClicked(ActionEvent event)
	{
		try {
			if( fileLoaderStage == null )
			{
				FXMLLoader loader = new FXMLLoader(getClass().getResource("FilePreviewWindow.fxml"));
				Parent parent = (Parent) loader.load();
				FilePreviewController fpc = (FilePreviewController) loader.getController();
				fpc.setFileChooser(fileChooser, filenameField.getText());
				fpc.setSkipLines( Integer.parseInt(preferences.getProperty(SKIP_LINES, "0")) );
				fpc.setTimefield( Integer.parseInt(preferences.getProperty( TIME_FIELD, "0")) );
				fpc.setObserver( new Observer()
				{
					public void update(Observable o, Object arg)
					{
						FilePreviewController fpc = (FilePreviewController) arg;
						File file = fpc.getFile();
						int skipLines = fpc.getSkipLines();
						int timeField = fpc.getTimeField();
						loadSimulationFile( file, skipLines, timeField );
					}
				});
				fileLoaderStage = new Stage();
				fileLoaderStage.setScene(new Scene(parent));
				fileLoaderStage.setTitle("Load from File");
				fileLoaderStage.initOwner(MainApplication.primaryStage);
				fileLoaderStage.initModality(Modality.WINDOW_MODAL);
			}
			fileLoaderStage.show();
		} catch (Exception ex) {
			System.out.println("Exception foundeth in showMessageBox");
			ex.printStackTrace();
		}

		//Node node = (Node) event.getSource();
		//File file = fileChooser.showOpenDialog(node.getScene().getWindow());
		//loadSimulationFile( file );
	}

	private void loadSimulationFile( File file, int skipLines, int timeField )
	{
		try
		{
			if (file != null && file.exists())
			{
				if( simulator.isRunning() )
					simulator.stop();
				simulator.setSimulationFile(file, skipLines, timeField);

				preferences.setProperty(LAST_DIRECTORY, file.getParent() );
				preferences.setProperty(LAST_FILENAME, file.getName());
				preferences.setProperty(SKIP_LINES,  ""+skipLines );
				preferences.setProperty(TIME_FIELD, ""+timeField );
				savePreferences();

				playButton.disableProperty().setValue(false);
				playButton.textProperty().setValue(">");
				stepButton.disableProperty().setValue(false);
				totalEventsLabel.textProperty().setValue("Total Events : " + simulator.getTotalEventCount());
				activeSimulationFileLabel.textProperty().setValue(simulator.getFilename());
				status("Successfully loaded file "+simulator.getFilename());
				try
				{
					filenameField.textProperty().setValue(file.getCanonicalPath());
				}
				catch (IOException ex)
				{

				}
			}
		}
		catch (IOException e)
		{
			status("Failed to load file "+file.getName());
		}
	}


	@FXML
	private void onRewindClicked(ActionEvent event)
	{
		simulator.rewind();
		playButton.disableProperty().setValue(false);
		playButton.textProperty().setValue(">");
		stepButton.disableProperty().setValue(false);

	}

	@FXML
	private void onPlayPauseClicked(ActionEvent event)
	{
		if (simulator.isRunning())
		{
			simulator.stop();
			playButton.textProperty().setValue(">");
			status("Stopped");
		}
		else if (simulator.isReady())
		{
			simulator.start();
			playButton.textProperty().setValue("||");
			status("Running");
		}
	}

	@FXML
	private void onStepClicked(ActionEvent event)
	{
		simulator.step();
	}

	@FXML
	private void onContinuousLoopClicked(ActionEvent event)
	{
		simulator.setLooping(continuousLoopCheckbox.isSelected());
	}

	@FXML
	private void onEventCountChanged(KeyEvent event)
	{
		updateFixedRate();
	}

	@FXML
	private void onMessageRateChanged(KeyEvent event)
	{
		updateFixedRate();
	}

	@FXML
	private void onResetCounter(ActionEvent event)
	{
		simulator.resetCounter();
	}

	private void updateFixedRate()
	{
		int batchSize = 0;
		int period = 0;
		try
		{
			batchSize = Integer.parseInt(eventCountField.textProperty().getValue());
			if( batchSize <= 0 )
				throw new NumberFormatException();
			eventCountField.setStyle("-fx-base: #ffffff;");
			
		}
		catch (NumberFormatException ex)
		{
			eventCountField.setStyle("-fx-base: #e45e4f;");
			return;
		}
		try
		{
			period = Integer.parseInt(messageRateField.textProperty().getValue());
			if( period <= 0 )
				throw new NumberFormatException();
			messageRateField.setStyle("-fx-base: #ffffff;");
			
		}
		catch (NumberFormatException ex)
		{
			messageRateField.setStyle("-fx-base: #e45e4f;");
			return;
		}
		simulator.setFixedRate(batchSize, period);
	}

	private void updateRealRate()
	{

		try
		{
			float percent = Float.parseFloat(realRateField.textProperty().get());
			if( percent <= 0 )
				throw new NumberFormatException();
			realRateField.setStyle("-fx-base: #ffffff;");
			simulator.setRealRate(percent);
		}
		catch (NumberFormatException ex)
		{
			realRateField.setStyle("-fx-base: #e45e4f;");
		}
	}

	@Override
	public void initialize(URL url, ResourceBundle res)
	{
		simulator = new DefaultSimulator();
		simulator.addEndOfSimulationListener(new Observer(){
			@Override
			public void update(Observable o, Object arg) {
				endOfSimulationReached();
			}});

		// Load the properties from a file if the properties file exists.
		preferences = new Properties();
		try {
			File propertiesFile = new File( propertiesFilePath );
			FileInputStream in;
			in = new FileInputStream(propertiesFile);
			preferences.load(in);
		} catch (IOException e) {			
		}
		
		statusLabel.setStyle("-fx-background-color: #ff7373;");

		hostnameField.textProperty().setValue(preferences.getProperty(HOST_NAME, hostnameField.textProperty().getValue()));
		portField.textProperty().setValue(preferences.getProperty(PORT, portField.textProperty().getValue()));
		usernameField.textProperty().setValue(preferences.getProperty(USER_NAME, usernameField.textProperty().getValue()));
		passwordField.textProperty().setValue(usernameField.textProperty().getValue());
		trustAllSSLCertificates.setSelected( Boolean.parseBoolean(preferences.getProperty(TRUST_ALL, Boolean.toString(trustAllSSLCertificates.isSelected()))));		

		ObservableList<ExtensionFilter> filterList = fileChooser.getExtensionFilters();
		filterList.add(new ExtensionFilter("CSV files (*.csv)", "*.csv"));

		String directory = preferences.getProperty(LAST_DIRECTORY, System.getProperty("user.dir"));
		fileChooser.setInitialDirectory(new File(directory));

		String filename = preferences.getProperty(LAST_FILENAME, "");
		fileChooser.initialFileNameProperty().set(filename);
		fileChooser.setInitialFileName(filename);
		File lastFile = new File( directory + File.separatorChar + filename );
		if( lastFile.exists() && !lastFile.isDirectory())
			filenameField.textProperty().setValue(directory + File.separatorChar + filename);
		else filenameField.textProperty().setValue("");

		// Now initialize the simulator
		File file = new File(filenameField.textProperty().getValue());
		if (file.exists() && ! file.isDirectory() )
			loadSimulationFile(file, Integer.parseInt(preferences.getProperty(SKIP_LINES, "0")), Integer.parseInt(preferences.getProperty( TIME_FIELD, "0")) );

		ChangeListener<String> fixedRatePropertylistener = new ChangeListener<String>(){
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				updateFixedRate();
			}};
		eventCountField.textProperty().addListener(fixedRatePropertylistener);
		messageRateField.textProperty().addListener(fixedRatePropertylistener);
		
		ChangeListener<String> realRatePropertyListener = new ChangeListener<String>(){
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				updateRealRate();
			}};
		realRateField.textProperty().addListener(realRatePropertyListener);
		updateFixedRate();
		
		simulator.setLooping(continuousLoopCheckbox.isSelected());

		playButton.setTooltip(new Tooltip("Play/Pause the simulation file."));
		stepButton.setTooltip(new Tooltip("Send the next event in the file."));

	}

	public void endOfSimulationReached()
	{
		Platform.runLater(new Runnable()
		{
			public void run()
			{
				playButton.disableProperty().setValue(true);
				stepButton.disableProperty().setValue(true);					
			}
		});
	}

	private void savePreferences() 
	{
		File propertiesFile = new File( propertiesFilePath );
		if( !propertiesFile.exists() )
			propertiesFile.getParentFile().mkdirs();
		try {
			FileOutputStream out = new FileOutputStream(propertiesFile);
			preferences.store(out, "Properties used to configure the Simulator user interface the way it was the last time it was run by a user");
		} catch (IOException e) {

		}

	}
}
