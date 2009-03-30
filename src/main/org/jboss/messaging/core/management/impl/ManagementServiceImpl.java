/*
 * JBoss, Home of Professional Open Source.
 * 
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors by the
 * 
 * @authors tag. See the copyright.txt in the distribution for a full listing of
 * individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package org.jboss.messaging.core.management.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;
import javax.management.StandardMBean;

import org.jboss.messaging.core.buffers.ChannelBuffers;
import org.jboss.messaging.core.client.management.impl.ManagementHelper;
import org.jboss.messaging.core.cluster.DiscoveryGroup;
import org.jboss.messaging.core.config.Configuration;
import org.jboss.messaging.core.config.TransportConfiguration;
import org.jboss.messaging.core.config.cluster.BridgeConfiguration;
import org.jboss.messaging.core.config.cluster.BroadcastGroupConfiguration;
import org.jboss.messaging.core.config.cluster.ClusterConnectionConfiguration;
import org.jboss.messaging.core.config.cluster.DiscoveryGroupConfiguration;
import org.jboss.messaging.core.config.cluster.DivertConfiguration;
import org.jboss.messaging.core.config.impl.ConfigurationImpl;
import org.jboss.messaging.core.logging.Logger;
import org.jboss.messaging.core.management.AcceptorControlMBean;
import org.jboss.messaging.core.management.BridgeControlMBean;
import org.jboss.messaging.core.management.BroadcastGroupControlMBean;
import org.jboss.messaging.core.management.ClusterConnectionControlMBean;
import org.jboss.messaging.core.management.DiscoveryGroupControlMBean;
import org.jboss.messaging.core.management.DivertControlMBean;
import org.jboss.messaging.core.management.ManagementService;
import org.jboss.messaging.core.management.MessagingServerControlMBean;
import org.jboss.messaging.core.management.Notification;
import org.jboss.messaging.core.management.NotificationListener;
import org.jboss.messaging.core.management.ObjectNames;
import org.jboss.messaging.core.management.ReplicationOperationInvoker;
import org.jboss.messaging.core.management.jmx.impl.ReplicationAwareAddressControlWrapper;
import org.jboss.messaging.core.management.jmx.impl.ReplicationAwareMessagingServerControlWrapper;
import org.jboss.messaging.core.management.jmx.impl.ReplicationAwareQueueControlWrapper;
import org.jboss.messaging.core.message.impl.MessageImpl;
import org.jboss.messaging.core.messagecounter.MessageCounter;
import org.jboss.messaging.core.messagecounter.MessageCounterManager;
import org.jboss.messaging.core.messagecounter.impl.MessageCounterManagerImpl;
import org.jboss.messaging.core.persistence.StorageManager;
import org.jboss.messaging.core.postoffice.PostOffice;
import org.jboss.messaging.core.remoting.server.RemotingService;
import org.jboss.messaging.core.remoting.spi.Acceptor;
import org.jboss.messaging.core.security.Role;
import org.jboss.messaging.core.server.Divert;
import org.jboss.messaging.core.server.MessagingServer;
import org.jboss.messaging.core.server.Queue;
import org.jboss.messaging.core.server.QueueFactory;
import org.jboss.messaging.core.server.ServerMessage;
import org.jboss.messaging.core.server.cluster.Bridge;
import org.jboss.messaging.core.server.cluster.BroadcastGroup;
import org.jboss.messaging.core.server.cluster.ClusterConnection;
import org.jboss.messaging.core.server.impl.ServerMessageImpl;
import org.jboss.messaging.core.settings.HierarchicalRepository;
import org.jboss.messaging.core.settings.impl.AddressSettings;
import org.jboss.messaging.core.transaction.ResourceManager;
import org.jboss.messaging.utils.SimpleString;
import org.jboss.messaging.utils.TypedProperties;

/*
 * @author <a href="mailto:jmesnil@redhat.com">Jeff Mesnil</a>
 * @author <a href="mailto:fox@redhat.com">Tim Fox</a>
 * 
 * @version <tt>$Revision$</tt>
 */
public class ManagementServiceImpl implements ManagementService
{
   // Constants -----------------------------------------------------

   private static final Logger log = Logger.getLogger(ManagementServiceImpl.class);

   private final MBeanServer mbeanServer;

   private final boolean jmxManagementEnabled;

   private final Map<ObjectName, Object> registry;

   private final NotificationBroadcasterSupport broadcaster;

   private PostOffice postOffice;

   private StorageManager storageManager;

