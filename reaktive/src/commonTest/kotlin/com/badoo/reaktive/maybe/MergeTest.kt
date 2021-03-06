package com.badoo.reaktive.maybe

import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.test.base.assertError
import com.badoo.reaktive.test.base.hasSubscribers
import com.badoo.reaktive.test.maybe.TestMaybe
import com.badoo.reaktive.test.observable.assertComplete
import com.badoo.reaktive.test.observable.assertNotComplete
import com.badoo.reaktive.test.observable.assertValue
import com.badoo.reaktive.test.observable.assertValues
import com.badoo.reaktive.test.observable.test
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MergeTest : MaybeToObservableTests by MaybeToObservableTestsImpl({ merge(this) }) {

    private val upstream1 = TestMaybe<Int?>()
    private val upstream2 = TestMaybe<Int?>()
    private val observer = merge(upstream1, upstream2).test()

    @Test
    fun subscribes_to_first_upstream_WHEN_subscribed() {
        assertTrue(upstream1.hasSubscribers)
    }

    @Test
    fun subscribes_to_second_upstream_WHEN_subscribed() {
        assertTrue(upstream2.hasSubscribers)
    }

    @Test
    fun produces_error_WHEN_first_upstream_produced_error() {
        val throwable = Throwable()
        upstream1.onError(throwable)

        observer.assertError(throwable)
    }

    @Test
    fun produces_error_WHEN_second_upstream_produced_error() {
        val throwable = Throwable()
        upstream1.onComplete()
        upstream2.onError(throwable)

        observer.assertError(throwable)
    }

    @Test
    fun does_not_complete_WHEN_first_upstream_completed() {
        upstream1.onComplete()

        observer.assertNotComplete()
    }

    @Test
    fun does_not_complete_WHEN_second_upstream_completed() {
        upstream2.onComplete()

        observer.assertNotComplete()
    }

    @Test
    fun does_not_complete_WHEN_first_upstream_succeeded() {
        upstream1.onSuccess(0)

        observer.assertNotComplete()
    }

    @Test
    fun does_not_complete_WHEN_second_upstream_succeeded() {
        upstream2.onSuccess(0)

        observer.assertNotComplete()
    }

    @Test
    fun completes_WHEN_both_upstreams_are_completed() {
        upstream1.onComplete()
        upstream2.onComplete()

        observer.assertComplete()
    }

    @Test
    fun completes_WHEN_first_upstream_succeeded_and_second_upstream_completed() {
        upstream1.onSuccess(0)
        upstream2.onComplete()

        observer.assertComplete()
    }

    @Test
    fun completes_WHEN_first_upstream_completed_and_second_upstream_succeeded() {
        upstream1.onComplete()
        upstream2.onSuccess(0)

        observer.assertComplete()
    }

    @Test
    fun completes_WHEN_both_upstreams_succeeded() {
        upstream1.onSuccess(0)
        upstream2.onSuccess(1)

        observer.assertComplete()
    }

    @Test
    fun produces_value_WHEN_first_upstream_succeeded_with_non_null_value() {
        upstream1.onSuccess(0)

        observer.assertValue(0)
    }

    @Test
    fun produces_value_WHEN_first_upstream_succeeded_with_null_value() {
        upstream1.onSuccess(null)

        observer.assertValue(null)
    }

    @Test
    fun produces_value_WHEN_second_upstream_succeeded_with_non_null_value() {
        upstream2.onSuccess(0)

        observer.assertValue(0)
    }

    @Test
    fun produces_value_WHEN_second_upstream_succeeded_with_null_value() {
        upstream2.onSuccess(null)

        observer.assertValue(null)
    }

    @Test
    fun produces_value_WHEN_first_upstream_completed_and_second_upstream_succeeded() {
        upstream1.onComplete()
        upstream2.onSuccess(0)

        observer.assertValue(0)
    }

    @Test
    fun produces_values_in_correct_order_WHEN_both_upstreams_succeeded() {
        upstream2.onSuccess(0)
        upstream1.onSuccess(1)

        observer.assertValues(0, 1)
    }

    @Test
    fun unsubscribes_from_both_upstreams_WHEN_disposed() {
        observer.dispose()

        assertFalse(upstream1.hasSubscribers)
        assertFalse(upstream2.hasSubscribers)
    }

    @Test
    fun completed_WHEN_sources_are_empty() {
        val observer = emptyList<Maybe<Any>>().merge().test()

        observer.assertComplete()
    }

    @Test
    fun does_not_complete_WHEN_one_upstream_completed_while_subscribing_to_others() {
        val observer = merge(
            maybeUnsafe<Any> { observer ->
                observer.onSubscribe(Disposable())
                observer.onComplete()
            },
            TestMaybe()
        ).test()

        observer.assertNotComplete()
    }
}
