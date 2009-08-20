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

package org.hornetq.core.server;

import java.util.List;

import org.hornetq.core.transaction.Transaction;

/**
 * 
 * A ServerConsumer
 * 
 * @author <a href="mailto:tim.fox@jboss.com">Tim Fox</a>
 *
 */
public interface ServerConsumer extends Consumer
{
	long getID();
	
	void close() throws Exception;
	
	int getCountOfPendingDeliveries();

	List<MessageReference> cancelRefs(boolean lastConsumedAsDelivered, Transaction tx) throws Exception;
	
	void setStarted(boolean started);
	
	void receiveCredits(int credits) throws Exception;
	
	Queue getQueue();

	MessageReference getExpired(long messageID) throws Exception;
	
	void acknowledge(boolean autoCommitAcks, Transaction tx, long messageID) throws Exception;
	
	void failedOver();
	
	void deliverReplicated(long messageID) throws Exception;
	
	void lock();
	
	void unlock();
}