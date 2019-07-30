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

package org.realor.hnap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;

/**
 *
 * @author realor
 */
public class HnapClient
{
  public static String HNAP1_XMLNS = "http://purenetworks.com/HNAP1/";
  public static String HNAP_METHOD = "POST";
  public static String HNAP_BODY_ENCODING = "utf-8";
  public static String HNAP_LOGIN_METHOD = "Login";

  protected String url;
  protected String username;
  protected String password;
  protected String loginResult;
  protected String challenge;
  protected String publicKey;
  protected String privateKey;
  protected String cookie;
  protected Document responseDOM;
  protected boolean debug;

  public String getUrl()
  {
    return url;
  }

  public void setUrl(String url)
  {
    this.url = url;
  }

  public String getUsername()
  {
    return username;
  }

  public void setUsername(String username)
  {
    this.username = username;
  }

  public String getPassword()
  {
    return password;
  }

  public void setPassword(String password)
  {
    this.password = password;
  }

  public Document getResponseDOM()
  {
    return responseDOM;
  }

  public boolean isDebug()
  {
    return debug;
  }

  public void setDebug(boolean debug)
  {
    this.debug = debug;
  }

  public String login() throws Exception
  {
    HttpURLConnection conn = (HttpURLConnection)(new URL(url)).openConnection();
    conn.setRequestMethod(HNAP_METHOD);
    conn.setRequestProperty("SOAPAction",
      '"' + HNAP1_XMLNS + HNAP_LOGIN_METHOD + '"');
    String body = requestBody(HNAP_LOGIN_METHOD, loginRequest());
    String response = sendRequest(conn, body);
    responseDOM = parseXML(response);
    loginResult = responseDOM.getElementsByTagName(HNAP_LOGIN_METHOD + "Result").
      item(0).getFirstChild().getNodeValue();
    challenge = responseDOM.getElementsByTagName("Challenge").
      item(0).getFirstChild().getNodeValue();
    publicKey = responseDOM.getElementsByTagName("PublicKey").
      item(0).getFirstChild().getNodeValue();
    cookie = responseDOM.getElementsByTagName("Cookie").
      item(0).getFirstChild().getNodeValue();
    privateKey = calcHmacMD5(publicKey + password, challenge);

    return soapAction(HNAP_LOGIN_METHOD, "LoginResult",
      requestBody(HNAP_LOGIN_METHOD, loginParameters()));
  }

  public String setSocketSettings(boolean status) throws Exception
  {
    return soapAction("SetSocketSettings", "SetSocketSettingsResult",
      requestBody("SetSocketSettings", controlParameters("1", status)));
  }

  public String getSocketSettings() throws Exception
  {
    return soapAction("GetSocketSettings", "OPStatus",
      requestBody("GetSocketSettings", moduleParameters("1")));
  }

  public String getCurrentPowerConsumition() throws Exception
  {
    return soapAction("GetCurrentPowerConsumption", "CurrentConsumption",
      requestBody("GetCurrentPowerConsumption", moduleParameters("2")));
  };

  public String getTotalConsumption() throws Exception
  {
    return soapAction("GetPMWarningThreshold", "TotalConsumption",
      requestBody("GetPMWarningThreshold", moduleParameters("2")));
  };

  public String getCurrentTemperature() throws Exception
  {
    return soapAction("GetCurrentTemperature", "CurrentTemperature",
      requestBody("GetCurrentTemperature", moduleParameters("3")));
  };

  public String getAPClientSettings() throws Exception
  {
    return soapAction("GetAPClientSettings", "GetAPClientSettingsResult",
      requestBody("GetAPClientSettings", radioParameters("RADIO_2.4GHz")));
  };

  public String setPowerWarning() throws Exception
  {
    return soapAction("SetPMWarningThreshold", "SetPMWarningThresholdResult",
      requestBody("SetPMWarningThreshold", powerWarningParameters()));
  };

  public String getPowerWarning() throws Exception
  {
    return soapAction("GetPMWarningThreshold", "GetPMWarningThresholdResult",
      requestBody("GetPMWarningThreshold", moduleParameters("2")));
  };

  public String getTempMonitorSettings() throws Exception
  {
    return soapAction("GetTempMonitorSettings", "GetTempMonitorSettingsResult",
      requestBody("GetTempMonitorSettings", moduleParameters("3")));
  };

  public String setTemperatureSettings() throws Exception
  {
    return soapAction("SetTempMonitorSettings", "SetTempMonitorSettingsResult",
      requestBody("SetTempMonitorSettings", temperatureSettingsParameters("3")));
  };

  public String getSiteSurvey() throws Exception
  {
    return soapAction("GetSiteSurvey", "GetSiteSurveyResult",
      requestBody("GetSiteSurvey", radioParameters("RADIO_2.4GHz")));
  };

