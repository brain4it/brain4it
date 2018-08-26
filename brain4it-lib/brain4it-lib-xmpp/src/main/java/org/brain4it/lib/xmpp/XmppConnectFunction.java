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

package org.brain4it.lib.xmpp;

import java.util.Map;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.brain4it.lang.Context;
import org.brain4it.lang.BList;
import org.brain4it.lang.Executor;
import org.brain4it.lang.Function;
import org.brain4it.lang.Utils;
import org.brain4it.lib.XmppLibrary;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.chat2.OutgoingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jxmpp.jid.EntityBareJid;

/**
 *
 * @author realor
 */
public class XmppConnectFunction extends XmppFunction
{
  public XmppConnectFunction(XmppLibrary library)
  {
    super(library);
  }
  
  @Override
  public Object invoke(Context context, BList args) throws Exception
  {
    Utils.checkArguments(args, 3);    
    String domain = (String)context.evaluate(args.get(1));
    String username = (String)context.evaluate(args.get(2));
    String password = (String)context.evaluate(args.get(3));
    
    BList connectionListener = 
      (BList)context.evaluate(args.get("connection-listener"));
    BList incomingListener = 
      (BList)context.evaluate(args.get("incoming-listener"));
    BList outgoingListener = 
      (BList)context.evaluate(args.get("outgoing-listener"));
    
    XMPPTCPConnectionConfiguration config = 
      XMPPTCPConnectionConfiguration.builder()
      .setXmppDomain(domain)
      .setDebuggerEnabled(false)
      .setSecurityMode(ConnectionConfiguration.SecurityMode.required)
      .build();
    XMPPTCPConnection connection = new XMPPTCPConnection(config);
    
    if (connectionListener != null && 
        context.isUserFunction(connectionListener))
    {
      connection.addConnectionListener(
        createConnectionListener(context, connectionListener));
    }

    connection.connect();
    connection.login(username, password);
        
    if (incomingListener != null && 
        context.isUserFunction(incomingListener))
    {
      ChatManager chatManager = ChatManager.getInstanceFor(connection);
      chatManager.addIncomingListener(
        createIncomingMessageListener(context, incomingListener));
    }    

    if (outgoingListener != null && 
        context.isUserFunction(outgoingListener))
    {
      ChatManager chatManager = ChatManager.getInstanceFor(connection);
      chatManager.addOutgoingListener(
        createOutgoingMessageListener(context, outgoingListener));
    }

    return library.putConnection(connection);
  }
  
  ConnectionListener createConnectionListener(
    final Context context, final BList func)
  {
    return new ConnectionListener()
    {
      @Override
      public void connected(XMPPConnection connection)
      {
        call(context, func, "Connected");
      }

      @Override
      public void authenticated(XMPPConnection connection, boolean resumed)
      {
        call(context, func, "Authenticated", resumed);
      }

      @Override
      public void connectionClosed()
      {
        call(context, func, "Closed");
      }

      @Override
      public void connectionClosedOnError(Exception exception)
      {
        call(context, func, "CloseError", exception);
      }

      @Override
      public void reconnectionSuccessful()
      {
        call(context, func, "ReconnectionSuccess");
      }

      @Override
      public void reconnectingIn(int seconds)
      {
        call(context, func, "Reconnecting", seconds);
      }

      @Override
      public void reconnectionFailed(Exception exception)
      {
        call(context, func, "ReconnectionFail", Utils.toBList(exception));
      }      
    };
  }
  
  IncomingChatMessageListener createIncomingMessageListener(
    final Context context, final BList func)
  {
    return new IncomingChatMessageListener() 
    {
      @Override
      public void newIncomingMessage(EntityBareJid ebj, Message msg, Chat chat)
      {
        call(context, func, ebj.asBareJid().toString(), msg.getBody());
      }
    };
  }

  OutgoingChatMessageListener createOutgoingMessageListener(
    final Context context, final BList func)
  {
    return new OutgoingChatMessageListener() 
    {
      @Override
      public void newOutgoingMessage(EntityBareJid ebj, Message msg, Chat chat)
      {
        call(context, func, ebj.asBareJid().toString(), msg.getBody());
      }
    };
  }
  
  void call(Context context, BList userFunction, Object... args)
  {
    try
    {
      Map<String, Function> functions = context.getFunctions();
      BList call = Utils.createFunctionCall(functions, userFunction, args);
      Executor.spawn(call, context.getGlobalScope(), functions, null);
    }
    catch (Exception ex)
    {      
      // igonore
    }
  }
}
