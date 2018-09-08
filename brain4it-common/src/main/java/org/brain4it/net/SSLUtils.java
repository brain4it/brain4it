/*
 * Brain4it
 *
 * Copyright (C) 2018, Ajuntament de Sant Feliu de Llobregat
 *
 * This program is licensed and may be used, modified and redistributed under
 * the terms of the European Public License (EUPL), either version 1.1 or (at
 * your option) any later version as soon as they are approved by the European
 * Commission.
 *
 * Alternatively, you may redistribute and/or modify this program under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either  version 3 of the License, or (at your option)
 * any later version.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the licenses for the specific language governing permissions, limitations
 * and more details.
 *
 * You should have received a copy of the EUPL1.1 and the LGPLv3 licenses along
 * with this program; if not, you may find them at:
 *
 *   https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 *   http://www.gnu.org/licenses/
 *   and
 *   https://www.gnu.org/licenses/lgpl.txt
 */
package org.brain4it.net;

import java.net.HttpURLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Secure Socket Layer utilites
 * 
 * @author realor
 */
public class SSLUtils
{
  static SSLContext noValidationSSLContext;
  static SSLSocketFactory noValidationSSLSocketFactory;

  static TrustManager[] trustAllCerts = new TrustManager[]
  {
    new X509TrustManager()
    {
      @Override
      public java.security.cert.X509Certificate[] getAcceptedIssuers()
      {
        return null;
      }

      @Override
      public void checkClientTrusted(X509Certificate[] certs, String authType)
      {
      }

      @Override
      public void checkServerTrusted(X509Certificate[] certs, String authType)
      {
      }
    }
  };

  static HostnameVerifier skipHostnameVerifier = new HostnameVerifier()
  {
    @Override
    public boolean verify(String hostname, SSLSession session)
    {
      // skip hostname check
      return true;
    }
  };

  /**
   * Gets a SSLContext configured to trust any certificate
   * 
   * @return a SSLContext that trust any certificate
   * @throws NoSuchAlgorithmException
   * @throws KeyManagementException 
   */
  public synchronized static SSLContext getNoValidationSSLContext()
    throws NoSuchAlgorithmException, KeyManagementException
  {
    if (noValidationSSLContext == null)
    {
      noValidationSSLContext = SSLContext.getInstance("SSL");
      noValidationSSLContext.init(null, trustAllCerts,
        new java.security.SecureRandom());
    }
    return noValidationSSLContext;
  }

  /**
   * Gets a SSLSocketFactory that trust any certificate
   * 
   * @return a SSLSocketFactory that trust any certificate
   * @throws NoSuchAlgorithmException
   * @throws KeyManagementException 
   */
  public synchronized static SSLSocketFactory getNoValidationSSLSocketFactory()
    throws NoSuchAlgorithmException, KeyManagementException
  {
    if (noValidationSSLSocketFactory == null)
    {
      noValidationSSLSocketFactory =
        getNoValidationSSLContext().getSocketFactory();
    }
    return noValidationSSLSocketFactory;
  }

  /**
   * Configures a secure HttpURLConnection to skip certificate and
   * hostname validation
   *
   * @param conn the HttpURLConnection to configure
   */
  public static void skipCertificateValidation(HttpURLConnection conn)
  {
    if (conn instanceof HttpsURLConnection)
    {
      HttpsURLConnection sconn = (HttpsURLConnection)conn;
      try
      {
        sconn.setSSLSocketFactory(getNoValidationSSLSocketFactory());
        sconn.setHostnameVerifier(skipHostnameVerifier);
      }
      catch (Exception ex)
      {
        // ignore, connect with default SSLContext
      }
    }
  }
}
