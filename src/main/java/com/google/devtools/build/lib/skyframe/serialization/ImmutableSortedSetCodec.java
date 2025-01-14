// Copyright 2018 The Bazel Authors. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.devtools.build.lib.skyframe.serialization;

import static com.google.devtools.build.lib.skyframe.serialization.ArrayProcessor.deserializeObjectArrayFully;
import static com.google.devtools.build.lib.unsafe.UnsafeProvider.getFieldOffset;

import com.google.common.collect.ImmutableSortedSet;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.function.Supplier;

/**
 * {@link ObjectCodec} for {@link ImmutableSortedSet}. Comparator must be serializable, ideally a
 * registered constant.
 */
class ImmutableSortedSetCodec<E> extends DeferredObjectCodec<ImmutableSortedSet<E>> {
  @SuppressWarnings("unchecked")
  @Override
  public Class<ImmutableSortedSet<E>> getEncodedClass() {
    return (Class<ImmutableSortedSet<E>>) (Class<?>) ImmutableSortedSet.class;
  }

  @Override
  public void serialize(
      SerializationContext context, ImmutableSortedSet<E> object, CodedOutputStream codedOut)
      throws SerializationException, IOException {
    codedOut.writeInt32NoTag(object.size());
    context.serialize(object.comparator(), codedOut);
    for (Object obj : object) {
      context.serialize(obj, codedOut);
    }
  }

  @Override
  public Supplier<ImmutableSortedSet<E>> deserializeDeferred(
      AsyncDeserializationContext context, CodedInputStream codedIn)
      throws SerializationException, IOException {
    int size = codedIn.readInt32();
    SortedSetShimForEfficientDeserialization<E> sortedSetShim =
        new SortedSetShimForEfficientDeserialization<>(size);
    context.deserializeFully(codedIn, sortedSetShim, COMPARATOR_OFFSET);
    deserializeObjectArrayFully(context, codedIn, sortedSetShim.sortedElementsArray, size);
    return sortedSetShim;
  }

  private static final long COMPARATOR_OFFSET;

  static {
    try {
      COMPARATOR_OFFSET =
          getFieldOffset(SortedSetShimForEfficientDeserialization.class, "comparator");
    } catch (NoSuchFieldException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  /**
   * Implementation of parts of the {@link SortedSet} interface minimally needed for efficient
   * {@link ImmutableSortedSet} construction that avoids re-sorting the list of elements.
   */
  @SuppressWarnings("JdkObsolete") // SortedSet required for ImmutableSortedSet.copyOfSorted
  private static class SortedSetShimForEfficientDeserialization<E>
      implements SortedSet<E>, Supplier<ImmutableSortedSet<E>> {
    private Comparator<E> comparator;
    private final Object[] sortedElementsArray;

    private SortedSetShimForEfficientDeserialization(int size) {
      this.sortedElementsArray = new Object[size];
    }

    @Override
    public ImmutableSortedSet<E> get() {
      return ImmutableSortedSet.copyOfSorted(this);
    }

    @Override
    public Comparator<? super E> comparator() {
      return comparator;
    }

    @Override
    public Object[] toArray() {
      return sortedElementsArray;
    }

    @Override
    public <T> T[] toArray(T[] a) {
      throw new UnsupportedOperationException();
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
      throw new UnsupportedOperationException();
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
      throw new UnsupportedOperationException();
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
      throw new UnsupportedOperationException();
    }

    @Override
    public E first() {
      throw new UnsupportedOperationException();
    }

    @Override
    public E last() {
      throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(Object o) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<E> iterator() {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(E e) {
      return false;
    }

    @Override
    public boolean remove(Object o) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object o) {
      throw new UnsupportedOperationException();
    }

    @Override
    public int hashCode() {
      throw new UnsupportedOperationException();
    }
  }
}
