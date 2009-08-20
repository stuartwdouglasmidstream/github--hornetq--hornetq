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

package org.hornetq.core.remoting.impl.wireformat;

import org.hornetq.core.remoting.spi.MessagingBuffer;
import org.hornetq.utils.DataConstants;

/**
 * @author <a href="mailto:tim.fox@jboss.com">Tim Fox</a>
 * @author <a href="mailto:jmesnil@redhat.com">Jeff Mesnil</a>.
 * 
 * @version <tt>$Revision$</tt>
 */
public class CreateSessionResponseMessage extends PacketImpl
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   private int serverVersion;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   public CreateSessionResponseMessage(final int serverVersion)
   {
      super(CREATESESSION_RESP);

      this.serverVersion = serverVersion;
   }

   public CreateSessionResponseMessage()
   {
      super(CREATESESSION_RESP);
   }

   // Public --------------------------------------------------------

   @Override
   public boolean isResponse()
   {
      return true;
   }

   public int getServerVersion()
   {
      return serverVersion;
   }

   @Override
   public void encodeBody(final MessagingBuffer buffer)
   {
      buffer.writeInt(serverVersion);
   }

   @Override
   public void decodeBody(final MessagingBuffer buffer)
   {
      serverVersion = buffer.readInt();
   }
   
   public int getRequiredBufferSize()
   {
      return BASIC_PACKET_SIZE + DataConstants.SIZE_INT; 
   }

   @Override
   public boolean equals(final Object other)
   {
      if (other instanceof CreateSessionResponseMessage == false)
      {
         return false;
      }

      CreateSessionResponseMessage r = (CreateSessionResponseMessage)other;

      boolean matches = super.equals(other) && serverVersion == r.serverVersion;

      return matches;
   }

   @Override
   public final boolean isRequiresConfirmations()
   {
      return false;
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}