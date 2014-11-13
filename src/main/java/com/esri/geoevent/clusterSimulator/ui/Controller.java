package com.esri.geoevent.clusterSimulator.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import com.esri.geoevent.clusterSimulator.AcceptAlwaysCertChecker;
import com.esri.geoevent.clusterSimulator.CertificateChecker;
import com.esri.geoevent.clusterSimulator.EndOfFileException;
import com.esri.geoevent.clusterSimulator.Machine;
import com.esri.geoevent.clusterSimulator.ServerAdminClient;
import com.esri.geoevent.clusterSimulator.Simulator;

public class Controller implements Initializable
{
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
	Label											clusterLabel;
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
	Label											totalEventsLabel;
	@FXML
	Label											activeSimulationFileLabel;
	@FXML
	Label											statusLabel;
	@FXML
	Label											sentEventsLabel;
	@FXML
	Rectangle									connectionIndicator;

	private Simulator					simulator;
	private ServerAdminClient	serverAdminClient;
	private Timeline					statisticsUpdater;
	private FileChooser				fileChooser	= new FileChooser();

	@FXML
	private void onConnectClicked(ActionEvent event)
	{
		if (serverAdminClient != null && serverAdminClient.isConnected())
		{
			serverAdminClient.disconnect();
			statusLabel.textProperty().setValue("Disconnected");
			connectButton.textProperty().setValue("Connect");
			connectionIndicator.setFill(Color.RED);
		}
		else
		{
			try
			{
				CertificateChecker certChecker = (trustAllSSLCertificates.isSelected()) ? new AcceptAlwaysCertChecker() : new CertificateCheckerDialog();
				serverAdminClient = new ServerAdminClient(hostnameField.textProperty().getValue(), usernameField.textProperty().getValue(), passwordField.textProperty().getValue(), simulator, certChecker);
				statusLabel.textProperty().setValue("Connected");
				connectButton.textProperty().setValue("Disconnect");
				connectionIndicator.setFill(Color.GREEN);
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
				System.err.println("IOException : " + ex.getMessage());
			}
		}
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
		String filename = filenameField.textProperty().getValue();
		File file = new File(filename);
		if (file.exists())
		{
			try
			{
				simulator.setSimulationFile(file);
				playButton.disableProperty().setValue(false);
				playButton.textProperty().setValue(">");
				stepButton.disableProperty().setValue(false);
				totalEventsLabel.textProperty().setValue("Total Events : " + simulator.getTotalEventCount());
				activeSimulationFileLabel.textProperty().setValue(simulator.getFilename());
			}
			catch (FileNotFoundException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@FXML
	private void onSimulationFileChooserButtonClicked(ActionEvent event)
	{
		try
		{
			Node node = (Node) event.getSource();
			File file = fileChooser.showOpenDialog(node.getScene().getWindow());
			if (file == null)
				return;
			if (file.exists())
			{
				simulator.setSimulationFile(file);
				playButton.disableProperty().setValue(false);
				playButton.textProperty().setValue(">");
				stepButton.disableProperty().setValue(false);
				totalEventsLabel.textProperty().setValue("Total Events : " + simulator.getTotalEventCount());
				activeSimulationFileLabel.textProperty().setValue(simulator.getFilename());
				try
				{
					filenameField.textProperty().setValue(file.getCanonicalPath());
				}
				catch (IOException ex)
				{

				}
			}
		}
		catch (FileNotFoundException e)
		{
			simulator.stop();
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
		try
		{
			if (simulator.isRunning())
			{
				simulator.stop();
				playButton.textProperty().setValue(">");
			}
			else if (simulator.isReady())
			{
				simulator.start();
				playButton.textProperty().setValue("||");
			}
		}
		catch (IOException ex)
		{

		}
	}

	@FXML
	private void onStepClicked(ActionEvent event)
	{
		try
		{
			simulator.step();
		}
		catch (EndOfFileException e)
		{
			playButton.disableProperty().setValue(true);
			stepButton.disableProperty().setValue(true);

		}
	}

	@FXML
	private void onContinuousLoopClicked(ActionEvent event)
	{
		simulator.setLoop(continuousLoopCheckbox.isSelected());
	}

	@FXML
	private void onEventCountChanged(KeyEvent event)
	{
		String valueString = eventCountField.textProperty().getValue();
		int eventCount = -1;
		try
		{
			eventCount = Integer.parseInt(valueString);
		}
		catch (NumberFormatException ex)
		{
			// TODO constrain value to integer.
			return;
		}
		simulator.setBatchSize(eventCount);

	}

	@FXML
	private void onMessageRateChanged(KeyEvent event)
	{
		String valueString = messageRateField.textProperty().getValue();
		int messageRate = -1;
		try
		{
			messageRate = Integer.parseInt(valueString);
		}
		catch (NumberFormatException ex)
		{
			// TODO constrain value to integer.
		}
		simulator.setPeriod(messageRate);
	}

	@Override
	public void initialize(URL url, ResourceBundle res)
	{
		simulator = new Simulator(this);
		try
		{
			File file = new File(filenameField.textProperty().getValue());
			if (file.exists())
				simulator.setSimulationFile(file);

			String batchSizeString = eventCountField.textProperty().getValue();
			simulator.setBatchSize(Integer.parseInt(batchSizeString));

			String messageRate = messageRateField.textProperty().getValue();
			simulator.setPeriod(Integer.parseInt(messageRate));

			simulator.setLoop(continuousLoopCheckbox.isSelected());

			simulator.setRoundRobinDataToAllClients(true);
		}
		catch (NumberFormatException | FileNotFoundException ex)
		{
			ex.printStackTrace();
		}

	}

	public void endOfSimulationReached()
	{
		playButton.disableProperty().setValue(true);
		stepButton.disableProperty().setValue(true);

	}

}
