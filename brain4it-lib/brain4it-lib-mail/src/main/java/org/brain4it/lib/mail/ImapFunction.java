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

import com.sun.mail.imap.IMAPFolder;
import java.util.Properties;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMultipart;
import org.brain4it.lang.Context;
import org.brain4it.lang.BList;
import org.brain4it.lang.Utils;
import org.brain4it.lang.Function;
import org.brain4it.lang.Structure;

/**
 *
 * @author realor
 */
public class ImapFunction implements Function
{  
  @Override
  public Object invoke(Context context, BList args) throws Exception
  {
    Properties props = System.getProperties();
    props.setProperty("mail.debug", "false");

    String host = (String)context.evaluate(args.get("host"));
    Number port = Utils.toNumber(context.evaluate(args.get("port")));
    if (port == null) port = 143;
    String username = (String)context.evaluate(args.get("username"));
    String password = (String)context.evaluate(args.get("password"));
    String folderName = (String)context.evaluate(args.get("folder"));
    if (folderName == null) folderName = "INBOX";
    Number start = Utils.toNumber(context.evaluate(args.get("start")));
    Number end = Utils.toNumber(context.evaluate(args.get("end")));
    if (start == null) start = 0;
    if (end == null) end = 10;
    Boolean expunge = Utils.toBoolean(context.evaluate(args.get("expunge")));

    Session session = Session.getDefaultInstance(props, null);
    Store store = session.getStore("imaps");
    store.connect(host, port.intValue(), username, password);
    IMAPFolder folder = (IMAPFolder)store.getFolder(folderName);
    BList messageList = new BList();
    folder.open(Folder.READ_WRITE);
    Message[] messages = folder.getMessages(start.intValue(), end.intValue());
    Structure structure = 
      new Structure("from", "subject", "received", "body");
    
    for (Message message : messages)
    {
      BList messageBlock = new BList(structure);
      messageBlock.put("from", message.getFrom()[0].toString());
      messageBlock.put("subject", message.getSubject());
      messageBlock.put("received", message.getReceivedDate().getTime());
      messageBlock.put("body", getTextFromMessage(message));
      messageList.add(messageBlock);
      if (expunge) message.setFlag(Flags.Flag.DELETED, true);
    }
    if (expunge) folder.expunge();
    folder.close(false);
    return messageList;
  }

  protected String getTextFromMessage(Message message) throws Exception
  {
    if (message.isMimeType("text/plain"))
    {
      return message.getContent().toString();
    }
    else if (message.isMimeType("multipart/*"))
    {
      String result = "";
      MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
      int count = mimeMultipart.getCount();
      for (int i = 0; i < count; i++)
      {
        BodyPart bodyPart = mimeMultipart.getBodyPart(i);
        if (bodyPart.isMimeType("text/plain"))
        {
          result = result + "\n" + bodyPart.getContent();
          break;
        }
        else if (bodyPart.isMimeType("text/html"))
        {
          String html = (String) bodyPart.getContent();
          result = result + "\n" + html;
        }
      }
      return result;
    }
    return "";
  }
}
