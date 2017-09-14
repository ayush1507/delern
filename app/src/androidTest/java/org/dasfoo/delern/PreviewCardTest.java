/*
 * Copyright (C) 2017 Katarina Sheremet
 * This file is part of Delern.
 *
 * Delern is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * Delern is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with  Delern.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.dasfoo.delern;


import android.content.Context;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.text.InputType;

import org.dasfoo.delern.listdecks.DelernMainActivity;
import org.dasfoo.delern.test.FirebaseOperationInProgressRule;
import org.dasfoo.delern.util.DeckPostfix;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.action.ViewActions.typeTextIntoFocusedView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withInputType;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.dasfoo.delern.test.WaitView.waitView;
import static org.hamcrest.core.AllOf.allOf;

/**
 * Tests preview card operations.
 */
@RunWith(AndroidJUnit4.class)
public class PreviewCardTest {

    @Rule
    public ActivityTestRule<DelernMainActivity> mActivityRule = new ActivityTestRule<>(
            DelernMainActivity.class);

    @Rule
    public FirebaseOperationInProgressRule mFirebaseRule = new FirebaseOperationInProgressRule();

    @Rule
    public TestName mName = new TestName();

    private String mDeckName;

    @Before
    public void createDeck() {
        mDeckName = mName.getMethodName() + DeckPostfix.getRandomNumber();
        waitView(withId(R.id.fab)).perform(click());
        onView(withInputType(InputType.TYPE_CLASS_TEXT))
                .perform(typeTextIntoFocusedView(mDeckName), closeSoftKeyboard());
        onView(withText(R.string.add)).perform(click());
    }

    @Test
    public void createCardToDeleteFromPreview() {
        Context context = mActivityRule.getActivity().getApplicationContext();
        String frontCard = "front";
        String backCard = "back";
        waitView(withId(R.id.add_card_to_db)).check(matches(isDisplayed()));
        onView(withId(R.id.front_side_text)).perform(typeText(frontCard));
        onView(withId(R.id.back_side_text)).perform(typeText(backCard), closeSoftKeyboard());
        onView(withId(R.id.add_card_to_db)).perform(click());
        // Check that fields are empty after adding card
        waitView(withId(R.id.front_side_text)).check(matches(withText("")));
        onView(withId(R.id.back_side_text)).check(matches(withText("")));
        pressBack();
        waitView(withText(mDeckName)).check(matches(hasSibling(withText("1"))));
        onView(allOf(withId(R.id.deck_popup_menu), hasSibling(withText(mDeckName))))
                .perform(click());
        onView(withText(R.string.edit)).perform(click());
        waitView(withId(R.id.number_of_cards)).check(matches(withText(
                String.format(context.getString(R.string.number_of_cards), 1))));
        onView(allOf(withText(frontCard), hasSibling(withText(backCard)))).perform(click());
        waitView(withId(R.id.textFrontPreview)).check(matches(withText(frontCard)));
        onView(withId(R.id.textBackPreview)).check(matches(withText(backCard)));
        onView(withId(R.id.delete_card_menu)).perform(click());
        onView(withText(R.string.delete)).perform(click());
        waitView(withId(R.id.number_of_cards)).check(matches(withText(
                String.format(context.getString(R.string.number_of_cards), 0))));
        pressBack();
        // Check that card was deleted
        waitView(withText(mDeckName)).check(matches(hasSibling(withText("0"))));
    }

    @After
    public void deleteDeck() {
        waitView(allOf(withId(R.id.deck_popup_menu), hasSibling(withText(mDeckName))))
                .perform(click());
        onView(withText(R.string.delete)).perform(click());
        onView(withText(R.string.delete)).perform(click());
    }
}