   private HierarchicalRepository<Set<Role>> securityRepository;

   private HierarchicalRepository<AddressSettings> addressSettingsRepository;

   private MessagingServerControl managedServer;

   private final MessageCounterManager messageCounterManager = new MessageCounterManagerImpl();

   private SimpleString managementNotificationAddress = ConfigurationImpl.DEFAULT_MANAGEMENT_NOTIFICATION_ADDRESS;

   private SimpleString managementAddress = ConfigurationImpl.DEFAULT_MANAGEMENT_ADDRESS;

   private String managementClusterPassword = ConfigurationImpl.DEFAULT_MANAGEMENT_CLUSTER_PASSWORD;

   private long managementRequestTimeout = ConfigurationImpl.DEFAULT_MANAGEMENT_REQUEST_TIMEOUT;

   private boolean started = false;

   private boolean noticationsEnabled;

   private final Set<NotificationListener> listeners = new org.jboss.messaging.utils.ConcurrentHashSet<NotificationListener>();

   private ReplicationOperationInvoker replicationInvoker;

   // Constructor ----------------------------------------------------

   public ManagementServiceImpl(final MBeanServer mbeanServer, final boolean jmxManagementEnabled)
   {
      this.mbeanServer = mbeanServer;
      this.jmxManagementEnabled = jmxManagementEnabled;
      registry = new HashMap<ObjectName, Object>();
      broadcaster = new NotificationBroadcasterSupport();
      noticationsEnabled = true;
   }

   // Public --------------------------------------------------------

   // ManagementService implementation -------------------------

   public MessageCounterManager getMessageCounterManager()
   {
      return messageCounterManager;
   }

   public MessagingServerControlMBean registerServer(final PostOffice postOffice,
                                                     final StorageManager storageManager,
                                                     final Configuration configuration,
                                                     final HierarchicalRepository<AddressSettings> addressSettingsRepository,
                                                     final HierarchicalRepository<Set<Role>> securityRepository,
                                                     final ResourceManager resourceManager,
                                                     final RemotingService remotingService,
                                                     final MessagingServer messagingServer,
                                                     final QueueFactory queueFactory,
                                                     final boolean backup) throws Exception
   {
      this.postOffice = postOffice;
      this.addressSettingsRepository = addressSettingsRepository;
      this.securityRepository = securityRepository;
      this.storageManager = storageManager;
      
      managedServer = new MessagingServerControl(postOffice,
                                                 storageManager,
                                                 configuration,
                                                 resourceManager,
                                                 remotingService,
                                                 messagingServer,
                                                 messageCounterManager,
                                                 broadcaster,
                                                 queueFactory);
      ObjectName objectName = ObjectNames.getMessagingServerObjectName();
      registerInJMX(objectName, new ReplicationAwareMessagingServerControlWrapper(objectName,
                                                                                  managedServer,
                                                                                  replicationInvoker));
      registerInRegistry(objectName, managedServer);

      return managedServer;
   }

   public void unregisterServer() throws Exception
   {
      ObjectName objectName = ObjectNames.getMessagingServerObjectName();
      unregisterResource(objectName);
   }  
   
   public void registerAddress(final SimpleString address) throws Exception
   {
      ObjectName objectName = ObjectNames.getAddressObjectName(address);
      AddressControl addressControl = new AddressControl(address, postOffice, securityRepository);

      registerInJMX(objectName, new ReplicationAwareAddressControlWrapper(objectName,
                                                                          addressControl,
                                                                          replicationInvoker));

      registerInRegistry(objectName, addressControl);

      if (log.isDebugEnabled())
      {
         log.debug("registered address " + objectName);
      }
   }

   public void unregisterAddress(final SimpleString address) throws Exception
   {
      ObjectName objectName = ObjectNames.getAddressObjectName(address);

      unregisterResource(objectName);
   }

   public void registerQueue(final Queue queue, final SimpleString address, final StorageManager storageManager) throws Exception
   {
      MessageCounter counter = new MessageCounter(queue.getName().toString(),
                                                  null,
                                                  queue,
                                                  false,
                                                  queue.isDurable(),
                                                  messageCounterManager.getMaxDayCount());
      messageCounterManager.registerMessageCounter(queue.getName().toString(), counter);
      ObjectName objectName = ObjectNames.getQueueObjectName(address, queue.getName());
      QueueControl queueControl = new QueueControl(queue, address.toString(), postOffice, addressSettingsRepository, counter);
      registerInJMX(objectName, new ReplicationAwareQueueControlWrapper(objectName, queueControl, replicationInvoker));
      registerInRegistry(objectName, queueControl);

      if (log.isDebugEnabled())
      {
         log.debug("registered queue " + objectName);
      }
   }

