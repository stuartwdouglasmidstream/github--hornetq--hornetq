/*
 * Copyright 2009 Red Hat, Inc.
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.hornetq.api.core.management;

import javax.management.ObjectName;

import org.hornetq.api.SimpleString;
import org.hornetq.api.jms.management.ConnectionFactoryControl;
import org.hornetq.api.jms.management.JMSQueueControl;
import org.hornetq.api.jms.management.JMSServerControl;
import org.hornetq.api.jms.management.TopicControl;
import org.hornetq.core.config.impl.ConfigurationImpl;

/**
 * Helper class to build ObjectNames for HornetQ resources.
 *
 * @author <a href="jmesnil@redhat.com">Jeff Mesnil</a>
 *
 */
public class ObjectNameBuilder
{

   // Constants -----------------------------------------------------

   /**
    * Default JMX domain for HornetQ resources.
    */
   public static ObjectNameBuilder DEFAULT = new ObjectNameBuilder(ConfigurationImpl.DEFAULT_JMX_DOMAIN);

   public static final String JMS_MODULE = "JMS";

   public static final String CORE_MODULE = "Core";

   // Attributes ----------------------------------------------------

   private final String domain;

   // Static --------------------------------------------------------

   public static ObjectNameBuilder create(final String domain)
   {
      return new ObjectNameBuilder(domain);
   }

   // Constructors --------------------------------------------------

   private ObjectNameBuilder(final String domain)
   {
      this.domain = domain;
   }

   // Public --------------------------------------------------------

   /**
    * Returns the ObjectName used by the single HornetQServerControl.
    */
   public ObjectName getHornetQServerObjectName() throws Exception
   {
      return ObjectName.getInstance(domain + ":module=Core,type=Server");
   }

   /**
    * Returns the ObjectName used by AddressControl.
    * 
    * @see AddressControl
    */
   public ObjectName getAddressObjectName(final SimpleString address) throws Exception
   {
      return createObjectName(ObjectNameBuilder.CORE_MODULE, "Address", address.toString());
   }

   /**
    * Returns the ObjectName used by QueueControl.
    * 
    * @see QueueControl
    */
   public ObjectName getQueueObjectName(final SimpleString address, final SimpleString name) throws Exception
   {
      return ObjectName.getInstance(String.format("%s:module=%s,type=%s,address=%s,name=%s",
                                                  domain,
                                                  ObjectNameBuilder.CORE_MODULE,
                                                  "Queue",
                                                  ObjectName.quote(address.toString()),
                                                  ObjectName.quote(name.toString())));
   }

   /**
    * Returns the ObjectName used by DivertControl.
    * 
    * @see DivertControl
    */
   public ObjectName getDivertObjectName(final SimpleString name) throws Exception
   {
      return createObjectName(ObjectNameBuilder.CORE_MODULE, "Divert", name.toString());
   }

   /**
    * Returns the ObjectName used by AcceptorControl.
    * 
    * @see AcceptorControl
    */
   public ObjectName getAcceptorObjectName(final String name) throws Exception
   {
      return createObjectName(ObjectNameBuilder.CORE_MODULE, "Acceptor", name);
   }

   /**
    * Returns the ObjectName used by BroadcastGroupControl.
    * 
    * @see BroadcastGroupControl
    */
   public ObjectName getBroadcastGroupObjectName(final String name) throws Exception
   {
      return createObjectName(ObjectNameBuilder.CORE_MODULE, "BroadcastGroup", name);
   }

   /**
    * Returns the ObjectName used by BridgeControl.
    * 
    * @see BridgeControl
    */
   public ObjectName getBridgeObjectName(final String name) throws Exception
   {
      return createObjectName(ObjectNameBuilder.CORE_MODULE, "JMSBridge", name);
   }

   /**
    * Returns the ObjectName used by ClusterConnectionControl.
    * 
    * @see ClusterConnectionControl
    */
   public ObjectName getClusterConnectionObjectName(final String name) throws Exception
   {
      return createObjectName(ObjectNameBuilder.CORE_MODULE, "ClusterConnection", name);
   }

   /**
    * Returns the ObjectName used by DiscoveryGroupControl.
    * 
    * @see DiscoveryGroupControl
    */
   public ObjectName getDiscoveryGroupObjectName(final String name) throws Exception
   {
      return createObjectName(ObjectNameBuilder.CORE_MODULE, "DiscoveryGroup", name);
   }

   /**
    * Returns the ObjectName used by JMSServerControl.
    * 
    * @see JMSServerControl
    */
   public ObjectName getJMSServerObjectName() throws Exception
   {
      return ObjectName.getInstance(domain + ":module=JMS,type=Server");
   }

   /**
    * Returns the ObjectName used by JMSQueueControl.
    * 
    * @see JMSQueueControl
    */
   public ObjectName getJMSQueueObjectName(final String name) throws Exception
   {
      return createObjectName(ObjectNameBuilder.JMS_MODULE, "Queue", name);
   }

   /**
    * Returns the ObjectName used by TopicControl.
    * 
    * @see TopicControl
    */
   public ObjectName getJMSTopicObjectName(final String name) throws Exception
   {
      return createObjectName(ObjectNameBuilder.JMS_MODULE, "Topic", name);
   }

   /**
    * Returns the ObjectName used by ConnectionFactoryControl.
    * 
    * @see ConnectionFactoryControl
    */
   public ObjectName getConnectionFactoryObjectName(final String name) throws Exception
   {
      return createObjectName(ObjectNameBuilder.JMS_MODULE, "ConnectionFactory", name);
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   private ObjectName createObjectName(final String module, final String type, final String name) throws Exception
   {
      return ObjectName.getInstance(String.format("%s:module=%s,type=%s,name=%s",
                                                  domain,
                                                  module,
                                                  type,
                                                  ObjectName.quote(name)));
   }

   // Inner classes -------------------------------------------------

}
