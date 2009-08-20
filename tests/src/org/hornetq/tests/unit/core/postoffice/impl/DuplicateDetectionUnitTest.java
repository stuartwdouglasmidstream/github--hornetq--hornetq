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

package org.hornetq.tests.unit.core.postoffice.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.hornetq.core.config.Configuration;
import org.hornetq.core.config.impl.ConfigurationImpl;
import org.hornetq.core.paging.PageTransactionInfo;
import org.hornetq.core.paging.PagingManager;
import org.hornetq.core.paging.PagingStore;
import org.hornetq.core.persistence.QueueBindingInfo;
import org.hornetq.core.persistence.impl.journal.JournalStorageManager;
import org.hornetq.core.postoffice.PostOffice;
import org.hornetq.core.postoffice.impl.DuplicateIDCacheImpl;
import org.hornetq.core.server.JournalType;
import org.hornetq.core.server.Queue;
import org.hornetq.core.server.ServerMessage;
import org.hornetq.core.transaction.impl.ResourceManagerImpl;
import org.hornetq.tests.util.RandomUtil;
import org.hornetq.tests.util.ServiceTestBase;
import org.hornetq.utils.Pair;
import org.hornetq.utils.SimpleString;

/**
 * A DuplicateDetectionUnitTest
 *
 * @author <a href="mailto:clebert.suconic@jboss.org">Clebert Suconic</a>
 *
 *
 */
public class DuplicateDetectionUnitTest extends ServiceTestBase
{

   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   @Override
   protected void tearDown() throws Exception
   {
      super.tearDown();
   }

   // Public --------------------------------------------------------

   public void testReloadDuplication() throws Exception
   {

      JournalStorageManager journal = null;

      try
      {
         clearData();

         SimpleString ADDRESS = new SimpleString("address");

         Configuration configuration = createConfigForJournal();

         configuration.start();

         configuration.setJournalType(JournalType.ASYNCIO);

         ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(ConfigurationImpl.DEFAULT_SCHEDULED_THREAD_POOL_MAX_SIZE);

         journal = new JournalStorageManager(configuration, Executors.newCachedThreadPool());

         journal.start();
         journal.loadBindingJournal(new ArrayList<QueueBindingInfo>());

         HashMap<SimpleString, List<Pair<byte[], Long>>> mapDups = new HashMap<SimpleString, List<Pair<byte[], Long>>>();

         journal.loadMessageJournal(new FakePagingManager(),
                                    new ResourceManagerImpl(0, 0, scheduledThreadPool),
                                    new HashMap<Long, Queue>(),
                                    mapDups);

         assertEquals(0, mapDups.size());

         DuplicateIDCacheImpl cacheID = new DuplicateIDCacheImpl(ADDRESS, 10, journal, true);

         for (int i = 0; i < 100; i++)
         {
            cacheID.addToCache(RandomUtil.randomBytes(), null);
         }

         journal.stop();

         journal = new JournalStorageManager(configuration, Executors.newCachedThreadPool());
         journal.start();
         journal.loadBindingJournal(new ArrayList<QueueBindingInfo>());

         journal.loadMessageJournal(new FakePagingManager(),
                                    new ResourceManagerImpl(0, 0, scheduledThreadPool),
                                    new HashMap<Long, Queue>(),
                                    mapDups);

         assertEquals(1, mapDups.size());

         List<Pair<byte[], Long>> values = mapDups.get(ADDRESS);

         assertEquals(10, values.size());

         cacheID = new DuplicateIDCacheImpl(ADDRESS, 10, journal, true);
         cacheID.load(values);

         for (int i = 0; i < 100; i++)
         {
            cacheID.addToCache(RandomUtil.randomBytes(), null);
         }

         journal.stop();

         mapDups.clear();

         journal = new JournalStorageManager(configuration, Executors.newCachedThreadPool());
         journal.start();
         journal.loadBindingJournal(new ArrayList<QueueBindingInfo>());

         journal.loadMessageJournal(new FakePagingManager(),
                                    new ResourceManagerImpl(0, 0, scheduledThreadPool),
                                    new HashMap<Long, Queue>(),
                                    mapDups);

         assertEquals(1, mapDups.size());

         values = mapDups.get(ADDRESS);

         assertEquals(10, values.size());
      }
      finally
      {
         if (journal != null)
         {
            try
            {
               journal.stop();
            }
            catch (Throwable ignored)
            {
            }
         }
      }

   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
   static class FakePagingManager implements PagingManager
   {

      public void activate()
      {
      }

      public long addSize(final long size)
      {
         return 0;
      }

      public void addTransaction(final PageTransactionInfo pageTransaction)
      {
      }

      public PagingStore createPageStore(final SimpleString destination) throws Exception
      {
         return null;
      }

      public long getTotalMemory()
      {
         return 0;
      }
      
      public SimpleString[] getStoreNames()
      {
         return null;
      }

      public long getMaxMemory()
      {
         return 0;
      }

      public PagingStore getPageStore(final SimpleString address) throws Exception
      {
         return null;
      }

      public PageTransactionInfo getTransaction(final long transactionID)
      {
         return null;
      }

      public boolean isBackup()
      {
         return false;
      }

      public boolean isGlobalPageMode()
      {
         return false;
      }

      public boolean isPaging(final SimpleString destination) throws Exception
      {
         return false;
      }

      public boolean page(final ServerMessage message, final boolean duplicateDetection) throws Exception
      {
         return false;
      }

      public boolean page(final ServerMessage message, final long transactionId, final boolean duplicateDetection) throws Exception
      {
         return false;
      }

      public void reloadStores() throws Exception
      {
      }

      public void removeTransaction(final long transactionID)
      {

      }

      public void setGlobalPageMode(final boolean globalMode)
      {
      }

      public void setPostOffice(final PostOffice postOffice)
      {
      }

      public void resumeDepages()
      {
      }

      public void sync(final Collection<SimpleString> destinationsToSync) throws Exception
      {
      }

      public boolean isStarted()
      {
         return false;
      }

      public void start() throws Exception
      {
      }

      public void stop() throws Exception
      {
      }

      /* (non-Javadoc)
       * @see org.hornetq.core.paging.PagingManager#isGlobalFull()
       */
      public boolean isGlobalFull()
      {
         return false;
      }

   }

}