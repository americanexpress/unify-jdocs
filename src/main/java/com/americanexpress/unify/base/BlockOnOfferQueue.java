/*
 * Copyright 2020 American Express Travel Related Services Company, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.americanexpress.unify.base;

import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class BlockOnOfferQueue<T> implements BlockingQueue<T> {

  private BlockingQueue<T> q = null;

  public BlockOnOfferQueue(BlockingQueue<T> q) {
    this.q = q;
  }

  @Override
  public boolean add(T t) {
    return q.add(t);
  }

  @Override
  public boolean offer(T t) {
    try {
      q.put(t);
    }
    catch (InterruptedException e) {
      return false;
    }
    return true;
  }

  @Override
  public void put(T t) throws InterruptedException {
    q.put(t);
  }

  @Override
  public boolean offer(T t, long timeout, TimeUnit unit) throws InterruptedException {
    return q.offer(t, timeout, unit);
  }

  @Override
  public T take() throws InterruptedException {
    return q.take();
  }

  @Override
  public T poll(long timeout, TimeUnit unit) throws InterruptedException {
    return q.poll(timeout, unit);
  }

  @Override
  public int remainingCapacity() {
    return q.remainingCapacity();
  }

  @Override
  public boolean remove(Object o) {
    return q.remove(o);
  }

  @Override
  public boolean contains(Object o) {
    return q.contains(o);
  }

  @Override
  public int drainTo(Collection<? super T> c) {
    return q.drainTo(c);
  }

  @Override
  public int drainTo(Collection<? super T> c, int maxElements) {
    return q.drainTo(c, maxElements);
  }

  @Override
  public T remove() {
    return q.remove();
  }

  @Override
  public T poll() {
    return q.poll();
  }

  @Override
  public T element() {
    return q.element();
  }

  @Override
  public T peek() {
    return q.peek();
  }

  @Override
  public int size() {
    return q.size();
  }

  @Override
  public boolean isEmpty() {
    return q.isEmpty();
  }

  @Override
  public Iterator<T> iterator() {
    return q.iterator();
  }

  @Override
  public Object[] toArray() {
    return q.toArray();
  }

  @Override
  public <T1> T1[] toArray(T1[] a) {
    return q.toArray(a);
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    return q.containsAll(c);
  }

  @Override
  public boolean addAll(Collection<? extends T> c) {
    return q.addAll(c);
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    return q.removeAll(c);
  }

  @Override
  public boolean removeIf(Predicate<? super T> filter) {
    return q.removeIf(filter);
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    return q.retainAll(c);
  }

  @Override
  public void clear() {
    q.clear();
  }

  @Override
  public boolean equals(Object o) {
    return q.equals(o);
  }

  @Override
  public int hashCode() {
    return q.hashCode();
  }

  @Override
  public Spliterator<T> spliterator() {
    return q.spliterator();
  }

  @Override
  public Stream<T> stream() {
    return q.stream();
  }

  @Override
  public Stream<T> parallelStream() {
    return q.parallelStream();
  }

  @Override
  public void forEach(Consumer<? super T> action) {
    q.forEach(action);
  }

}