  public String setTriggerWirelessSiteSurvey() throws Exception
  {
    return soapAction("SetTriggerWirelessSiteSurvey",
      "SetTriggerWirelessSiteSurveyResult",
      requestBody("SetTriggerWirelessSiteSurvey",
        radioParameters("RADIO_2.4GHz")));
  };

  public String getLatestDetection() throws Exception
  {
    return soapAction("GetLatestDetection", "GetLatestDetectionResult",
      requestBody("GetLatestDetection", moduleParameters("2")));
  };

  public String reboot() throws Exception
  {
    return soapAction("Reboot", "RebootResult", requestBody("Reboot", ""));
  };

  public String isDeviceReady() throws Exception
  {
    return soapAction("IsDeviceReady", "IsDeviceReadyResult",
      requestBody("IsDeviceReady", ""));
  };

  public String getModuleSchedule() throws Exception
  {
    return soapAction("GetModuleSchedule", "GetModuleScheduleResult",
      requestBody("GetModuleSchedule", moduleParameters("0")));
  };

  public String getModuleEnabled() throws Exception
  {
    return soapAction("GetModuleEnabled", "GetModuleEnabledResult",
      requestBody("GetModuleEnabled", moduleParameters("0")));
  };

  public String getModuleGroup() throws Exception
  {
    return soapAction("GetModuleGroup", "GetModuleGroupResult",
      requestBody("GetModuleGroup", groupParameters("0")));
  };

  public String getScheduleSettings() throws Exception
  {
    return soapAction("GetScheduleSettings", "GetScheduleSettingsResult",
      requestBody("GetScheduleSettings", ""));
  };

  public String setFactoryDefault() throws Exception
  {
    return soapAction("SetFactoryDefault", "SetFactoryDefaultResult",
      requestBody("SetFactoryDefault", ""));
  };

  public String getWLanRadios() throws Exception
  {
    return soapAction("GetWLanRadios", "GetWLanRadiosResult",
      requestBody("GetWLanRadios", ""));
  };

  public String getInternetSettings() throws Exception
  {
    return soapAction("GetInternetSettings", "GetInternetSettingsResult",
      requestBody("GetInternetSettings", ""));
  };

  public String setTriggerADIC() throws Exception
  {
    return soapAction("SettriggerADIC", "SettriggerADICResult",
      requestBody("SettriggerADIC", ""));
  };

  // protected methods

  protected String soapAction(String method, String responseElement, String body)
    throws Exception
  {
    HttpURLConnection conn = (HttpURLConnection)(new URL(url)).openConnection();
    conn.setRequestMethod(HNAP_METHOD);
    String soapAction = '"' + HNAP1_XMLNS + method + '"';
    conn.setRequestProperty("SOAPAction", soapAction);
    conn.setRequestProperty("HNAP_AUTH", getHnapAuth(soapAction));
    conn.setRequestProperty("Cookie", "uid=" + cookie);
    responseDOM = null;
    if (debug) System.out.println("Request:\n" + body);
    String response = sendRequest(conn, body);
    if (debug) System.out.println("Response:\n" + response);
    responseDOM = parseXML(response);
    if (responseElement != null)
    {
      response = responseDOM.getElementsByTagName(responseElement).item(0).
        getFirstChild().getNodeValue();
    }
    return response;
  }

  protected String sendRequest(HttpURLConnection conn, String body)
    throws IOException
  {
    conn.setDoInput(true);
    if (body != null)
    {
      conn.setRequestProperty("Content-Type",
        "text/plain; charset=" + HNAP_BODY_ENCODING);
      conn.setDoOutput(true);
      OutputStream os = conn.getOutputStream();
      try
      {
        byte[] data = body.getBytes(HNAP_BODY_ENCODING);
        os.write(data);
        os.flush();
      }
      finally
      {
        os.close();
      }
    }
    try
    {
      conn.connect();
      int status = conn.getResponseCode();
      String contentEncoding = conn.getContentEncoding();
      byte[] response;
      try
      {
        response = readInputStream(conn.getInputStream());
      }
      catch (IOException ex)
      {
        InputStream errorStream = conn.getErrorStream();
        if (errorStream != null)
        {
          response = readInputStream(errorStream);
        }
        else throw ex;
      }
      if (contentEncoding == null) contentEncoding = HNAP_BODY_ENCODING;
      String resultString = new String(response, contentEncoding);
      if (status == HttpURLConnection.HTTP_OK)
      {
        return resultString;
      }
      else
      {
        throw new IOException(resultString);
      }
    }
    finally
    {
      conn.disconnect();
    }
  }

