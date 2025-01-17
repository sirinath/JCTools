/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * This package aims to fill a gap in current JDK implementations in offering lock free (wait free where possible)
 * queues for inter-thread message passing with finer grained guarantees and an emphasis on performance.<br>
 * At the time of writing the only lock free queue available in the JDK is {@link java.util.concurrent.ConcurrentLinkedQueue}
 * which is an unbounded multi-producer, multi-consumer queue which is further encumbered by the need to implement
 * the full range of {@link java.util.Queue} methods. In this package we offer a range of implementations:
 * <ol>
 * <li> Bounded/Unbounded SPSC queues - Serving the Single Producer Single Consumer use case.
 * <li> Bounded/Unbounded MPSC queues - The Multi Producer Single Consumer case also has a multi-lane implementation on
 * offer which trades the FIFO ordering(re-ordering is not limited) for reduced contention and increased throughput
 * under contention.
 * <li> Bounded SPMC/MPMC queues
 * </ol>
 * <p>
 * <b>Limited Queue methods support:</b><br>
 * The queues implement a subset of the {@link java.util.Queue} interface which is documented under the
 * {@link org.jctools.queues.MessagePassingQueue} interface. In particular {@link java.util.Queue#iterator()} is not
 * supported and dependent methods from {@link java.util.AbstractQueue} are also not supported such as:
 * <ol>
 * <li> {@link java.util.Queue#remove(Object)}
 * <li> {@link java.util.Queue#removeAll(java.util.Collection)}
 * <li> {@link java.util.Queue#removeIf(java.util.function.Predicate)}
 * <li> {@link java.util.Queue#contains(Object)}
 * <li> {@link java.util.Queue#containsAll(java.util.Collection)}
 * </ol>
 * <p>
 * <b>Memory layout controls and False Sharing:</b><br>
 * The classes in this package use what is considered at the moment the most reliable method of controlling
 * class field layout, namely inheritance. The method is described in this <a
 * href="http://psy-lob-saw.blogspot.com/2013/05/know-thy-java-object-memory-layout.html">post</a> which also
 * covers why other methods are currently suspect.<br>
 * Note that we attempt to tackle both active (write/write) and passive(read/write) false sharing case:
 * <ol>
 * <li> Hot counters (or write locations) are padded.
 * <li> Read-Only shared fields are padded.
 * <li> Array edges are padded.
 * </ol> 
 * <p>
 * <b>Use of sun.misc.Unsafe:</b><br>
 * A choice is made in this library to utilize sun.misc.Unsafe for performance reasons. In this package we have two
 * use cases:
 * <ol>
 * <li>The queue counters in the queues are all inlined (i.e. are primitive fields of the queue classes). To allow
 * lazySet/CAS semantics to these fields we could use {@link java.util.concurrent.atomic.AtomicLongFieldUpdater} but choose not to.
 * <li>We use Unsafe to gain volatile/lazySet access to array elements. We could use {@link java.util.concurrent.atomic.AtomicReferenceArray} but
 * the extra reference chase and redundant boundary checks are considered too high a price to pay at this time.
 * </ol>
 * Both use cases should be made obsolete by VarHandles at some point.
 * 
 * <b>Avoiding redundant loads of fields<b/>
 * Because a volatile load will force any following field access to reload the field value an effort is made to cache field values in local variables
 * where possible and expose interfaces which allow the code to capitalize on such caching. As a convention the local variable name will be the field
 * name and will be final.
 * @author nitsanw
 */
package org.jctools.queues;

