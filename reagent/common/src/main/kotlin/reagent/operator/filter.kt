/*
 * Copyright 2017 Jake Wharton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package reagent.operator

import reagent.Emitter
import reagent.Observable
import reagent.Maybe
import reagent.One
import reagent.Task
import kotlin.DeprecationLevel.ERROR

fun <I> Observable<I>.filter(predicate: (I) -> Boolean): Observable<I> = ObservableFilter(this, predicate)

fun <I> Maybe<I>.filter(predicate: (I) -> Boolean): Maybe<I> = MaybeFilter(this, predicate)

fun <I> One<I>.filter(predicate: (I) -> Boolean): Maybe<I> = OneFilter(this, predicate)

@Suppress("DeprecatedCallableAddReplaceWith") // TODO https://youtrack.jetbrains.com/issue/KT-19512
@Deprecated("Task has no items so filtering does not make sense.", level = ERROR)
fun Task.filter(predicate: (Nothing) -> Boolean) = this

internal class ObservableFilter<out I>(
    private val upstream: Observable<I>,
    private val predicate: (I) -> Boolean
) : Observable<I>() {
  override suspend fun subscribe(emit: Emitter<I>) {
    upstream.subscribe {
      if (predicate(it)) {
        emit(it)
      }
    }
  }
}

internal class MaybeFilter<out I>(
    private val upstream: Maybe<I>,
    private val predicate: (I) -> Boolean
) : Maybe<I>() {
  override suspend fun produce(): I? {
    upstream.produce()?.let { if (predicate(it)) return it }
    return null
  }
}

internal class OneFilter<out I>(
    private val upstream: One<I>,
    private val predicate: (I) -> Boolean
) : Maybe<I>() {
  override suspend fun produce(): I? {
    val value = upstream.produce()
    return if (predicate(value)) value else null
  }
}