   public void unregisterQueue(final SimpleString name, final SimpleString address) throws Exception
   {
      ObjectName objectName = ObjectNames.getQueueObjectName(address, name);
      unregisterResource(objectName);
      messageCounterManager.unregisterMessageCounter(name.toString());
   }
   
   public void registerDivert(Divert divert, DivertConfiguration config) throws Exception
   {
      ObjectName objectName = ObjectNames.getDivertObjectName(divert.getUniqueName());
      DivertControlMBean divertControl = new DivertControl(divert, config);
      registerInJMX(objectName, new StandardMBean(divertControl, DivertControlMBean.class));
      registerInRegistry(objectName, divertControl);

      if (log.isDebugEnabled())
      {
         log.debug("registered divert " + objectName);
      }
   }

   public void unregisterDivert(final SimpleString name) throws Exception
   {
      ObjectName objectName = ObjectNames.getDivertObjectName(name);
      unregisterResource(objectName);
   }

   public void registerAcceptor(final Acceptor acceptor, final TransportConfiguration configuration) throws Exception
   {
      ObjectName objectName = ObjectNames.getAcceptorObjectName(configuration.getName());
      AcceptorControlMBean control = new AcceptorControl(acceptor, configuration);
      registerInJMX(objectName, new StandardMBean(control, AcceptorControlMBean.class));
      registerInRegistry(objectName, control);
   }

   public void unregisterAcceptor(final String name) throws Exception
   {
      ObjectName objectName = ObjectNames.getAcceptorObjectName(name);
      unregisterResource(objectName);
   }

   public void registerBroadcastGroup(BroadcastGroup broadcastGroup, BroadcastGroupConfiguration configuration) throws Exception
   {
      ObjectName objectName = ObjectNames.getBroadcastGroupObjectName(configuration.getName());
      BroadcastGroupControlMBean control = new BroadcastGroupControl(broadcastGroup, configuration);
      registerInJMX(objectName, new StandardMBean(control, BroadcastGroupControlMBean.class));
      registerInRegistry(objectName, control);
   }

   public void unregisterBroadcastGroup(String name) throws Exception
   {
      ObjectName objectName = ObjectNames.getBroadcastGroupObjectName(name);
      unregisterResource(objectName);
   }

   public void registerDiscoveryGroup(DiscoveryGroup discoveryGroup, DiscoveryGroupConfiguration configuration) throws Exception
   {
      ObjectName objectName = ObjectNames.getDiscoveryGroupObjectName(configuration.getName());
      DiscoveryGroupControlMBean control = new DiscoveryGroupControl(discoveryGroup, configuration);
      registerInJMX(objectName, new StandardMBean(control, DiscoveryGroupControlMBean.class));
      registerInRegistry(objectName, control);
   }

   public void unregisterDiscoveryGroup(String name) throws Exception
   {
      ObjectName objectName = ObjectNames.getDiscoveryGroupObjectName(name);
      unregisterResource(objectName);
   }

   public void registerBridge(Bridge bridge, BridgeConfiguration configuration) throws Exception
   {
      ObjectName objectName = ObjectNames.getBridgeObjectName(configuration.getName());
      BridgeControlMBean control = new BridgeControl(bridge, configuration);
      registerInJMX(objectName, new StandardMBean(control, BridgeControlMBean.class));
      registerInRegistry(objectName, control);
   }

   public void unregisterBridge(String name) throws Exception
   {
      ObjectName objectName = ObjectNames.getBridgeObjectName(name);
      unregisterResource(objectName);
   }
   
   public void registerCluster(final ClusterConnection cluster, final ClusterConnectionConfiguration configuration) throws Exception
   {
      ObjectName objectName = ObjectNames.getClusterConnectionObjectName(configuration.getName());
      ClusterConnectionControlMBean control = new ClusterConnectionControl(cluster, configuration);
      registerInJMX(objectName, new StandardMBean(control, ClusterConnectionControlMBean.class));
      registerInRegistry(objectName, control);
   }

   public void unregisterCluster(final String name) throws Exception
   {
      ObjectName objectName = ObjectNames.getClusterConnectionObjectName(name);
      unregisterResource(objectName);
   }

