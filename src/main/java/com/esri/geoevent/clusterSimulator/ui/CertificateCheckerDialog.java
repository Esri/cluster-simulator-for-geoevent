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

import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.ArrayBlockingQueue;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import org.apache.commons.codec.binary.Base64;

import com.esri.geoevent.clusterSimulator.CertificateChecker;

public class CertificateCheckerDialog implements CertificateChecker
{
	static private Collection<X509Certificate>	trustedCerts;

	static
	{
		PriorityQueue<X509Certificate> certsQueue = new PriorityQueue<>(5, new Comparator<X509Certificate>()
			{

				@Override
				public int compare(X509Certificate o1, X509Certificate o2)
				{
					try
					{
						String c1B64 = Base64.encodeBase64String(o1.getEncoded());
						String c2B64 = Base64.encodeBase64String(o2.getEncoded());
						return c1B64.compareTo(c2B64);
					}
					catch (Exception e)
					{
						if (o1.equals(o2))
						{
							return 0;
						}
						return 1;
					}

				}

			});
		trustedCerts = Collections.synchronizedCollection(certsQueue);
	}

	@Override
	public boolean allowConnection(final X509Certificate[] chain)
	{
		if (trustedCerts.contains(chain[0]))
		{
			return true;
		}
		final ArrayBlockingQueue<Boolean> responseQueue = new ArrayBlockingQueue<Boolean>(1);
		Runnable runnable = new Runnable()
		{

			@Override
			public void run()
			{
				try
				{
					final Stage dialog = new Stage();
					dialog.initModality(Modality.APPLICATION_MODAL);
					dialog.initOwner(MainApplication.primaryStage);
					dialog.setTitle("Certificate Check");
					FXMLLoader loader = new FXMLLoader(getClass().getResource("CertificateCheckerDialog.fxml"));
					Parent parent = (Parent) loader.load();
					CertCheckController controller = (CertCheckController) loader.getController();
					controller.certText.setText(chain[0].toString());
					Scene scene = new Scene(parent);
					dialog.setScene(scene);
					dialog.showAndWait();
					responseQueue.put(Boolean.valueOf(controller.allowConnection));
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}

		};
		if (Platform.isFxApplicationThread())
		{
			runnable.run();
		}
		else
		{
			Platform.runLater(runnable);
		}
		
		try
		{
			boolean retVal = responseQueue.take();
			if (retVal)
			{
				trustedCerts.add(chain[0]);
			}
			return retVal;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}

}
