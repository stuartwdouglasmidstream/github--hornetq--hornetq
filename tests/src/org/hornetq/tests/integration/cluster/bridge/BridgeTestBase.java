/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005-2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.hornetq.tests.integration.cluster.bridge;

import java.util.HashMap;
import java.util.Map;

import org.hornetq.core.config.Configuration;
import org.hornetq.core.config.TransportConfiguration;
import org.hornetq.core.config.impl.ConfigurationImpl;
import org.hornetq.core.remoting.impl.invm.TransportConstants;
import org.hornetq.core.server.Messaging;
import org.hornetq.core.server.MessagingServer;
import org.hornetq.tests.util.UnitTestCase;

/**
 * A BridgeTestBase
 *
 * @author <a href="mailto:tim.fox@jboss.com">Tim Fox</a>
 * 
 * Created 21 Nov 2008 10:32:23
 *
 *
 */
public abstract class BridgeTestBase extends UnitTestCase
{
   protected MessagingServer createMessagingServerNIO(final int id, final Map<String, Object> params)
   {
      return createMessagingServerNIO(id, params, false);
   }

   protected MessagingServer createMessagingServerNIO(final int id,
                                                      final Map<String, Object> params,
                                                      final boolean backup)
   {
      Configuration serviceConf = new ConfigurationImpl();
      serviceConf.setClustered(true);
      serviceConf.setSecurityEnabled(false);
      serviceConf.setBackup(backup);
      serviceConf.setJournalMinFiles(2);
      serviceConf.setJournalFileSize(100 * 1024);
      params.put(TransportConstants.SERVER_ID_PROP_NAME, id);
      serviceConf.getAcceptorConfigurations()
                 .add(new TransportConfiguration("org.hornetq.core.remoting.impl.invm.InVMAcceptorFactory",
                                                 params));
      MessagingServer service = Messaging.newMessagingServer(serviceConf);
      return service;
   }

   protected MessagingServer createMessagingServer(final int id, final Map<String, Object> params)
   {
      return createMessagingServer(id, params, false);
   }

   protected MessagingServer createMessagingServer(final int id, final Map<String, Object> params, final boolean backup)
   {
      Configuration serviceConf = new ConfigurationImpl();
      serviceConf.setClustered(true);
      serviceConf.setSecurityEnabled(false);
      serviceConf.setBackup(backup);
      params.put(TransportConstants.SERVER_ID_PROP_NAME, id);
      serviceConf.getAcceptorConfigurations()
                 .add(new TransportConfiguration("org.hornetq.core.remoting.impl.invm.InVMAcceptorFactory",
                                                 params));
      MessagingServer service = Messaging.newMessagingServer(serviceConf, false);
      return service;
   }

   protected MessagingServer createMessagingServer(final int id)
   {
      return this.createMessagingServer(id, new HashMap<String, Object>());
   }
}