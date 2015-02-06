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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Observer;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import au.com.bytecode.opencsv.CSVParser;

public class FilePreviewController
{
	
	@FXML
	TextField									filenameField;
	@FXML
	Button										openButton;
	@FXML
	TextArea									previewEvents;
	@FXML
	CheckBox									skipCheckbox;
	@FXML
	TextField									skipLineTextField;
	@FXML
	TextField									timeFieldTextField;
	@FXML
	TextField									timeFieldPreview;
	@FXML
	Label										previewDescriptionLabel;
	
	public URL location;
	public ResourceBundle resources;
	private ArrayList<String> fileContent = null;
	private File file;	
	private FileChooser fileChooser	= new FileChooser();
	private Observer observer;

	@FXML
	private void onSimulationFileChooserButtonClicked(ActionEvent event)
	{ 
		Node node = (Node) event.getSource();
		file = fileChooser.showOpenDialog(node.getScene().getWindow());
		loadSimulationFile( file );
		updatePreview();
	}

	@FXML
	private void onLoad(ActionEvent event)
	{ 
		Stage stage = (Stage) openButton.getScene().getWindow();
		stage.close();
		observer.update(null, this);
	}
	
	@FXML
	private void onCancel(ActionEvent event)
	{ 
		file = null;
		Stage stage = (Stage) openButton.getScene().getWindow();
		stage.close();
	}
	
	private void loadSimulationFile( File file )
	{
		if( file == null )
			return;
		try ( BufferedReader in = new BufferedReader( new FileReader( file )) )
		{
			String line = null;
			fileContent = new ArrayList<>();
			
			while( (line = in.readLine()) != null )
				fileContent.add(line);
			
			filenameField.setText(file.getPath());
		} catch ( IOException e) 
		{
			fileContent = null;
			showDialog("File Not Found : "+file.getName()+".");
		}
	}
	
	private void updatePreview()
	{
		if( fileContent == null )
			return;
		int linesInPreview = 0;
		int skipCount = getSkipLines();
		String fileContentPreview = "";
		for( int lineNumber = skipCount; lineNumber < 100 + skipCount && lineNumber < fileContent.size() ; lineNumber++ )
		{
			fileContentPreview += (fileContent.get(lineNumber) + "\n");
			linesInPreview++;
		}
		previewEvents.setText(fileContentPreview);
		previewDescriptionLabel.setText( "Preview Events ("+(fileContent.size()-skipCount)+") showing first "+linesInPreview );
		
		timeFieldPreview.setText("");
		CSVParser parser = new CSVParser();
		int timeFieldPosition = getTimeField();
		if( fileContent.size() > skipCount && timeFieldPosition > 0 )
		{
			try
			{
				String[] fields = parser.parseLine(fileContent.get(skipCount));
				if( timeFieldPosition <= fields.length )
					timeFieldPreview.setText(fields[timeFieldPosition-1]);
			}catch( IOException ex ){}
		}
	}

	private void showDialog(String string)
	{
		System.err.println(string);
	}

	public void initialize()
	{
		previewEvents.setText("");
		skipCheckbox.setSelected(false);
		
		ChangeListener<Boolean> booleanListener = new ChangeListener<Boolean>()
		{
			@Override
			public void changed( ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) 
			{
				skipLineTextField.disableProperty().set( ! newValue.booleanValue() );
				updatePreview();
			}
		};
		skipCheckbox.selectedProperty().addListener(booleanListener);

		ChangeListener<String> stringListener = new ChangeListener<String>()
		{
			@Override
			public void changed( ObservableValue<? extends String> observable, String oldValue, String newValue) 
			{
				updatePreview();
			}
		};
		skipLineTextField.textProperty().addListener(stringListener);
		timeFieldTextField.textProperty().addListener(stringListener);
		
	}

	public File getFile()
	{
		return file;
	}
	
	public int getSkipLines()
	{
		if( skipCheckbox.isSelected() )
		{
			try
			{
				int skipLines = Integer.parseInt(skipLineTextField.getText());
				return Math.max( 0, skipLines );
			}catch(NumberFormatException ex)
			{
			}
		}
		return 0;
	}
	
	public int getTimeField()
	{
		try
		{
			int timeField = Integer.parseInt(timeFieldTextField.getText());
			return Math.max( 0, timeField );
		}catch(NumberFormatException ex)
		{
		}
		return 0;
	}

	public void setFileChooser(FileChooser fileChooser, String filename) 
	{
		this.fileChooser = fileChooser;
		filenameField.setText(filename);
	}

	public void setObserver(Observer observer)
	{
		this.observer = observer;
	}

	public void setSkipLines(int newValue)
	{
		skipCheckbox.setSelected(newValue > 0);
		skipLineTextField.textProperty().set(""+newValue);
	}

	public void setTimefield(int newValue) 
	{
		timeFieldTextField.textProperty().set(""+newValue);
	}
}
