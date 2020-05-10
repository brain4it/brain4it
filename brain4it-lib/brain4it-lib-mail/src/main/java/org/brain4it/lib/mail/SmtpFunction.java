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

package org.brain4it.lib.mail;

import java.util.Date;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.brain4it.lang.Context;
import org.brain4it.lang.BList;
import org.brain4it.lang.Utils;
import org.brain4it.lang.Function;

/**
 *
 * @author realor
 */
public class SmtpFunction implements Function
{
  @Override
  public Object invoke(Context context, BList args) throws Exception
  {
    String host = (String)context.evaluate(args.get("host"));
    Number port = Utils.toNumber(context.evaluate(args.get("port")));
    Object to = context.evaluate(args.get("to"));
    Object cc = context.evaluate(args.get("cc"));
    Object bcc = context.evaluate(args.get("bcc"));
    String from = (String)context.evaluate(args.get("from"));
    String subject = (String)context.evaluate(args.get("subject"));
    String body = (String)context.evaluate(args.get("body"));
    String contentType = (String)context.evaluate(args.get("content-type"));
    BList properties = (BList)context.evaluate(args.get("properties"));
    final String username = (String)context.evaluate(args.get("username"));
    final String password = (String)context.evaluate(args.get("password"));

    Properties props = System.getProperties();

    if (properties != null)
    {
      for (int i = 0; i < properties.size(); i++)
      {
        String name = properties.getName(i);
        if (name != null)
        {
          props.put(name, properties.get(i));
        }
      }
    }
    
		if (host != null)
    {
      props.put("mail.smtp.host", host);
    }
    if (port != null)
    {
      props.put("mail.smtp.port", port.intValue());
    }

    Authenticator authenticator;
    if (username != null && password != null)
    {
      authenticator = new Authenticator()
      {
        @Override
        protected PasswordAuthentication getPasswordAuthentication()
        {
          return new PasswordAuthentication(
            username, password);
        }
      };
    }
    else authenticator = null;
    
	  Session session = Session.getInstance(props, authenticator);
		session.setDebug(true);

	  MimeMessage msg = new MimeMessage(session);
    if (from != null)
    {
  		msg.setFrom(new InternetAddress(from));
    }
    else
    {
      msg.setFrom();
    }

    setupRecipients(msg, Message.RecipientType.TO, to);
    setupRecipients(msg, Message.RecipientType.CC, cc);
    setupRecipients(msg, Message.RecipientType.BCC, bcc);

    if (subject != null)
    {
  	  msg.setSubject(subject);
    }
    
    if (contentType == null)
    {
      contentType = "text/plain; charset=UTF-8";
    }
    msg.setContent(body, contentType);
    
	  msg.setHeader("X-Mailer", "brain4it");
	  msg.setSentDate(new Date());

	  Transport.send(msg);

    return null;
  }

  private void setupRecipients(Message msg, RecipientType type, 
    Object recipients) throws Exception
  {
    if (recipients instanceof String)
    {
      msg.setRecipients(type,
        InternetAddress.parse((String)recipients, false));
    }
    else if (recipients instanceof BList)
    {
      BList list = (BList)recipients;
      InternetAddress addresses[] = new InternetAddress[list.size()];
      for (int i = 0; i < list.size(); i++)
      {
        addresses[i] = new InternetAddress(Utils.toString(list.get(i)));
      }
      msg.setRecipients(type, addresses);      
    }
  }
}
