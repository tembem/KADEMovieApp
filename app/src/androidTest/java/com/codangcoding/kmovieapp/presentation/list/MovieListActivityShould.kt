package com.codangcoding.kmovieapp.presentation.list

import android.app.Activity
import android.app.Instrumentation.ActivityResult
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.action.ViewActions.swipeDown
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.intent.Intents
import android.support.test.espresso.intent.Intents.intended
import android.support.test.espresso.intent.Intents.intending
import android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent
import android.support.test.espresso.intent.matcher.IntentMatchers.hasExtra
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.codangcoding.kmovieapp.R
import com.codangcoding.kmovieapp.domain.entity.Movie
import com.codangcoding.kmovieapp.presentation.detail.MovieDetailActivity
import com.codangcoding.kmovieapp.presentation.list.MovieListContract.ViewState.ResultState
import kotlinx.coroutines.experimental.channels.Channel
import org.hamcrest.CoreMatchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.mockito.Mockito.`when` as whenever

@RunWith(AndroidJUnit4::class)
class MovieListActivityShould {

    private val presenter = Mockito.mock(MovieListContract.Presenter::class.java)

    @get:Rule
    val rule = object : ActivityTestRule<MovieListActivity>(MovieListActivity::class.java) {
        override fun beforeActivityLaunched() {
            whenever(presenter.viewStates())
                .thenReturn(Channel()) // it is verify that viewStates from presenter is called

            MovieListActivity.presenter = presenter
        }
    }

    @Test
    fun subscribe_to_presenter_view_states_and_load_popular_movies() {
        verify(presenter).loadPopularMovies()
    }

    @Test
    fun load_popular_movies_when_click_popular_menu_and_swipe_refresh() {
        // reset, because on create presenter already load popular movies once
        Mockito.reset(presenter)

        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext())

        onView(withText(R.string.popular)).perform(click())
        onView(withId(R.id.swipeRefresh)).perform(swipeDown())

        verify(presenter, times(2)).loadPopularMovies()
        verify(presenter, never()).loadNowPlayingMovies()
    }

    @Test
    fun load_now_playing_movies_when_click_now_playing_menu_and_swipe_refresh() {
        // reset, because on create presenter already load popular movies once
        Mockito.reset(presenter)

        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext())

        onView(withText(R.string.now_playing)).perform(click())
        onView(withId(R.id.swipeRefresh)).perform(swipeDown())

        verify(presenter, times(2)).loadNowPlayingMovies()
        verify(presenter, never()).loadPopularMovies()
    }

    @Test
    fun render_result_state() {
        val movies = listOf(
            Movie(
                title = "Bleach Live Action da Movie",
                overview = "When Kamen Rider Fourze trapped in Shinigami World",
                vote = 8.9,
                releaseDate = "1986-10-10",
                posterPath = "/url_shota_fukushi",
                backdropPath = "/url_soul_society"
            ),
            Movie(
                title = "Haganeno Renkinjutsushi Live Action",
                overview = "Chibi Alchemist failed Adaptation",
                vote = 5.6,
                releaseDate = "2018-10-10",
                posterPath = null,
                backdropPath = null
            )
        )

        renderState(ResultState(movies))

        movies.forEach {
            onView(withText(it.title)).check(matches(isDisplayed()))
            onView(withText(it.overview)).check(matches(isDisplayed()))
            onView(withText(it.releaseDate)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun open_movie_detail_screen_when_click_movie_list_item() {
        val movies = listOf(
            Movie(
                title = "Bleach Live Action da Movie",
                overview = "When Kamen Rider Fourze trapped in Shinigami World",
                vote = 8.9,
                releaseDate = "1986-10-10",
                posterPath = "/url_shota_fukushi",
                backdropPath = "/url_soul_society"
            ),
            Movie(
                title = "Haganeno Renkinjutsushi Live Action",
                overview = "Chibi Alchemist failed Adaptation",
                vote = 5.6,
                releaseDate = "2018-10-10",
                posterPath = null,
                backdropPath = null
            )
        )

        renderState(ResultState(movies))

        try {
            Intents.init() // indicate we want to test intent

            // the intent that will recorded should be MovieDetailActivity with extra_movie
            val expectedIntent = allOf(
                hasComponent(MovieDetailActivity::class.java.name),
                hasExtra(MovieDetailActivity.EXTRA_MOVIE, movies[1])
            )

            // before click happen, make sure we will back to test screen
            // or test will blocking because we move to MovieDetailActivity
            intending(expectedIntent)
                .respondWith(ActivityResult(Activity.RESULT_CANCELED, null))

            // do click item here
            onView(withText("Haganeno Renkinjutsushi Live Action")).perform(click())

            // verify that click item generate the expected intent
            intended(expectedIntent)
        } finally {
            Intents.release() // indicate that we are done with intent testing
        }
    }

    private fun renderState(viewState: MovieListContract.ViewState) {
        rule.activity.runOnUiThread {
            rule.activity.renderState(viewState)
        }
    }
}