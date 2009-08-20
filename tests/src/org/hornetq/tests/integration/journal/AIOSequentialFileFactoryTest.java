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

package org.hornetq.tests.integration.journal;

import java.io.File;
import java.nio.ByteBuffer;

import org.hornetq.core.asyncio.impl.AsynchronousFileImpl;
import org.hornetq.core.journal.SequentialFile;
import org.hornetq.core.journal.SequentialFileFactory;
import org.hornetq.core.journal.impl.AIOSequentialFileFactory;
import org.hornetq.tests.unit.core.journal.impl.SequentialFileFactoryTestBase;

/**
 * 
 * A AIOSequentialFileFactoryTest
 * 
 * @author <a href="mailto:clebert.suconic@jboss.com">Clebert Suconic</a>
 *
 */
public class AIOSequentialFileFactoryTest extends SequentialFileFactoryTestBase
{

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();

      if (!AsynchronousFileImpl.isLoaded())
      {
         fail(String.format("libAIO is not loaded on %s %s %s",
                            System.getProperty("os.name"),
                            System.getProperty("os.arch"),
                            System.getProperty("os.version")));
      }

      File file = new File(getTestDir());

      deleteDirectory(file);

      file.mkdirs();
   }

   @Override
   protected SequentialFileFactory createFactory()
   {
      return new AIOSequentialFileFactory(getTestDir());
   }

   public void testBuffer() throws Exception
   {
      SequentialFile file = factory.createSequentialFile("filtetmp.log", 10);
      file.open();
      ByteBuffer buff = factory.newBuffer(10);
      assertEquals(512, buff.limit());
      file.close();
      factory.releaseBuffer(buff);
   }

}