   public ServerMessage handleMessage(final ServerMessage message)
   {
      // a reply message is sent with the result stored in the message body.
      // we set its type to MessageImpl.OBJECT_TYPE so that I can be received
      // as an ObjectMessage when using JMS to send management message
      ServerMessageImpl reply = new ServerMessageImpl(message);
      reply.setType(MessageImpl.OBJECT_TYPE);
      
      SimpleString objectName = (SimpleString)message.getProperty(ManagementHelper.HDR_JMX_OBJECTNAME);
      if (log.isDebugEnabled())
      {
         log.debug("handling management message for " + objectName);
      }
      Set<SimpleString> propertyNames = message.getPropertyNames();
      // use an array with all the property names to avoid a
      // ConcurrentModificationException
      // when invoking an operation or retrieving attributes (since they add
      // properties to the message)
      List<SimpleString> propNames = new ArrayList<SimpleString>(propertyNames);

      if (propNames.contains(ManagementHelper.HDR_JMX_OPERATION_NAME))
      {
         SimpleString operation = (SimpleString)message.getProperty(ManagementHelper.HDR_JMX_OPERATION_NAME);
         List<Object> operationParameters = ManagementHelper.retrieveOperationParameters(message);

         if (operation != null)
         {
            try
            {
               Object result = invokeOperation(objectName.toString(), operation.toString(), operationParameters);
               reply.putBooleanProperty(ManagementHelper.HDR_JMX_OPERATION_SUCCEEDED, true);
               ManagementHelper.storeResult(reply, result);
            }
            catch (Exception e)
            {               
               log.warn("exception while invoking " + operation + " on " + objectName, e);
               reply.putBooleanProperty(ManagementHelper.HDR_JMX_OPERATION_SUCCEEDED, false);
               String exceptionMessage = e.getMessage();
               if (e instanceof InvocationTargetException)
               {
                  exceptionMessage = ((InvocationTargetException)e).getTargetException().getMessage();
               }
               if (e != null)
               {
                  reply.putStringProperty(ManagementHelper.HDR_JMX_OPERATION_EXCEPTION,
                                            new SimpleString(exceptionMessage));
               }
            }
         }
      }
      else
      {
         for (SimpleString propertyName : propNames)
         {
            if (propertyName.equals(ManagementHelper.HDR_JMX_ATTRIBUTE))
            {
               SimpleString attribute = (SimpleString)message.getProperty(propertyName);
               Object result = getAttribute(objectName.toString(), attribute.toString());
               ManagementHelper.storeResult(reply, result);
            }
         }
      }
      
      return reply;
   }

   public void registerResource(final ObjectName objectName, final Object resource) throws Exception
   {
      registerInRegistry(objectName, resource);
      registerInJMX(objectName, resource);
   }

   public void unregisterResource(final ObjectName objectName) throws Exception
   {
      unregisterFromRegistry(objectName);
      unregisterFromJMX(objectName);
   }

   public Object getResource(final ObjectName objectName)
   {
      return registry.get(objectName);
   }

   public void registerInJMX(final ObjectName objectName, final Object managedResource) throws Exception
   {
      if (!jmxManagementEnabled)
      {
         return;
      }
      synchronized (mbeanServer)
      {
         unregisterFromJMX(objectName);
         mbeanServer.registerMBean(managedResource, objectName);
      }
   }

   public void registerInRegistry(final ObjectName objectName, final Object managedResource)
   {
      unregisterFromRegistry(objectName);
      registry.put(objectName, managedResource);
   }

   public void addNotificationListener(final NotificationListener listener)
   {
      listeners.add(listener);
   }

   public void removeNotificationListener(final NotificationListener listener)
   {
      listeners.remove(listener);
   }

   public SimpleString getManagementAddress()
   {
      return managementAddress;
   }

   public void setManagementAddress(SimpleString managementAddress)
   {
      this.managementAddress = managementAddress;
   }

   public SimpleString getManagementNotificationAddress()
   {
      return managementNotificationAddress;
   }

   public void setManagementNotificationAddress(SimpleString managementNotificationAddress)
   {
      this.managementNotificationAddress = managementNotificationAddress;
   }

   public String getClusterPassword()
   {
      return managementClusterPassword;
   }

   public void setClusterPassword(String clusterPassword)
   {
      this.managementClusterPassword = clusterPassword;
   }

   public long getManagementRequestTimeout()
   {
      return managementRequestTimeout;
   }

   public void setManagementRequestTimeout(long timeout)
   {
      this.managementRequestTimeout = timeout;
   }

