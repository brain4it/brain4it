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

package org.brain4it.lib.management;

/**
 *
 * @author realor
 */

import java.util.Vector;
import org.brain4it.lang.Context;
import org.brain4it.lang.BList;
import org.brain4it.lang.Utils;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.brain4it.lang.Function;

public class SnmpGetFunction implements Function
{
  @Override
  public Object invoke(Context context, BList args) throws Exception
  {
    Utils.checkArguments(args, 1);    
    String ipPort = Utils.toString(context.evaluate(args.get(1)));

    TransportMapping transport = new DefaultUdpTransportMapping();
    transport.listen();

    CommunityTarget comtarget = new CommunityTarget();
    comtarget.setCommunity(new OctetString("public"));
    comtarget.setVersion(SnmpConstants.version1);
    comtarget.setAddress(new UdpAddress(ipPort));
    comtarget.setRetries(2);
    comtarget.setTimeout(1000);

    PDU pdu = new PDU();
    for (int i = 2; i < args.size(); i++)
    {
      String oid = Utils.toString(context.evaluate(args.get(i)));
      pdu.add(new VariableBinding(new OID(oid)));
    }
    pdu.setType(PDU.GET);
    pdu.setRequestID(new Integer32(1));

    Object result = null;
    Snmp snmp = new Snmp(transport);
    try
    {
      ResponseEvent response = snmp.get(pdu, comtarget);
      if (response != null)
      {
        PDU responsePDU = response.getResponse();

        if (responsePDU != null)
        {
          int errorStatus = responsePDU.getErrorStatus();
          int errorIndex = responsePDU.getErrorIndex();
          String errorStatusText = responsePDU.getErrorStatusText();

          if (errorStatus == PDU.noError)
          {
            Vector<? extends VariableBinding> bindings = 
              responsePDU.getVariableBindings();
            BList list = new BList();
            for (VariableBinding binding : bindings)
            {
              list.put(binding.getOid().toString(), binding.toValueString());
            }
            result = list;
          }
          else
          {
            throw new Exception("SNMP Error " + errorStatus + "/" + 
              errorIndex + ": " + errorStatusText);
          }
        }
      }
      else
      {
        throw new Exception("SNMP Timeout");
      }
    }
    finally
    {
      snmp.close();    
    }
    return result;
  }
}