  protected byte[] readInputStream(InputStream is) throws IOException
  {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    try
    {
      byte[] buffer = new byte[1024];
      int count = is.read(buffer);
      while (count != -1)
      {
        os.write(buffer, 0, count);
        count = is.read(buffer);
      }
    }
    finally
    {
      is.close();
    }
    return os.toByteArray();
  }

  protected String requestBody(String method, String parameters)
  {
    return "<?xml version=\"1.0\" encoding=\"" + HNAP_BODY_ENCODING +"\"?>" +
      "<soap:Envelope " +
      "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
      "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
      "xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
      "<soap:Body>" +
      "<" + method + " xmlns=\"" + HNAP1_XMLNS + "\">" +
         parameters +
      "</" + method + ">" +
      "</soap:Body></soap:Envelope>";
  }

  protected String loginRequest()
  {
    return "<Action>request</Action>" +
      "<Username>" + username + "</Username>" +
      "<LoginPassword></LoginPassword>" +
      "<Captcha></Captcha>";
  }

  protected String loginParameters() throws Exception
  {
    String loginPwd = calcHmacMD5(privateKey, challenge);
    return "<Action>login</Action>" +
      "<Username>" + username + "</Username>" +
      "<LoginPassword>" + loginPwd + "</LoginPassword>" +
      "<Captcha></Captcha>";
  }

  protected String radioParameters(String radio)
  {
    return "<RadioID>" + radio + "</RadioID>";
  }

  protected String moduleParameters(String module)
  {
    return "<ModuleID>" + module + "</ModuleID>";
  }

  protected String controlParameters(String module, boolean status)
  {
    return moduleParameters(module) +
      "<NickName>Socket 1</NickName><Description>Socket 1</Description>" +
      "<OPStatus>" + status + "</OPStatus><Controller>1</Controller>";
  }

  protected String groupParameters(String group)
  {
    return "<ModuleGroupID>" + group + "</ModuleGroupID>";
  }

  protected String temperatureSettingsParameters(String module)
  {
    return moduleParameters(module) +
      "<NickName>TemperatureMonitor 3</NickName>" +
      "<Description>Temperature Monitor 3</Description>" +
      "<UpperBound>80</UpperBound>" +
      "<LowerBound>Not Available</LowerBound>" +
      "<OPStatus>true</OPStatus>";
  }

  protected String powerWarningParameters()
  {
    return "<Threshold>28</Threshold>" +
      "<Percentage>70</Percentage>" +
      "<PeriodicType>Weekly</PeriodicType>" +
      "<StartTime>1</StartTime>";
  }

  protected String getHnapAuth(String soapAction) throws Exception
  {
    Date now = new Date();
    long timeStamp = now.getTime() / 1000;
    String auth = calcHmacMD5(privateKey, timeStamp + soapAction);
    return auth + " " + timeStamp;
  }

  protected String calcHmacMD5(String key, String data) throws Exception
  {
    SecretKeySpec secretKey = 
      new SecretKeySpec(key.getBytes("UTF-8"), "HmacMD5");
    Mac mac = Mac.getInstance("HmacMD5");
    mac.init(secretKey);
    byte[] digest = mac.doFinal(data.getBytes("UTF-8"));
    return byteArrayToHex(digest).toUpperCase();
  }

  protected Document parseXML(String xml) throws Exception
  {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    byte[] data = xml.getBytes(HNAP_BODY_ENCODING);
    return builder.parse(new ByteArrayInputStream(data));
  }

  protected String byteArrayToHex(byte[] array)
  {
    StringBuilder buffer = new StringBuilder(2 * array.length);
    for (byte b: array)
    {
      buffer.append(String.format("%02x", b));
    }
    return buffer.toString();
  }

  public static void main(String[] args) throws Exception
  {
    HnapClient client = new HnapClient();

    client.setUrl("http://192.168.2.33/HNAP1");
    client.setUsername("admin");
    client.setPassword("111111");
    client.setDebug(true);
    System.out.println(client.login());

    for (int i = 0; i < 1; i++)
    {
      System.out.println(client.setSocketSettings(true));
      Thread.sleep(1000);
      System.out.println(client.getSocketSettings());
      Thread.sleep(1000);
      System.out.println(client.setSocketSettings(false));
      Thread.sleep(1000);
      System.out.println(client.getSocketSettings());
      Thread.sleep(1000);
    }
    System.out.println(client.getInternetSettings());
    System.out.println(client.getCurrentPowerConsumition() + " W");
    System.out.println(client.getCurrentTemperature() + " C");

    client.setDebug(false);
    for (int i = 0; i < 0; i++)
    {
      double kwh = Double.parseDouble(client.getTotalConsumption());
      double price = 0.15 * kwh;
      System.out.println(kwh + " Kwh, " + price + " euros");
      Thread.sleep(1000);
    }
    client.setDebug(true);
    System.out.println(client.getSocketSettings());
  }
}
