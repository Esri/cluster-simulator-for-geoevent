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