   public ReplicationOperationInvoker getReplicationOperationInvoker()
   {
      return replicationInvoker;
   }

   // MessagingComponent implementation -----------------------------

   public void start() throws Exception
   {
      replicationInvoker = new ReplicationOperationInvokerImpl(managementClusterPassword,
                                                               managementAddress,
                                                               managementRequestTimeout);
      started = true;
   }

   public synchronized void stop() throws Exception
   {
      Set<ObjectName> objectNames = new HashSet<ObjectName>(registry.keySet());

      for (ObjectName objectName : objectNames)
      {
         unregisterResource(objectName);
      }

      replicationInvoker.stop();

      started = false;
   }

   public boolean isStarted()
   {
      return started;
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   private void unregisterFromRegistry(final ObjectName objectName)
   {
      registry.remove(objectName);
   }

   // the JMX unregistration is synchronized to avoid race conditions if 2 clients tries to
   // unregister the same resource (e.g. a queue) at the same time since unregisterMBean()
   // will throw an exception if the MBean has already been unregistered
   private void unregisterFromJMX(final ObjectName objectName) throws Exception
   {
      if (!jmxManagementEnabled)
      {
         return;
      }
      synchronized (mbeanServer)
      {
         if (mbeanServer.isRegistered(objectName))
         {
            mbeanServer.unregisterMBean(objectName);
         }
      }
   }
   
   public void sendNotification(final Notification notification) throws Exception
   {     
      if (managedServer != null && noticationsEnabled)
      {
         // This needs to be synchronized since we need to ensure notifications are processed in strict sequence
         synchronized (this)
         {
            // First send to any local listeners
            for (NotificationListener listener : listeners)
            {
               try
               {
                  listener.onNotification(notification);
               }
               catch (Exception e)
               {
                  // Exception thrown from one listener should not stop execution of others
                  log.error("Failed to call listener", e);
               }
            }

            // Now send message

            ServerMessage notificationMessage = new ServerMessageImpl(storageManager.generateUniqueID());
   
            notificationMessage.setBody(ChannelBuffers.EMPTY_BUFFER);
            // Notification messages are always durable so the user can choose whether to add a durable queue to consume
            // them in
            notificationMessage.setDurable(true);
            notificationMessage.setDestination(managementNotificationAddress);

            TypedProperties notifProps;
            if (notification.getProperties() != null)
            {
               notifProps = new TypedProperties(notification.getProperties());
            }
            else
            {
               notifProps = new TypedProperties();
            }

            notifProps.putStringProperty(ManagementHelper.HDR_NOTIFICATION_TYPE,
                                         new SimpleString(notification.getType().toString()));

            notifProps.putLongProperty(ManagementHelper.HDR_NOTIFICATION_TIMESTAMP, System.currentTimeMillis());

            notificationMessage.putTypedProperties(notifProps);

            postOffice.route(notificationMessage, null);
         }
      }
   }

   public void enableNotifications(boolean enabled)
   {
      noticationsEnabled = enabled;
   }

   public Object getAttribute(final String objectNameStr, final String attribute)
   {
      try
      {
         ObjectName objectName = ObjectName.getInstance(objectNameStr);
         Object resource = registry.get(objectName);
         Method method = null;

         try
         {
            method = resource.getClass().getMethod("get" + attribute, new Class[0]);
         }
         catch (NoSuchMethodException nsme)
         {
            try
            {
               method = resource.getClass().getMethod("is" + attribute, new Class[0]);
            }
            catch (NoSuchMethodException nsme2)
            {
               throw new IllegalArgumentException("no getter method for " + attribute);
            }
         }
         return method.invoke(resource, new Object[0]);
      }
      catch (Throwable t)
      {
         throw new IllegalStateException("Problem while retrieving attribute " + attribute, t);
      }
   }

   private Object invokeOperation(final String objectNameStr, final String operation, final List<Object> params) throws Exception
   {
      ObjectName objectName = ObjectName.getInstance(objectNameStr);
      Object resource = registry.get(objectName);
      Method method = null;

      Method[] methods = resource.getClass().getMethods();
      for (Method m : methods)
      {
         if (m.getName().equals(operation) && m.getParameterTypes().length == params.size())
         {
            method = m;
         }
      }
      if (method == null)
      {
         throw new IllegalArgumentException("no operation " + operation + "/" + params.size());
      }
      Object[] p = params.toArray(new Object[params.size()]);
      Object result = method.invoke(resource, p);
      return result;
   }

   // Inner classes -------------------------------------------------
}
