/*
 * Copyright 2015 Kejun Xia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.shipdream.lib.android.mvc.view.nav;

import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.shipdream.lib.android.mvc.BaseTestCase;
import com.shipdream.lib.android.mvc.Forwarder;
import com.shipdream.lib.android.mvc.Mvc;
import com.shipdream.lib.android.mvc.view.injection.controller.ControllerA;
import com.shipdream.lib.android.mvc.view.injection.controller.ControllerB;
import com.shipdream.lib.android.mvc.view.injection.controller.ControllerC;
import com.shipdream.lib.android.mvc.view.injection.controller.ControllerD;
import com.shipdream.lib.poke.Graph;
import com.shipdream.lib.poke.exception.ProvideException;
import com.shipdream.lib.poke.exception.ProviderConflictException;

import org.junit.Assert;
import org.junit.Test;

import javax.inject.Inject;

public class TestCaseNavigationAndInjection extends BaseTestCase<MvcTestActivityNavigation> {
    @Inject
    private AnotherController anotherPresenter;

    private CountMonitor monitor;

    public TestCaseNavigationAndInjection() {
        super(MvcTestActivityNavigation.class);
    }

    @Override
    protected void waitTest() throws InterruptedException {
        waitTest(800);
    }

    abstract class CountMonitor implements Graph.Monitor {
//        Activity activity;
//        int fragAInjectCount = 0;
//        int fragBInjectCount = 0;
//        int fragCInjectCount = 0;
//        int fragDInjectCount = 0;
//        int fragAReleaseCount = 0;
//        int fragBReleaseCount = 0;
//        int fragCReleaseCount = 0;
//        int fragDReleaseCount = 0;
    }
    
    @Override
    protected void prepareDependencies() throws ProvideException, ProviderConflictException {
        super.prepareDependencies();

        monitor = new CountMonitor(){
            @Override
            public void onInject(Object target) {
                if (target instanceof NavFragment) {
                    MvcTestActivityNavigation act = ((MvcTestActivityNavigation) ((NavFragment)target).getActivity());
                    if (target instanceof NavFragmentA) {
                        act.fragAInjectCount++;
                    } else if (target instanceof NavFragmentB) {
                        act.fragBInjectCount++;
                    } else if (target instanceof NavFragmentC) {
                        act.fragCInjectCount++;
                    } else if (target instanceof NavFragmentD) {
                        act.fragDInjectCount++;
                    }
                }
            }

            @Override
            public void onRelease(Object target) {
                if (target instanceof NavFragment) {
                    MvcTestActivityNavigation act = ((MvcTestActivityNavigation) ((NavFragment)target).getActivity());

                    if (target instanceof NavFragmentA) {
                        act.fragAReleaseCount++;
                    } else if (target instanceof NavFragmentB) {
                        act.fragBReleaseCount++;
                    } else if (target instanceof NavFragmentC) {
                        act.fragCReleaseCount++;
                    } else if (target instanceof NavFragmentD) {
                        act.fragDReleaseCount++;
                    }
                }
            }
        };

        Mvc.graph().registerMonitor(monitor);
    }

    @Override
    public void tearDown() throws Exception {
        Mvc.graph().unregisterMonitor(monitor);
        super.tearDown();
    }

    @Test
    public void test_should_reinject_last_fragment_and_release_top_fragment_on_single_step_back_navigation() throws Throwable {
        prepareAndCheckStack(true);
        //->A->B->C->D
        FragmentManager fm = activity.getRootFragmentManager();

        resetGraphMonitorCounts();
        navigationManager.navigate(this).back();
        //->A->B->C
        waitTest(1000);
        Assert.assertEquals(fm.getFragments().size(), 4);
        Assert.assertTrue(fm.getFragments().get(0) instanceof NavFragmentA);
        Assert.assertTrue(fm.getFragments().get(1) instanceof NavFragmentB);
        Assert.assertTrue(fm.getFragments().get(2) instanceof NavFragmentC);
        Assert.assertNull(fm.getFragments().get(3));
        Assert.assertEquals(fm.getBackStackEntryCount(), 3);
        Assert.assertTrue(fm.getBackStackEntryAt(0).getName().contains(ControllerA.class.getSimpleName()));
        Assert.assertTrue(fm.getBackStackEntryAt(1).getName().contains(ControllerB.class.getSimpleName()));
        Assert.assertTrue(fm.getBackStackEntryAt(2).getName().contains(ControllerC.class.getSimpleName()));
        Assert.assertEquals(getActivity().fragAInjectCount, 0);
        Assert.assertEquals(getActivity().fragAReleaseCount, 0);
        Assert.assertEquals(getActivity().fragBInjectCount, 0);
        Assert.assertEquals(getActivity().fragBReleaseCount, 0);
        Assert.assertEquals(getActivity().fragCInjectCount, 0);
        Assert.assertEquals(getActivity().fragCReleaseCount, 0);
        Assert.assertEquals(getActivity().fragDInjectCount, 0);
        Assert.assertEquals(getActivity().fragDReleaseCount, 1);

        resetGraphMonitorCounts();
        navigationManager.navigate(this).back();
        //->A->B
        waitTest();
        Assert.assertEquals(fm.getFragments().size(), 4);
        Assert.assertTrue(fm.getFragments().get(0) instanceof NavFragmentA);
        Assert.assertTrue(fm.getFragments().get(1) instanceof NavFragmentB);
        Assert.assertNull(fm.getFragments().get(2));
        Assert.assertNull(fm.getFragments().get(3));
        Assert.assertEquals(fm.getBackStackEntryCount(), 2);
        Assert.assertTrue(fm.getBackStackEntryAt(0).getName().contains(ControllerA.class.getSimpleName()));
        Assert.assertTrue(fm.getBackStackEntryAt(1).getName().contains(ControllerB.class.getSimpleName()));
        Assert.assertEquals(getActivity().fragAInjectCount, 0);
        Assert.assertEquals(getActivity().fragAReleaseCount, 0);
        Assert.assertEquals(getActivity().fragBInjectCount, 0);
        Assert.assertEquals(getActivity().fragBReleaseCount, 0);
        Assert.assertEquals(getActivity().fragCInjectCount, 0);
        Assert.assertEquals(getActivity().fragCReleaseCount, 1);
        Assert.assertEquals(getActivity().fragDInjectCount, 0);
        Assert.assertEquals(getActivity().fragDReleaseCount, 0);

        resetGraphMonitorCounts();
        navigationManager.navigate(this).back();
        //->A
        waitTest();
        Assert.assertEquals(fm.getFragments().size(), 4);
        Assert.assertTrue(fm.getFragments().get(0) instanceof NavFragmentA);
        Assert.assertNull(fm.getFragments().get(1));
        Assert.assertNull(fm.getFragments().get(2));
        Assert.assertNull(fm.getFragments().get(3));
        Assert.assertEquals(fm.getBackStackEntryCount(), 1);
        Assert.assertTrue(fm.getBackStackEntryAt(0).getName().contains(ControllerA.class.getSimpleName()));
        Assert.assertEquals(getActivity().fragAInjectCount, 0);
        Assert.assertEquals(getActivity().fragAReleaseCount, 0);
        Assert.assertEquals(getActivity().fragBInjectCount, 0);
        Assert.assertEquals(getActivity().fragBReleaseCount, 1);
        Assert.assertEquals(getActivity().fragCInjectCount, 0);
        Assert.assertEquals(getActivity().fragCReleaseCount, 0);
        Assert.assertEquals(getActivity().fragDInjectCount, 0);
        Assert.assertEquals(getActivity().fragDReleaseCount, 0);

        resetGraphMonitorCounts();
        navigationManager.navigate(this).back();
        //quit
        waitTest(2000);
        Assert.assertEquals(fm.getFragments().size(), 4);
        Assert.assertNull(fm.getFragments().get(0));
        Assert.assertNull(fm.getFragments().get(1));
        Assert.assertNull(fm.getFragments().get(2));
        Assert.assertNull(fm.getFragments().get(3));
        Assert.assertEquals(fm.getBackStackEntryCount(), 1);
        Assert.assertEquals(getActivity().fragAInjectCount, 0);
        Assert.assertEquals(getActivity().fragAReleaseCount, 1);
        Assert.assertEquals(getActivity().fragBInjectCount, 0);
        Assert.assertEquals(getActivity().fragBReleaseCount, 0);
        Assert.assertEquals(getActivity().fragCInjectCount, 0);
        Assert.assertEquals(getActivity().fragCReleaseCount, 0);
        Assert.assertEquals(getActivity().fragDInjectCount, 0);
        Assert.assertEquals(getActivity().fragDReleaseCount, 0);
    }

    @Test
    public void test_should_release_top_fragment_and_inject_new_fragment_on_forward_nav_with_fast_rewind_to_specific_location() throws Throwable {
        FragmentManager fm = activity.getRootFragmentManager();

        prepareAndCheckStack();
        //->A->B->C->D

        waitTest(200);
        resetGraphMonitorCounts();
        //Now clear history up to A and put C on it. Then A should pop out without re
        navigationManager.navigate(this).to(ControllerC.class, new Forwarder().clearTo(ControllerA.class));
        //->A->C
        waitTest();
        Assert.assertEquals(fm.getFragments().size(), 4);
        Assert.assertTrue(fm.getFragments().get(0) instanceof NavFragmentA);
        Assert.assertTrue(fm.getFragments().get(1) instanceof NavFragmentC);
        Assert.assertNull(fm.getFragments().get(2));
        Assert.assertNull(fm.getFragments().get(3));
        Assert.assertEquals(fm.getBackStackEntryCount(), 2);
        Assert.assertTrue(fm.getBackStackEntryAt(0).getName().contains(ControllerA.class.getSimpleName()));
        Assert.assertTrue(fm.getBackStackEntryAt(1).getName().contains(ControllerC.class.getSimpleName()));
        Assert.assertEquals(getActivity().fragAInjectCount, 0);
        Assert.assertEquals(getActivity().fragAReleaseCount, 0);
        Assert.assertEquals(getActivity().fragBInjectCount, 0);
        Assert.assertEquals(getActivity().fragBReleaseCount, 1);
        Assert.assertEquals(getActivity().fragCInjectCount, 1);
        Assert.assertEquals(getActivity().fragCReleaseCount, 1);
        Assert.assertEquals(getActivity().fragDInjectCount, 0);
        Assert.assertEquals(getActivity().fragDReleaseCount, 1);
    }

    @Test
    public void test_should_release_top_fragment_and_inject_new_fragment_on_forward_nav_with_clearing_all_history_locations() throws Throwable {
        FragmentManager fm = activity.getRootFragmentManager();

        prepareAndCheckStack();
        //->A->B->C->D

        waitTest();
        resetGraphMonitorCounts();
        //Now clear history up to A and put C on it. Then A should pop out without re
        navigationManager.navigate(this).to(ControllerB.class, new Forwarder().clearAll());
        //->B
        waitTest();
        Assert.assertEquals(fm.getFragments().size(), 4);
        Assert.assertTrue(fm.getFragments().get(0) instanceof NavFragmentB);
        Assert.assertNull(fm.getFragments().get(1));
        Assert.assertNull(fm.getFragments().get(2));
        Assert.assertNull(fm.getFragments().get(3));
        Assert.assertEquals(fm.getBackStackEntryCount(), 1);
        Assert.assertTrue(fm.getBackStackEntryAt(0).getName().contains(ControllerB.class.getSimpleName()));
        Assert.assertEquals(getActivity().fragAInjectCount, 0);
        Assert.assertEquals(getActivity().fragAReleaseCount, 1);
        Assert.assertEquals(getActivity().fragBInjectCount, 1);
        Assert.assertEquals(getActivity().fragBReleaseCount, 1);
        Assert.assertEquals(getActivity().fragCInjectCount, 0);
        Assert.assertEquals(getActivity().fragCReleaseCount, 1);
        Assert.assertEquals(getActivity().fragDInjectCount, 0);
        Assert.assertEquals(getActivity().fragDReleaseCount, 1);
    }

    @Test
    public void test_should_release_top_fragment_and_inject_home_fragment_with_fast_rewind_to_specific_location() throws Throwable {
        FragmentManager fm = activity.getRootFragmentManager();

        prepareAndCheckStack();
        //->A->B->C->D

        waitTest(200);
        resetGraphMonitorCounts();
        navigationManager.navigate(this).back(ControllerB.class);
        //->A->B
        waitTest();
        Assert.assertEquals(fm.getFragments().size(), 4);
        Assert.assertTrue(fm.getFragments().get(0) instanceof NavFragmentA);
        Assert.assertTrue(fm.getFragments().get(1) instanceof NavFragmentB);
        Assert.assertNull(fm.getFragments().get(2));
        Assert.assertNull(fm.getFragments().get(3));
        Assert.assertEquals(fm.getBackStackEntryCount(), 2);
        Assert.assertTrue(fm.getBackStackEntryAt(0).getName().contains(ControllerA.class.getSimpleName()));
        Assert.assertTrue(fm.getBackStackEntryAt(1).getName().contains(ControllerB.class.getSimpleName()));
        Assert.assertEquals(getActivity().fragAInjectCount, 0);
        Assert.assertEquals(getActivity().fragAReleaseCount, 0);
        Assert.assertEquals(getActivity().fragBInjectCount, 0);
        Assert.assertEquals(getActivity().fragBReleaseCount, 0);
        Assert.assertEquals(getActivity().fragCInjectCount, 0);
        Assert.assertEquals(getActivity().fragCReleaseCount, 1);
        Assert.assertEquals(getActivity().fragDInjectCount, 0);
        Assert.assertEquals(getActivity().fragDReleaseCount, 1);
    }

    @Test
    public void test_should_release_top_fragment_and_inject_home_fragment_when_clear_all_history_to_home_location_without_duplicate_history_items() throws Throwable {
        FragmentManager fm = activity.getRootFragmentManager();

        prepareAndCheckStack();
        //->A->B->C->D

        waitTest(200);
        resetGraphMonitorCounts();
        navigationManager.navigate(this).back(null);
        //->A
        waitTest();
        Assert.assertEquals(fm.getFragments().size(), 4);
        Assert.assertTrue(fm.getFragments().get(0) instanceof NavFragmentA);
        Assert.assertNull(fm.getFragments().get(1));
        Assert.assertNull(fm.getFragments().get(2));
        Assert.assertNull(fm.getFragments().get(3));
        Assert.assertEquals(fm.getBackStackEntryCount(), 1);
        Assert.assertTrue(fm.getBackStackEntryAt(0).getName().contains(ControllerA.class.getSimpleName()));
        Assert.assertEquals(getActivity().fragAInjectCount, 0);
        Assert.assertEquals(getActivity().fragAReleaseCount, 0);
        Assert.assertEquals(getActivity().fragBInjectCount, 0);
        Assert.assertEquals(getActivity().fragBReleaseCount, 1);
        Assert.assertEquals(getActivity().fragCInjectCount, 0);
        Assert.assertEquals(getActivity().fragCReleaseCount, 1);
        Assert.assertEquals(getActivity().fragDInjectCount, 0);
        Assert.assertEquals(getActivity().fragDReleaseCount, 1);
    }

    @Test
    public void test_should_release_top_fragment_and_inject_home_fragment_when_clear_all_history_to_home_location_with_duplicate_history_items() throws Throwable {
        FragmentManager fm = activity.getRootFragmentManager();

        prepareAndCheckStack();
        //->A->B->C->D

        navigationManager.navigate(this).to(ControllerA.class);
        //->A->B->C->D->A
        waitTest();
        navigationManager.navigate(this).to(ControllerC.class);
        //->A->B->C->D->A->C
        waitTest();

        resetGraphMonitorCounts();
        navigationManager.navigate(this).back(null);
        //->A
        waitTest();
        Assert.assertEquals(fm.getFragments().size(), 6);
        Assert.assertTrue(fm.getFragments().get(0) instanceof NavFragmentA);
        Assert.assertNull(fm.getFragments().get(1));
        Assert.assertNull(fm.getFragments().get(2));
        Assert.assertNull(fm.getFragments().get(3));
        Assert.assertNull(fm.getFragments().get(4));
        Assert.assertNull(fm.getFragments().get(5));
        Assert.assertEquals(fm.getBackStackEntryCount(), 1);
        Assert.assertTrue(fm.getBackStackEntryAt(0).getName().contains(ControllerA.class.getSimpleName()));
        Assert.assertEquals(getActivity().fragAInjectCount, 0);
        Assert.assertEquals(getActivity().fragAReleaseCount, 1);
        Assert.assertEquals(getActivity().fragBInjectCount, 0);
        Assert.assertEquals(getActivity().fragBReleaseCount, 1);
        Assert.assertEquals(getActivity().fragCInjectCount, 0);
        Assert.assertEquals(getActivity().fragCReleaseCount, 2);
        Assert.assertEquals(getActivity().fragDInjectCount, 0);
        Assert.assertEquals(getActivity().fragDReleaseCount, 1);
    }

    @Test
    public void test_should_release_and_inject_properly_on_single_step_back_navigation_after_being_killed_by_OS() throws Throwable {
        if (!isDontKeepActivities()) {
            Log.i(getClass().getSimpleName(), getClass().getSimpleName() + " not tested as Don't Keep Activities setting is disabled");
            return;
        }

        prepareAndCheckStack();
        //->A->B->C->D

        waitTest(200);
        resetGraphMonitorCounts();
        pressHome();
        waitTest();
        bringBack();
        waitTest(2000);

        Assert.assertEquals(getActivity().fragAInjectCount, 1);
        Assert.assertEquals(getActivity().fragAReleaseCount, 1);
        Assert.assertEquals(getActivity().fragBInjectCount, 1);
        Assert.assertEquals(getActivity().fragBReleaseCount, 1);
        Assert.assertEquals(getActivity().fragCInjectCount, 1);
        Assert.assertEquals(getActivity().fragCReleaseCount, 1);
        Assert.assertEquals(getActivity().fragDInjectCount, 1);
        Assert.assertEquals(getActivity().fragDReleaseCount, 1);

        resetGraphMonitorCounts();
        navigationManager.navigate(this).back();
        //->A->B->C
        waitTest(1000);
        Assert.assertEquals(getActivity().fragAInjectCount, 0);
        Assert.assertEquals(getActivity().fragAReleaseCount, 0);
        Assert.assertEquals(getActivity().fragBInjectCount, 0);
        Assert.assertEquals(getActivity().fragBReleaseCount, 0);
        Assert.assertEquals(getActivity().fragCInjectCount, 0);
        Assert.assertEquals(getActivity().fragCReleaseCount, 0);
        Assert.assertEquals(getActivity().fragDInjectCount, 0);
        Assert.assertEquals(getActivity().fragDReleaseCount, 1);

        resetGraphMonitorCounts();
        navigationManager.navigate(this).back();
        //->A->B
        waitTest();
        Assert.assertEquals(getActivity().fragAInjectCount, 0);
        Assert.assertEquals(getActivity().fragAReleaseCount, 0);
        Assert.assertEquals(getActivity().fragBInjectCount, 0);
        Assert.assertEquals(getActivity().fragBReleaseCount, 0);
        Assert.assertEquals(getActivity().fragCInjectCount, 0);
        Assert.assertEquals(getActivity().fragCReleaseCount, 1);
        Assert.assertEquals(getActivity().fragDInjectCount, 0);
        Assert.assertEquals(getActivity().fragDReleaseCount, 0);

        resetGraphMonitorCounts();
        navigationManager.navigate(this).back();
        //->A
        waitTest();
        Assert.assertEquals(getActivity().fragAInjectCount, 0);
        Assert.assertEquals(getActivity().fragAReleaseCount, 0);
        Assert.assertEquals(getActivity().fragBInjectCount, 0);
        Assert.assertEquals(getActivity().fragBReleaseCount, 1);
        Assert.assertEquals(getActivity().fragCInjectCount, 0);
        Assert.assertEquals(getActivity().fragCReleaseCount, 0);
        Assert.assertEquals(getActivity().fragDInjectCount, 0);
        Assert.assertEquals(getActivity().fragDReleaseCount, 0);

        resetGraphMonitorCounts();
        navigationManager.navigate(this).back();
        //quit
        waitTest(1000);
        Assert.assertEquals(getActivity().fragAInjectCount, 0);
        Assert.assertEquals(getActivity().fragAReleaseCount, 1);
        Assert.assertEquals(getActivity().fragBInjectCount, 0);
        Assert.assertEquals(getActivity().fragBReleaseCount, 0);
        Assert.assertEquals(getActivity().fragCInjectCount, 0);
        Assert.assertEquals(getActivity().fragCReleaseCount, 0);
        Assert.assertEquals(getActivity().fragDInjectCount, 0);
        Assert.assertEquals(getActivity().fragDReleaseCount, 0);
    }

    @Test
    public void test_should_release_and_inject_properly_on_fast_rewind_back_navigation_after_being_killed_by_OS() throws Throwable {
        if (!isDontKeepActivities()) {
            Log.i(getClass().getSimpleName(), getClass().getSimpleName() + " not tested as Don't Keep Activities setting is disabled");
            return;
        }

        prepareAndCheckStack();
        //->A->B->C->D

        waitTest(200);
        resetGraphMonitorCounts();
        pressHome();
        waitTest(1000);
        bringBack();
        waitTest(2000);

        Assert.assertEquals(getActivity().fragAInjectCount, 1);
        Assert.assertEquals(getActivity().fragAReleaseCount, 1);
        Assert.assertEquals(getActivity().fragBInjectCount, 1);
        Assert.assertEquals(getActivity().fragBReleaseCount, 1);
        Assert.assertEquals(getActivity().fragCInjectCount, 1);
        Assert.assertEquals(getActivity().fragCReleaseCount, 1);
        Assert.assertEquals(getActivity().fragDInjectCount, 1);
        Assert.assertEquals(getActivity().fragDReleaseCount, 1);

        resetGraphMonitorCounts();
        navigationManager.navigate(this).back(null);
        //->A
        waitTest(1200);
        Assert.assertEquals(getActivity().fragAInjectCount, 0);
        Assert.assertEquals(getActivity().fragAReleaseCount, 0);
        Assert.assertEquals(getActivity().fragBInjectCount, 0);
        Assert.assertEquals(getActivity().fragBReleaseCount, 1);
        Assert.assertEquals(getActivity().fragCInjectCount, 0);
        Assert.assertEquals(getActivity().fragCReleaseCount, 1);
        Assert.assertEquals(getActivity().fragDInjectCount, 0);
        Assert.assertEquals(getActivity().fragDReleaseCount, 1);

        resetGraphMonitorCounts();
        navigationManager.navigate(this).back();
        //quit
        waitTest(1200);
        Assert.assertEquals(getActivity().fragAInjectCount, 0);
        Assert.assertEquals(getActivity().fragAReleaseCount, 1);
        Assert.assertEquals(getActivity().fragBInjectCount, 0);
        Assert.assertEquals(getActivity().fragBReleaseCount, 0);
        Assert.assertEquals(getActivity().fragCInjectCount, 0);
        Assert.assertEquals(getActivity().fragCReleaseCount, 0);
        Assert.assertEquals(getActivity().fragDInjectCount, 0);
        Assert.assertEquals(getActivity().fragDReleaseCount, 0);
    }

    @Test
    public void test_should_release_and_inject_properly_on_fast_rewind_forward_navigation_after_being_killed_by_OS() throws Throwable {
        if (!isDontKeepActivities()) {
            Log.i(getClass().getSimpleName(), getClass().getSimpleName() + " not tested as Don't Keep Activities setting is disabled");
            return;
        }

        prepareAndCheckStack();
        //->A->B->C->D
        waitTest(200);
        resetGraphMonitorCounts();
        pressHome();
        waitTest();
        bringBack();
        waitTest(2000);

        Assert.assertEquals(getActivity().fragAInjectCount, 1);
        Assert.assertEquals(getActivity().fragAReleaseCount, 1);
        Assert.assertEquals(getActivity().fragBInjectCount, 1);
        Assert.assertEquals(getActivity().fragBReleaseCount, 1);
        Assert.assertEquals(getActivity().fragCInjectCount, 1);
        Assert.assertEquals(getActivity().fragCReleaseCount, 1);
        Assert.assertEquals(getActivity().fragDInjectCount, 1);
        Assert.assertEquals(getActivity().fragDReleaseCount, 1);

        resetGraphMonitorCounts();
        navigationManager.navigate(this).to(ControllerA.class, new Forwarder().clearTo(ControllerB.class));
        //->A->B->A
        waitTest();
        Assert.assertEquals(getActivity().fragAInjectCount, 1);
        Assert.assertEquals(getActivity().fragAReleaseCount, 0);
        Assert.assertEquals(getActivity().fragBInjectCount, 0);
        Assert.assertEquals(getActivity().fragBReleaseCount, 0);
        Assert.assertEquals(getActivity().fragCInjectCount, 0);
        Assert.assertEquals(getActivity().fragCReleaseCount, 1);
        Assert.assertEquals(getActivity().fragDInjectCount, 0);
        Assert.assertEquals(getActivity().fragDReleaseCount, 1);

        resetGraphMonitorCounts();
        navigationManager.navigate(this).back();
        //->A->B
        waitTest();
        Assert.assertEquals(getActivity().fragAInjectCount, 0);
        Assert.assertEquals(getActivity().fragAReleaseCount, 1);
        Assert.assertEquals(getActivity().fragBInjectCount, 0);
        Assert.assertEquals(getActivity().fragBReleaseCount, 0);
        Assert.assertEquals(getActivity().fragCInjectCount, 0);
        Assert.assertEquals(getActivity().fragCReleaseCount, 0);
        Assert.assertEquals(getActivity().fragDInjectCount, 0);
        Assert.assertEquals(getActivity().fragDReleaseCount, 0);

        resetGraphMonitorCounts();
        navigationManager.navigate(this).back();
        //->A
        waitTest();
        Assert.assertEquals(getActivity().fragAInjectCount, 0);
        Assert.assertEquals(getActivity().fragAReleaseCount, 0);
        Assert.assertEquals(getActivity().fragBInjectCount, 0);
        Assert.assertEquals(getActivity().fragBReleaseCount, 1);
        Assert.assertEquals(getActivity().fragCInjectCount, 0);
        Assert.assertEquals(getActivity().fragCReleaseCount, 0);
        Assert.assertEquals(getActivity().fragDInjectCount, 0);
        Assert.assertEquals(getActivity().fragDReleaseCount, 0);

        resetGraphMonitorCounts();
        navigationManager.navigate(this).back();
        //quit
        waitTest(1000);
        Assert.assertEquals(getActivity().fragAInjectCount, 0);
        Assert.assertEquals(getActivity().fragAReleaseCount, 1);
        Assert.assertEquals(getActivity().fragBInjectCount, 0);
        Assert.assertEquals(getActivity().fragBReleaseCount, 0);
        Assert.assertEquals(getActivity().fragCInjectCount, 0);
        Assert.assertEquals(getActivity().fragCReleaseCount, 0);
        Assert.assertEquals(getActivity().fragDInjectCount, 0);
        Assert.assertEquals(getActivity().fragDReleaseCount, 0);
    }

    private void prepareAndCheckStack() throws InterruptedException {
        prepareAndCheckStack(false);
    }

    private void prepareAndCheckStack(boolean check) throws InterruptedException {
        if (check) {
            //The activity will navigate to fragment a on launch
            Assert.assertEquals(getActivity().fragAInjectCount, 1);
            Assert.assertEquals(getActivity().fragAReleaseCount, 0);
            Assert.assertEquals(getActivity().fragBInjectCount, 0);
            Assert.assertEquals(getActivity().fragBReleaseCount, 0);
            Assert.assertEquals(getActivity().fragCInjectCount, 0);
            Assert.assertEquals(getActivity().fragCReleaseCount, 0);
            Assert.assertEquals(getActivity().fragDInjectCount, 0);
            Assert.assertEquals(getActivity().fragDReleaseCount, 0);
        }

        FragmentManager fm = activity.getRootFragmentManager();
        //->A
        //should not take effect to navigate to the same location
        navigationManager.navigate(this).to(ControllerA.class);
        //->A
        if (check) {
            waitTest();
            Assert.assertEquals(fm.getFragments().size(), 1);
            Assert.assertTrue(fm.getFragments().get(0) instanceof NavFragmentA);
            Assert.assertEquals(fm.getBackStackEntryCount(), 1);
            Assert.assertTrue(fm.getBackStackEntryAt(0).getName().contains(ControllerA.class.getSimpleName()));

            Assert.assertEquals(getActivity().fragAInjectCount, 1);
            Assert.assertEquals(getActivity().fragAReleaseCount, 0);
        }

        navigationManager.navigate(this).to(ControllerB.class);
        //->A->B
        if (check) {
            waitTest();
            Assert.assertEquals(fm.getFragments().size(), 2);
            Assert.assertTrue(fm.getFragments().get(0) instanceof NavFragmentA);
            Assert.assertTrue(fm.getFragments().get(1) instanceof NavFragmentB);
            Assert.assertEquals(fm.getBackStackEntryCount(), 2);
            Assert.assertTrue(fm.getBackStackEntryAt(0).getName().contains(ControllerA.class.getSimpleName()));
            Assert.assertTrue(fm.getBackStackEntryAt(1).getName().contains(ControllerB.class.getSimpleName()));
            Assert.assertEquals(getActivity().fragAInjectCount, 1);
            Assert.assertEquals(getActivity().fragAReleaseCount, 0);
            Assert.assertEquals(getActivity().fragBInjectCount, 1);
            Assert.assertEquals(getActivity().fragBReleaseCount, 0);
        }

        navigationManager.navigate(this).to(ControllerC.class);
        //->A->B->C
        if (check) {
            waitTest();
            Assert.assertEquals(fm.getFragments().size(), 3);
            Assert.assertTrue(fm.getFragments().get(0) instanceof NavFragmentA);
            Assert.assertTrue(fm.getFragments().get(1) instanceof NavFragmentB);
            Assert.assertTrue(fm.getFragments().get(2) instanceof NavFragmentC);
            Assert.assertEquals(fm.getBackStackEntryCount(), 3);
            Assert.assertTrue(fm.getBackStackEntryAt(0).getName().contains(ControllerA.class.getSimpleName()));
            Assert.assertTrue(fm.getBackStackEntryAt(1).getName().contains(ControllerB.class.getSimpleName()));
            Assert.assertTrue(fm.getBackStackEntryAt(2).getName().contains(ControllerC.class.getSimpleName()));
            Assert.assertEquals(getActivity().fragAInjectCount, 1);
            Assert.assertEquals(getActivity().fragAReleaseCount, 0);
            Assert.assertEquals(getActivity().fragBInjectCount, 1);
            Assert.assertEquals(getActivity().fragBReleaseCount, 0);
            Assert.assertEquals(getActivity().fragCInjectCount, 1);
            Assert.assertEquals(getActivity().fragCReleaseCount, 0);
        }

        navigationManager.navigate(this).to(ControllerD.class);
        //->A->B->C->D
        if (check) {
            waitTest();
            Assert.assertEquals(fm.getFragments().size(), 4);
            Assert.assertTrue(fm.getFragments().get(0) instanceof NavFragmentA);
            Assert.assertTrue(fm.getFragments().get(1) instanceof NavFragmentB);
            Assert.assertTrue(fm.getFragments().get(2) instanceof NavFragmentC);
            Assert.assertTrue(fm.getFragments().get(3) instanceof NavFragmentD);
            Assert.assertEquals(fm.getBackStackEntryCount(), 4);
            Assert.assertTrue(fm.getBackStackEntryAt(0).getName().contains(ControllerA.class.getSimpleName()));
            Assert.assertTrue(fm.getBackStackEntryAt(1).getName().contains(ControllerB.class.getSimpleName()));
            Assert.assertTrue(fm.getBackStackEntryAt(2).getName().contains(ControllerC.class.getSimpleName()));
            Assert.assertTrue(fm.getBackStackEntryAt(3).getName().contains(ControllerD.class.getSimpleName()));
            Assert.assertEquals(getActivity().fragAInjectCount, 1);
            Assert.assertEquals(getActivity().fragAReleaseCount, 0);
            Assert.assertEquals(getActivity().fragBInjectCount, 1);
            Assert.assertEquals(getActivity().fragBReleaseCount, 0);
            Assert.assertEquals(getActivity().fragCInjectCount, 1);
            Assert.assertEquals(getActivity().fragCReleaseCount, 0);
            Assert.assertEquals(getActivity().fragDInjectCount, 1);
            Assert.assertEquals(getActivity().fragDReleaseCount, 0);
        }
    }

    private void resetGraphMonitorCounts() {
        getActivity().fragAInjectCount = 0;
        getActivity().fragBInjectCount = 0;
        getActivity().fragCInjectCount = 0;
        getActivity().fragDInjectCount = 0;
        getActivity().fragAReleaseCount = 0;
        getActivity().fragBReleaseCount = 0;
        getActivity().fragCReleaseCount = 0;
        getActivity().fragDReleaseCount = 0;
    }

}
