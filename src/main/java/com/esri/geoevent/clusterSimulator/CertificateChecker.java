package com.esri.geoevent.clusterSimulator;

import java.security.cert.X509Certificate;

public interface CertificateChecker
{
	public boolean allowConnection(X509Certificate[] chain); 
}
