/*
  * JBoss, Home of Professional Open Source
  * Copyright 2005, JBoss Inc., and individual contributors as indicated
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
package org.jboss.messaging.util;

import javax.jms.Session;

/**
 * @author <a href="mailto:ovidiu@jboss.org">Ovidiu Feodorov</a>
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class Util
 {
   // Constants -----------------------------------------------------

   // Static --------------------------------------------------------

   public static String guidToString(Object o)
   {
      if (o == null)
      {
         return "null";
      }
      if (!(o instanceof String))
      {
         return o.toString();
      }
      String s = (String)o;
      int idx = s.lastIndexOf('-', s.lastIndexOf('-') - 1);
      if (idx < 0)
      {
         return s;
      }
      return "...-" + s.substring(idx + 1);
   }

   public static String acknowledgmentModeToString(int ackMode)
   {

      if (ackMode == Session.AUTO_ACKNOWLEDGE)
      {
         return "AUTO_ACKNOWLEDGE";
      }
      else if (ackMode == Session.CLIENT_ACKNOWLEDGE)
      {
         return "CLIENT_ACKNOWLEDGE";
      }
      else if (ackMode == Session.DUPS_OK_ACKNOWLEDGE)
      {
         return "DUPS_OK_ACKNOWLEDGE";
      }
      else if (ackMode == Session.SESSION_TRANSACTED)
      {
         return "SESSION_TRANSACTED";
      }
      return "UNKNOWN: " + ackMode;
   }

   // Attributes ----------------------------------------------------

   // Constructors --------------------------------------------------

   private Util()
   {
   }

   // Public --------------------------------------------------------

   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
