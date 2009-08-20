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
package org.hornetq.tests.integration.jms.consumer;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.hornetq.core.config.Configuration;
import org.hornetq.core.config.TransportConfiguration;
import org.hornetq.core.config.impl.ConfigurationImpl;
import org.hornetq.core.server.Messaging;
import org.hornetq.core.server.MessagingServer;
import org.hornetq.core.server.Queue;
import org.hornetq.jms.JBossQueue;
import org.hornetq.jms.client.JBossConnectionFactory;
import org.hornetq.jms.client.JBossSession;
import org.hornetq.jms.server.impl.JMSServerManagerImpl;
import org.hornetq.tests.integration.jms.server.management.NullInitialContext;
import org.hornetq.tests.util.UnitTestCase;
import org.hornetq.utils.SimpleString;

/**
 * @author <a href="mailto:andy.taylor@jboss.org">Andy Taylor</a>
 */
public class ConsumerTest extends UnitTestCase
{
   private MessagingServer server;

   private JMSServerManagerImpl jmsServer;

   private JBossConnectionFactory cf;

   private static final String Q_NAME = "ConsumerTestQueue";

   private JBossQueue jBossQueue;

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();

      Configuration conf = new ConfigurationImpl();
      conf.setSecurityEnabled(false);
      conf.setJMXManagementEnabled(true);
      conf.getAcceptorConfigurations()
          .add(new TransportConfiguration("org.hornetq.core.remoting.impl.invm.InVMAcceptorFactory"));
      server = Messaging.newMessagingServer(conf, false);
      jmsServer = new JMSServerManagerImpl(server);
      jmsServer.setContext(new NullInitialContext());
      jmsServer.start();
      jmsServer.createQueue(Q_NAME, Q_NAME, null, true);
      cf = new JBossConnectionFactory(new TransportConfiguration("org.hornetq.core.remoting.impl.invm.InVMConnectorFactory"));
      cf.setBlockOnPersistentSend(true);
      cf.setPreAcknowledge(true);
   }

   @Override
   protected void tearDown() throws Exception
   {
      jmsServer.stop();
      
      cf = null;
      if (server != null && server.isStarted())
      {
         try
         {
            server.stop();
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
         server = null;
      }

      server = null;
      jmsServer = null;
      cf = null;
      jBossQueue = null;

      super.tearDown();
   }

   public void testPreCommitAcks() throws Exception
   {
      Connection conn = cf.createConnection();
      Session session = conn.createSession(false, JBossSession.PRE_ACKNOWLEDGE);
      jBossQueue = new JBossQueue(Q_NAME);
      MessageProducer producer = session.createProducer(jBossQueue);
      MessageConsumer consumer = session.createConsumer(jBossQueue);
      int noOfMessages = 100;
      for (int i = 0; i < noOfMessages; i++)
      {
         producer.send(session.createTextMessage("m" + i));
      }

      conn.start();
      for (int i = 0; i < noOfMessages; i++)
      {
         Message m = consumer.receive(500);
         assertNotNull(m);
      }
      // assert that all the messages are there and none have been acked
      SimpleString queueName = new SimpleString(JBossQueue.JMS_QUEUE_ADDRESS_PREFIX + Q_NAME);
      assertEquals(0, ((Queue)server.getPostOffice().getBinding(queueName).getBindable()).getDeliveringCount());
      assertEquals(0, ((Queue)server.getPostOffice().getBinding(queueName).getBindable()).getMessageCount());
      conn.close();
   }

   public void testPreCommitAcksWithMessageExpiry() throws Exception
   {
      Connection conn = cf.createConnection();
      Session session = conn.createSession(false, JBossSession.PRE_ACKNOWLEDGE);
      jBossQueue = new JBossQueue(Q_NAME);
      MessageProducer producer = session.createProducer(jBossQueue);
      MessageConsumer consumer = session.createConsumer(jBossQueue);
      int noOfMessages = 1000;
      for (int i = 0; i < noOfMessages; i++)
      {
         TextMessage textMessage = session.createTextMessage("m" + i);
         producer.setTimeToLive(1);
         producer.send(textMessage);
      }

      conn.start();
      for (int i = 0; i < noOfMessages; i++)
      {
         Message m = consumer.receive(500);
         assertNotNull(m);
      }
      // assert that all the messages are there and none have been acked
      SimpleString queueName = new SimpleString(JBossQueue.JMS_QUEUE_ADDRESS_PREFIX + Q_NAME);
      assertEquals(0, ((Queue)server.getPostOffice().getBinding(queueName).getBindable()).getDeliveringCount());
      assertEquals(0, ((Queue)server.getPostOffice().getBinding(queueName).getBindable()).getMessageCount());
      conn.close();
   }

   public void testClearExceptionListener() throws Exception
   {
      Connection conn = cf.createConnection();
      Session session = conn.createSession(false, JBossSession.PRE_ACKNOWLEDGE);
      jBossQueue = new JBossQueue(Q_NAME);
      MessageConsumer consumer = session.createConsumer(jBossQueue);
      consumer.setMessageListener(new MessageListener()
      {
         public void onMessage(Message msg)
         {
         }
      });

      consumer.setMessageListener(null);
      consumer.receiveNoWait();
   }
   
   public void testCantReceiveWhenListenerIsSet() throws Exception
   {
      Connection conn = cf.createConnection();
      Session session = conn.createSession(false, JBossSession.PRE_ACKNOWLEDGE);
      jBossQueue = new JBossQueue(Q_NAME);
      MessageConsumer consumer = session.createConsumer(jBossQueue);
      consumer.setMessageListener(new MessageListener()
      {
         public void onMessage(Message msg)
         {
         }
      });

      try
      {
         consumer.receiveNoWait();
         fail("Should throw exception");
      }
      catch (JMSException e)
      {
         //Ok
      }
   }
}