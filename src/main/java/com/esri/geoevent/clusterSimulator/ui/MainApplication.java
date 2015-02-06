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

import java.io.IOException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;


public class MainApplication extends Application
{
	//Controller		controller;
	static Stage	primaryStage;
	
	static class AppCloser implements EventHandler<WindowEvent>
	{

		@Override
		public void handle(WindowEvent event)
		{
			Platform.exit();
			System.exit(0);
		}
		
	}

	@Override
	public void start(Stage primaryStage)
	{

		Platform.setImplicitExit(true);
		try
		{
			FXMLLoader loader = new FXMLLoader(getClass().getResource("SimulatorDisplay.fxml"));
			Parent parent = (Parent) loader.load();
			Scene scene = new Scene(parent);

			primaryStage.setOnCloseRequest(new AppCloser());
			primaryStage.setTitle("GeoEvent Simulator Mk.2");
			primaryStage.setScene(scene);
			primaryStage.show();
			MainApplication.primaryStage = primaryStage;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}

	public static void main(String[] args)
	{
		launch(args);
	}
}
