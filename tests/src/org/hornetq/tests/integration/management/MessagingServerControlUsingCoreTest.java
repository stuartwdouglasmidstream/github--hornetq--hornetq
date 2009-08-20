/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005-2009, Red Hat Middleware LLC, and individual contributors
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

package org.hornetq.tests.integration.management;

import org.hornetq.core.client.ClientSession;
import org.hornetq.core.client.ClientSessionFactory;
import org.hornetq.core.client.impl.ClientSessionFactoryImpl;
import org.hornetq.core.config.Configuration;
import org.hornetq.core.config.TransportConfiguration;
import org.hornetq.core.management.MessagingServerControl;
import org.hornetq.core.management.ResourceNames;
import org.hornetq.core.remoting.impl.invm.InVMConnectorFactory;

/**
 * A MessagingServerControlUsingCoreTest
 *
 * @author <a href="mailto:jmesnil@redhat.com">Jeff Mesnil</a>
 */
public class MessagingServerControlUsingCoreTest extends MessagingServerControlTest
{

   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   // MessagingServerControlTest overrides --------------------------

   private ClientSession session;

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();

      ClientSessionFactory sf = new ClientSessionFactoryImpl(new TransportConfiguration(InVMConnectorFactory.class.getName()));
      session = sf.createSession(false, true, true);
      session.start();

   }

   @Override
   protected void tearDown() throws Exception
   {
      session.close();
      
      session = null;

      super.tearDown();
   }

   @Override
   protected MessagingServerControl createManagementControl() throws Exception
   {

      return new MessagingServerControl()
      {
         private final CoreMessagingProxy proxy = new CoreMessagingProxy(session,
                                                                         ResourceNames.CORE_SERVER);
         
         public boolean closeConnectionsForAddress(String ipAddress) throws Exception
         {
            return (Boolean)proxy.invokeOperation("closeConnectionsForAddress", ipAddress);
         }

         public boolean commitPreparedTransaction(String transactionAsBase64) throws Exception
         {
            return (Boolean)proxy.invokeOperation("commitPreparedTransaction", transactionAsBase64);
         }

         public void createQueue(String address, String name) throws Exception
         {
            proxy.invokeOperation("createQueue", address, name);
         }

         public void createQueue(String address, String name, String filter, boolean durable) throws Exception
         {
            proxy.invokeOperation("createQueue", address, name, filter, durable);
         }
         
         public void deployQueue(String address, String name, String filter, boolean durable) throws Exception
         {
            proxy.invokeOperation("deployQueue", address, name, filter, durable);
         }

         public void deployQueue(String address, String name, String filterString) throws Exception
         {
            proxy.invokeOperation("deployQueue", address, name);
         }

         public void destroyQueue(String name) throws Exception
         {
            proxy.invokeOperation("destroyQueue", name);
         }

         public void disableMessageCounters() throws Exception
         {
            proxy.invokeOperation("disableMessageCounters");
         }

         public void enableMessageCounters() throws Exception
         {
            proxy.invokeOperation("enableMessageCounters");
         }

         public String getBackupConnectorName()
         {
            return (String)proxy.retrieveAttributeValue("backupConnectorName");
         }

         public String getBindingsDirectory()
         {
            return (String)proxy.retrieveAttributeValue("bindingsDirectory");
         }

         public Configuration getConfiguration()
         {
            return (Configuration)proxy.retrieveAttributeValue("configuration");
         }

         public int getConnectionCount()
         {
            return (Integer)proxy.retrieveAttributeValue("connectionCount");
         }

         public long getConnectionTTLOverride()
         {
            return (Long)proxy.retrieveAttributeValue("connectionTTLOverride", Long.class);
         }

         public Object[] getConnectors() throws Exception
         {
            return (Object[])proxy.retrieveAttributeValue("connectors");
         }
         
         public String getConnectorsAsJSON() throws Exception
         {
            return (String)proxy.retrieveAttributeValue("connectorsAsJSON");
         }

         public int getIDCacheSize()
         {
            return (Integer)proxy.retrieveAttributeValue("IDCacheSize");
         }

         public String[] getInterceptorClassNames()
         {
            Object[] res = (Object[])proxy.retrieveAttributeValue("interceptorClassNames");
            String[] names = new String[res.length];
            for (int i = 0; i < res.length; i++)
            {
               names[i] = res[i].toString();               
            }
            return names;
         }

         public String getJournalDirectory()
         {
            return (String)proxy.retrieveAttributeValue("journalDirectory");
         }

         public int getJournalFileSize()
         {
            return (Integer)proxy.retrieveAttributeValue("journalFileSize");
         }

         public int getJournalMaxAIO()
         {
            return (Integer)proxy.retrieveAttributeValue("journalMaxAIO");
         }

         public int getJournalMinFiles()
         {
            return (Integer)proxy.retrieveAttributeValue("journalMinFiles");
         }

         public String getJournalType()
         {
            return (String)proxy.retrieveAttributeValue("journalType");
         }

         public String getLargeMessagesDirectory()
         {
            return (String)proxy.retrieveAttributeValue("largeMessagesDirectory");
         }

         public String getManagementAddress()
         {
            return (String)proxy.retrieveAttributeValue("managementAddress");
         }

         public String getManagementNotificationAddress()
         {
            return (String)proxy.retrieveAttributeValue("managementNotificationAddress");
         }

         public long getManagementRequestTimeout()
         {
            return (Long)proxy.retrieveAttributeValue("managementRequestTimeout", Long.class);
         }

         public int getMessageCounterMaxDayCount()
         {
            return (Integer)proxy.retrieveAttributeValue("messageCounterMaxDayCount");
         }

         public long getMessageCounterSamplePeriod()
         {
            return (Long)proxy.retrieveAttributeValue("messageCounterSamplePeriod", Long.class);
         }

         public long getMessageExpiryScanPeriod()
         {
            return (Long)proxy.retrieveAttributeValue("messageExpiryScanPeriod", Long.class);
         }

         public long getMessageExpiryThreadPriority()
         {
            return (Long)proxy.retrieveAttributeValue("messageExpiryThreadPriority", Long.class);
         }

         public String getPagingDirectory()
         {
            return (String)proxy.retrieveAttributeValue("pagingDirectory");
         }

         public int getGlobalPageSize()
         {
            return (Integer)proxy.retrieveAttributeValue("globalPageSize");
         }

         public long getPagingMaxGlobalSizeBytes()
         {
            return (Long)proxy.retrieveAttributeValue("pagingMaxGlobalSizeBytes", Long.class);
         }

         public long getQueueActivationTimeout()
         {
            return (Long)proxy.retrieveAttributeValue("queueActivationTimeout", Long.class);
         }

         public int getScheduledThreadPoolMaxSize()
         {
            return (Integer)proxy.retrieveAttributeValue("scheduledThreadPoolMaxSize");
         }
         
         public int getThreadPoolMaxSize()
         {
            return (Integer)proxy.retrieveAttributeValue("threadPoolMaxSize");
         }

         public long getSecurityInvalidationInterval()
         {
            return (Long)proxy.retrieveAttributeValue("securityInvalidationInterval", Long.class);
         }

         public long getTransactionTimeout()
         {
            return (Long)proxy.retrieveAttributeValue("transactionTimeout", Long.class);
         }

         public long getTransactionTimeoutScanPeriod()
         {
            return (Long)proxy.retrieveAttributeValue("transactionTimeoutScanPeriod", Long.class);
         }

         public String getVersion()
         {
            return (String)proxy.retrieveAttributeValue("version");
         }

         public boolean isBackup()
         {
            return (Boolean)proxy.retrieveAttributeValue("backup");
         }

         public boolean isClustered()
         {
            return (Boolean)proxy.retrieveAttributeValue("clustered");
         }

         public boolean isCreateBindingsDir()
         {
            return (Boolean)proxy.retrieveAttributeValue("createBindingsDir");
         }

         public boolean isCreateJournalDir()
         {
            return (Boolean)proxy.retrieveAttributeValue("createJournalDir");
         }

         public boolean isJournalSyncNonTransactional()
         {
            return (Boolean)proxy.retrieveAttributeValue("journalSyncNonTransactional");
         }

         public boolean isJournalSyncTransactional()
         {
            return (Boolean)proxy.retrieveAttributeValue("journalSyncTransactional");
         }

         public boolean isMessageCounterEnabled()
         {
            return (Boolean)proxy.retrieveAttributeValue("messageCounterEnabled");
         }

         public boolean isPersistDeliveryCountBeforeDelivery()
         {
            return (Boolean)proxy.retrieveAttributeValue("persistDeliveryCountBeforeDelivery");
         }

         public boolean isPersistIDCache()
         {
            return (Boolean)proxy.retrieveAttributeValue("persistIDCache");
         }

         public boolean isSecurityEnabled()
         {
            return (Boolean)proxy.retrieveAttributeValue("securityEnabled");
         }

         public boolean isStarted()
         {
            return (Boolean)proxy.retrieveAttributeValue("started");
         }

         public boolean isWildcardRoutingEnabled()
         {
            return (Boolean)proxy.retrieveAttributeValue("wildcardRoutingEnabled");
         }

         public String[] listConnectionIDs() throws Exception
         {
            return (String[])proxy.invokeOperation("listConnectionIDs");
         }

         public String[] listPreparedTransactions() throws Exception
         {
            return (String[])proxy.invokeOperation("listPreparedTransactions");
         }

         public String[] listRemoteAddresses() throws Exception
         {
            return (String[])proxy.invokeOperation("listRemoteAddresses");
         }

         public String[] listRemoteAddresses(String ipAddress) throws Exception
         {
            return (String[])proxy.invokeOperation("listRemoteAddresses", ipAddress);
         }

         public String[] listSessions(String connectionID) throws Exception
         {
            return (String[])proxy.invokeOperation("listSessions", connectionID);
         }

         public void resetAllMessageCounterHistories() throws Exception
         {
            proxy.invokeOperation("resetAllMessageCounterHistories");
         }

         public void resetAllMessageCounters() throws Exception
         {
            proxy.invokeOperation("resetAllMessageCounters");
         }

         public boolean rollbackPreparedTransaction(String transactionAsBase64) throws Exception
         {
            return (Boolean)proxy.invokeOperation("rollbackPreparedTransaction", transactionAsBase64);
         }

         public void sendQueueInfoToQueue(String queueName, String address) throws Exception
         {
            proxy.invokeOperation("sendQueueInfoToQueue", queueName, address);
         }

         public void setMessageCounterMaxDayCount(int count) throws Exception
         {
            proxy.invokeOperation("setMessageCounterMaxDayCount", count);
         }

         public void setMessageCounterSamplePeriod(long newPeriod) throws Exception
         {
            proxy.invokeOperation("setMessageCounterSamplePeriod", newPeriod);
         }

         public int getAIOBufferSize()
         {
            return (Integer)proxy.retrieveAttributeValue("AIOBufferSize");
         }

         public int getAIOBufferTimeout()
         {
            return (Integer)proxy.retrieveAttributeValue("AIOBufferTimeout");
         }

      };
   }
   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------

}