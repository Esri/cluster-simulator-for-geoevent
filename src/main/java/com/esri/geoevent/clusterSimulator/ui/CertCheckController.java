package com.esri.geoevent.clusterSimulator.ui;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class CertCheckController implements Initializable
{
	boolean	allowConnection	= false;

	@FXML
	Button	okButton;

	@FXML
	Text		certText;

	private void closeWindow()
	{
		Stage stage = (Stage) okButton.getScene().getWindow();
		stage.close();
	}

	@FXML
	private void onCancelButtonPushed(ActionEvent event)
	{
		closeWindow();
	}

	@FXML
	private void onOKButtonPushed(ActionEvent event)
	{
		allowConnection = true;
		closeWindow();
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1)
	{
	}

}
