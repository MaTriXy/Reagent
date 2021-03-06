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

import kotlinx.coroutines.experimental.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.experimental.Unconfined
import kotlinx.coroutines.experimental.launch
import reagent.Emitter
import reagent.Observable
import reagent.Maybe
import reagent.One
import reagent.Task
import kotlin.DeprecationLevel.ERROR

fun <I, O> Observable<I>.flatMap(func: (I) -> Observable<O>): Observable<O> = ObservableFlatMapObservable(this, func)

internal class ObservableFlatMapObservable<U, out D>(
    private val upstream: Observable<U>,
    private val func: (U) -> Observable<D>
) : Observable<D>() {
  override suspend fun subscribe(emit: Emitter<D>) = TODO()
}

fun <I> Observable<I>.flatMap(func: (I) -> Task): Task = ObservableFlatMapTask(this, func)
fun <I> Maybe<I>.flatMap(func: (I) -> Task): Task = MaybeFlatMapTask(this, func)
fun <I> One<I>.flatMap(func: (I) -> Task): Task = MaybeFlatMapTask(this, func)

internal class ObservableFlatMapTask<in U>(
    private val upstream: Observable<U>,
    private val func: (U) -> Task
) : Task() {
  override suspend fun run() {
    upstream.subscribe {
      launch(Unconfined, UNDISPATCHED) {
        func(it).run()
      }
    }
  }
}

internal class MaybeFlatMapTask<U>(
  private val upstream: Maybe<U>,
    private val func: (U) -> Task
) : Task() {
  override suspend fun run() {
    upstream.produce()?.let(func)?.run()
  }
}

fun <I, O> Maybe<I>.flatMap(func: (I) -> Maybe<O>): Maybe<O> = MaybeFlatMapMaybe(this, func)

internal class MaybeFlatMapMaybe<U, out D>(
    private val upstream: Maybe<U>,
    private val func: (U) -> Maybe<D>
) : Maybe<D>() {
  override suspend fun produce() = upstream.produce()?.let(func)?.produce()
}

fun <I, O> One<I>.flatMap(func: (I) -> One<O>): One<O> = OneFlatMapOne(this, func)

internal class OneFlatMapOne<U, D>(
    private val upstream: One<U>,
    private val func: (U) -> One<D>
) : One<D>() {
  override suspend fun produce() = func(upstream.produce()).produce()
}

@Deprecated("Task produces no items so mapping has no effect.", level = ERROR)
fun Task.flatMap(func: (Nothing) -> Observable<*>): Task = this
