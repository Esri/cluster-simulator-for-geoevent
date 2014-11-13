package com.esri.geoevent.clusterSimulator;

import java.security.cert.X509Certificate;

public class AcceptAlwaysCertChecker implements CertificateChecker
{

	@Override
	public boolean allowConnection(X509Certificate[] chain)
	{
		return true;
	}

}
